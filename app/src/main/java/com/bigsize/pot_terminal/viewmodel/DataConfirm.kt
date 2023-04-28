package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bigsize.pot_terminal.model.PotDataModel02

class DataConfirm:ViewModel() {
  // 全データ数とチェックしたデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntCheck:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // POTで読んだ商品データ
  public var potDataArray:MutableList<PotDataModel02> = mutableListOf<PotDataModel02>()

  // 選択したPOTファイル
  public var selectedItem:String = ""

  init {}
}
