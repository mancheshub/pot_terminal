package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShelfReplaceS: ViewModel() {
  // 今回読んだ"入棚・出棚・商品・数量"
  public val txtLocation01:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtLocation02:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtItem:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val edtAmt:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 前回読んだ"入棚・出棚・商品"
  public var inputedLocation01:String = ""
  public var inputedLocation02:String = ""
  public var inputedItem:String = ""

  init {}
}
