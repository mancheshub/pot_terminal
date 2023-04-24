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

  // 箱番号と店舗名
  public val txtBoxno:MutableLiveData<String> by lazy { MutableLiveData( "" ) }
  public val txtShopname:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 商品リスト
  private val _itemList:MutableLiveData<MutableList<PotDataModel01>> = MutableLiveData( mutableListOf() )
  public val itemList:LiveData<MutableList<PotDataModel01>> get() = _itemList

  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // API通信状況
  // ST → 通信開始 ER → 通信エラー FN → 通信終了
  private val _apiCondition:MutableLiveData<String> = MutableLiveData( "" )
  public val apiCondition:LiveData<String> get() = _apiCondition

  init {}

  /**
   * 箱番号から店舗名と商品を取得します
   */
  public fun pickItem() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        // 箱番号から店舗名を取得します
        val pairValue01 = model01.pickShopname( AppBase.boxConfirmURL, ( txtBoxno.value as String ) )
        txtShopname.value = pairValue01.second

        // 箱番号から商品を取得します
        val pairValue02 = model01.pickItem( AppBase.boxConfirmURL, ( txtBoxno.value as String ) )
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