package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.Observer
import com.bigsize.pot_terminal.databinding.ShelfReplaceSBinding
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.PotDataModel02
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.viewmodel.ShelfReplaceS as VM_ShelfReplaceS

class ShelfReplaceS:DensoWaveBase() {
  private val binding01:ShelfReplaceSBinding by dataBinding()
  private val viewModel01:VM_ShelfReplaceS by viewModels()

  private val model01:AppUtility = AppUtility()
  private val model02:FileOperation = FileOperation()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.shelf_replace_s )

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "商品移動"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ 変更を補足します

    scanShelf.observe(this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ShelfReplaceS", "棚データ = " + scanShelf.value )

      readShelf( scanShelf.value )
    })

    scanBox.observe(this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ShelfReplaceS", "箱データ = " + scanBox.value )

      readBox( scanBox.value )
    })

    scanItem.observe(this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ShelfReplaceS", "商品データ = " + scanItem.value )

      readItem( scanItem.value )
    })

    // ■ イベントを補足します
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    if( event.keyCode != KEY_ENT ) return super.dispatchKeyEvent( event )

    execEnter()

    return super.dispatchKeyEvent( event );
  }

  /**
   * 棚を読んだ時の処理を定義します
   *
   * @param [scanShelf] 読み取った棚QRデータ
   * @return 処理結果
   */
  fun readShelf( scanShelf:String? ):Boolean {
    if( scanShelf == null ) return false

    // 入力チェックを行います
    if( inputCheck( "shelf" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // すでに出棚と入棚が読まれていた場合はクリアします
    if( viewModel01.memLocation01 != "" && viewModel01.memLocation02 != "" ) {
      viewModel01.memLocation01 = ""
      viewModel01.memLocation02 = ""
      viewModel01.memItem = ""
      viewModel01.txtLocation01.value = ""
      viewModel01.txtLocation02.value = ""
      viewModel01.txtItem.value = ""
    }

    if( viewModel01.memLocation01 == "" && viewModel01.memLocation02 == "" ) {
      // 出棚記録用データを作成します
      viewModel01.memLocation01 = scanShelf.substring( 3 )

      var ssb = viewModel01.memLocation01.substring( 0, 1 );
      var ssh = viewModel01.memLocation01.substring( 1, 2 );
      var ssf = viewModel01.memLocation01.substring( 2, 4 );
      var sss = viewModel01.memLocation01.substring( 4, 7 );
      var sst = viewModel01.memLocation01.substring( 7, 8 );
      var sso = viewModel01.memLocation01.substring( 8 );

      // 出棚表示用データを作成します
      viewModel01.txtLocation01.value = ssb + ssh + ssf + "-" + sss + "-" + sst + "-" + sso

      return true
    }

    if( viewModel01.memLocation01 != "" && viewModel01.memLocation02 == "" ) {
      // 入棚記録用データを作成します
      viewModel01.memLocation02 = scanShelf.substring( 3 )

      var ssb = viewModel01.memLocation02.substring( 0, 1 );
      var ssh = viewModel01.memLocation02.substring( 1, 2 );
      var ssf = viewModel01.memLocation02.substring( 2, 4 );
      var sss = viewModel01.memLocation02.substring( 4, 7 );
      var sst = viewModel01.memLocation02.substring( 7, 8 );
      var sso = viewModel01.memLocation02.substring( 8 );

      // 入棚表示用データを作成します
      viewModel01.txtLocation02.value = ssb + ssh + ssf + "-" + sss + "-" + sst + "-" + sso

      return true
    }

    return true
  }

  /**
   * 箱を読んだ時の処理を定義します
   *
   * @param [scanBox] 読み取った箱QRデータ
   * @return 処理結果
   */
  fun readBox( scanBox:String? ):Boolean {
    if( scanBox == null ) return false

    // 入力チェックを行います
    if( inputCheck( "box" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    if( viewModel01.memLocation01 != "" && viewModel01.memLocation02 == "" ) {
      // 出棚記録用データを作成します
      viewModel01.memLocation01 = viewModel01.memLocation01.substring( 0, 8 ) + scanBox.substring( 3 )

      var ssb = viewModel01.memLocation01.substring( 0, 1 );
      var ssh = viewModel01.memLocation01.substring( 1, 2 );
      var ssf = viewModel01.memLocation01.substring( 2, 4 );
      var sss = viewModel01.memLocation01.substring( 4, 7 );
      var sst = viewModel01.memLocation01.substring( 7, 8 );
      var sso = viewModel01.memLocation01.substring( 8 );

      // 出棚表示用データを作成します
      viewModel01.txtLocation01.value = ssb + ssh + ssf + "-" + sss + "-" + sst + "-" + sso

      return true
    }

    if( viewModel01.memLocation01 != "" && viewModel01.memLocation02 != "" ) {
      // 入棚記録用データを作成します
      viewModel01.memLocation02 = viewModel01.memLocation02.substring( 0, 8 ) + scanBox.substring( 3 )

      var ssb = viewModel01.memLocation02.substring( 0, 1 );
      var ssh = viewModel01.memLocation02.substring( 1, 2 );
      var ssf = viewModel01.memLocation02.substring( 2, 4 );
      var sss = viewModel01.memLocation02.substring( 4, 7 );
      var sst = viewModel01.memLocation02.substring( 7, 8 );
      var sso = viewModel01.memLocation02.substring( 8 );

      // 入棚表示用データを作成します
      viewModel01.txtLocation02.value = ssb + ssh + ssf + "-" + sss + "-" + sst + "-" + sso

      return true
    }

    return true
  }

  /**
   * 商品を読んだ時の処理を定義します
   *
   * @param [scanItem] 読み取った箱QRデータ
   * @return 処理結果
   */
  fun readItem( scanItem:String? ):Boolean {
    if( scanItem == null ) return false

    // 入力チェックを行います
    if( inputCheck( "item" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 記録用データを作成します
    viewModel01.memItem = scanItem.substring( 3, 21 )

    var cd = model01.eightdigitsCd( viewModel01.memItem.substring( 0, 10 ) )
    var cn = viewModel01.memItem.substring( 11, 13 )
    var sz = viewModel01.memItem.substring( 14, 18 ).replace( " ", "" )

    // 表示用データを作成します
    viewModel01.txtItem.value = cd + "  " + cn + "  " + sz

    return true
  }

  /**
   * ENTERキーを押した時の処理を定義します
   *
   * @return 処理結果
   */
  fun execEnter():Boolean {
    binding01.layLocation01.error = null
    binding01.layLocation02.error = null
    binding01.layItem.error = null
    binding01.layAmt.error = null

    // 入力チェックを行います
    if( inputCheck( "amt" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // POTデータを作成します
    execDataSave()

    // クリアします
    viewModel01.memItem = ""
    viewModel01.txtItem.value = ""
    viewModel01.edtAmt.value = ""

    return true
  }

  /**
   * 入力チェックを行います
   *
   * @param [execSubject] 処理対象
   * @return 入力チェック結果
   */
  private fun inputCheck( execSubject:String ):Boolean {
    var msgError01:String = ""
    var msgError02:String = ""
    var msgError03:String = ""
    var msgError04:String = ""

    binding01.layLocation01.error = null
    binding01.layLocation02.error = null
    binding01.layItem.error = null
    binding01.layAmt.error = null

    val edtAmt:String = viewModel01.edtAmt.value.toString()

    if( msgError04 == "" && execSubject == "amt" && edtAmt.isEmpty() ) {
      msgError04 = getString( R.string.err_edt_amt01 )
    }

    if( msgError04 == "" && execSubject == "amt" && model01.isNumber(edtAmt) == false ) {
      msgError04 = getString( R.string.err_edt_amt02 )
    }

    if( msgError04 == "" && execSubject == "amt" && edtAmt.length > 3 ) {
      msgError04 = getString( R.string.err_edt_amt03 )
    }

    if( msgError01 == "" && ( execSubject == "item" || execSubject == "amt" ) && viewModel01.memLocation01 == "" ) {
      msgError01 = getString( R.string.err_shelf_replace_s01 )
    }

    if( msgError02 == "" && ( execSubject == "item" || execSubject == "amt" ) && viewModel01.memLocation02 == "" ) {
      msgError02 = getString( R.string.err_shelf_replace_s02 )
    }

    if( msgError03 == "" && execSubject == "amt" && viewModel01.memItem == "" ) {
      msgError03 = getString( R.string.err_shelf_replace_s03 )
    }

    if( msgError01 != "" || msgError02 != "" || msgError03 != "" || msgError04 != "" ) {
      if( msgError01 != "" ) { binding01.layLocation01.error = msgError01 }
      if( msgError02 != "" ) { binding01.layLocation02.error = msgError02 }
      if( msgError03 != "" ) { binding01.layItem.error = msgError03 }
      if( msgError04 != "" ) { binding01.layAmt.error = msgError04 }

      claimSound( playSoundNG )

      return false
    }

    return true
  }

  /**
   * POTデータを作成します
   */
  private fun execDataSave() {
    val devision = "3"
    val dataArray:MutableList<PotDataModel02> = mutableListOf()
    val dateHash:Map<String,String> = model01.returnPRecodeDate()

    dataArray.add( PotDataModel02(
      AppBase.deviceNO, dateHash["date"]!!, dateHash["time"]!!, AppBase.staffNO, devision,
      viewModel01.memItem.substring( 0, 10 ), viewModel01.memItem.substring( 11, 13 ), viewModel01.memItem.substring( 14, 18 ),
      viewModel01.memLocation01, viewModel01.memLocation02, viewModel01.edtAmt.value.toString(), false,
    ) )

    try {
      model02.savePotData( "APPEND", devision, dataArray )
    } catch( e:Exception ) {
      val intent = Intent( applicationContext, Failure::class.java )
      intent.putExtra( "MESSAGE", e.message )
      startActivity( intent )
    }
  }
}
