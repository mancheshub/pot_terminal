package com.bigsize.pot_terminal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.BoxConfirmAPI
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.model.PotDataModel03
import kotlinx.coroutines.launch

class BoxConfirm: ViewModel() {
  private var model01:BoxConfirmAPI = BoxConfirmAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 箱ラベルと店舗名
  public val txtBoxno:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val txtShopname:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 入力した箱ラベル
  public var inputedBoxno:String = ""

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel01>> = MutableLiveData()
  public val itemList:LiveData<MutableList<PotDataModel01>> get() = _itemList

  init {}

  /**
   * 箱ラベルから店舗名と商品を取得します
   */
  public fun pickItemList() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        // 箱ラベルから店舗名を取得します
        val pairValue01 = model01.pickShopname( AppBase.boxConfirmURL, inputedBoxno )
        txtShopname.value = pairValue01.second

        // 箱ラベルから商品を取得します
        val pairValue02 = model01.pickItemList( AppBase.boxConfirmURL, inputedBoxno )
        _itemList.value = pairValue02.second

        _apiCondition.value = "FN"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxConfirm", "致命的エラー" )
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
