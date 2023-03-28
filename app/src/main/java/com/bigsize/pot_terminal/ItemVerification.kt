package com.bigsize.pot_terminal

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.Observer
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.databinding.ItemVerificationBinding
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.viewmodel.ItemVerification as VM_ItemVerification
import com.bigsize.pot_terminal.adapter.ItemVerification as AD_ItemVerification

class ItemVerification:DensoWaveBase(),DialogCallback {
  private val binding01:ItemVerificationBinding by dataBinding()
  private val viewModel01:VM_ItemVerification by viewModels()

  private lateinit var adapter01:AD_ItemVerification

  private val model01:AppUtility = AppUtility()

  private var dialogFIN:MessageDialog? = null

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.item_verification )

    // ■ スキャナを設定します

    isPointMode = true

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "商品照合"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "デバイス番号 = " + AppBase.deviceNO + " " + AppBase.specialDeviceNO )

    // 上書きモードスイッチの初期値を設定します
    if( AppBase.deviceNO == AppBase.specialDeviceNO ) binding01.swhMode01.isChecked = true

    // ■ ListViewアダプタをセットします

    adapter01 = AD_ItemVerification( applicationContext, viewModel01.potDataArray )
    binding01.lstView01.adapter = adapter01

    // ■ 変更を補足します

    scanMultiItem.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "複数商品データ = " + scanMultiItem.value )

      readMultiItem( scanMultiItem.value )
    })

    scanItemM.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "商品データ = " + scanItemM.value )


      readItem( scanItemM.value )
    })

    // ■ イベントを補足します
  }

  override fun onDestroy() {
    super.onDestroy()

    binding01.lstView01.adapter = null
  }

  /**
   * ダイアログで実行する処理を実装します
   */
  override fun fromMessageDialog( callbackType:String ) {}

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F03 ) finish()

    return super.dispatchKeyEvent( event )
  }

  /**
   * 複数商品を読んだ時の処理を定義します
   *
   * @param [scanShelf] 読み取った複数商品QRデータ
   * @return 処理結果
   */
  fun readMultiItem( scanMultiItem:String? ):Boolean {
    if( scanMultiItem == null ) return false

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 上書きモードがONの場合は商品データを一旦クリアします

    if( binding01.swhMode01.isChecked == true ) {
      // 読込済な商品データをクリアします
      viewModel01.potDataArray = mutableListOf<PotDataModel01>()

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntRead.value = "0"
      viewModel01.cntTotal.value = "0"
    }

    // 特別動作する端末番号である場合は完了ダイアログを閉じます

    if( AppBase.deviceNO == AppBase.specialDeviceNO && dialogFIN != null ) {
      dialogFIN?.dismiss()
    }

    var ii:Int = 0
    var indexNO:Int = 0
    val tmpDataArray:MutableList<PotDataModel01> = mutableListOf()

    while( true ) {
      ii = ii + 1

      val lastChar:String = scanMultiItem.substring( 3 + indexNO, 3 + indexNO + 1 )

      if( lastChar == "/" || ii == 10000 ) break

      var cd:String = scanMultiItem.substring( 3 + indexNO, 13 + indexNO )
      var cn:String = scanMultiItem.substring( 13 + indexNO, 15 + indexNO )
      var sz:String = scanMultiItem.substring( 15 + indexNO, 19 + indexNO )
      var amt:String = scanMultiItem.substring( 19 + indexNO, 22 + indexNO )

      if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "品番 色番 サイズ 数量 = " + cd + " " + cn + " " + sz + " " + amt )

      tmpDataArray.add( PotDataModel01(model01.eightdigitsCd(cd),cn,sz.replace(" ",""),"0",amt.toInt().toString()) )

      indexNO = indexNO + 19;
    }

    // 読んだ商品データを読込済な商品データに統合します
    viewModel01.potDataArray.addAll( tmpDataArray )

    if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "件数 = " + viewModel01.potDataArray.size )

    // 全データ数を更新します
    viewModel01.cntTotal.value = ( viewModel01.potDataArray.sumBy { it.amt_p.toInt() } ).toString()

    // ListViewの内容を更新します
    adapter01.refreshItem( viewModel01.potDataArray )

    return true
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

    var cd:String = scanItem.substring( 3, 13 );
    var cn:String = scanItem.substring( 14, 16 );
    var sz:String = scanItem.substring( 17, 21 );

    if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "品番 色番 サイズ = " + cd + " " + cn + " " + sz )

    // 該当商品を検索します
    position = viewModel01.potDataArray.indexOfFirst { it.cd == model01.eightdigitsCd(cd) && it.cn == cn && it.sz == sz.replace(" ","") && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "00", "", getString( R.string.err_item_verification01 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
    } else {
      // 該当位置のSKUの数量を増やします
      potData = viewModel01.potDataArray[position]
      potData.amt_n = (potData.amt_n.toInt()+1).toString()

      // ViewModelを更新します
      viewModel01.potDataArray.set( position, potData )

      // ListViewの内容を更新します
      adapter01.refreshItem( viewModel01.potDataArray )

      // POTで読んだデータ数を更新します
      viewModel01.cntRead.value = ( viewModel01.potDataArray.sumBy { it.amt_n.toInt() } ).toString()

      // 終了したらその旨を表示します

      position = viewModel01.potDataArray.indexOfFirst { it.amt_n.toInt() < it.amt_p.toInt() }
      if( position == -1 ) {
        claimSound( playSoundFN )
        claimVibration( AppBase.vibrationFN )

        dialogFIN = MessageDialog( "00", "完了", getString( R.string.msg_item_verification01 ), "OK", "" )
        dialogFIN?.show( supportFragmentManager, "simple" )
      } else {
        claimSound( playSoundOK )
        claimVibration( AppBase.vibrationOK )
      }
    }

    return true
  }
}
