package com.bigsize.pot_terminal

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.PotDivision

class AppBase:Application() {
  companion object {
    // アクセス先FQDN
    // 本番 : hightech.bigsize.co.jp
    // 社内LAN : t-hightech-jp.corp.bigsize.com
    // ｴﾐｭﾚｰﾀｰ : 10.0.2.2
    private val fqdnURL:String = "t-hightech-jp.corp.bigsize.com"

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
      PotDivision( "棚移データ", "3", "sreplace.dat", "0", false ),
      PotDivision( "箱移データ", "4", "breplace.dat", "0", false ),
      PotDivision( "棚卸データ", "9", "inventory.dat", "0", false ),
    )

    // Wifiの許容レベル - -50〜0まで
    public val  permitWifiLevel:Int = -100

    // 特別動作する端末番号
    public val specialDeviceNO:String = "999"

    // 場所確認関連URL
    public val locationConfirmURL:String = "http://" + fqdnURL + "/api.php?at=potTerminal&st=locationConfirm"

    // POTデータ転送関連URL
    public val transferURL:String = "http://" + fqdnURL + "/api.php?at=potTerminal&st=potTransfer"

    // 一覧入庫関連URL
    public val collationReceivingURL:String = "http://" + fqdnURL + "/api.php?at=potTerminal&st=collationReceiving"

    // 商品照合関連URL
    public val itemInspectionURL:String = "http://" + fqdnURL + "/api.php?at=potTerminal&st=itemInspection"

    // 商品仕分関連URL
    public val boxReceivingURL:String = "http://" + fqdnURL + "/api.php?at=potTerminal&st=boxReceiving"

    // 商品箱出関連URL
    public val boxShippingURL:String = "http://" + fqdnURL + "/api.php?at=potTerminal&st=boxShipping"

    // 棚操作関連URL
    public val boxOperationURL:String = "http://" + fqdnURL + "/api.php?at=potTerminal&st=boxOperation"

    // 正常読取した場合のブザーとバイブレーションのレベル
    public val buzzerOK:Map<String,String> = mapOf( "frequency" to "2000", "sinDuration" to "20", "resDuration" to "0" )
    public val vibrationOK:Map<String,Array<String>> = mapOf( "rate" to arrayOf( "0", "100" ), "volume" to arrayOf( "0", "255" ) )

    // 異常読取した場合のブザーとバイブレーションのレベル
    public val buzzerNG:Map<String,String> = mapOf( "frequency" to "2000", "sinDuration" to "20", "resDuration" to "10" )
    public val vibrationNG:Map<String,Array<String>> = mapOf( "rate" to arrayOf( "0", "150", "20", "150", "20", "150" ), "volume" to arrayOf( "0", "255", "0", "255", "0", "255" ) )

    // 読取エラーした場合のブザーとバイブレーションのレベル
    public val buzzerAR:Map<String,String> = mapOf( "frequency" to "1950", "sinDuration" to "20", "resDuration" to "10" )
    public val vibrationAR:Map<String,Array<String>> = mapOf( "rate" to arrayOf( "0", "150", "20", "150", "20", "150" ), "volume" to arrayOf( "0", "255", "0", "255", "0", "255" ) )

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
