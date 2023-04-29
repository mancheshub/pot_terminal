package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShelfReceiving: ViewModel() {
  // 今回読んだ"入棚・商品・数量"
  public val txtLocation:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtItem:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val edtAmt:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 前回読んだ"入棚・商品"
  public var inputedLocation:String = ""
  public var inputedItem:String = ""

  init {}
}
