package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bigsize.pot_terminal.model.PotDataModel02

class DataConfirm:ViewModel() {
  // 全データ数とPOTで読んだデータ数
  public val txtView01:MutableLiveData<String> by lazy { MutableLiveData<String>() }
  public val spnSelect01:MutableLiveData<String> by lazy { MutableLiveData<String>() }

  // POTで読んだ商品データ
  public var potDataArray:MutableList<PotDataModel02> = mutableListOf<PotDataModel02>()

  // 選択したPOTファイル
  public var selectedItem:String = ""

  init {}
}