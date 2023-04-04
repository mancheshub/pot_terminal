package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bigsize.pot_terminal.databinding.ExamLocationBinding
import com.bigsize.pot_terminal.model.*
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.adapter.ExamLocation as AD_ExamLocation
import com.bigsize.pot_terminal.viewmodel.ExamLocation as VM_ExamLocation

class ExamLocation:DensoWaveBase(),View.OnClickListener,TextView.OnEditorActionListener,DialogCallback {
  private val binding01:ExamLocationBinding by dataBinding()
  private val viewModel01:VM_ExamLocation by viewModels()

  private lateinit var adapter01:AD_ExamLocation

  private val model01:AppUtility = AppUtility()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.exam_location )

    // ■ スキャナを設定します

    isPointMode = true

    // ■ Wifi状態を確認します

    val rssi:Int = statusWifi.checkWifi()

    if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "電波強度 = " + rssi )

    if( rssi < AppBase.permitWifiLevel ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "02", "", getString( R.string.alt_wifi01 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
    }

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "場所確認"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ ListViewアダプタをセットします

    adapter01 = AD_ExamLocation( applicationContext, ( viewModel01.locationList.value as MutableList<PotDataModel04> ) )
    binding01.lstView01.adapter = adapter01

    // ■ 変更を補足します

    viewModel01.apiCondition.observe( this, Observer<String> {
      if( ( viewModel01.apiCondition.value as String ) != "" ) {

      // observeの処理中に"viewModel01.apiCondition.value"の値が変更になると困るのでここで一旦記録します
      val apiCondition:String = viewModel01.apiCondition.value as String

      // プログレスバーを表示します

      if( apiCondition == "ST" ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.VISIBLE
      }

      // プログレスバーを消します - 異常終了

      if( apiCondition == "ER" ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        claimSound( playSoundNG )
        claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( "00", "通信エラー", getString( R.string.err_communication01 ), "OK", "" )
        dialog.show( supportFragmentManager, "simple" )
      }

      // プログレスバーを消します - 正常終了

      if( apiCondition == "FN" ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE
      }

      }
    })

    viewModel01.locationList.observe( this, Observer<MutableList<PotDataModel04>> {
      if( ( viewModel01.locationList.value as MutableList<PotDataModel04> ).size != 0 ) {

      if( BuildConfig.DEBUG ) Log.d( "APP-ExamLocation", "ロケーションデータ内容更新" )

      // 商品名を取得します
      viewModel01.txtItn.value = ( viewModel01.locationList.value as MutableList<PotDataModel04> )[0].itn

      // ListViewの内容を更新します
      adapter01.refreshItem( ( viewModel01.locationList.value as MutableList<PotDataModel04> ) )

      }
    })

    scanItemM.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ExamLocation", "商品データ = " + scanItemM.value )

      readItem( scanItemM.value )
    })

    // ■ イベントを補足します

    binding01.exeButton01.setOnClickListener( this )
    binding01.txtCd.setOnEditorActionListener( this )
  }

  override fun onDestroy() {
    super.onDestroy()

    binding01.lstView01.adapter = null
  }

  /**
   * ダイアログで実行する処理を実装します
   */
  override fun fromMessageDialog( callbackType:String ) {
    if( callbackType == "02" ) {
      val intent = Intent( Settings.Panel.ACTION_WIFI )
      startActivityForResult( intent, 0 )
    }
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    return super.dispatchKeyEvent( event )
  }

  /**
   * EditTextのキーイベントを捕捉します
   */
  override fun onEditorAction( v:TextView, actionId:Int, event:KeyEvent ):Boolean {
    if( event.keyCode != KeyEvent.KEYCODE_ENTER ) return false
    if( actionId == EditorInfo.IME_ACTION_DONE ) return true

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 今回読んだ商品を記録します
    viewModel01.memCd = viewModel01.txtCd.value.toString()
    viewModel01.memCn = ""
    viewModel01.memSz = ""

    // 品番・色番・サイズから商品のロケーションを取得します
    viewModel01.pickLocation( "NON" )

    return false
  }

  /**
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    viewModel01.txtCd.value = ""
    viewModel01.txtCn.value = ""
    viewModel01.txtSz.value = ""
    viewModel01.txtItn.value = ""

    // 商品のロケーションをクリアします
    viewModel01.pickLocation( "YES" )

    // ListViewの内容を更新します
    adapter01.refreshItem( ( viewModel01.locationList.value as MutableList<PotDataModel04> ) )
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

    // 今回読んだ商品を表示します
    viewModel01.txtCd.value = model01.eightdigitsCd( scanItem.substring( 3, 13 ) )
    viewModel01.txtCn.value = scanItem.substring( 14, 16 )
    viewModel01.txtSz.value = scanItem.substring( 17, 21 ).replace( " ", "" )

    // 今回読んだ商品を記録します
    viewModel01.memCd = scanItem.substring( 3, 13 )
    viewModel01.memCn = scanItem.substring( 14, 16 )
    viewModel01.memSz = scanItem.substring( 17, 21 ).replace( " ", "" )

    // 品番・色番・サイズから商品のロケーションを取得します
    viewModel01.pickLocation( "NON" )

    return true
  }
}
