package com.bigsize.pot_terminal

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bigsize.pot_terminal.databinding.SortShippingBinding
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.HashItem
import com.bigsize.pot_terminal.model.MessageDialog
import com.wada811.databinding.dataBinding
import java.nio.charset.StandardCharsets
import com.bigsize.pot_terminal.adapter.SortShipping as AD_SortShipping
import com.bigsize.pot_terminal.viewmodel.SortShipping as VM_SortShipping

class SortShipping:DensoWaveBase(),View.OnClickListener,AdapterView.OnItemClickListener,DialogCallback {
  private val binding01:SortShippingBinding by dataBinding()
  private val viewModel01:VM_SortShipping by viewModels()

  private lateinit var adapter01:AD_SortShipping

  private var receiver:SBReceiver? = null

  // デバイス検索状況
  // 検索状態 → SEARCH 待機状態 → STANDBY
  private val _searchCondition:String = "STANDBY"

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.sort_shipping )

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "仕分出荷"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ ListViewアダプタをセットします

    adapter01 = AD_SortShipping( applicationContext, viewModel01.itemDataArray )
    binding01.lstView01.adapter = adapter01

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ 変更を補足します

    viewModel01.socketCondition.observe( this, Observer<String> {
      if( ( viewModel01.socketCondition.value as String ) != "" ) {

        if( ( viewModel01.socketCondition.value as String ) == "CONNECTED" ) {
          claimSound( playSoundOK )
          claimVibration( AppBase.vibrationOK )

          val dialog:MessageDialog = MessageDialog( "00", "成功", getString( R.string.msg_bluetooth01 ), "OK", "" )
          dialog.show( supportFragmentManager, "simple" )
        }

        if( ( viewModel01.socketCondition.value as String ) == "CONNERROR" ) {
          claimSound( playSoundNG )
          claimVibration( AppBase.vibrationNG )

          val dialog:MessageDialog = MessageDialog( "00", "失敗", getString( R.string.err_bluetooth03 ), "OK", "" )
          dialog.show( supportFragmentManager, "simple" )
        }

        if( ( viewModel01.socketCondition.value as String ) == "DISCONNECT" ) {
          claimSound( playSoundOK )
          claimVibration( AppBase.vibrationOK )

          val dialog:MessageDialog = MessageDialog( "00", "成功", getString( R.string.msg_bluetooth02 ), "OK", "" )
          dialog.show( supportFragmentManager, "simple" )
        }
      }
    })

    scanItem.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-SortShipping", "商品データ = " + scanItem.value )

      claimSound( playSoundOK )
      claimVibration( AppBase.vibrationOK )

      // デバイスに書き込みします
      viewModel01.write( scanItem.value.toString().toByteArray(StandardCharsets.UTF_8) )
    })

    // ■ イベントを補足します

    binding01.lstView01.setOnItemClickListener( this )
    binding01.btnSearch.setOnClickListener( this )
    binding01.btnStart.setOnClickListener( this )
    binding01.btnClose.setOnClickListener( this )
  }

  override fun onDestroy() {
    super.onDestroy()

    if( receiver != null ) { unregisterReceiver( receiver ); receiver = null; }
  }

  /**
   * ダイアログで実行する処理を実装します
   */
  override fun fromMessageDialog( callbackType:String ) {}

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    return super.dispatchKeyEvent( event );
  }

  /**
   * アイテムが選択された時に呼ばれるリスナー定義です
   */
  override fun onItemClick( parent:AdapterView<*>, view:View, position:Int, id:Long ) {
    val item:HashItem = adapter01.getItem( position )

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    if( BuildConfig.DEBUG ) Log.d( "APP-SortShipping", "選択アイテム = " + item )

    ( binding01.txtName as TextView ).text = item.id
    ( binding01.txtAddress as TextView ).text = item.item
  }

  /**
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    // 検索中の場合は検索に関わる全ての状態を解除します

    if( bluetooth.isDiscovering() ) {
      bluetooth.cancelDiscovery();

      binding01.btnSearch.isEnabled = true
      binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

      if( receiver != null ) { unregisterReceiver( receiver ); receiver = null; }
    }

    when( v.id ) {
      R.id.btn_search -> { // 検索ボタン
        if( BuildConfig.DEBUG ) Log.d( "APP-SortShipping", "検索ボタン押下" )

        // ■ 接続したことのあるデバイスの情報を取得します

        val bondedDevices:Collection<BluetoothDevice> = bluetooth.getBondedDevices()

        for( device in bondedDevices ) viewModel01.itemDataArray.add( HashItem( device.name, device.address ) )
        adapter01.refreshItem( viewModel01.itemDataArray )

        // ■ 接続したことのないデバイスの情報を取得します

        // 検索されたデバイスからのブロードキャストを受け取る定義を作成します

        val filter = IntentFilter()

        filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_STARTED )
        filter.addAction( BluetoothDevice.ACTION_FOUND )
        filter.addAction( BluetoothDevice.ACTION_NAME_CHANGED )
        filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_FINISHED )

        receiver = SBReceiver()
        registerReceiver( receiver!!, filter )

        // デバイスを検索します
        bluetooth.startDiscovery()
      }
      R.id.btn_start -> { // 接続ボタン
        if( BuildConfig.DEBUG ) Log.d( "APP-SortShipping", "接続ボタン押下 = " + ( binding01.txtName as TextView ).text )

        if( inputCheck() == true ) {
          // デバイスとペアリングします
          viewModel01.pairing( bluetooth )

          // デバイスにソケット接続します
          viewModel01.connect()
        }
      }

      R.id.btn_close -> { // 切断ボタン
        if( BuildConfig.DEBUG ) Log.d( "APP-SortShipping", "切断ボタン押下" )

        if( inputCheck() == true ) {
          // デバイスからソケット切断します
          viewModel01.disconnect()

          ( binding01.txtName as TextView ).text = ""
          ( binding01.txtAddress as TextView ).text = ""
        }
      }
    }
  }

  /**
   * 入力チェックを行います
   *
   * @return 入力チェック結果
   */
  private fun inputCheck():Boolean {
    var msgError01:String = ""
    var msgError02:String = ""

    binding01.layName.error = null
    binding01.layAddress.error = null

    if( ( binding01.txtName as TextView ).text.toString() == "" ) msgError01 = "isERROR"
    if( ( binding01.txtAddress as TextView ).text.toString() == "" ) msgError02 = "isERROR"

    if( msgError01 != "" || msgError02 != "" ) {
      if( msgError01 != "" ) { binding01.layName.error = msgError01 }
      if( msgError02 != "" ) { binding01.layAddress.error = msgError02 }

      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      return false
    }

    return true
  }

  /**
   * bluetoothのブロードキャストレシーバークラス
   */
  inner class SBReceiver():BroadcastReceiver() {
    override fun onReceive( context:Context?, intent:Intent? ) {
      when( intent?.getAction() ) {
        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
          if( BuildConfig.DEBUG ) Log.d( "APP-SortShipping", "デバイス検出開始" )

          claimSound( playSoundOK )
          claimVibration( AppBase.vibrationOK )

          // 検索中は検索ボタンをグレーアウトしてプログレスバーを表示します
          binding01.btnSearch.isEnabled = false
          binding01.prgView01.visibility = android.widget.ProgressBar.VISIBLE
        }
        BluetoothDevice.ACTION_FOUND, BluetoothDevice.ACTION_NAME_CHANGED -> {
          if( BuildConfig.DEBUG ) Log.d( "APP-SortShipping", "デバイス検出・デバイス名の判明" )

          val btDevice = intent?.getParcelableExtra<Parcelable>( BluetoothDevice.EXTRA_DEVICE ) as BluetoothDevice

          if( btDevice.bondState != BluetoothDevice.BOND_BONDED && btDevice.name != null  ) {
            if( BuildConfig.DEBUG ) Log.d( "APP-SortShipping", "デバイス名 デバイスアドレス = " + btDevice.name + " " + btDevice.address )

            viewModel01.itemDataArray.add( HashItem( btDevice.name, btDevice.address ) )
            adapter01.refreshItem( viewModel01.itemDataArray )
          }
        }
        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
          if( BuildConfig.DEBUG ) Log.d( "APP-SortShipping", "デバイス検出終了 " )

          claimSound( playSoundOK )
          claimVibration( AppBase.vibrationOK )

          // 検索が完了したら検索ボタンのグレーアウトを解除してプログレスバーを消します
          binding01.btnSearch.isEnabled = true
          binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

          // レシーバーを解除します
          if( receiver != null ) { unregisterReceiver( receiver ); receiver = null; }
        }
        else -> {}
      }
    }
  }

}
