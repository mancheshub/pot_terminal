package com.bigsize.pot_terminal

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.databinding.ItemVerificationBinding
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.viewmodel.ItemVerification as VM_ItemVerification
import com.bigsize.pot_terminal.adapter.ItemVerification as AD_ItemVerification

class ItemVerification:DensoWaveBase(),View.OnClickListener {
  private val binding01:ItemVerificationBinding by dataBinding()
  private val viewModel01:VM_ItemVerification by viewModels()

  private lateinit var adapter01:AD_ItemVerification

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.item_verification )

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    val actionBar:ActionBar? = supportActionBar

    actionBar?.setDisplayShowTitleEnabled( false )
    actionBar?.setDisplayShowHomeEnabled( false )
    actionBar?.setDisplayShowCustomEnabled( true )
    actionBar?.setCustomView( R.layout.actionbar_incontents );

    val txtTitle = findViewById<TextView>( R.id.txt_title )
    val txtStaffNO = findViewById<TextView>( R.id.txt_staffNO )
    val btnClose = findViewById<ImageView>( R.id.btn_close )

    txtTitle.text = "商品照合"
    txtStaffNO.text = AppBase.staffNO
    btnClose.setOnClickListener { finish() }

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ ListViewアダプタをセットします

    adapter01 = AD_ItemVerification( applicationContext, viewModel01.potDataArray )
    binding01.lstView01.adapter = adapter01

    // ■ イベントを補足します

    binding01.exeButton01.setOnClickListener( this )
    binding01.exeButton02.setOnClickListener( this )
    binding01.exeButton03.setOnClickListener( this )
  }

  override fun onDestroy() {
    super.onDestroy()

    binding01.lstView01.adapter = null
  }

  /**
   * キーイベントを捕捉します
   */
  override fun dispatchKeyEvent( event:KeyEvent ):Boolean {
    if( event.action != KeyEvent.ACTION_UP ) return super.dispatchKeyEvent( event )
    if( event.keyCode == KEY_F02 ) finish()

    return super.dispatchKeyEvent( event )
  }

  /**
   * ボタンがされた時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    val vButton = v as Button

    if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "ボタン名 = " + vButton.text )

    lateinit var potData:PotDataModel01
    var position:Int = 0

    // ■ ボタンごとの処理を行います

    // 初読ボタン

    if( vButton.id == R.id.exe_button01 ) {
      // 読込済な商品データをクリアします
      viewModel01.potDataArray = mutableListOf<PotDataModel01>()

      // 照合済件数を更新します
      viewModel01.txtView01.value = "照合 0 点"

      // 全件数を更新します
      viewModel01.txtView02.value = "全 0 点"

      // ListViewの内容を更新します
      adapter01.refreshItem( viewModel01.potDataArray )
    }

    // 部読

    if( vButton.id == R.id.exe_button02 ) {
      // 該当商品を検索します
      position = viewModel01.potDataArray.indexOfFirst { it.cd == "1158-1235" && it.cn == "01" && it.sz == "4L" && it.amt_p.toInt() < it.amt.toInt() }

      if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "検索位置 = " + position.toString() )

      if( position == -1 ) {
        Toast.makeText(this,"該当の商品がありません。",Toast.LENGTH_SHORT).show()
      } else {
        // 該当位置のSKUの数量を増やします
        potData = viewModel01.potDataArray[position]
        potData.amt_p = (potData.amt_p.toInt()+1).toString()

        // ViewModelを更新します
        viewModel01.potDataArray.set( position, potData )

        // ListViewの内容を更新します
        adapter01.refreshItem( viewModel01.potDataArray )

        // 照合済件数を更新します
        viewModel01.txtView01.value = "照合 " + viewModel01.potDataArray.sumBy { it.amt_p.toInt() } + " 点"

        // 終了したらその旨を表示します
        position = viewModel01.potDataArray.indexOfFirst { it.amt_p.toInt() < it.amt.toInt() }
        if( position == -1 ) { Toast.makeText( this, "終了しました。", Toast.LENGTH_SHORT ).show() }
      }
    }

    // 追読

    if( vButton.id == R.id.exe_button03 ) {
      // 読んだ商品データ
      val tmpDataArray:List<PotDataModel01> = listOf(
        PotDataModel01("1158-1234","01","3L","0","10"),PotDataModel01("1158-1235","01","4L","0","10"),PotDataModel01("1158-1236","01","5L","0","10"),
      )

      // 読んだ商品データを読込済な商品データに統合します
      viewModel01.potDataArray.addAll(tmpDataArray)

      if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "件数 = " + viewModel01.potDataArray.size )

      // 全件数を更新します
      viewModel01.txtView02.value = "全 " + viewModel01.potDataArray.sumBy { it.amt.toInt() } + " 点"

      // ListViewの内容を更新します
      adapter01.refreshItem( viewModel01.potDataArray )
    }
  }
}
