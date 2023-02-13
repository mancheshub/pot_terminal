package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bigsize.pot_terminal.databinding.DataTransferBinding
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.MessageDialog
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.adapter.DataTransfer as AD_DataTransfer
import com.bigsize.pot_terminal.viewmodel.DataTransfer as VM_DataTransfer


class DataTransfer:DensoWaveBase(),View.OnClickListener,DialogCallback {
  private val binding01:DataTransferBinding by dataBinding()
  private val viewModel01:VM_DataTransfer by viewModels()

  private lateinit var adapter01:AD_DataTransfer
  private lateinit var adapter02:ArrayAdapter<String>

  private var model01:FileOperation = FileOperation()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.data_transfer )

    // ■ Wifi状態を確認します

    val rssi:Int = statusWifi.checkWifi()

    if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "電波強度 = " + rssi )

    if( rssi < -25 ) {
      val dialog:MessageDialog = MessageDialog( "02", "", "Wifiの電波が弱くなっています。Wifiを再設定してください。", "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
    }

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "データ転送"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ ListViewアダプタをセットします

    adapter01 = AD_DataTransfer( applicationContext, viewModel01.potFileArray )
    binding01.lstView01.adapter = adapter01

    // ■ 変更を補足します

    adapter01.chkCount.observe(this, Observer<Int> {
      if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "チェックした数量 = " + adapter01.chkCount.value )
      binding01.txtView02.text = "%,d".format( adapter01.chkCount.value!!.toInt() )
    })

    viewModel01.apiStatus.observe(this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "APIステータス = " + viewModel01.apiStatus.value )

      if( viewModel01.apiStatus.value == "成功" ) {
        viewModel01.initPOTData()
        adapter01.refreshItem( viewModel01.potFileArray )

        val dialog:MessageDialog = MessageDialog( "01", "転送完了", "POTデータの転送が完了しました。", "OK", "" )
        dialog.show( supportFragmentManager, "simple" )

        claimVibration( AppBase.vibrationFN )
      }

      if( viewModel01.apiStatus.value == "失敗" ) {
        val intent = Intent( applicationContext, Failure::class.java )
        intent.putExtra( "MESSAGE", "POTデータ受け皿のサーバの調子がよくありません。" )
        startActivity( intent )
      }
    })

    // ■ イベントを補足します

    binding01.exeButton01.setOnClickListener( this )

    binding01.lstView01.setOnItemClickListener { adapterView:AdapterView<*>?, view:View?, position:Int, id:Long ->
      val check = view!!.findViewById<CheckBox>( R.id.check )
      check.isChecked = ! check.isChecked
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    binding01.lstView01.adapter = null
  }

  /**
   * ダイアログで実行する処理を実装します
   */
  override fun fromMessageDialog01() {}
  override fun fromMessageDialog02() {
    val intent = Intent( Settings.Panel.ACTION_WIFI )
    startActivityForResult( intent, 0 )
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    return super.dispatchKeyEvent( event )
  }

  /**
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    val vButton = v as Button

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // POTデータをアップロードします
    viewModel01.uploadPOTData( AppBase.deviceNO )
  }
}
