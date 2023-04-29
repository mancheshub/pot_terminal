package com.bigsize.pot_terminal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.HashItem
import com.bigsize.pot_terminal.model.BoxShippingAPI
import com.bigsize.pot_terminal.model.BoxConfirmAPI
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.model.PotDataModel05

class BoxShippingPage01:ViewModel() {
  private var model01:BoxShippingAPI = BoxShippingAPI()
  private var model02:BoxConfirmAPI = BoxConfirmAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 伝発グループリスト
  private val _groupList:MutableLiveData<MutableList<HashItem>> = MutableLiveData()
  public val groupList:LiveData<MutableList<HashItem>> get() = _groupList

  // 店舗リスト
  private val _shopList:MutableLiveData<MutableList<HashItem>> = MutableLiveData()
  public val shopList:LiveData<MutableList<HashItem>> get() = _shopList

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel01>> = MutableLiveData()
  public val itemList:LiveData<MutableList<PotDataModel01>> get() = _itemList

  // 今回決定した箱ラベル
  public val txtBoxno:MutableLiveData<String> by lazy { MutableLiveData( "" ) }

  // 選択した"伝発グループ・店舗・箱ラベル"
  public var selectedGroupID:String = " "
  public var selectedShopID:String = " "
  public var selectedBoxID:String = " "

  init {}

  /**
   * 伝発グループデータを取得します
   */
  public fun pickGroupList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.pickGroupList( AppBase.boxShippingURL )
        _groupList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "致命的エラー" )
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
        val pairHash01 = model01.pickShopList( AppBase.boxShippingURL, selectedGroupID )
        _shopList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 商品データを取得します
   */
  public fun pickItemList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        // 店舗の箱ラベルを取得します
        val pairHash01 = model01.pickBoxNO( AppBase.boxShippingURL, selectedShopID )
        txtBoxno.value = pairHash01.second
        selectedBoxID = pairHash01.second

        // 商品データを取得します
        val pairHash02 = model01.pickItemList( AppBase.boxShippingURL, selectedGroupID, selectedShopID )
        _itemList.value = pairHash02.second

        if( pairHash02.first == "AL" ) { _apiCondition.value = "AL02" }
        if( pairHash02.first == "OK" ) { _apiCondition.value = "FN99" }
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 箱出を完了します
   */
  public fun finishShipping() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.finishShipping( AppBase.boxShippingURL, selectedGroupID, selectedShopID )

        // 箱出が正常に完了したら箱ラベルに商品が残っていないかをチェックします
        if( pairHash01.first == "OK" ) {
          val pairHash02 = model02.pickItemList( AppBase.boxConfirmURL, selectedBoxID )

          if( pairHash02.second.size != 0 ) _apiCondition.value = "AL03"
          if( pairHash02.second.size == 0 ) _apiCondition.value = "FN01"
        }

        if( pairHash01.first == "NG" ) _apiCondition.value = "AL01"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "致命的エラー" )
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

class BoxShippingPage02:ViewModel() {
  private var model01:BoxShippingAPI = BoxShippingAPI()
  private var model02:BoxConfirmAPI = BoxConfirmAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel05>> = MutableLiveData()
  public val itemList:LiveData<MutableList<PotDataModel05>> get() = _itemList

  // 箱ラベルと店舗名
  public val txtBoxno:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val txtShopname:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 入力した箱ラベル
  public var inputedBoxno:String = ""

  init {}

  /**
   * キャンセル商品データを取得します
   */
  public fun pickItemList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.pickCancelItemList( AppBase.boxShippingURL )
        _itemList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 箱ラベルから店舗名を取得します
   */
  public fun pickShopName() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        // 箱ラベルから店舗名を取得します
        val pairValue01 = model02.pickShopname( AppBase.boxConfirmURL, inputedBoxno )
        txtShopname.value = pairValue01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxConfirm", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * キャンセル箱出を完了します
   *
   * @param [i_id] 明細番号
   */
  public fun finishShipping( i_id:String ) {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.finishCancelShipping( AppBase.boxShippingURL, i_id )

        _apiCondition.value = "FN01"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "致命的エラー" )
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
    var potData:PotDataModel05 = ( _itemList.value as MutableList<PotDataModel05> )[position]
    potData.amt_n = (potData.amt_n.toInt()+1).toString()

    // ViewModelを更新します
    ( _itemList.value as MutableList<PotDataModel05> ).set( position, potData )
  }
}

class BoxShippingPage03:ViewModel() {
  private var model01:BoxShippingAPI = BoxShippingAPI()
  private var model02:BoxConfirmAPI = BoxConfirmAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel05>> = MutableLiveData()
  public val itemList:LiveData<MutableList<PotDataModel05>> get() = _itemList

  // 箱ラベルと店舗名
  public val txtBoxno:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val txtShopname:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 入力した箱ラベル
  public var inputedBoxno:String = ""

  init {}

  /**
   * 先送商品データを取得します
   */
  public fun pickItemList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.pickPostponeItemList( AppBase.boxShippingURL )
        _itemList.value = pairHash01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 箱ラベルから店舗名を取得します
   */
  public fun pickShopName() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        // 箱ラベルから店舗名を取得します
        val pairValue01 = model02.pickShopname( AppBase.boxConfirmURL, inputedBoxno )
        txtShopname.value = pairValue01.second

        _apiCondition.value = "FN99"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxConfirm", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 先送箱出を完了します
   *
   * @param [i_id] 明細番号
   */
  public fun finishShipping( i_id:String ) {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        val pairHash01 = model01.finishPostponeShipping( AppBase.boxShippingURL, i_id )

        _apiCondition.value = "FN01"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "致命的エラー" )
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
    var potData:PotDataModel05 = ( _itemList.value as MutableList<PotDataModel05> )[position]
    potData.amt_n = (potData.amt_n.toInt()+1).toString()

    // ViewModelを更新します
    ( _itemList.value as MutableList<PotDataModel05> ).set( position, potData )
  }
}