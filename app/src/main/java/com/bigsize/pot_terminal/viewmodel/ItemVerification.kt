package com.bigsize.pot_terminal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bigsize.pot_terminal.model.PotDataModel01

class ItemVerification:ViewModel() {
  // 全データ数とPOTで読んだデータ数
  public val cntTotal:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }
  public val cntRead:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  public val txtView01:MutableLiveData<String> by lazy { MutableLiveData<String>() }
  public val txtView02:MutableLiveData<String> by lazy { MutableLiveData<String>() }

  // POTで読んだ商品データ
  public var potDataArray:MutableList<PotDataModel01> = mutableListOf<PotDataModel01>()

  init {
    txtView01.value = "照合 0 点"
    txtView02.value = "全 0 点"
  }
}
