package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShelfShipping:ViewModel() {
  // 今回読んだ"出棚・商品・数量"
  public val txtLocation:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtItem:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val edtAmt:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 前回読んだロケーション
  private var _memLocation:String = ""
  public val memLocation:String get() = _memLocation

  // 前回読んだ商品
  private var _memItem:String = ""
  public val memItem:String get() = _memItem

  init {}

  /**
   * 前回読んだ情報を記録します
   *
   * @param x
   */
  public fun setMemLocation( x:String ) { _memLocation = x }
  public fun setMemItem( x:String ) { _memItem = x }
}
