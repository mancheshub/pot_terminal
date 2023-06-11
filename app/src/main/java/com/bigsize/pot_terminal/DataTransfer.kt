package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.databinding.DataTransferBinding
import com.bigsize.pot_terminal.viewmodel.DataTransfer as VM_DataTransfer
import com.bigsize.pot_terminal.adapter.DataTransfer as AD_DataTransfer

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

    if( rssi < AppBase.permitWifiLevel ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "02", "", getString( R.string.alt_wifi01 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
    }

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "データ転送"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    adapter01 = AD_DataTransfer( applicationContext, viewModel01.potFileArray )
    binding01.lstView01.adapter = adapter01

    // ■ 変更を補足します

    adapter01.chkCount.observe( this, Observer<Int> {
      if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "チェックした数量 = " + adapter01.chkCount.value )
      viewModel01.cntCheck.value = adapter01.chkCount.value.toString()
    })

    viewModel01.apiCondition.observe( this, Observer<String> {
      it ?: return@Observer

      // observeの処理中に"viewModel01.apiCondition.value"の値が変更になると困るのでここで一旦記録します
      val apiCondition:String = viewModel01.apiCondition.value as String

      // プログレスバーを表示します

      if( apiCondition == "ST" ) {
        binding01.exeButton01.isEnabled = false
        binding01.prgView01.visibility = android.widget.ProgressBar.VISIBLE
      }

      // プログレスバーを消します - 異常終了

      if( apiCondition == "ER" ) {
        binding01.exeButton01.isEnabled = true

        val intent = Intent( applicationContext, Failure::class.java )
        intent.putExtra( "MESSAGE", "POTデータ受け皿のサーバの調子がよくありません。" )
        startActivity( intent )
      }

      // プログレスバーを消します - 正常終了

      if( viewModel01.apiCondition.value == "FN" ) {
        binding01.exeButton01.isEnabled = true
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        viewModel01.initPOTData()

        // POTで読んだデータ数を更新します
        viewModel01.cntCheck.value = "0"

        adapter01.refreshItem( viewModel01.potFileArray )

        claimSound( playSoundFN )
        claimVibration( AppBase.vibrationFN )

        val dialog:MessageDialog = MessageDialog( "00", "転送完了", getString( R.string.msg_data_transfer02 ), "OK", "" )
        dialog.show( supportFragmentManager, "simple" )
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
  override fun fromMessageDialog( callbackType:String ) {
    // 転送する場合

    if( callbackType == "01" ) {
      if( viewModel01.potFileArray.size.toString() == "0" || ( viewModel01.potFileArray.indexOfFirst { it.isChecked == true } ) == -1 ) {
        claimSound( playSoundNG )
        claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( "00", "エラー", getString( R.string.err_data_transfer01 ), "OK", "" )
        dialog.show( supportFragmentManager, "simple" )

        return
      }

      // POTデータをアップロードします
      viewModel01.uploadPOTData( AppBase.deviceNO )
    }

    // Wifi電波レベルが低下した場合
    if( callbackType == "02" ) startActivityForResult( Intent( Settings.Panel.ACTION_WIFI ), 0 )
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    return super.dispatchKeyEvent( event )
  }

  /**
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    val dialog:MessageDialog = MessageDialog( "01", "転送確認", getString( R.string.msg_data_transfer01 ), "はい", "いいえ" )
    dialog.show( supportFragmentManager, "simple" )
  }
}
