package com.bigsize.pot_terminal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.DataTransferAPI
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.PotDivision
import kotlinx.coroutines.launch
import java.io.File

class DataTransfer:ViewModel() {
  private var model01:FileOperation = FileOperation()
  private var model02:AppUtility = AppUtility()
  private var model03:DataTransferAPI = DataTransferAPI()

  // API通信状況
  private val _apiCondition:MutableLiveData<String> = MutableLiveData()
  public val apiCondition:LiveData<String> get() = _apiCondition

  // チェックしたデータ数
  public val cntCheck:MutableLiveData<String> by lazy { MutableLiveData( "0" ) }

  // 全データ数・進捗バー
  public val prgRate:MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

  // POTファイル
  public var potFileArray:MutableList<PotDivision> = mutableListOf<PotDivision>()

  init { initPOTData() }

  /**
   * POTデータを初期化します
   */
  public fun initPOTData() {
    potFileArray.clear()

    for( _item in AppBase.potDivision ) {
      var potData:PotDivision = PotDivision( _item.name, _item.division, _item.fileName, "0", false )

      val count:Int = model01.countPotData( potData.division )

      // POTデータ行数をカウントします
      potData.amt = count.toString()

      potFileArray.add( potData )
    }
  }

  /**
   * POTデータをアップロードします
   *
   * @return HTTP通信可否
   */
  public fun uploadPOTData( deviceNO:String ) = viewModelScope.launch {
    var type:String = ""
    var line:String? = null
    var lineNO:Int = 0
    var allLineNO:Int = 0
    var maxCount:Int = 0
    var potType:String = ""
    var fileKey:String = ""
    var fileData:String = ""
    val dateHash:Map<String,String> = model02.returnPFileNameDate()
    var fileArray:MutableList<String> = mutableListOf()

    _apiCondition.value = "ST"

    dofor@for( _item in potFileArray ) {
      if( _item.isChecked == false ) continue

      if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "チェックしたPOTファイル = " + _item.name )

      // POTデータのタイプを決定します
      if( _item.division == "1" ) { type = "P"; }
      if( _item.division == "2" ) { type = "P"; }
      if( _item.division == "3" ) { type = "P"; }
      if( _item.division == "4" ) { type = "P"; }
      if( _item.division == "9" ) { type = "T"; }

      val file:File = File( AppBase.app.filesDir.toString() + "/" + _item.fileName )

      // ファイルが存在しなければ以降は何もしません
      if( file.exists() == false ) continue

      // POTデータ行数をカウントします
      maxCount = model01.countPotData( _item.division )

      // POTデータが0行の場合は以降は何もしません
      if( maxCount == 0 ) continue

      // POT種別(POT区分＋端末番号)とファイルキーを作成します
      potType = type + deviceNO
      fileKey = dateHash["date"]!! + dateHash["time"]!! + _item.division;

      // データ転送を開始します

      try {
        model03.startPOTData( AppBase.transferURL, potType, fileKey )
      } catch( e:Exception ) {
        e.printStackTrace()
        _apiCondition.value = "ER"
        break@dofor
      }

      if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "データ転送開始 ..." )

      val bfReader = file.bufferedReader()

      while( true ) {
        line = bfReader.readLine()
        if( line == null ) { break }

        lineNO ++
        allLineNO ++
        fileArray.add( line )

        if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "データ読み込み = " + line )

        // 250件に満たない場合は次の行を記録します
        if( lineNO != 250 ) continue

        // 進捗値を計算します
        prgRate.value = allLineNO*100/maxCount

        // POTデータを転送します

        if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "データ転送(ループ内)" )

        //_apiCondition.value = _item.name + "を" + "%,d".format(allLineNO) + "件まで転送済み ..."

        // データ転送を実施します

        try {
          for( _item in fileArray ) { fileData = fileData + _item + "\r\n" }

          model03.execPOTData( AppBase.transferURL, potType, fileKey, fileData )
        } catch( e:Exception ) {
          e.printStackTrace()
          _apiCondition.value = "ER"
          break@dofor
        }

        if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "データ転送中 ..." )

        lineNO = 0
        fileArray.clear()
      }

      // 進捗値を計算します
      prgRate.value = allLineNO*100/maxCount

      //_apiCondition.value = _item.name + "を" + "%,d".format(allLineNO) + "件まで転送済み ..."

      // 残りのPOTデータの転送を実施します

      if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "データ転送(ループ外)" )

      try {
        for( _item in fileArray ) { fileData = fileData + _item + "\r\n" }

        model03.execPOTData( AppBase.transferURL, potType, fileKey, fileData )
      } catch( e:Exception ) {
        e.printStackTrace()
        _apiCondition.value = "ER"
        break@dofor
      }

      if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "データ転送完了 ..." )

      lineNO = 0
      allLineNO = 0
      fileArray.clear()
      prgRate.value = 0

      // ファイルを削除します

      try {
        model01.deletePotData( _item.division )
      } catch( e:Exception ) {
        _apiCondition.value = "ER"
        break@dofor
      }
    }

    if( _apiCondition.value != "" &&  _apiCondition.value != "ER" ) {
      _apiCondition.value = "FN"

      // データ転送を終了します

      try {
        model03.finishPOTData( AppBase.transferURL, potType, fileKey )
      } catch( e:Exception ) {
        e.printStackTrace()
        _apiCondition.value = "ER"
      }
    }
  }
}
