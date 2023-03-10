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
import com.bigsize.pot_terminal.databinding.ShelfShippingBinding
import com.bigsize.pot_terminal.model.*
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.viewmodel.ShelfShipping as VM_ShelfShipping

class ShelfShipping:DensoWaveBase() {
  private val binding01:ShelfShippingBinding by dataBinding()
  private val viewModel01:VM_ShelfShipping by viewModels()

  private val model01:AppUtility = AppUtility()
  private val model02:FileOperation = FileOperation()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.shelf_shipping )

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "商品出庫"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ 変更を補足します

    scanShelf.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ShelfShipping", "棚データ = " + scanShelf.value )

      readShelf( scanShelf.value )
    })

    scanBox.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ShelfReceiving", "箱データ = " + scanBox.value )

      readBox( scanBox.value )
    })

    scanItemM.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ShelfShipping", "商品データ = " + scanItemM.value )

      readItem( scanItemM.value )
    })

    // ■ イベントを補足します
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( AppBase.isDialogPrint == "YES" ) return true
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

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 今回読んだ棚を記録します
    viewModel01.setMemLocation( scanShelf.substring( 3 ) )

    var ssb = viewModel01.memLocation.substring( 0, 1 );
    var ssh = viewModel01.memLocation.substring( 1, 2 );
    var ssf = viewModel01.memLocation.substring( 2, 4 );
    var sss = viewModel01.memLocation.substring( 4, 7 );
    var sst = viewModel01.memLocation.substring( 7, 8 );
    var sso = viewModel01.memLocation.substring( 8 );

    // 今回読んだ棚を表示します
    viewModel01.txtLocation.value = ssb + ssh + ssf + "-" + sss + "-" + sst + "-" + sso

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

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 今回読んだ棚を記録します
    viewModel01.setMemLocation( viewModel01.memLocation.substring( 0, 8 ) + scanBox.substring( 3 ) )

    var ssb = viewModel01.memLocation.substring( 0, 1 );
    var ssh = viewModel01.memLocation.substring( 1, 2 );
    var ssf = viewModel01.memLocation.substring( 2, 4 );
    var sss = viewModel01.memLocation.substring( 4, 7 );
    var sst = viewModel01.memLocation.substring( 7, 8 );
    var sso = viewModel01.memLocation.substring( 8 );

    // 今回読んだ棚を表示します
    viewModel01.txtLocation.value = ssb + ssh + ssf + "-" + sss + "-" + sst + "-" + sso

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

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 今回読んだ商品を記録します
    viewModel01.setMemItem( scanItem.substring( 3, 21 ) )

    var cd = model01.eightdigitsCd( viewModel01.memItem.substring( 0, 10 ) )
    var cn = viewModel01.memItem.substring( 11, 13 )
    var sz = viewModel01.memItem.substring( 14, 18 ).replace( " ", "" )

    // 今回読んだ商品を表示します
    viewModel01.txtItem.value = cd + "  " + cn + "  " + sz

    return true
  }

  /**
   * ENTERキーを押した時の処理を定義します
   *
   * @return 処理結果
   */
  fun execEnter():Boolean {
    // 入力チェックを行います
    if( inputCheck() == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // POTデータを作成します
    execDataSave()

    // 前回読んだ商品をクリアします
    viewModel01.setMemItem( "" )

    // 商品と数量の表示をクリアします
    viewModel01.txtItem.value = ""
    viewModel01.edtAmt.value = ""

    return true
  }

  /**
   * 入力チェックを行います
   *
   * @return 入力チェック結果
   */
  private fun inputCheck():Boolean {
    var msgError01:String = ""
    var msgError02:String = ""
    var msgError03:String = ""

    binding01.layLocation.error = null
    binding01.layItem.error = null
    binding01.layAmt.error = null

    val edtAmt:String = viewModel01.edtAmt.value.toString()

    if( msgError03 == "" && edtAmt.isEmpty() ) {
      msgError03 = getString( R.string.err_edt_amt01 )
    }

    if( msgError03 == "" && model01.isNumber(edtAmt) == false ) {
      msgError03 = getString( R.string.err_edt_amt02 )
    }

    if( msgError03 == "" && edtAmt.length > 3 ) {
      msgError03 = getString( R.string.err_edt_amt03 )
    }

    if( msgError01 == "" && viewModel01.memLocation == "" ) {
      msgError01 = getString( R.string.err_shelf_shipping01 )
    }

    if( msgError02== "" && viewModel01.memItem == "" ) {
      msgError02 = getString( R.string.err_shelf_shipping02 )
    }

    if( msgError01 != "" || msgError02 != "" || msgError03 != "" ) {
      if( msgError01 != "" ) { binding01.layLocation.error = msgError01 }
      if( msgError02 != "" ) { binding01.layItem.error = msgError02 }
      if( msgError03 != "" ) { binding01.layAmt.error = msgError03 }

      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      return false
    }

    return true
  }

  /**
   * POTデータを作成します
   */
  private fun execDataSave() {
    val devision = "2"
    val dataArray:MutableList<PotDataModel02> = mutableListOf()
    val dateHash:Map<String,String> = model01.returnPRecodeDate()

    dataArray.add( PotDataModel02(
      AppBase.deviceNO, dateHash["date"]!!, dateHash["time"]!!, AppBase.staffNO, devision,
      viewModel01.memItem.substring( 0, 10 ), viewModel01.memItem.substring( 11, 13 ), viewModel01.memItem.substring( 14, 18 ),
      viewModel01.memLocation, "00000000000", viewModel01.edtAmt.value.toString(), false,
    ) )

    if( BuildConfig.DEBUG ) Log.d( "APP-ShelfShipping", "登録" )

    try {
      model02.savePotData( "APPEND", devision, dataArray )
    } catch( e:Exception ) {
      val intent = Intent( applicationContext, Failure::class.java )
      intent.putExtra( "MESSAGE", e.message )
      startActivity( intent )
    }
  }
}
