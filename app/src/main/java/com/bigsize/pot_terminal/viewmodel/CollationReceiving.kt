package com.bigsize.pot_terminal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.HashItem
import com.bigsize.pot_terminal.model.PotDataModel04
import com.bigsize.pot_terminal.model.CollationReceivingAPI
import kotlinx.coroutines.launch

class CollationReceivingPage01:ViewModel() {
  private var model01:CollationReceivingAPI = CollationReceivingAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 単位リスト
  private val _unitList:MutableLiveData<MutableList<HashItem>> = MutableLiveData()
  public val unitList:LiveData<MutableList<HashItem>> get() = _unitList

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel04>> = MutableLiveData()
  public val itemList:LiveData<MutableList<PotDataModel04>> get() = _itemList

  // 入庫ロケーションとロケーションと商品
  public val txtAddress:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val txtLocation:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val txtCd:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtCn:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtSz:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 選択した"階・単位"
  public var selectedFloorID:String = ""
  public var selectedUnitID:String = ""

  // 入力した"ロケーション・商品"
  public var inputedLocation:String = ""
  public var inputedCd:String = ""
  public var inputedCn:String = ""
  public var inputedSz:String = ""

  // 照合のために記録した位置
  public var memPosition:String = ""

  // 階リスト
  public val floorList:List<HashItem> = mutableListOf(
    HashItem( "0000", "全階" ), HashItem( "9101", "01階" ), HashItem( "9102", "02階" ), HashItem( "9103", "03階" ),
    HashItem( "9104", "04階" ), HashItem( "9105", "05階" ),
  )

  init {}

  /**
   * 返品入庫の単位データを取得します
   */
  public fun pickH_UnitList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.pickH_UnitList( AppBase.collationReceivingURL, selectedFloorID )
        _unitList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 返品入庫対象となる商品データを取得します
   */
  public fun pickH_ItemList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.pickH_ItemList( AppBase.collationReceivingURL, selectedFloorID, selectedUnitID )
        _itemList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "致命的エラー" )
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
    var potData:PotDataModel04 = ( _itemList.value as MutableList<PotDataModel04> )[position]
    potData.amt_n = (potData.amt_n.toInt()+1).toString()

    // ViewModelを更新します
    ( _itemList.value as MutableList<PotDataModel04> ).set( position, potData )
  }
}

class CollationReceivingPage02:ViewModel() {
  private var model01:CollationReceivingAPI = CollationReceivingAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel04>> = MutableLiveData()
  public val itemList:LiveData<MutableList<PotDataModel04>> get() = _itemList

  // 入庫ロケーションとロケーションと商品
  public val txtAddress:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val txtLocation:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val txtCd:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtCn:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtSz:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 選択した階
  public var selectedFloorID:String = ""

  // 入力した"ロケーション・商品"
  public var inputedLocation:String = ""
  public var inputedCd:String = ""
  public var inputedCn:String = ""
  public var inputedSz:String = ""

  // 照合のために記録した位置
  public var memPosition:String = ""

  // 階リスト
  public val floorList:List<HashItem> = mutableListOf(
    HashItem( "0000", "全階" ), HashItem( "9101", "01階" ), HashItem( "9102", "02階" ), HashItem( "9103", "03階" ),
    HashItem( "9104", "04階" ), HashItem( "9105", "05階" ),
  )

  init {}

  /**
   * Fキャンセル入庫対象となる商品データを取得します
   */
  public fun pickF_ItemList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.pickF_ItemList( AppBase.collationReceivingURL, selectedFloorID )
        _itemList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "致命的エラー" )
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
    var potData:PotDataModel04 = ( _itemList.value as MutableList<PotDataModel04> )[position]
    potData.amt_n = (potData.amt_n.toInt()+1).toString()

    // ViewModelを更新します
    ( _itemList.value as MutableList<PotDataModel04> ).set( position, potData )
  }
}

class CollationReceivingPage03:ViewModel() {
  init {}
}
