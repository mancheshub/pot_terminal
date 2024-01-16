package com.bigsize.pot_terminal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.AssortHistory
import com.bigsize.pot_terminal.model.BoxReceivingAPI
import kotlinx.coroutines.launch

class BoxReceiving: ViewModel() {
  private var model01:BoxReceivingAPI = BoxReceivingAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // 連続仕分回数
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 商品と箱ラベルと箱ラベル背景色と箱ラベル横帯の背景色
  public val txtCd:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtCn:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtSz:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtBoxno:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val bkgBoxno:MutableLiveData<String> by lazy { MutableLiveData( "M" ) }
  public val bkgBand:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 過去読んだ商品と決定した箱ラベル
  public var historyArray:MutableList<AssortHistory> = mutableListOf()

  // 読んだ商品と決定した箱ラベル
  public var inputedCd:String = ""
  public var inputedCn:String = ""
  public var inputedSz:String = ""
  public var selectedBoxno:String = ""

  init {
    // 履歴を初期化します
    historyArray.add( AssortHistory( "", "", "", "" ) )
    historyArray.add( AssortHistory( "", "", "", "" ) )
    historyArray.add( AssortHistory( "", "", "", "" ) )
  }

  /**
   * 箱ラベルを決定します
   */
  public fun pickBoxNO() {
    viewModelScope.launch {
      _apiCondition.value = "ST"

      try {
        // 箱ラベルを決定します
        val pairHash01 = model01.pickBoxNO( AppBase.boxReceivingURL, inputedCd, inputedCn, inputedSz )

        if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "箱ラベル = " + pairHash01.second )

        // 箱ラベルを表示します
        txtBoxno.value = pairHash01.second

        // 箱ラベル背景色を決定します
        if( pairHash01.second != "" && pairHash01.second.substring( 0, 1 ) == "A" ) bkgBoxno.value = "A"
        if( pairHash01.second != "" && pairHash01.second.substring( 0, 1 ) == "B" ) bkgBoxno.value = "B"
        if( pairHash01.second != "" && pairHash01.second.substring( 0, 1 ) == "C" ) bkgBoxno.value = "C"
        if( pairHash01.second != "" && pairHash01.second.substring( 0, 1 ) == "D" ) bkgBoxno.value = "D"
        if( pairHash01.second != "" && pairHash01.second.substring( 0, 1 ) == "E" ) bkgBoxno.value = "E"

        // 決定した箱ラベルを記録します
        selectedBoxno = pairHash01.second

        _apiCondition.value = pairHash01.first
      } catch( e:Exception ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "致命的エラー" )
        _apiCondition.value = "ER"
        e.printStackTrace()
      }
    }
  }

  /**
   * 履歴を更新します
   */
  public fun updateHistory() {
    // 履歴01回前と02回前を取得します
    val assortData02:AssortHistory = historyArray[0]
    val assortData03:AssortHistory = historyArray[1]

    // 今回の履歴を作ります
    val assortData01:AssortHistory = AssortHistory( inputedCd, inputedCn, inputedSz, selectedBoxno )

    historyArray.clear()
    historyArray.add( assortData01 )
    historyArray.add( assortData02 )
    historyArray.add( assortData03 )
  }
}
