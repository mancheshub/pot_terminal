package com.bigsize.pot_terminal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.LocationConfirmAPI
import com.bigsize.pot_terminal.model.PotDataModel02
import com.bigsize.pot_terminal.model.PotDataModel04
import kotlinx.coroutines.launch

class LocationConfirm: ViewModel() {
  private var model01:LocationConfirmAPI = LocationConfirmAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 商品と商品名
  public val txtCd:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtCn:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtSz:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtItn:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 入力した商品
  public var inputedCd:String = ""
  public var inputedCn:String = ""
  public var inputedSz:String = ""

  // ロケーションリスト
  private val _locationList:MutableLiveData<MutableList<PotDataModel04>> = MutableLiveData()
  public val locationList:LiveData<MutableList<PotDataModel04>> get() = _locationList

  init {}

  /**
   * 品番・色番・サイズから商品のロケーションを取得します
   *
   * @param [isClear] ロケーションデータのクリア指示 YES → クリアする NON → クリアしない
   */
  public fun pickLocation( isClear:String ) {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        if( isClear == "NON" ) {
          val pairHash01 = model01.pickLocation( AppBase.locationConfirmURL, inputedCd, inputedCn, inputedSz )
          _locationList.value = pairHash01.second
        }

        if( isClear == "YES" ) _locationList.value = mutableListOf()
        _apiCondition.value = "FN"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-LocationConfirm", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }
}
