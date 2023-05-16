package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.databinding.BoxReceivingBinding
import com.bigsize.pot_terminal.viewmodel.BoxReceiving as VM_BoxReceiving

class BoxReceiving:DensoWaveBase(),View.OnClickListener,DialogCallback {
  private val binding01:BoxReceivingBinding by dataBinding()
  private val viewModel01:VM_BoxReceiving by viewModels()

  private val model01:AppUtility = AppUtility()

  private var dialogFIN:MessageDialog? = null
  private var dialogERR:MessageDialog? = null

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.box_receiving )

    // ■ スキャナを設定します

    isPointMode = true

    // ■ Wifi状態を確認します

    val rssi:Int = statusWifi.checkWifi()

    if( BuildConfig.DEBUG ) Log.d( "APP-DataTransfer", "電波強度 = " + rssi )

    if( rssi < AppBase.permitWifiLevel ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "04", "", getString( R.string.alt_wifi01 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
    }

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "商品仕分"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ 変更を補足します

    viewModel01.apiCondition.observe( this, Observer<String> {
      it ?: return@Observer

      var regex:Regex? = null

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

      // プログレスバーを消します - 警告終了

      if( apiCondition == "AL" ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        claimSound( playSoundNG )
        claimVibration( AppBase.vibrationNG )

        // 商品が仕分データになければ商品と箱ラベルをクリアします
        viewModel01.inputedCd = ""
        viewModel01.inputedCn = ""
        viewModel01.inputedSz = ""
        viewModel01.selectedBoxno = ""

        // 連続仕分回数と箱ラベル背景色と帯の色をクリアします
        viewModel01.cntRead.value = "0"
        viewModel01.bkgBoxno.value = "M"
        viewModel01.bkgBand.value = ""

        dialogERR = MessageDialog( "00", "警告", getString( R.string.err_box_receiving01 ), "OK", "" )
        dialogERR?.show( supportFragmentManager, "simple" )
      }

      // プログレスバーを消します - 正常終了

      regex = Regex( "FN" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        // 前回決定した箱ラベルと同じ箱に仕分ける場合は連続仕分回数を更新します
        if( viewModel01.historyArray.get(0).boxno == viewModel01.selectedBoxno ) {
          viewModel01.cntRead.value = ( ( viewModel01.cntRead.value as String ).toInt() + 1 ).toString()
        } else {
          viewModel01.cntRead.value = "0"
        }

        // 連続仕分回数によって帯の色を設定します

        val num:Int = ( viewModel01.cntRead.value as String ).toInt() % 2
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "連続仕分回数 2で割った余り = " + ( viewModel01.cntRead.value as String ) + " " + num )

        if( num == 0 ) viewModel01.bkgBand.value = "LEFT"
        if( num != 0 ) viewModel01.bkgBand.value = "RIGHT"

        // 今回読んだ商品と決定した箱ラベルを履歴に保管します
        viewModel01.updateHistory()

        // 終了パターンによってバイブレーションを変更します

        if( apiCondition == "FN01" ) {
          claimSound( playSoundOK )
          claimVibration( AppBase.vibrationOK )
        }

        if( apiCondition == "FN02" ) {
          claimSound( playSoundFN )
          claimVibration( AppBase.vibrationFN )

          dialogFIN = MessageDialog( "00", "完了", getString( R.string.msg_box_receiving01 ), "OK", "" )
          dialogFIN?.show( supportFragmentManager, "simple" )

          // 商品と箱ラベルをクリアします
          viewModel01.inputedCd = ""
          viewModel01.inputedCn = ""
          viewModel01.inputedSz = ""
          viewModel01.selectedBoxno = ""
        }
      }
    })

    scanItemM.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "商品データ = " + scanItemM.value )

      readItem( scanItemM.value )
    })

    // ■ イベントを補足します

    binding01.btnHistory01.setOnClickListener( this )
    binding01.btnHistory02.setOnClickListener( this )
    binding01.btnHistory03.setOnClickListener( this )
  }

  /**
   * ダイアログで実行する処理を実装します
   */
  override fun fromMessageDialog( callbackType:String ) {
    // Wifi電波レベルが低下した場合
    if( callbackType == "04" ) startActivityForResult( Intent( Settings.Panel.ACTION_WIFI ), 0 )
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
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    // 今回読んだ商品の表示をクリアします
    viewModel01.txtCd.value = ""
    viewModel01.txtCn.value = ""
    viewModel01.txtSz.value = ""

    // 箱ラベルの表示をクリアします
    viewModel01.txtBoxno.value = ""

    // 連続仕分回数と箱ラベル背景色と帯の色をクリアします
    viewModel01.cntRead.value = "0"
    viewModel01.bkgBoxno.value = "M"
    viewModel01.bkgBand.value = ""

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "01回前履歴 = " + viewModel01.historyArray.get(0).cd + " " + viewModel01.historyArray.get(0).cn + " " + viewModel01.historyArray.get(0).sz + " " + viewModel01.historyArray.get(0).boxno )
    if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "02回前履歴 = " + viewModel01.historyArray.get(1).cd + " " + viewModel01.historyArray.get(1).cn + " " + viewModel01.historyArray.get(1).sz + " " + viewModel01.historyArray.get(1).boxno )
    if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "03回前履歴 = " + viewModel01.historyArray.get(2).cd + " " + viewModel01.historyArray.get(2).cn + " " + viewModel01.historyArray.get(2).sz + " " + viewModel01.historyArray.get(2).boxno )

    when( v.id ) {
      R.id.btn_history01 -> {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "履歴01" )

        if( viewModel01.historyArray.get(0).cd == "" ) return

        // 今回読んだ商品を表示します
        viewModel01.txtCd.value = model01.eightdigitsCd( viewModel01.historyArray.get(0).cd )
        viewModel01.txtCn.value = viewModel01.historyArray.get(0).cn
        viewModel01.txtSz.value = viewModel01.historyArray.get(0).sz

        // 箱ラベルを表示します
        viewModel01.txtBoxno.value = viewModel01.historyArray.get(0).boxno
      }
      R.id.btn_history02 -> {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "履歴02" )

        if( viewModel01.historyArray.get(1).cd == "" ) return

        // 今回読んだ商品を表示します
        viewModel01.txtCd.value = model01.eightdigitsCd( viewModel01.historyArray.get(1).cd )
        viewModel01.txtCn.value = viewModel01.historyArray.get(1).cn
        viewModel01.txtSz.value = viewModel01.historyArray.get(1).sz

        // 箱ラベルを表示します
        viewModel01.txtBoxno.value = viewModel01.historyArray.get(1).boxno
      }
      R.id.btn_history03 -> {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "履歴03" )

        if( viewModel01.historyArray.get(2).cd == "" ) return

        // 今回読んだ商品を表示します
        viewModel01.txtCd.value = model01.eightdigitsCd( viewModel01.historyArray.get(2).cd )
        viewModel01.txtCn.value = viewModel01.historyArray.get(2).cn
        viewModel01.txtSz.value = viewModel01.historyArray.get(2).sz

        // 箱ラベルを表示します
        viewModel01.txtBoxno.value = viewModel01.historyArray.get(2).boxno
      }
    }

    // 箱ラベル背景色を決定します
    if( ( viewModel01.txtBoxno.value as String ).substring( 0, 1 ) == "A" ) viewModel01.bkgBoxno.value = "A"
    if( ( viewModel01.txtBoxno.value as String ).substring( 0, 1 ) == "B" ) viewModel01.bkgBoxno.value = "B"
    if( ( viewModel01.txtBoxno.value as String ).substring( 0, 1 ) == "C" ) viewModel01.bkgBoxno.value = "C"
    if( ( viewModel01.txtBoxno.value as String ).substring( 0, 1 ) == "D" ) viewModel01.bkgBoxno.value = "D"
    if( ( viewModel01.txtBoxno.value as String ).substring( 0, 1 ) == "E" ) viewModel01.bkgBoxno.value = "E"
  }

  /**
   * 商品を読んだ時の処理を定義します
   *
   * @param [scanItem] 読み取ったQRデータ
   * @return 処理結果
   */
  fun readItem( scanItem:String? ):Boolean {
    if( scanItem == null ) return false

    lateinit var potData:PotDataModel01
    var position:Int = 0

    // ダイアログが表示されていれば閉じます
    dialogFIN?.dismiss()
    dialogERR?.dismiss()

    // 今回読んだ商品を表示します
    viewModel01.txtCd.value = model01.eightdigitsCd( scanItem.substring( 3, 13 ) )
    viewModel01.txtCn.value = scanItem.substring( 14, 16 )
    viewModel01.txtSz.value = scanItem.substring( 17, 21 ).replace( " ", "" )

    // 帯の色をクリアします
    viewModel01.bkgBand.value = ""

    // 箱ラベルをクリアします
    viewModel01.txtBoxno.value = ""

    // 今回読んだ商品を記録します
    viewModel01.inputedCd = scanItem.substring( 3, 13 )
    viewModel01.inputedCn = scanItem.substring( 14, 16 )
    viewModel01.inputedSz = scanItem.substring( 17, 21 )

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxReceiving", "品番 色番 サイズ = " + viewModel01.inputedCd + " " + viewModel01.inputedCn + " " + viewModel01.inputedSz )

    // 箱ラベルを決定します
    viewModel01.pickBoxNO()

    return true
  }
}
