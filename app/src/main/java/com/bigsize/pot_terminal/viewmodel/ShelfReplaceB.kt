package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShelfReplaceB:ViewModel() {
  // 表示用の"出棚・入物・入棚"
  public val txtLocation01:MutableLiveData<String> by lazy { MutableLiveData<String>() }
  public val txtBox:MutableLiveData<String> by lazy { MutableLiveData<String>() }
  public val txtLocation02:MutableLiveData<String> by lazy { MutableLiveData<String>() }

  // 記録用の"ロケーション・商品"
  public var memLocation01:String = ""
  public var memBox:String = ""
  public var memLocation02:String = ""

  init {}
}
