package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.PotDataModel02
import com.bigsize.pot_terminal.databinding.ShelfShippingBinding
import com.bigsize.pot_terminal.viewmodel.ShelfShipping as VM_ShelfShipping

class ShelfShipping:DensoWaveBase() {
  private val binding01:ShelfShippingBinding by dataBinding()
  private val viewModel01:VM_ShelfShipping by viewModels()

  private val model01:AppUtility = AppUtility()
  private val model02:FileOperation = FileOperation()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.shelf_shipping )

    // ■ スキャナを設定します

    isPointMode = true

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
    if( inputCheck( "01" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 今回読んだ棚を記録します
    viewModel01.inputedLocation = scanShelf.substring( 3 )

    var ssb = viewModel01.inputedLocation.substring( 0, 1 );
    var ssh = viewModel01.inputedLocation.substring( 1, 2 );
    var ssf = viewModel01.inputedLocation.substring( 2, 4 );
    var sss = viewModel01.inputedLocation.substring( 4, 7 );
    var sst = viewModel01.inputedLocation.substring( 7, 8 );
    var sso = viewModel01.inputedLocation.substring( 8 );

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

    // 入力チェックを行います
    if( inputCheck( "02" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 今回読んだ棚を記録します
    viewModel01.inputedLocation = viewModel01.inputedLocation.substring( 0, 8 ) + scanBox.substring( 3 )

    var ssb = viewModel01.inputedLocation.substring( 0, 1 );
    var ssh = viewModel01.inputedLocation.substring( 1, 2 );
    var ssf = viewModel01.inputedLocation.substring( 2, 4 );
    var sss = viewModel01.inputedLocation.substring( 4, 7 );
    var sst = viewModel01.inputedLocation.substring( 7, 8 );
    var sso = viewModel01.inputedLocation.substring( 8 );

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

    // 入力チェックを行います
    if( inputCheck( "03" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // ■ 前回読んだ商品があればPOTデータを作成します

    if( BuildConfig.DEBUG ) Log.d( "APP-ShelfShipping", "前回の商品 = " + viewModel01.inputedItem )

    if( viewModel01.inputedItem != "" ) {
      execDataSave( "1" )
    }

    // 今回読んだ商品を記録します
    viewModel01.inputedItem = scanItem.substring( 3, 21 )

    var cd = model01.eightdigitsCd( viewModel01.inputedItem.substring( 0, 10 ) )
    var cn = viewModel01.inputedItem.substring( 11, 13 )
    var sz = viewModel01.inputedItem.substring( 14, 18 ).replace( " ", "" )

    // 今回読んだ商品を表示します
    viewModel01.txtItem.value = cd + "  " + cn + "  " + sz

    // 数量を入力可能とします
    binding01.edtAmt.isEnabled = true

    return true
  }

  /**
   * ENTERキーを押した時の処理を定義します
   *
   * @return 処理結果
   */
  fun execEnter():Boolean {
    // 入力チェックを行います
    if( inputCheck( "04" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // POTデータを作成します
    execDataSave( viewModel01.edtAmt.value.toString() )

    // 前回読んだ商品をクリアします
    viewModel01.inputedItem = ""

    // 商品と数量の表示をクリアします
    viewModel01.txtItem.value = ""
    viewModel01.edtAmt.value = ""

    // 数量を入力不可とします
    binding01.edtAmt.isEnabled = false

    return true
  }

  /**
   * 入力チェックを行います
   *
   * @param [execSubject] チェックモード 01 : 棚読み取り時 02 : 箱読み取り時 03 : 商品読み取り時 04 : ENT押下時
   * @return 入力チェック結果
   */
  private fun inputCheck( execSubject:String ):Boolean {
    var msgError01:String = ""
    var msgError02:String = ""
    var msgError03:String = ""

    binding01.layLocation.error = null
    binding01.layItem.error = null
    binding01.layAmt.error = null

    val edtAmt:String = viewModel01.edtAmt.value.toString()

    if( msgError03 == "" && execSubject == "04" && edtAmt.isEmpty() ) {
      msgError03 = getString( R.string.err_edt_amt01 )
    }

    if( msgError03 == "" && execSubject == "04" && model01.isNumber(edtAmt) == false ) {
      msgError03 = getString( R.string.err_edt_amt02 )
    }

    if( msgError03 == "" && execSubject == "04" && edtAmt.length > 3 ) {
      msgError03 = getString( R.string.err_edt_amt03 )
    }

    if( msgError01 == "" && ( execSubject == "02" || execSubject == "03" || execSubject == "04" ) && viewModel01.inputedLocation == "" ) {
      msgError01 = getString( R.string.err_shelf_shipping01 )
    }

    if( msgError02== "" && execSubject == "04" && viewModel01.inputedItem == "" ) {
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
  private fun execDataSave( amt:String ) {
    val devision = "2"
    val dataArray:MutableList<PotDataModel02> = mutableListOf()
    val dateHash:Map<String,String> = model01.returnPRecodeDate()

    dataArray.add( PotDataModel02(
      AppBase.deviceNO, dateHash["date"]!!, dateHash["time"]!!, AppBase.staffNO, devision,
      viewModel01.inputedItem.substring( 0, 10 ), viewModel01.inputedItem.substring( 11, 13 ), viewModel01.inputedItem.substring( 14, 18 ),
      viewModel01.inputedLocation, "00000000000", amt, false,
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
