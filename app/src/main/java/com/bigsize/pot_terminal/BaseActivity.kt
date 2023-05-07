package com.bigsize.pot_terminal

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bigsize.pot_terminal.model.*
import com.densowave.bhtsdk.barcode.*
import com.densowave.bhtsdk.barcode.BarcodeManager.BarcodeManagerListener
import com.densowave.bhtsdk.barcode.BarcodeScanner.BarcodeDataListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

  // 異常読取した場合のブザー - ピッピッピッ
  public val playSoundNG:PlaySound by lazy {
    PlaySound( AppBase.buzzerNG["frequency"]!!, AppBase.buzzerNG["sinDuration"]!!, AppBase.buzzerNG["resDuration"]!! )
  }

  // 異常読取した場合のブザー - ピッピッ
  public val playSoundAR:PlaySound by lazy {
    PlaySound( AppBase.buzzerAR["frequency"]!!, AppBase.buzzerAR["sinDuration"]!!, AppBase.buzzerAR["resDuration"]!! )
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
   * bluetooth関係
   */

  public val bluetooth:BluetoothAdapter by lazy {
    BluetoothAdapter.getDefaultAdapter()
  }

  /**
   * wifi関係
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

  // 読み取ったマンチェス商品QRデータ
  protected val _scanItemM:MutableLiveData<String> = MutableLiveData()
  public val scanItemM:LiveData<String> get() = _scanItemM

  // 読み取ったはるやま商品QRデータ
  protected val _scanItemH:MutableLiveData<String> = MutableLiveData()
  public val scanItemH:LiveData<String> get() = _scanItemH

  /**
   * -- 共通ライフサイクルメソッド
   */

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "onCreate" )

    // ■ ナビゲーションの戻るリンクをクリックしたときのイベントを補足します

    onBackPressedDispatcher.addCallback(
      this, object: OnBackPressedCallback( true ) { override fun handleOnBackPressed() { finish() } }
    )

    if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "クラス名 = " + componentName.shortClassName )

    // ■ bluetoothを有効化します - 許可しない場合はActivityを閉じます

    if( componentName.shortClassName == ".SortShipping" && bluetooth.isEnabled == false ) {
      val intent = Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE )

      val launcher01:ActivityResultLauncher<Intent> = registerForActivityResult( ActivityResultContracts.StartActivityForResult() ) { result:ActivityResult? ->
        if( result?.resultCode == RESULT_CANCELED ) {
          Toast.makeText( this, getString( R.string.err_bluetooth01 ), Toast.LENGTH_LONG ).show()
          finish()
        }
      }

      // bluetooth有効化ダイアログを表示します
      launcher01.launch( intent )
    }

    // ■ 位置情報の権限を要求します

    val launcher02:ActivityResultLauncher<String> = registerForActivityResult( ActivityResultContracts.RequestPermission() ) { granted:Boolean ->
      if( granted ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "ACCESS_FINE_LOCATION : 許可した" )
      }
    }

    if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
      if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "ACCESS_FINE_LOCATION : 元々許可していた" )
    } else {
      // 権限承認要求ダイアログを表示します
      launcher02.launch( Manifest.permission.ACCESS_FINE_LOCATION )
    }
  }

  override fun onStart() {
    super.onStart()

    if( BuildConfig.DEBUG ) Log.d( "APP-CommonBase", "onStart" )

    hideSystemUI()
  }

  override fun onResume() {
    super.onResume()

    // フラグメント対応していないアクティビティに限ってonResume()時にサウンド＆バイブレーションを発動します

    if( componentName.shortClassName != ".BoxShipping" && componentName.shortClassName != ".BoxOperation" ) {
      claimSound( playSoundOK )
      claimVibration( AppBase.vibrationOK )
    }

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
 * デンソー社製ハンディーターミナル用のアクティビティクラス
 */
open class DensoWaveBase:CommonBase(),BarcodeManagerListener,BarcodeDataListener {
  /**
   * -- 機器特有のキーボードレイアウト
   */

  protected val KEY_F01:Int = 131
  protected val KEY_F02:Int = 61
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
  protected val KEY_UP:Int = 19
  protected val KEY_DOWN:Int = 20
  protected val KEY_LEFT:Int = 21
  protected val KEY_RIGHT:Int = 22

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
          var readFlag:String = "OK"

          // クラス名に従ったコードが読めていなければエラーとします

          if( componentName.shortClassName == ".BoxShipping" &&
              _data.substring( 0, 3 ) != "M-P" && _data.substring( 0, 3 ) != "M-T" && _data.substring( 0, 3 ) != "M-H" ) {
            readFlag = "NG"
            claimSound( playSoundAR )
            claimVibration( AppBase.vibrationAR )
          }

          if( componentName.shortClassName == ".BoxReceiving" && _data.substring( 0, 3 ) != "M-H" ) {
            readFlag = "NG"
            claimSound( playSoundAR )
            claimVibration( AppBase.vibrationAR )
          }

          if( componentName.shortClassName == ".ItemInspection" && _data.substring( 0, 1 ) != "a" ) {
            readFlag = "NG"
            claimSound( playSoundAR )
            claimVibration( AppBase.vibrationAR )
          }

          if( componentName.shortClassName == ".LocationConfirm" && _data.substring( 0, 3 ) != "M-H" ) {
            readFlag = "NG"
            claimSound( playSoundAR )
            claimVibration( AppBase.vibrationAR )
          }

          if( componentName.shortClassName == ".BoxOperation" && _data.substring( 0, 3 ) != "M-T" && _data.substring( 0, 3 ) != "M-H" ) {
            readFlag = "NG"
            claimSound( playSoundAR )
            claimVibration( AppBase.vibrationAR )
          }

          if(
            ( componentName.shortClassName == ".ShelfReceiving" || componentName.shortClassName == ".ShelfShipping" ||
              componentName.shortClassName == ".ShelfReplaceB" || componentName.shortClassName == ".ShelfReplaceS" ||
              componentName.shortClassName == ".Inventory" ) &&
            _data.substring( 0, 3 ) != "M-L" && _data.substring( 0, 3 ) != "M-C" && _data.substring( 0, 3 ) != "M-H" ) {
            readFlag = "NG"
            claimSound( playSoundAR )
            claimVibration( AppBase.vibrationAR )
          }

          // スタッフ番号が不意にクリアされてしまった場合はエラーとします

          if( readFlag == "OK" && AppBase.staffNO == "000" ) {
            val intent = Intent( applicationContext, Failure::class.java )
            intent.putExtra( "MESSAGE", getString( R.string.err_communication02 ) )
            startActivity( intent )
          } else if( readFlag == "OK" && AppBase.staffNO != "000" ) {
            if( _data.substring( 0, 3 ) == "M-P" ) _scanMultiItem.value = _data
            if( _data.substring( 0, 3 ) == "M-L" ) _scanShelf.value = _data
            if( _data.substring( 0, 3 ) == "M-C" ) _scanBox.value = _data
            if( _data.substring( 0, 3 ) == "M-T" ) _scanBox.value = _data
            if( _data.substring( 0, 3 ) == "M-H" ) _scanItemM.value = _data
            if( _data.substring( 0, 1 ) == "a" ) _scanItemH.value = _data
          }
        }
      }.setBarcodeData( data ) )
    }
  }
}
