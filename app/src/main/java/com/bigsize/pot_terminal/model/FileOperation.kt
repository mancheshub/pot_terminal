package com.bigsize.pot_terminal.model

import android.util.Log
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BuildConfig
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class FileOperation() {
  /**
   * 端末番号を取得します
   *
   * @return 端末番号
   */
  public fun readDeviceNO():String {
    var fileArray:MutableList<String> = mutableListOf()
    var deviceNO:String = ""

    fileArray = readData( AppBase.app.filesDir.toString() + "/deviceNO.dat" )

    if( fileArray.size != 0 ) { deviceNO = fileArray[0] }

    return deviceNO
  }

  /**
   * 端末番号を保存します
   *
   * @param [mode] 書き込みモード OVERWRITE：上書 APPEND：追記
   * @param [deviceNO] 端末番号
   */
  public fun saveDeviceNO( mode:String, deviceNO:String ) {
    // ファイルパスを決定します
    var filePath = AppBase.app.filesDir.toString() + "/deviceNO.dat"

    var fileArray:MutableList<String> = mutableListOf()
    fileArray.add( deviceNO )

    saveData( mode, filePath, fileArray )
  }

  /**
   * POTデータ行数をカウントします
   *
   * @param [division] POTデータ種別 "1"：入庫データ "2"：出庫データ "9"：棚卸しデータ
   * @return POTデータ行数
   */
  public fun countPotData( division:String ):Int {
    // ファイルパスを決定します
    var position = AppBase.potDivision.indexOfFirst { it.division == division }
    var filePath = AppBase.app.filesDir.toString() + "/" + AppBase.potDivision[position].fileName

    val path = Paths.get( filePath )
    var count:Int = 0

    if( Files.exists( path ) == true ) count = Files.lines( path ).count().toInt()

    return count
  }

  /**
   * POTデータを削除します
   *
   * @param [division] POTデータ種別 "1"：入庫データ "2"：出庫データ "9"：棚卸しデータ
   * @return POTデータ行数
   */
  public fun deletePotData( division:String ) {
    // ファイルパスを決定します
    var position = AppBase.potDivision.indexOfFirst { it.division == division }
    var filePath = AppBase.app.filesDir.toString() + "/" + AppBase.potDivision[position].fileName

    val path = Paths.get( filePath )

    try {
      Files.delete( path );
    } catch( e:IOException ) {
      e.printStackTrace()
    }
  }

  /**
   * POTデータを取得します
   *
   * @param [division] POTデータ種別 "1"：入庫データ "2"：出庫データ "9"：棚卸しデータ
   * @return POTデータ
   */
  public fun readPotData( division:String ):MutableList<PotDataModel02> {
    var fileArray:MutableList<String> = mutableListOf()
    var dataArray:MutableList<PotDataModel02> = mutableListOf()

    // ファイルパスを決定します
    var position = AppBase.potDivision.indexOfFirst { it.division == division }
    var filePath = AppBase.app.filesDir.toString() + "/" + AppBase.potDivision[position].fileName

    fileArray = readData( filePath )

    // 001 12/09/11 15:01:10 149 1 00000000000 91040110000 1011492256 01 5L   006
    // 012 34567890 12345678 901 2 34567890123 45678901234 5678901234 56 7890 1
    // 0          1           2           3          4          5           6

    for( _item in fileArray ) {
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "日付 = " + _item.substring(3,11) )
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "時間 = " + _item.substring(11,19) )
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "スタッフ番号 = " + _item.substring(19,22) )
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "モード = " + _item.substring(22,23) )
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "品番 = " + _item.substring(45,55) )
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "色番 = " + _item.substring(55,57) )
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "サイズ = " + _item.substring(57,61).replace(" ","") )
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "ロケーション01 = " + _item.substring(23,34) )
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "ロケーション02 = " + _item.substring(34,45) )
      if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "数量 = " + _item.substring(61) )

      var model01:PotDataModel02 = PotDataModel02(
        "000",
        _item.substring(3,11),
        _item.substring(11,19),
        _item.substring(19,22),
        _item.substring(22,23),
        _item.substring(45,55),
        _item.substring(55,57),
        _item.substring(57,61).replace(" ",""),
        _item.substring(23,34),
        _item.substring(34,45),
        _item.substring(61),
        false,
      )

      dataArray.add( model01 )
    }

    return dataArray
  }

  /**
   * POTデータを作成します
   *
   * @param [mode] 書き込みモード OVERWRITE：上書 APPEND：追記
   * @param [division] POTデータ種別 "1"：入庫データ "2"：出庫データ "9"：棚卸しデータ
   * @param [dataArray] POTデータ(複数指定可)
   */
  public fun savePotData( mode:String, division:String, dataArray:MutableList<PotDataModel02> ) {
    // ファイルパスを決定します
    var position = AppBase.potDivision.indexOfFirst { it.division == division }
    var filePath = AppBase.app.filesDir.toString() + "/" + AppBase.potDivision[position].fileName

    // MutableList<PotDataModel02>をMutableList<String>に変換してファイルデータを作成します

    var fileArray:MutableList<String> = mutableListOf()
    lateinit var fileData:String

    for( _item in dataArray ) {
      fileData =  _item.deviceNO + _item.date + _item.time + _item.staffNO + division + _item.location01 + _item.location02 +
                  _item.cd.padStart(10,'0') + _item.cn + _item.sz.padEnd(4,' ') + _item.amt.padStart(3,'0')
      fileArray.add( fileData )
    }

    saveData( mode, filePath, fileArray )
  }

  /**
   * ファイルにデータを保存します
   *
   * @param [mode] データ保存モード OVERWRITE or APPEND
   * @param [filePath] データファイルパス
   * @param [fileArray] 保存するデータ
   */
  private fun saveData( mode:String, filePath:String, fileArray:MutableList<String> ) {
    if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "モード = " + mode )
    if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "ファイルパス = " + filePath )

    val path = Paths.get( filePath )
    val charset = Charset.forName( "UTF-8" )

    if( mode == "APPEND" && Files.exists(path) == false ) { Files.createFile( path ) }

    try {
      if( mode == "OVERWRITE" ) { Files.write( path, fileArray, charset, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING ) }
      if( mode == "APPEND" ) { Files.write( path, fileArray, charset, StandardOpenOption.APPEND ) }
    } catch( e:IOException ) {
      e.printStackTrace()
    }
  }

  /**
   * ファイルからデータを読み込みます
   *
   * @param [filePath] データファイルパス
   * @return ファイルから読み取ったデータ
   */
  private fun readData( filePath:String ):MutableList<String> {
    var fileData:MutableList<String> = mutableListOf<String>()

    if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "ファイルパス = " + filePath )

    try {
      val file = File( filePath )
      val bfReader = file.bufferedReader()

      var line:String? = null

      while( true ) {
        line = bfReader.readLine()
        if( line == null ) { break }

        fileData.add( line )

        if( BuildConfig.DEBUG ) Log.d( "APP-FileOperation", "１行データ = " + line )
      }
    } catch( e:IOException ) {
      return fileData
    }

    return fileData
  }
}
