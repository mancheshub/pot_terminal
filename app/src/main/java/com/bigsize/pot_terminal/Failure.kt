package com.bigsize.pot_terminal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

class Failure:DensoWaveBase(),View.OnClickListener {
  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.failure )

    // ■ 全てのActivityを終了します

    AppBase.killApp()

    // ■ ActionBarを設定します

    val actionBar:ActionBar? = supportActionBar

    actionBar?.setDisplayShowTitleEnabled( false )
    actionBar?.setDisplayShowHomeEnabled( false )
    actionBar?.setDisplayShowCustomEnabled( true )
    actionBar?.setCustomView( R.layout.actionbar_incontents );

    val txtTitle = findViewById<TextView>( R.id.txt_title )
    val txtStaffNO = findViewById<TextView>( R.id.txt_staffNO )
    val btnClose = findViewById<ImageView>( R.id.btn_close )

    txtTitle.text = "エラー"
    txtStaffNO.text = AppBase.staffNO
    btnClose.setOnClickListener { finish() }

    val txtMessage = findViewById<TextView>( R.id.txt_message )
    val btnLogin = findViewById<Button>( R.id.btn_entry )

    // ■ レイアウトにデータをセットします

    txtMessage.text = intent.getStringExtra( "MESSAGE" )

    // ■ イベントを補足します

    btnLogin.setOnClickListener( this )
  }

  /**
   * ボタンがされた時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    val vButton = v as Button

    finish()
  }
}
