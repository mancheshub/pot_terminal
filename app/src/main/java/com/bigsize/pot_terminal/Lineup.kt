package com.bigsize.pot_terminal

import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar

class Lineup:DensoWaveBase() {
  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.lineup )

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "メニュー"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ イベントを補足します

    val btnMenu01 = findViewById<Button>( R.id.btn_menu01 )
    val btnMenu02 = findViewById<Button>( R.id.btn_menu02 )
    val btnMenu03 = findViewById<Button>( R.id.btn_menu03 )
    val btnMenu04 = findViewById<Button>( R.id.btn_menu04 )
    val btnMenu05 = findViewById<Button>( R.id.btn_menu05 )
    val btnMenu06 = findViewById<Button>( R.id.btn_menu06 )
    val btnMenu07 = findViewById<Button>( R.id.btn_menu07 )
    val btnMenu08 = findViewById<Button>( R.id.btn_menu08 )
    val btnMenu09 = findViewById<Button>( R.id.btn_menu09 )

    btnMenu01.setOnClickListener { startActivity( Intent( applicationContext, ItemVerification::class.java ) ) }
    btnMenu02.setOnClickListener { startActivity( Intent( applicationContext, ShelfReceiving::class.java ) ) }
    btnMenu03.setOnClickListener { startActivity( Intent( applicationContext, ShelfShipping::class.java ) ) }
    btnMenu04.setOnClickListener { startActivity( Intent( applicationContext, ShelfReplaceS::class.java ) ) }
    btnMenu05.setOnClickListener { startActivity( Intent( applicationContext, ShelfReplaceB::class.java ) ) }
    btnMenu06.setOnClickListener { startActivity( Intent( applicationContext, DataConfirm::class.java ) ) }
    btnMenu07.setOnClickListener { startActivity( Intent( applicationContext, DataTransfer::class.java ) ) }
    btnMenu08.setOnClickListener { startActivity( Intent( applicationContext, Inventory::class.java ) ) }
    btnMenu09.setOnClickListener { startActivity( Intent( applicationContext, ItemInspection::class.java ) ) }
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    if( event.keyCode != KEY_000 && event.keyCode != KEY_001 && event.keyCode != KEY_002 && event.keyCode != KEY_003 &&
        event.keyCode != KEY_004 && event.keyCode != KEY_005 && event.keyCode != KEY_006 && event.keyCode != KEY_007 &&
        event.keyCode != KEY_008 && event.keyCode != KEY_009 ) return super.dispatchKeyEvent( event )

    if( event.keyCode == KEY_001 ) startActivity( Intent( applicationContext, ItemVerification::class.java ) )
    if( event.keyCode == KEY_002 ) startActivity( Intent( applicationContext, ShelfReceiving::class.java ) )
    if( event.keyCode == KEY_003 ) startActivity( Intent( applicationContext, ShelfShipping::class.java ) )
    if( event.keyCode == KEY_004 ) startActivity( Intent( applicationContext, ShelfReplaceS::class.java ) )
    if( event.keyCode == KEY_005 ) startActivity( Intent( applicationContext, ShelfReplaceB::class.java ) )
    if( event.keyCode == KEY_006 ) startActivity( Intent( applicationContext, DataConfirm::class.java ) )
    if( event.keyCode == KEY_007 ) startActivity( Intent( applicationContext, DataTransfer::class.java ) )
    if( event.keyCode == KEY_008 ) startActivity( Intent( applicationContext, Inventory::class.java ) )
    if( event.keyCode == KEY_009 ) startActivity( Intent( applicationContext, ItemInspection::class.java ) )

    return super.dispatchKeyEvent( event );
  }
}
