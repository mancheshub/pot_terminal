package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import com.bigsize.pot_terminal.databinding.LineupBinding
import com.wada811.databinding.dataBinding

class Lineup:DensoWaveBase(),View.OnClickListener,View.OnFocusChangeListener {
  private val binding01:LineupBinding by dataBinding()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.lineup )

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "メニュー"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ イベントを補足します

    binding01.btnMenu01.setOnClickListener( this )
    binding01.btnMenu02.setOnClickListener( this )
    binding01.btnMenu03.setOnClickListener( this )
    binding01.btnMenu04.setOnClickListener( this )
    binding01.btnMenu05.setOnClickListener( this )
    binding01.btnMenu06.setOnClickListener( this )
    binding01.btnMenu07.setOnClickListener( this )
    binding01.btnMenu08.setOnClickListener( this )
    binding01.btnMenu09.setOnClickListener( this )
    binding01.btnMenu10.setOnClickListener( this )
    binding01.btnMenu11.setOnClickListener( this )
    binding01.btnMenu12.setOnClickListener( this )

    binding01.btnMenu01.setOnFocusChangeListener( this )
    binding01.btnMenu02.setOnFocusChangeListener( this )
    binding01.btnMenu03.setOnFocusChangeListener( this )
    binding01.btnMenu04.setOnFocusChangeListener( this )
    binding01.btnMenu05.setOnFocusChangeListener( this )
    binding01.btnMenu06.setOnFocusChangeListener( this )
    binding01.btnMenu07.setOnFocusChangeListener( this )
    binding01.btnMenu08.setOnFocusChangeListener( this )
    binding01.btnMenu09.setOnFocusChangeListener( this )
    binding01.btnMenu10.setOnFocusChangeListener( this )
    binding01.btnMenu11.setOnFocusChangeListener( this )
    binding01.btnMenu12.setOnFocusChangeListener( this )
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    if( event.keyCode == KEY_F02 || event.keyCode == KEY_UP || event.keyCode == KEY_DOWN || event.keyCode == KEY_LEFT || event.keyCode == KEY_RIGHT ) {
      claimSound( playSoundOK )
    }

    return super.dispatchKeyEvent( event );
  }

  /**
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    when( v.id ) {
      R.id.btn_menu01 -> { startActivity( Intent( applicationContext, ItemVerification::class.java ) ) }
      R.id.btn_menu02 -> { startActivity( Intent( applicationContext, ShelfReceiving::class.java ) ) }
      R.id.btn_menu03 -> { startActivity( Intent( applicationContext, ShelfShipping::class.java ) ) }
      R.id.btn_menu04 -> { startActivity( Intent( applicationContext, ShelfReplaceS::class.java ) ) }
      R.id.btn_menu05 -> { startActivity( Intent( applicationContext, ShelfReplaceB::class.java ) ) }
      R.id.btn_menu06 -> { startActivity( Intent( applicationContext, DataConfirm::class.java ) ) }
      R.id.btn_menu07 -> { startActivity( Intent( applicationContext, DataTransfer::class.java ) ) }
      R.id.btn_menu08 -> { startActivity( Intent( applicationContext, Inventory::class.java ) ) }
      R.id.btn_menu09 -> { startActivity( Intent( applicationContext, ItemInspection::class.java ) ) }
      R.id.btn_menu10 -> { startActivity( Intent( applicationContext, SortShipping::class.java ) ) }
      R.id.btn_menu11 -> { startActivity( Intent( applicationContext, ExamLocation::class.java ) ) }
      R.id.btn_menu12 -> { startActivity( Intent( applicationContext, HetVerification::class.java ) ) }
    }
  }

  /**
   * ボタンがフォーカスされた時に呼ばれるリスナー定義です
   */
  override fun onFocusChange( v:View, hasFocus:Boolean ) {
    if( hasFocus == false ) { return }
    supportActionBar?.title = (v as Button).text
  }
}
