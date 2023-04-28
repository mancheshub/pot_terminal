package com.bigsize.pot_terminal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.HashItem
import com.bigsize.pot_terminal.model.ItemInspectionAPI
import com.bigsize.pot_terminal.model.PotDataModel03
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class ItemInspection:ViewModel() {
  private var model01:ItemInspectionAPI = ItemInspectionAPI()

  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // API通信状況
  // ST → 通信開始 ER → 通信エラー FN → 通信終了 SI_*** → 他人スタッフ***が検品中
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 作業グループリスト
  private val _groupList:MutableLiveData<MutableList<HashItem>> = MutableLiveData()
  public val groupList:LiveData<MutableList<HashItem>> get() = _groupList

  // 店舗リスト
  private val _shopList:MutableLiveData<MutableList<HashItem>> = MutableLiveData()
  public val shopList:LiveData<MutableList<HashItem>> get() = _shopList

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel03>> = MutableLiveData()
  public val itemList:LiveData<MutableList<PotDataModel03>> get() = _itemList

  // 箱リスト
  public val boxList:List<HashItem> = mutableListOf(
    HashItem( "01", "箱01" ), HashItem( "02", "箱02" ), HashItem( "03", "箱03" ),
    HashItem( "04", "箱04" ), HashItem( "05", "箱05" ), HashItem( "06", "箱06" ), HashItem( "07", "箱07" ),
    HashItem( "08", "箱08" ), HashItem( "09", "箱09" ), HashItem( "10", "箱10" ), HashItem( "11", "箱11" ),
    HashItem( "12", "箱12" ), HashItem( "13", "箱13" ), HashItem( "14", "箱14" ), HashItem( "15", "箱15" ),
    HashItem( "16", "箱16" ), HashItem( "17", "箱17" ), HashItem( "18", "箱18" ), HashItem( "19", "箱19" ),
    HashItem( "20", "箱20" ), HashItem( "", " " ),
  )

  // 印刷機リスト
  public val printList:List<HashItem> = mutableListOf(
    HashItem( "ELS_FEL_P01", "印刷機01" ), HashItem( "ELS_FEL_P02", "印刷機02" ), HashItem( "", " " ),
  )

  // 現在選択している"作業グループ・店舗・箱・印刷機"
  public var selectedGroupID:String = " "
  public var selectedShopID:String = " "
  public var selectedBoxID:String = " "
  public var selectedPrintID:String = " "

  // 確定処理実行判断フラグ - 店舗のSCMラベルが発行されている場合に警告表示する判定に利用します
  public var isExecute03:String = ""

  // クリア処理実行判断フラグ - 店舗の担当者が自分自身ではない場合にエラー表示する判定に利用します
  public var isExecute01:String = ""

  init {}

  /**
   * 作業グループデータを取得します
   */
  public fun pickGroupList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.pickGroupList( AppBase.itemInspectionURL )
        _groupList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 店舗データを取得します
   */
  public fun pickShopList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.pickShopList( AppBase.itemInspectionURL, selectedGroupID )
        _shopList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 商品データを取得します
   *
   * @param [flagExclusive] 排他処理実行フラグ
   *                        exeExclusive : 検品状態チェックと検品開始を実施します
   *                        nonExclusive : 検品状態チェックと検品開始を実施しません
   */
  public fun pickItemList( flagExclusive:String ) {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "排他処理実行フラグ = " + flagExclusive )

      try {
        var apiExclusive:String = "999"

        // 検品状態を確認します
        if( flagExclusive == "exeExclusive" ) {
          val pairHash01 = model01.updateSituation( AppBase.itemInspectionURL, "SI-check", selectedGroupID, selectedShopID, AppBase.staffNO )
          apiExclusive = pairHash01.second
        }

        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "検品スタッフ = " + apiExclusive )

        // 自分が検品した店舗もしくは誰も検品していない店舗の場合のみ処理します

        _itemList.value = mutableListOf()

        if( apiExclusive == "999" ) {
          // 検品開始状態とします
          if( flagExclusive == "exeExclusive" ) {
            val pairHash02 = model01.updateSituation( AppBase.itemInspectionURL, "SI-start", selectedGroupID, selectedShopID, AppBase.staffNO )
          }

          // 商品データを取得します
          val pairHash03 =model01.pickItemList( AppBase.itemInspectionURL, selectedGroupID, selectedShopID )
          _itemList.value = pairHash03.second

          _apiCondition.value = "FN99"
        } else {
          _apiCondition.value = "SI_" + apiExclusive
        }
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 箱確定・確定処理を実施します
   *
   * @param [kind] 処理区分 01 : クリア 02 : 箱確定 03 : 確定
   * @return 処理の合否
   */
  public fun deceded( kind:String ) {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "isExecute01 = " + isExecute01 )

      try {
        // クリアボタンが押されたときは isExecute01 == "" となっているから自身が店舗の担当者であるかを調査します
        // - 自身が担当者でない場合は isExecute01 == "0"  となるのでエラー画面をActivityから出力します
        // - 自身が担当者である場合は isExecute01 != "0"  となるのでそのままクリア処理を実施します
        if( kind == "01" && isExecute01 == "" ) {
          val pairHash01 = model01.pickSICondition( AppBase.itemInspectionURL, selectedGroupID, selectedShopID, AppBase.staffNO )
          isExecute01 = pairHash01.second
        }

        // 確定ボタンが押されたときは isExecute03 == "" となっているからSCMラベル印刷状況を調査します
        // - SCMラベルが印刷されていた場合は isExecute03 != "0" となるので警告画面をActivityから出力します
        //   警告画面で"はい"を押したのちは一旦 isExecute03 = "0" で更新してこの処理をもう一度呼びます
        // - SCMラベルが印刷されていない場合は isExecute03 == "0" となるのでそのまま確定処理を実施します

        if( kind == "03" && isExecute03 == "" ) {
          val pairHash02 = model01.pickSMCondition( AppBase.itemInspectionURL, selectedGroupID, selectedShopID )
          isExecute03 = pairHash02.second
        }

        if( ( kind == "01" && isExecute01 != "0" ) || kind == "02" || ( kind == "03" && isExecute03 == "0" ) ) {
          model01.deceded( AppBase.itemInspectionURL, kind, selectedGroupID, selectedShopID, selectedBoxID, selectedPrintID, ( itemList.value as MutableList<PotDataModel03> ) )

          // 検品完了状態とします
          if( kind == "03" ) { val pairHash03 = model01.updateSituation( AppBase.itemInspectionURL, "SI-close", selectedGroupID, selectedShopID, AppBase.staffNO ) }

          // 検品取消状態とします
          if( kind == "01" ) { val pairHash04 = model01.updateSituation( AppBase.itemInspectionURL, "SI-stop", selectedGroupID, selectedShopID, AppBase.staffNO ) }

          _apiCondition.value = "FN" + kind
        } else {
          _apiCondition.value = "AL" + kind
        }

        // 調査状況をクリアします
        isExecute01 = ""
        isExecute03 = ""
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 商品情報の検品数を更新します
   *
   * @param [position] 商品情報の位置
   */
  public fun updateItemList( position:Int ) {
    // 該当位置のSKUの数量を増やします
    var potData:PotDataModel03 = ( _itemList.value as MutableList<PotDataModel03> )[position]
    potData.amt_n = (potData.amt_n.toInt()+1).toString()

    // ViewModelを更新します
    ( _itemList.value as MutableList<PotDataModel03> ).set( position, potData )
  }
}
