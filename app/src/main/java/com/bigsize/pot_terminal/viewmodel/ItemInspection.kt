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

  // 更新系API通信状況
  // ST → 通信開始 ER → 通信エラー FN → 通信終了 SI_*** → 他人スタッフ***が検品中
  private val _apiCondition:MutableLiveData<String> = MutableLiveData( "" )
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 作業グループリスト
  private val _groupList:MutableLiveData<MutableList<HashItem>> = MutableLiveData( mutableListOf() )
  public val groupList:LiveData<MutableList<HashItem>> get() = _groupList

  // 店舗リスト
  private val _shopList:MutableLiveData<MutableList<HashItem>> = MutableLiveData( mutableListOf() )
  public val shopList:LiveData<MutableList<HashItem>> get() = _shopList

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel03>> = MutableLiveData( mutableListOf() )
  public val itemList:LiveData<MutableList<PotDataModel03>> get() = _itemList

  // 箱リスト
  public val boxList:List<HashItem> = mutableListOf(
    HashItem( "", "" ), HashItem( "01", "箱01" ), HashItem( "02", "箱02" ), HashItem( "03", "箱03" ), HashItem( "04", "箱04" ), HashItem( "05", "箱05" ),
  )

  // 印刷機リスト
  public val printList:List<HashItem> = mutableListOf(
    HashItem( "", "" ), HashItem( "ELS_FEL_P01", "印刷機01" ), HashItem( "ELS_FEL_P02", "印刷機02" ),
  )

  // 現在選択している"作業グループ・店舗・箱・印刷機"データ
  public var selectedGroupID:String = ""
  public var selectedShopID:String = ""
  public var selectedBoxID:String = ""
  public var selectedPrintID:String = ""

  init {}

  /**
   * 作業グループデータを取得します
   */
  public fun pickGroupList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        _groupList.value = model01.pickGroupList( AppBase.itemInspectionURL )
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
        _shopList.value = model01.pickShopList( AppBase.itemInspectionURL, selectedGroupID )
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
        if( flagExclusive == "exeExclusive" ) apiExclusive = model01.updateSituation( AppBase.itemInspectionURL, "SI-check", selectedGroupID, selectedShopID, AppBase.staffNO )
        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "検品スタッフ = " + apiExclusive )

        // 自分が検品した店舗もしくは誰も検品していない店舗の場合のみ処理します

        if( apiExclusive == "999" ) {
          // 検品開始状態とします
          if( flagExclusive == "exeExclusive" ) model01.updateSituation( AppBase.itemInspectionURL, "SI-start", selectedGroupID, selectedShopID, AppBase.staffNO )

          _itemList.value = model01.pickItemList( AppBase.itemInspectionURL, selectedGroupID, selectedShopID )

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

      try {
        model01.deceded( AppBase.itemInspectionURL, kind, selectedGroupID, selectedShopID, selectedBoxID, selectedPrintID, ( itemList.value as MutableList<PotDataModel03> ) )

        // 検品完了状態とします
        if( kind == "03" ) model01.updateSituation( AppBase.itemInspectionURL, "SI-close", selectedGroupID, selectedShopID, AppBase.staffNO )

        // 検品取消状態とします
        if( kind == "01" ) model01.updateSituation( AppBase.itemInspectionURL, "SI-stop", selectedGroupID, selectedShopID, AppBase.staffNO )

        _apiCondition.value = "FN" + kind
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
