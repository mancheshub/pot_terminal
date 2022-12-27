package com.bigsize.pot_terminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import android.widget.Button

class Menu: AppCompatActivity() {
  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.menu )

    // ■ ToolBarを設定します

    val toolbar = findViewById<Toolbar>( R.id.toolbar )
    toolbar.setTitle( "メニュー" )
    setSupportActionBar( toolbar )

    val btnMenu01 = findViewById<Button>( R.id.btn_menu01 )
    val btnMenu02 = findViewById<Button>( R.id.btn_menu02 )

    /*
    btnMenu01.setOnClickListener {
      val intent = Intent( this, ItemVerification::class.java )
      startActivity( intent )
    }

    btnMenu02.setOnClickListener {
      val intent = Intent( this, ShelfReceiving::class.java )
      startActivity( intent  )
    }
   */
  }
}