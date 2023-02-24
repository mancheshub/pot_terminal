package com.bigsize.pot_terminal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bigsize.pot_terminal.AppBase.Companion.vibrationOK
import com.bigsize.pot_terminal.model.*
import com.densowave.bhtsdk.barcode.*
import com.densowave.bhtsdk.barcode.BarcodeManager.BarcodeManagerListener
import com.densowave.bhtsdk.barcode.BarcodeScanner.BarcodeDataListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import kotlin.concurrent.thread

/**
 * 共通のアクティビティクラス
 */
open class CommonBase:AppCompatActivity() {
  /**
   * -- 共通プロパティ
   */

  /**
   * コルーチン関係
   */

  // コルーチンスコープを定義します
  private val mainScope = CoroutineScope( Job() + Dispatchers.Main )
  private val defaultScope = CoroutineScope( Job() + Dispatchers.Default )

  /**
   * サウンド関係
   */

  // 正常読取した場合のブザー - ピッ
  public val playSoundOK:PlaySound by lazy {
    PlaySound( AppBase.buzzerOK["frequency"]!!, AppBase.buzzerOK["sinDuration"]!!, AppBase.buzzerOK["resDuration"]!! )
  }

  // 異常読取した場合のブザー - ピッピッ
  public val playSoundNG:PlaySound by lazy {
    PlaySound( AppBase.buzzerNG["frequency"]!!, AppBase.buzzerNG["sinDuration"]!!, AppBase.buzzerNG["resDuration"]!! )
  }

  // 途中読取した場合のブザー - 高音のピッ
  public val playSoundCT:PlaySound by lazy {
    PlaySound( AppBase.buzzerCT["frequency"]!!, AppBase.buzzerCT["sinDuration"]!!, AppBase.buzzerCT["resDuration"]!! )
  }

  // 完了した場合のブザー - ピッー
  public val playSoundFN:PlaySound by lazy {
    PlaySound( AppBase.buzzerFN["frequency"]!!, AppBase.buzzerFN["sinDuration"]!!, AppBase.buzzerFN["resDuration"]!! )
  }

  /**
   * Wifi関係
   */

  public val statusWifi:StatusWifi by lazy {
    StatusWifi( this )
  }

  /**
   * バイブレーション関係
   */

  public val playVibration:PlayVibration by lazy {
    PlayVibration( this )
  }

  /**
   * スキャナ関係
   */

  // 読み取った複数商品QRデータ
  protected val _scanMultiItem:MutableLiveData<String> = MutableLiveData()
  public val scanMultiItem:LiveData<String> get() = _scanMultiItem

  // 読み取った棚QRデータ
  protected val _scanShelf:MutableLiveData<String> = MutableLiveData()
  public val scanShelf:LiveData<String> get() = _scanShelf

  // 読み取った箱QRデータ
  protected val _scanBox:MutableLiveData<String> = MutableLiveData()
  public val scanBox:LiveData<String> get() = _scanBox

  // 読み取った商品QRデータ
  protected val _scanItem:MutableLiveData<String> = MutableLiveData()
  public val scanItem:LiveData<String> get() = _scanItem

  /**
   * -- 共通ライフサイクルメソッド
   */

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "onCreate" )

    // ナビゲーションの戻るリンクをクリックしたときのイベントを補足します
    onBackPressedDispatcher.addCallback(
      this, object: OnBackPressedCallback( true ) { override fun handleOnBackPressed() { finish() } }
    )
  }

  override fun onStart() {
    super.onStart()

    if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "onStart" )

    hideSystemUI()
  }

  override fun onResume() {
    super.onResume()

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "onResume" )
  }

  override fun onPause() {
    super.onPause()

    if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "onPause" )
  }

  override fun onDestroy() {
    super.onDestroy()

    if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "onDestroy" )

    playSoundOK.release()
    playSoundNG.release()
    playSoundFN.release()
    playSoundCT.release()

    playVibration.release()
  }

  /**
   * -- 共通ActionBarメソッド
   */

  /**
   * ActionBarメニューを実装します
   *
   * @param [menu] メニューレイアウト
   * @return
   */
  override fun onCreateOptionsMenu( menu:Menu ):Boolean {
    menuInflater.inflate( R.menu.actionbar_contents, menu )

    return true
  }

  /**
   * ActionBarメニューのイベントを補足します
   * @param [item] アイテムオブジェクト
   * @return
   */
  override fun onOptionsItemSelected( item:MenuItem ):Boolean {
    when( item.itemId ) {
      android.R.id.home -> {
        if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "戻るボタンが選択されました。" )

        finish()
      }
      R.id.iconItem01 -> {
        if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "メニュー01ボタンが選択されました。" )

        val dialog = StaffNODialog( AppBase.staffNO )
        dialog.show( supportFragmentManager, "simple" )
      }
      else -> {}
    }

    return true
  }

  /**
   * -- 共通各種メソッド
   */

  /**
   * 全画面に切り替えます
   */
  private fun hideSystemUI() {
    window.decorView.systemUiVisibility = (
      View.SYSTEM_UI_FLAG_FULLSCREEN or
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
      )
  }

  override fun onWindowFocusChanged( hasFocus:Boolean ) {
    super.onWindowFocusChanged( hasFocus )
    if( hasFocus ) hideSystemUI()
  }

  /**
   * ビープ音を発動します
   *
   * @param [playSound] AudioTrackオブジェクト
   */
  public fun claimSound( playSound:PlaySound ) {
    mainScope.launch {
      // サウンドファイルを再生します
      val job = defaultScope.launch { playSound.play() }

      // スレッドの完了を待ちます
      job.join()

      // サウンドを停止します
      playSound.stop()
    }
  }

  /**
   * バイブレーションを発動します
   *
   * @param [pattern] バイブレーションパターン
   */
  public fun claimVibration( pattern:Map<String,Array<String>> ) {
    playVibration.play( pattern["rate"]!!, pattern["volume"]!! )
  }
}

/**
 * ユニテック社製ハンディーターミナル用のアクティビティクラス
 */
open class UnitechBase:CommonBase() {
  /**
   * -- キーボードの各種プロパティ
   */

  protected val KEY_F01:Int = 999
  protected val KEY_F02:Int = 999
  protected val KEY_F03:Int = 999
  protected val KEY_F04:Int = 999
  protected val KEY_ENT:Int = 66
  protected val KEY_BAK:Int = 999
  protected val KEY_000:Int = 7
  protected val KEY_001:Int = 8
  protected val KEY_002:Int = 9
  protected val KEY_003:Int = 10
  protected val KEY_004:Int = 11
  protected val KEY_005:Int = 12
  protected val KEY_006:Int = 13
  protected val KEY_007:Int = 14
  protected val KEY_008:Int = 15
  protected val KEY_009:Int = 16

  /**
   * -- スキャナの各種プロパティ
   */

  protected val receiver by lazy {
    val readerReceiver = UssReceiver()
    readerReceiver
  }

  protected val filter by lazy {
    val filter = IntentFilter()
    filter.addAction( "unitech.scanservice.data" )
    filter
  }

  /**
   * -- スキャナの各種メソッド
   */

  /**
   * USSサービスを開始します
   */
  protected fun startScanService() {
    var intent = Intent()

    intent.action = "unitech.scanservice.start"
    sendBroadcast( intent )
  }

  /**
   * USSサービスを終了します
   */
  protected fun closeScanService() {
    var intent = Intent()

    intent.action = "unitech.scanservice.close"
    intent.putExtra( "close", true );
    sendBroadcast( intent )
  }

  /**
   * USSサービスの"App Settings"を変更します
   */
  protected fun applyAppSettings() {
    var bundle = Bundle()
    var intent = Intent()

    // 読み取り結果をIntentで通知します

    bundle.clear()
    bundle.putBoolean( "scan2key", false )

    intent.action = "unitech.scanservice.scan2key_setting"
    intent.putExtras( bundle )
    sendBroadcast( intent )

    // バーコードの読み取りに成功したとき振動によってユーザーに通知します

    bundle.clear()
    bundle.putBoolean( "vibration", true )

    intent.action = "unitech.scanservice.vibration"
    intent.putExtras( bundle )
    sendBroadcast( intent )
  }

  /**
   * USSサービスの"Append.Settings"を変更します
   */
  protected fun applyAppendSettings() {
    var bundle = Bundle()
    var intent = Intent()

    // スキャンしたデータの末尾に空白を挿入します

    bundle.clear()
    bundle.putString( "terminator", "" )

    intent.action = "unitech.scanservice.terminator"
    intent.putExtras( bundle )
    sendBroadcast( intent )
  }

  /**
   * -- ライフサイクルのオーバーライドメソッド
   */

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    thread( start = true ) {
      try {
        startScanService()
        Thread.sleep( 500 ) //.USS の開始を 500 ミリ秒以上待機します
        applyAppSettings()
        applyAppendSettings()
      } catch( e:InterruptedException ) {
        e.printStackTrace()
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    if( BuildConfig.DEBUG ) Log.d( "APP-UnitechBase", "onDestroy()" )

    // USSサービスを終了します
    closeScanService()
  }

  override fun onResume() {
    super.onResume()

    if( BuildConfig.DEBUG ) Log.d( "APP-UnitechBase", "onResume()" )

    // SCANの受信を開始します
    registerReceiver( receiver, filter )
  }

  override fun onPause() {
    super.onPause()

    if( BuildConfig.DEBUG ) Log.d( "APP-UnitechBase", "onPause()" )

    // SCANの受信を終了します
    unregisterReceiver( receiver )
  }

  /**
   * スキャナのブロードキャストレシーバークラス
   */

  inner class UssReceiver:BroadcastReceiver() {
    override fun onReceive(context:Context?,intent:Intent? ) {
      intent ?: return

      if( intent.action.equals( "unitech.scanservice.data" ) ) {
        val scanData = intent.getStringExtra( "text" )

        if( BuildConfig.DEBUG ) Log.d( "APP-UnitechBase", "読み取り生データ = " + scanData )

        if( scanData.substring( 0, 3 ) == "M-L" ) this@UnitechBase._scanShelf.value = scanData
        if( scanData.substring( 0, 3 ) == "M-C" ) this@UnitechBase._scanBox.value = scanData
        if( scanData.substring( 0, 3 ) == "M-H" ) this@UnitechBase._scanItem.value = scanData
      }
    }
  }
}

/**
 * デンソー社製ハンディーターミナル用のアクティビティクラス
 */
open class DensoWaveBase:CommonBase(),BarcodeManagerListener,BarcodeDataListener {
  /**
   * -- 機器特有のキーボードレイアウト
   */

  protected val KEY_F01:Int = 131
  protected val KEY_F02:Int = 132
  protected val KEY_F03:Int = 133
  protected val KEY_F04:Int = 134
  protected val KEY_ENT:Int = 66
  protected val KEY_BAK:Int = 67
  protected val KEY_000:Int = 7
  protected val KEY_001:Int = 8
  protected val KEY_002:Int = 9
  protected val KEY_003:Int = 10
  protected val KEY_004:Int = 11
  protected val KEY_005:Int = 12
  protected val KEY_006:Int = 13
  protected val KEY_007:Int = 14
  protected val KEY_008:Int = 15
  protected val KEY_009:Int = 16

  /**
   * -- スキャナの各種プロパティ
   */

  protected var isPointMode = false
  private var mBarcodeManager:BarcodeManager? = null
  private var mBarcodeScanner:BarcodeScanner? = null

  /**
   * -- ライフサイクルのオーバーライドメソッド
   */

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    try {
      BarcodeManager.create( this, this )
    } catch( e:BarcodeException ) {
      e.printStackTrace()
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    if( mBarcodeScanner != null ) {
      try {
        mBarcodeScanner!!.destroy()
      } catch( e:BarcodeException ) {
        e.printStackTrace()
      }

      mBarcodeScanner = null
    }

    if( mBarcodeManager != null ) {
      mBarcodeManager!!.destroy()
      mBarcodeManager = null
    }
  }

  override fun onPause() {
    super.onPause();

    if( mBarcodeScanner != null ) {
      try {
        mBarcodeScanner!!.deleteSQRCEncryptKeys( this );
        mBarcodeScanner!!.close();
      } catch( e:BarcodeException ) {
        e.printStackTrace()
      }
    }
  }

  override fun onResume() {
    super.onResume()

    if( mBarcodeScanner != null ) {
      try {
        val settings = mBarcodeScanner!!.settings
        settings.decode.symbologies.itf.enabled = true

        mBarcodeScanner!!.settings = settings
        mBarcodeScanner!!.claim()
      } catch( e:BarcodeException ) {
        e.printStackTrace()
      }
    }
  }

  /**
   * -- バーコードAPIの各種メソッド
   */

  override fun onBarcodeManagerCreated( barcodeManager:BarcodeManager ) {
    mBarcodeManager = barcodeManager

    try {
      val listScanner = mBarcodeManager!!.barcodeScanners

      if( listScanner.size > 0 ) {
        mBarcodeScanner = listScanner[0]

        mBarcodeScanner!!.addDataListener( this )

        val settings:BarcodeScannerSettings = mBarcodeScanner!!.getSettings()
        settings.notification.sound.enabled = false
        settings.notification.vibrate.enabled = false
        settings.decode.symbologies.codabar.enabled = true

        // ポイントスキャンモードを設定します
        if( isPointMode == false ) settings.decode.pointScanMode = DecodeSettings.PointScanMode.DISABLED
        if( isPointMode == true ) settings.decode.pointScanMode = DecodeSettings.PointScanMode.ENABLED

        mBarcodeScanner!!.setSettings( settings )

        mBarcodeScanner!!.claim()
      }
    } catch( e:BarcodeException ) {
      e.printStackTrace()
    }
  }

  override fun onBarcodeDataReceived( event:BarcodeDataReceivedEvent ) {
    val listBarcodeData = event.barcodeData

    for( barcodeData in listBarcodeData ) {
      val data = barcodeData.data
      val barcodeDataLength = data.length

      runOnUiThread(object:Runnable {
        var data:String? = null

        fun setBarcodeData( data:String? ):Runnable? {
          this.data = data
          return this
        }

        override fun run() {
          if( BuildConfig.DEBUG ) Log.d( "APP-DensoWaveBase", "読み取り生データ = " + this.data )

          val _data:String = this.data ?: ""

          if( _data.substring( 0, 3 ) == "M-P" ) _scanMultiItem.value = _data
          if( _data.substring( 0, 3 ) == "M-L" ) _scanShelf.value = _data
          if( _data.substring( 0, 3 ) == "M-C" ) _scanBox.value = _data
          if( _data.substring( 0, 3 ) == "M-H" ) _scanItem.value = _data
          if( _data.substring( 0, 1 ) == "a" ) _scanItem.value = _data
        }
      }.setBarcodeData( data ) )
    }
  }
}
