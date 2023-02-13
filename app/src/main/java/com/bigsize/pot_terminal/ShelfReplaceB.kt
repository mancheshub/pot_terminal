package com.bigsize.pot_terminal

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.bigsize.pot_terminal.databinding.ShelfReplaceBBinding
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.FileOperation
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.viewmodel.ShelfReplaceB as VM_ShelfReplaceB

class ShelfReplaceB:DensoWaveBase(),View.OnClickListener {
  private val binding01:ShelfReplaceBBinding by dataBinding()
  private val viewModel01:VM_ShelfReplaceB by viewModels()

  private val model01:AppUtility = AppUtility()
  private val model02:FileOperation = FileOperation()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.shelf_replace_b )

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "入物移動"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ イベントを補足します

    binding01.exeButton01.setOnClickListener( this )
    binding01.exeButton02.setOnClickListener( this )
    binding01.exeButton03.setOnClickListener( this )
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    if( event.keyCode != KEY_ENT ) return super.dispatchKeyEvent( event )

    return super.dispatchKeyEvent( event );
  }

  /**
   * ボタンがされた時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    val vButton = v as Button

    if( BuildConfig.DEBUG ) Log.d( "APP-ShelfReplaceB", "ボタン名 = " + vButton.text )

    var ssb:String = ""
    var ssh:String = ""
    var ssf:String = ""
    var sss:String = ""
    var sst:String = ""
    var sso:String = ""
    var cd:String = ""
    var cn:String = ""
    var sz:String = ""

    // ■ 入力チェックを行います


    // ■ ボタンごとの処理を行います

    // 出場読ボタン

    if( vButton.id == R.id.exe_button01 ) {
      var tmpString:String = "M-L91032560000"

      // 出棚記録用データを作成します
      viewModel01.memLocation01 = tmpString.substring( 3 )

      ssb = viewModel01.memLocation01.substring( 0, 1 );
      ssh = viewModel01.memLocation01.substring( 1, 2 );
      ssf = viewModel01.memLocation01.substring( 2, 4 );
      sss = viewModel01.memLocation01.substring( 4, 7 );
      sst = viewModel01.memLocation01.substring( 7, 8 );
      sso = viewModel01.memLocation01.substring( 8 );

      // 出棚表示用データを作成します
      viewModel01.txtLocation01.value = ssb + ssh + ssf + "-" + sss + "-" + sst + "-" + sso
    }

    // 箱読ボタン

    if( vButton.id == R.id.exe_button02 ) {
      var tmpString:String = "M-C020"

      // 記録用データを作成します
      viewModel01.memBox = tmpString.substring( 3 )

      // 表示用データを作成します
      viewModel01.txtBox.value = viewModel01.memBox
    }

    // 入場読ボタン

    if( vButton.id == R.id.exe_button03 ) {
      var tmpString:String = "M-L91050510000"

      // 入棚記録用データを作成します
      viewModel01.memLocation02 = tmpString.substring( 3 )

      ssb = viewModel01.memLocation02.substring( 0, 1 );
      ssh = viewModel01.memLocation02.substring( 1, 2 );
      ssf = viewModel01.memLocation02.substring( 2, 4 );
      sss = viewModel01.memLocation02.substring( 4, 7 );
      sst = viewModel01.memLocation02.substring( 7, 8 );
      sso = viewModel01.memLocation02.substring( 8 );

      // 入棚表示用データを作成します
      viewModel01.txtLocation02.value = ssb + ssh + ssf + "-" + sss + "-" + sst + "-" + sso
    }
  }
}
