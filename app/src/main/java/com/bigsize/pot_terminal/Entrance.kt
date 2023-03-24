package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.bigsize.pot_terminal.databinding.EntranceBinding
import com.bigsize.pot_terminal.model.DeviceNODialog
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.viewmodel.Entrance
import com.wada811.databinding.dataBinding

class Entrance:DensoWaveBase(),DialogCallback {
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

    supportActionBar?.title = "利用申告"
    supportActionBar?.setDisplayHomeAsUpEnabled( false )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ イベントを補足します
  }

  /**
   * ActionBarメニューを実装します
   *
   * @param [menu] メニューレイアウト
   * @return
   */
  override fun onCreateOptionsMenu( menu:Menu ):Boolean {
    menuInflater.inflate( R.menu.actionbar_entrance, menu )

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
        finish()
      }
      R.id.iconItem01 -> {
        val dialog = DeviceNODialog( model02.readDeviceNO() )
        dialog.show( supportFragmentManager, "simple" )
      }
      R.id.iconItem02 -> {
        val dialog:MessageDialog = MessageDialog( "01", "", "再起動しますがよろしいですか？", "はい", "いいえ" )
        dialog.show( supportFragmentManager, "simple" )
      }
      else -> {}
    }

    return true
  }

  /**
   * ダイアログで実行する処理を実装します
   */
  override fun fromMessageDialog( callbackType:String ) {
    val intent = Intent()

    intent.setClassName(
      "com.densowave.powermanagerservice",
      "com.densowave.powermanagerservice.PowerManagerService"
    )

    intent.action = "com.densowave.powermanagerservice.action.REBOOT"

    startService( intent )
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

      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

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
      val intent = Intent( applicationContext, Lineup::class.java )
      startActivity( intent )
    }

    return super.dispatchKeyEvent( event )
  }
}
