package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.bigsize.pot_terminal.databinding.EntranceBinding
import com.bigsize.pot_terminal.model.*
import com.bigsize.pot_terminal.viewmodel.Entrance
import com.wada811.databinding.dataBinding

class Entrance:DensoWaveBase() {
  private val binding01:EntranceBinding by dataBinding()
  private val viewModel01:Entrance by viewModels()

  private val model01:AppUtility = AppUtility()
  private val model02:FileOperation = FileOperation()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.entrance )

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

   // ■ ActionBarを設定します

    val actionBar:ActionBar? = supportActionBar

    actionBar?.setDisplayShowTitleEnabled( false )
    actionBar?.setDisplayShowHomeEnabled( false )
    actionBar?.setDisplayShowCustomEnabled( true )
    actionBar?.setCustomView( R.layout.actionbar_entrance );

    val txtTitle = findViewById<TextView>(R.id.txt_title)
    val btnSettings = findViewById<ImageView>(R.id.btn_settings)

    txtTitle.text = "利用申告"
    btnSettings.setOnClickListener {
      val dialog = DeviceNODialog( model02.readDeviceNO() )
      dialog.show( supportFragmentManager, "simple" )
    }

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ イベントを補足します
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )

    if( event.keyCode != KEY_ENT ) return super.dispatchKeyEvent( event )

    val txtNumber:String = viewModel01.txtNumber.value.toString()

    // ■ 入力チェックを行います

    var msgError:String = ""

    if( msgError == "" && txtNumber.isEmpty() ) {
      msgError = getString( R.string.err_txt_number01 )
    }

    if( msgError == "" && model01.isNumber(txtNumber) == false ) {
      msgError = getString( R.string.err_txt_number02 )
    }

    if( msgError == "" && txtNumber.length > 3 ) {
      msgError = getString( R.string.err_txt_number03 )
    }

    if( msgError != "" ) {
      binding01.layInput.error = msgError
      return super.dispatchKeyEvent( event )
    }

    // ■ スタッフ番号をグローバル変数にセットします

    AppBase.staffNO = viewModel01.txtNumber.value.toString().padStart( 3, '0' )

    if( BuildConfig.DEBUG ) Log.d( "APP-Entrance", "スタッフ番号 = " + AppBase.staffNO )

    // ■ 端末番号をグローバル変数にセットします

    AppBase.deviceNO = model02.readDeviceNO()

    if( AppBase.deviceNO == "" ) {
      val intent = Intent( applicationContext, Failure::class.java )
      intent.putExtra( "MESSAGE", "端末番号がセットアップされていません。" )
      startActivity( intent )
    } else {
      val intent = Intent( applicationContext, Menu::class.java )
      startActivity( intent )
    }

    return super.dispatchKeyEvent( event )
  }
}
