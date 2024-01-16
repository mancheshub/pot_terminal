package com.bigsize.pot_terminal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.BoxOperationAPI
import com.bigsize.pot_terminal.model.HashItem
import com.bigsize.pot_terminal.model.PotDataModel01

class BoxOperationPage01:ViewModel() {
  private var model01:BoxOperationAPI = BoxOperationAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 箱ラベルリスト
  private val _boxList:MutableLiveData<MutableList<HashItem>> = MutableLiveData()
  public val boxList:LiveData<MutableList<HashItem>> get() = _boxList

  // 箱ラベルと箱ラベル背景色と店舗名
  public val txtBoxno:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val bkgBoxno:MutableLiveData<String> by lazy { MutableLiveData( "N" ) }
  public val txtShopname:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 入力した箱ラベル
  public var inputedBoxno:String = ""

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel01>> = MutableLiveData()
  public val itemList:LiveData<MutableList<PotDataModel01>> get() = _itemList

  init {}

  /**
   * 箱ラベルデータを取得します
   */
  public fun pickBoxList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.pickBoxList( AppBase.boxOperationURL )
        _boxList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 箱ラベルから店舗名と商品を取得します
   */
  public fun pickItemList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        // 箱ラベルの情報を取得します
        val pairHash01 = model01.pickBoxInfomation( AppBase.boxOperationURL, inputedBoxno )
        txtShopname.value = pairHash01.second

        // 箱ラベルから商品を取得します
        val pairHash02 = model01.pickItemList( AppBase.boxOperationURL, inputedBoxno )
        _itemList.value = pairHash02.second

        _apiCondition.value = "FN"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "致命的エラー" )
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
    var potData:PotDataModel01 = ( _itemList.value as MutableList<PotDataModel01> )[position]
    potData.amt_n = (potData.amt_n.toInt()+1).toString()

    // ViewModelを更新します
    ( _itemList.value as MutableList<PotDataModel01> ).set( position, potData )
  }
}

class BoxOperationPage02:ViewModel() {
  private var model01:BoxOperationAPI = BoxOperationAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 全データ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 箱ラベルと箱ラベル背景色
  public val txtBoxno01:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val txtBoxno02:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val bkgBoxno01:MutableLiveData<String> by lazy { MutableLiveData( "N" ) }
  public val bkgBoxno02:MutableLiveData<String> by lazy { MutableLiveData( "N" ) }

  // 入力した箱ラベル
  public var inputedBoxno01:String = ""
  public var inputedBoxno02:String = ""

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel01>> = MutableLiveData()
  public val itemList:LiveData<MutableList<PotDataModel01>> get() = _itemList

  init {}

  /**
   * 箱付替を実施します
   */
  public fun finishReplace() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        // 付替後の箱ラベルから商品を取得します
        val pairHash01 = model01.pickItemList( AppBase.boxOperationURL, inputedBoxno02 )

        if( pairHash01.second.size != 0 ) {
          _itemList.value = pairHash01.second

          _apiCondition.value = "AL"
        }

        if( pairHash01.second.size == 0 ) {
          // 箱付替を完了します
          val pairHash02 = model01.finishReplace( AppBase.boxOperationURL, inputedBoxno01, inputedBoxno02 )

          // 付替後の箱ラベルから商品を取得します
          val pairHash03 = model01.pickItemList( AppBase.boxOperationURL, inputedBoxno02 )
          _itemList.value = pairHash03.second

          _apiCondition.value = "FN"
        }
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }
}
