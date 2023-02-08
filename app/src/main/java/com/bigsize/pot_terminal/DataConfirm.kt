package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bigsize.pot_terminal.databinding.DataConfirmBinding
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.model.PotDataModel02
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.adapter.DataConfirm as AD_DataConfirm
import com.bigsize.pot_terminal.viewmodel.DataConfirm as VM_DataConfirm

class DataConfirm:DensoWaveBase(),View.OnClickListener,AdapterView.OnItemClickListener {
  private val binding01:DataConfirmBinding by dataBinding()
  private val viewModel01:VM_DataConfirm by viewModels()

  private lateinit var adapter01:AD_DataConfirm
  private lateinit var adapter02:ArrayAdapter<String>

  private var model01:FileOperation = FileOperation()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.data_confirm )

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

    txtTitle.text = "データ確認"
    txtStaffNO.text = AppBase.staffNO
    btnClose.setOnClickListener { finish() }

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ ListViewアダプタをセットします

    adapter01 = AD_DataConfirm( applicationContext, viewModel01.potDataArray )
    binding01.lstView01.adapter = adapter01

    // ■ AutoCompleteTextViewアダプタをセットします

    var menuItems:MutableList<String> = mutableListOf()

    for( _item in AppBase.potDivision ) { menuItems.add( _item.name ) }

    adapter02 = ArrayAdapter( applicationContext, R.layout.data_confirm_popup01, menuItems )
    binding01.txtPotfile.setAdapter( adapter02 )

    // ■ 変更を補足します

    adapter01.chkCount.observe(this, Observer<Int> {
      if( BuildConfig.DEBUG ) Log.d( "APP-DataConfirm", "チェックした数量 = " + adapter01.chkCount.value )
      binding01.txtView02.text = adapter01.chkCount.value.toString()
    })

    // ■ イベントを補足します

    binding01.exeButton01.setOnClickListener( this )
    binding01.txtPotfile.setOnItemClickListener( this )
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
   * アイテムが選択された時に呼ばれるリスナー定義です
   */
  override fun onItemClick( parent:AdapterView<*>, view:View, position:Int, id:Long ) {
    val item:String? = adapter02.getItem( position )

    claimSound( playSoundOK )

    if( BuildConfig.DEBUG ) Log.d( "APP-DataConfirm", "選択アイテム = " + item )

    // 選択したアイテムを保存します
    viewModel01.selectedItem = item!!

    // POTデータ区分を決定します

    var position:Int = AppBase.potDivision.indexOfFirst { it.name == item  }
    var division:String = AppBase.potDivision[position].division

    var fileArray:MutableList<PotDataModel02> = mutableListOf()

    try {
      fileArray = model01.readPotData( division )
    } catch( e:Exception ) {
      val intent = Intent( applicationContext, Failure::class.java )
      intent.putExtra( "MESSAGE", e.message )
      startActivity( intent )
    }

    adapter01.refreshItem( fileArray )
  }

  /**
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    val vButton = v as Button

    claimSound( playSoundOK )

    adapter01.updateItem()

    // アイテムが表示されていなければ以降何もしません
    if( viewModel01.potDataArray.count() == 0 ) return

    // POTデータ区分を決定します

    var position:Int = AppBase.potDivision.indexOfFirst { it.name == viewModel01.selectedItem }
    var division:String = AppBase.potDivision[position].division

    try {
      model01.savePotData( "OVERWRITE", division, adapter01.potDataArray )
    } catch( e:Exception ) {
      val intent = Intent( applicationContext, Failure::class.java )
      intent.putExtra( "MESSAGE", e.message )
      startActivity( intent )
    }
  }
}
