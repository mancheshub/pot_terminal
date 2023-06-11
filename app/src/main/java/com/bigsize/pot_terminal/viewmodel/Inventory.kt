package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Inventory: ViewModel() {
  // 全体累計数量とロケーション累計数量
  public val txtCount01:MutableLiveData<String> = MutableLiveData( "0" )
  public val txtCount02:MutableLiveData<String> = MutableLiveData( "0" )

  // 今回読んだ"検棚・商品・数量"
  public val txtLocation:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtItem:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val edtAmt:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 前回読んだ"検棚・商品"
  public var inputedLocation:String = ""
  public var inputedItem:String = ""

  init {}

  /**
   * 累計数量をカウントアップします
   *
   * @param [mode] モード 01: 全体累計数量 02: ロケーション累計数量
   * @param [amt] カウントアップする数量
   */
  public fun countUPAmt( mode:String, amt:String ) {
    if( mode == "01" ) { txtCount01.value = ( txtCount01.value!!.toInt() + amt.toInt() ).toString() }
    if( mode == "02" ) { txtCount02.value = ( txtCount02.value!!.toInt() + amt.toInt() ).toString() }
  }

  /**
   * 累計数量をクリアします
   *
   * @param [mode] モード 01: 全体累計数量 02: ロケーション累計数量
   */
  public fun clearAmt( mode:String ) {
    if( mode == "01" ) { txtCount01.value = "0" }
    if( mode == "02" ) { txtCount02.value = "0" }
  }
}
