package com.bigsize.pot_terminal.model

import android.util.Log
import com.bigsize.pot_terminal.BuildConfig
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppUtility {
  /**
   * 品番を"4桁-4桁"の形式に変換します
   *
   * @param [cdData] 変換する品番
   * @return 変換された品番
   */
  public fun eightdigitsCd( cdData:String ):String {
    var trueData = ""

    // 10桁品番に変換します
    trueData = convertTrueCd( cdData )

    if( BuildConfig.DEBUG ) Log.d( "APP-Utility - eightdigitsCd","調整前10桁品番 = " + trueData )

    trueData = trueData.padStart( 10, '0' )

    if( BuildConfig.DEBUG ) Log.d( "APP-Utility - eightdigitsCd","調整後10桁品番 = " + trueData )

    if( BuildConfig.DEBUG ) Log.d( "APP-Utility - eightdigitsCd","前4桁 = " + trueData.substring( 2, 6 ) )
    if( BuildConfig.DEBUG ) Log.d( "APP-Utility - eightdigitsCd","後4桁 = " + trueData.substring( 6, 10 ) )

    return trueData.substring( 2, 6 ) + "-" + trueData.substring( 6, 10 )
  }

  /**
   * 品番を"10桁"の形式に変換します
   *
   * @param [cdData] 変換する品番
   * @return 変換された品番
   */
  public fun convertTrueCd( cdData:String ):String {
    var trueData:String = ""
    var ii:String = ""

    if( BuildConfig.DEBUG ) Log.d( "APP-Utility", "isNumber = " + isNumber(cdData) )
    if( BuildConfig.DEBUG ) Log.d( "APP-Utility", "lenfth = " + cdData.length )

    // 品番が9999999999の場合はisNumber()がfalseとなるのでここで判定します
    if( cdData == "9999999999" ) { return cdData }

    // 10桁品番の場合はそのまま返却します
    if( isNumber(cdData) && cdData.length == 10 ) { return cdData }

    val tmpArray:List<String> = cdData.split( "-" )

    if( BuildConfig.DEBUG ) Log.d( "APP-Utility - convertTrueCd","品番ハイフン分割数 = " + tmpArray.size.toString() )

    if( tmpArray.size == 1 ) { trueData = tmpArray[0] }

    else if( tmpArray.size == 2 ) {
      if( BuildConfig.DEBUG ) Log.d( "APP-Utility - convertTrueCd","品番ハイフン分割数0番目の長さ = " + tmpArray[0].length.toString() )
      if( BuildConfig.DEBUG ) Log.d( "APP-Utility - convertTrueCd","品番ハイフン分割数1番目の長さ = " + tmpArray[1].length.toString() )

      // "XXXX-XXXX"の場合
      if( tmpArray[0].length == 4 && tmpArray[1].length == 4 ) {
        trueData = tmpArray[0] + tmpArray[1]
      }

      // "XX-XXXX"の場合
      if( tmpArray[0].length == 2 && tmpArray[1].length == 4 ) {
        trueData = "00" + tmpArray[0] + tmpArray[1]
      }

      // "XXX-XXXXXX"の場合
      if( tmpArray[1].length == 6 ) {
        trueData = tmpArray[1]
      }
    }

    else if( tmpArray.size == 3 ) {
      // "XXX-XXXX-XXXX"の場合
      if( tmpArray[1].length == 4 && tmpArray[2].length == 4 ) {
        trueData = tmpArray[1] + tmpArray[2]
      }
    }

    else { return "" }

    if( BuildConfig.DEBUG ) Log.d( "APP-Utility - convertTrueCd", "途中品番 = " + trueData )

    if( trueData.length == 8 ) {
      val first:String = trueData.substring( 0, 1 )

      if( first == "0" ) { trueData = trueData.substring( 2 ) }
      else if( first == "1" || first == "2" ) { trueData = "10" + trueData }
      else { trueData = "99" + trueData }

      if( BuildConfig.DEBUG ) Log.d( "APP-Utility - convertTrueCd", "品番 = " + trueData )
    }

    else if( 3 <= trueData.length && trueData.length < 8 ) {
      trueData = trueData
    }

    else { return "" }

    return trueData
  }

  /**
   * POTに記録する日時文字列を返却します
   *
   * @return POTに記録する日時文字列
   */
  public fun returnPRecodeDate():Map<String,String> {
    val nowDT = LocalDateTime.now()

    val dateHash:Map<String,String> = mapOf(
      "date" to DateTimeFormatter.ofPattern("yy/MM/dd").format( nowDT ),
      "time" to DateTimeFormatter.ofPattern("HH:mm:ss").format( nowDT ),
    )

    return dateHash
  }

  /**
   * POTファイルに含める日時文字列を返却します
   *
   * @return POTファイルに含める日時文字列
   */
  public fun returnPFileNameDate():Map<String,String> {
    val nowDT = LocalDateTime.now()

    val dateHash:Map<String,String> = mapOf(
      "date" to DateTimeFormatter.ofPattern("yyMMdd").format( nowDT ),
      "time" to DateTimeFormatter.ofPattern("HHmmss").format( nowDT ),
    )

    return dateHash
  }

  /**
   * 文字列が数値であるかを判定します
   *
   * @param [s] 判定する文字列
   * @return 判定結果 true : 数値である false : 数値でない
   */
  public fun isNumber( s:String ):Boolean {
    return try {
      s.toInt()
      true
    } catch( ex:NumberFormatException ) {
      false
    }
  }
}