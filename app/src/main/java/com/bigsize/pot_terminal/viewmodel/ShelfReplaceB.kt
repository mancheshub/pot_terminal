package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShelfReplaceB:ViewModel() {
  // 今回読んだ"出棚・入物・入棚"
  public val txtLocation01:MutableLiveData<String> by lazy { MutableLiveData<String>() }
  public val txtBox:MutableLiveData<String> by lazy { MutableLiveData<String>() }
  public val txtLocation02:MutableLiveData<String> by lazy { MutableLiveData<String>() }

  // 前回読んだ"出棚・入物・商品"
  public var inputedLocation01:String = ""
  public var inputedBox:String = ""
  public var inputedLocation02:String = ""

  init {}
}
