package com.bigsize.pot_terminal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.ExamLocationAPI
import com.bigsize.pot_terminal.model.PotDataModel02
import com.bigsize.pot_terminal.model.PotDataModel04
import kotlinx.coroutines.launch

class ExamLocation: ViewModel() {
  private var model01:ExamLocationAPI = ExamLocationAPI()

  // 読んだ商品
  public val txtCd:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtCn:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtSz:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtItn:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 現在入力されている商品
  public var memCd:String = ""
  public var memCn:String = ""
  public var memSz:String = ""

  // ロケーションリスト
  private val _locationList:MutableLiveData<MutableList<PotDataModel04>> = MutableLiveData( mutableListOf() )
  public val locationList:LiveData<MutableList<PotDataModel04>> get() = _locationList

  // API通信状況
  // ST → 通信開始 ER → 通信エラー FN → 通信終了
  private val _apiCondition:MutableLiveData<String> = MutableLiveData( "" )
  public val apiCondition:LiveData<String> get() = _apiCondition

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
        if( isClear == "NON" ) _locationList.value = model01.pickLocation( AppBase.examLocationURL, memCd, memCn, memSz )
        if( isClear == "YES" ) _locationList.value = mutableListOf()
        _apiCondition.value = "FN"
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-ExamLocation", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }
}