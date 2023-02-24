package com.bigsize.pot_terminal

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.PotDivision

class AppBase:Application() {
  companion object {
    // Applicationインスタンス
    public lateinit var app:Application

    // アクティビティインスタンス
    public var activitySet:MutableSet<Activity> = mutableSetOf()

    // 端末番号
    public var deviceNO:String = "000"

    // スタッフ番号
    public var staffNO:String = "000"

    // POTデータ種別
    public var potDivision:List<PotDivision> = listOf(
      PotDivision( "入庫データ", "1", "receiving.dat", "0", false ),
      PotDivision( "出庫データ", "2", "shipping.dat", "0", false ),
      PotDivision( "棚移動データ", "3", "sreplace.dat", "0", false ),
      PotDivision( "箱移動データ", "4", "breplace.dat", "0", false ),
      PotDivision( "棚卸データ", "9", "inventory.dat", "0", false ),
    )

    // Wifiの許容レベル - -50〜0まで
    public val  permitWifiLevel:Int = -40

    // 特別動作する端末番号
    public val specialDeviceNO:String = "999"

    // POTデータ転送先URL
    public val transferURL:String = "http://d-hightech-jp.corp.bigsize.com/api.php?at=potTerminal&st=potTransfer"

    // 商品照合関連URL
    public val itemInspectionURL:String = "http://d-hightech-jp.corp.bigsize.com/api.php?at=potTerminal&st=itemInspection"

    // 正常読取した場合のブザーとバイブレーションのレベル
    public val buzzerOK:Map<String,String> = mapOf( "frequency" to "2000", "sinDuration" to "20", "resDuration" to "0" )
    public val vibrationOK:Map<String,Array<String>> = mapOf( "rate" to arrayOf( "0", "100" ), "volume" to arrayOf( "0", "255" ) )

    // 異常読取した場合のブザーとバイブレーションのレベル
    public val buzzerNG:Map<String,String> = mapOf( "frequency" to "2000", "sinDuration" to "20", "resDuration" to "10" )
    public val vibrationNG:Map<String,Array<String>> = mapOf( "rate" to arrayOf( "0", "150", "20", "150", "20", "150" ), "volume" to arrayOf( "0", "255", "0", "255", "0", "255" ) )

    // 途中読取した場合のブザーとバイブレーションのレベル
    public val buzzerCT:Map<String,String> = mapOf( "frequency" to "2500", "sinDuration" to "20", "resDuration" to "0" )

    // 完了した場合のブザーとバイブレーションのレベル
    public val buzzerFN:Map<String,String> = mapOf( "frequency" to "2000", "sinDuration" to "350", "resDuration" to "0" )
    public val vibrationFN:Map<String,Array<String>> = mapOf( "rate" to arrayOf( "0", "800" ), "volume" to arrayOf( "0", "255" ) )

    // 今まで開いたActivityを全て終了します
    public fun killApp() { for( a in activitySet ) a.finish() }
  }

  override fun onCreate() {
    super.onCreate()

    // モデルでApplicationクラスを利用するためApplicationクラスのインスタンスを記録します
    app = this
  }
}
