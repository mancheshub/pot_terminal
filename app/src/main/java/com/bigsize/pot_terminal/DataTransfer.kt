package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bigsize.pot_terminal.databinding.DataTransferBinding
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.viewmodel.DataTransfer as VM_DataTransfer
import com.bigsize.pot_terminal.adapter.DataTransfer as AD_DataTransfer
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.MessageDialog

class DataTransfer:DensoWaveBase(),View.OnClickListener {
  private val binding01:DataTransferBinding by dataBinding()
  private val viewModel01:VM_DataTransfer by viewModels()

  private lateinit var adapter01:AD_DataTransfer
  private lateinit var adapter02:ArrayAdapter<String>

  private var model01:FileOperation = FileOperation()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.data_transfer )

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    val actionBar:ActionBar? = supportActionBar

    actionBar?.setDisplayShowTitleEnabled( false )
    actionBar?.setDisplayShowHomeEnabled( false )
    actionBar?.setDisplayShowCustomEnabled( true )
    actionBar?.setCustomView( R.layout.actionbar_incontents );

    val txtTitle = findViewById<TextView>( R.id.txt_title )
    val txtStaffNO = findViewById<TextView>( R.id.txt_staffNO )
    val btnClose = findViewById<ImageView>( R.id.btn_close )

    txtTitle.text = "データ転送"
    txtStaffNO.text = AppBase.staffNO
    btnClose.setOnClickListener { finish() }

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

      if( viewModel01.apiStatus.value != "" && viewModel01.apiStatus.value != "成功" && viewModel01.apiStatus.value != "失敗" ) { binding01.prgText.text = viewModel01.apiStatus.value }

      if( viewModel01.apiStatus.value == "成功" ) {
        binding01.prgText.text = "転送完了"

        viewModel01.initPOTData()
        adapter01.refreshItem( viewModel01.potFileArray )

        val args = Bundle()
        args.putString( "title", "転送完了" )
        args.putString( "message", "すべてのPOTデータの転送が完了しました。" )

        val dialog = MessageDialog()
        dialog.setArguments( args )
        dialog.show( supportFragmentManager, "simple" )
      }

      if( viewModel01.apiStatus.value == "失敗" ) {
        val intent = Intent( applicationContext, Failure::class.java )
        intent.putExtra( "MESSAGE", "POTデータ受け皿のサーバの調子がよくありません。" )
        startActivity( intent )
      }
    })

    // ■ イベントを補足します

    binding01.exeButton01.setOnClickListener( this )
  }

  override fun onDestroy() {
    super.onDestroy()

    binding01.lstView01.adapter = null
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F02 ) finish()

    return super.dispatchKeyEvent( event )
  }

  /**
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    val vButton = v as Button

    claimSound( playSoundOK )

    // POTデータをアップロードします
    viewModel01.uploadPOTData( AppBase.deviceNO )
  }
}
