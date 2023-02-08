package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Entrance: ViewModel() {
  // 利用者番号
  public val txtNumber:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  init {}
}