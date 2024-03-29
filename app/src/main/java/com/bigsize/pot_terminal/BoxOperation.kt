package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.ScanCallback
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.databinding.BoxOperationBinding
import com.bigsize.pot_terminal.adapter.BoxOperation as AD_BoxOperation

class BoxOperation:DensoWaveBase(),DialogCallback {
  private val binding01:BoxOperationBinding by dataBinding()

  private lateinit var myFragment:ScanCallback

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.box_operation )

    // ■ スキャナを設定します

    isPointMode = true

    // ■ Wifi状態を確認します

    val rssi:Int = statusWifi.checkWifi()

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "電波強度 = " + rssi )

    if( rssi < AppBase.permitWifiLevel ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "04", "", getString( R.string.alt_wifi01 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
    }

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "箱操作"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ アダプタを初期化します

    binding01.pagView01.adapter = AD_BoxOperation( supportFragmentManager, lifecycle )

    TabLayoutMediator( binding01.layTab01, binding01.pagView01 ) { tab, position ->
      if( position == 0 ) tab.text = "箱確認"
      if( position == 1 ) tab.text = "箱付替"
    }.attach()

    // ◾️ スキャナイベントを補足します

    scanBox.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "箱データ = " + scanBox.value )

      myFragment = supportFragmentManager.findFragmentByTag( "f" + binding01.pagView01.currentItem ) as ScanCallback
      myFragment.readBox( scanBox.value )
    })

    scanItemM.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "商品データ = " + scanItemM.value )

      myFragment = supportFragmentManager.findFragmentByTag( "f" + binding01.pagView01.currentItem ) as ScanCallback
      myFragment.readItem( scanItemM.value )
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

    return super.dispatchKeyEvent( event )
  }
}
