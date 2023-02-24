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

    supportActionBar?.title = "エラー"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

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
    finish()
  }
}
