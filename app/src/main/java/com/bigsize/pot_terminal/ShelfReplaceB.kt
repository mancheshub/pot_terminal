package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import com.bigsize.pot_terminal.databinding.ShelfReplaceBBinding
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.PotDataModel02
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.viewmodel.ShelfReplaceB as VM_ShelfReplaceB

class ShelfReplaceB:DensoWaveBase() {
  private val binding01:ShelfReplaceBBinding by dataBinding()
  private val viewModel01:VM_ShelfReplaceB by viewModels()

  private val model01:AppUtility = AppUtility()
  private val model02:FileOperation = FileOperation()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.shelf_replace_b )

    // ■ スキャナを設定します

    isPointMode = true

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "入物移動"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ 変更を補足します

    scanShelf.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ShelfReplaceS", "棚データ = " + scanShelf.value )

      readShelf( scanShelf.value )
    })

    scanBox.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ShelfReplaceS", "箱データ = " + scanBox.value )

      readBox( scanBox.value )
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

    // すでに出棚と入棚が読まれていた場合はクリアします
    if( viewModel01.memLocation01 != "" && viewModel01.memLocation02 != "" ) {
      viewModel01.memLocation01 = ""
      viewModel01.memBox = ""
      viewModel01.memLocation02 = ""
      viewModel01.txtLocation01.value = ""
      viewModel01.txtBox.value = ""
      viewModel01.txtLocation02.value = ""
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
    if( inputCheck( "02" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 入物記録用データを作成します
    viewModel01.memBox = scanBox.substring( 3 )

    // 出棚表示用データを作成します
    viewModel01.txtBox.value = viewModel01.memBox

    return true
  }

  /**
   * ENTERキーを押した時の処理を定義します
   *
   * @return 処理結果
   */
  fun execEnter():Boolean {
    // 入力チェックを行います
    if( inputCheck( "03" ) == false ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // POTデータを作成します
    execDataSave()

    // クリアします
    viewModel01.memLocation01 = ""
    viewModel01.memBox = ""
    viewModel01.memLocation02 = ""
    viewModel01.txtLocation01.value = ""
    viewModel01.txtBox.value = ""
    viewModel01.txtLocation02.value = ""

    return true
  }

  /**
   * 入力チェックを行います
   *
   * @param [execSubject] チェックモード 01 : 棚読み取り時 02 : 箱読み取り時 03 : ENT押下時
   * @return 入力チェック結果
   */
  private fun inputCheck( execSubject:String ):Boolean {
    var msgError01:String = ""
    var msgError02:String = ""
    var msgError03:String = ""

    binding01.layLocation01.error = null
    binding01.layBox.error = null
    binding01.layLocation02.error = null

    if( msgError02 == "" && execSubject == "02" && viewModel01.memLocation01 == "" ) {
      msgError01 = getString( R.string.err_shelf_replace_b02 )
    }

    if( msgError02 == "" && execSubject == "01" && viewModel01.memLocation01 != "" && viewModel01.memBox == "" ) {
      msgError02 = getString( R.string.err_shelf_replace_b01 )
    }

    if( msgError01 == "" && execSubject == "03" && viewModel01.memLocation01 == "" ) {
      msgError01 = getString( R.string.err_shelf_replace_b02 )
    }

    if( msgError02 == "" && execSubject == "03" && viewModel01.memBox == "" ) {
      msgError02 = getString( R.string.err_shelf_replace_b01 )
    }

    if( msgError03 == "" && execSubject == "03" && viewModel01.memLocation02 == "" ) {
      msgError03 = getString( R.string.err_shelf_replace_b03 )
    }

    if( msgError01 != "" || msgError02 != "" || msgError03 != "" ) {
      if( msgError01 != "" ) { binding01.layLocation01.error = msgError01 }
      if( msgError02 != "" ) { binding01.layBox.error = msgError02 }
      if( msgError03 != "" ) { binding01.layLocation02.error = msgError03 }

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
    val devision = "4"
    val dataArray:MutableList<PotDataModel02> = mutableListOf()
    val dateHash:Map<String,String> = model01.returnPRecodeDate()

    // 箱を含む出棚を作成します

    var location01:String = viewModel01.memLocation01.substring( 0, 8 ) + viewModel01.memBox.substring( 0 )
    var location02:String = viewModel01.memLocation02.substring( 0, 8 ) + viewModel01.memBox.substring( 0 )

    dataArray.add( PotDataModel02(
      AppBase.deviceNO, dateHash["date"]!!, dateHash["time"]!!, AppBase.staffNO, devision,
      "0000000000", "00", "    ",
      location01, location02, "000", false,
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
