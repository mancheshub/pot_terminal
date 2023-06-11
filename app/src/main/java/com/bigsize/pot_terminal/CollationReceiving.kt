package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.Observer
import com.bigsize.pot_terminal.adapter.DataConfirm
import com.google.android.material.tabs.TabLayoutMediator
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.ScanCallback
import com.bigsize.pot_terminal.model.KeyCallback
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.databinding.CollationReceivingBinding
import com.bigsize.pot_terminal.adapter.CollationReceiving as AD_CollationReceiving

class CollationReceiving:DensoWaveBase(),DialogCallback {
  private val binding01:CollationReceivingBinding by dataBinding()

  private lateinit var myFragment01:ScanCallback
  private lateinit var myFragment02:KeyCallback

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.collation_receiving )

    // ■ スキャナを設定します

    isPointMode = true

    // ■ Wifi状態を確認します

    val rssi:Int = statusWifi.checkWifi()

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "電波強度 = " + rssi )

    if( rssi < AppBase.permitWifiLevel ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "04", "", getString( R.string.alt_wifi01 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
    }

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "一覧入庫"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ アダプタを初期化します

    binding01.pagView01.adapter = AD_CollationReceiving( supportFragmentManager, lifecycle )

    TabLayoutMediator( binding01.layTab01, binding01.pagView01 ) { tab, position ->
      if( position == 0 ) tab.text = "返品入庫"
      if( position == 1 ) tab.text = "Fｷｬﾝｾﾙ入庫"
      if( position == 2 ) tab.text = "Mｷｬﾝｾﾙ入庫"
    }.attach()

    // ◾️ スキャナイベントを補足します

    scanShelf.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "棚データ = " + scanShelf.value )

      myFragment01 = supportFragmentManager.findFragmentByTag( "f" + binding01.pagView01.currentItem ) as ScanCallback
      myFragment01.readShelf( scanShelf.value )
    })

    scanBox.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "箱データ = " + scanBox.value )

      myFragment01 = supportFragmentManager.findFragmentByTag( "f" + binding01.pagView01.currentItem ) as ScanCallback
      myFragment01.readBox( scanBox.value )
    })

    scanItemM.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "商品データ = " + scanItemM.value )

      myFragment01 = supportFragmentManager.findFragmentByTag( "f" + binding01.pagView01.currentItem ) as ScanCallback
      myFragment01.readItem( scanItemM.value )
    })
  }

  override fun onDestroy() {
    super.onDestroy()

    binding01.pagView01.adapter = null
  }

  /**
   * ダイアログで実行する処理を実装します
   */
  override fun fromMessageDialog( callbackType:String ) {
    // Wifi電波レベルが低下した場合
    if( callbackType == "04" ) startActivityForResult( Intent( Settings.Panel.ACTION_WIFI ), 0 )
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    if( event.keyCode == KEY_ENT ) {
      try {
        myFragment02 = supportFragmentManager.findFragmentByTag( "f" + binding01.pagView01.currentItem ) as KeyCallback
        val retFlag:Boolean = myFragment02.enterEvent()
      } catch( e:ClassCastException ) {}
    }

    return super.dispatchKeyEvent( event )
  }
}
