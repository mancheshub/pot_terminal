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
import com.bigsize.pot_terminal.model.*
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.adapter.DataConfirm as AD_DataConfirm
import com.bigsize.pot_terminal.viewmodel.DataConfirm as VM_DataConfirm

class DataConfirm:DensoWaveBase(),View.OnClickListener,AdapterView.OnItemClickListener,DialogCallback {
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

    supportActionBar?.title = "データ確認"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    adapter01 = AD_DataConfirm( applicationContext, viewModel01.potDataArray )
    binding01.lstView01.adapter = adapter01

    // ファイル名を抽出します
    var menuItems:MutableList<String> = mutableListOf()
    for( _item in AppBase.potDivision ) {
      menuItems.add( _item.name + " ( " + model01.countPotData( _item.division ).toString() + " 行 )" )
    }

    adapter02 = ArrayAdapter( applicationContext, R.layout.data_confirm_popup01, menuItems )
    binding01.txtPotfile.setAdapter( adapter02 )

    // ■ 変更を補足します

    adapter01.chkCount.observe( this, Observer<Int> {
      if( BuildConfig.DEBUG ) Log.d( "APP-DataConfirm", "チェックした数量 = " + adapter01.chkCount.value )
      viewModel01.cntCheck.value = adapter01.chkCount.value.toString()
    })

    // ■ イベントを補足します

    binding01.exeButton01.setOnClickListener( this )
    binding01.txtPotfile.setOnItemClickListener( this )

    binding01.lstView01.setOnItemClickListener { adapterView:AdapterView<*>?, view:View?, position:Int, id:Long ->
      val check = view!!.findViewById<CheckBox>( R.id.check )
      check.isChecked = ! check.isChecked
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    binding01.lstView01.adapter = null
  }

  /**
   * ダイアログで実行する処理を実装します
   */
  override fun fromMessageDialog( callbackType:String ) {
    // 削除する場合

    if( callbackType == "01" ) {
      if( viewModel01.potDataArray.size.toString() == "0" || ( viewModel01.potDataArray.indexOfFirst { it.isChecked == true } ) == -1 ) {
        claimSound( playSoundNG )
        claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( "00", "エラー", getString( R.string.err_data_confirm01 ), "OK", "" )
        dialog.show( supportFragmentManager, "simple" )

        return
      }

      viewModel01.potDataArray.removeIf { it.isChecked == true }

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntCheck.value = "0"
      viewModel01.cntTotal.value = viewModel01.potDataArray.size.toString()

      adapter01.refreshItem( viewModel01.potDataArray )

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

      claimSound( playSoundFN )
      claimVibration( AppBase.vibrationFN )

      val dialog:MessageDialog = MessageDialog( "00", "削除完了", getString( R.string.msg_data_confirm02 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
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
   * アイテムが選択された時に呼ばれるリスナー定義です
   */
  override fun onItemClick( parent:AdapterView<*>, view:View, position:Int, id:Long ) {
    val item:String? = adapter02.getItem( position )

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    // 選択アイテムからPOTファイル名のみを抽出します

    val match = Regex( "(.+) \\(.+\\)" ).find( item!! )

    var itemName:String? = ""
    match?.groups?.forEach { itemName = it?.value }

    if( BuildConfig.DEBUG ) Log.d( "APP-DataConfirm", "選択アイテム = " + itemName )

    // 選択したアイテムを保存します
    viewModel01.selectedItem = itemName!!

    // POTデータ区分を決定します
    var position:Int = AppBase.potDivision.indexOfFirst { it.name == itemName!! }
    var division:String = AppBase.potDivision[position].division

    try {
      viewModel01.potDataArray = model01.readPotData( division )
    } catch( e:Exception ) {
      val intent = Intent( applicationContext, Failure::class.java )
      intent.putExtra( "MESSAGE", e.message )
      startActivity( intent )
    }

    // 全データ数とPOTで読んだデータ数を更新します
    viewModel01.cntCheck.value = "0"
    viewModel01.cntTotal.value = viewModel01.potDataArray.size.toString()

    adapter01.refreshItem( viewModel01.potDataArray )
  }

  /**
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    val dialog:MessageDialog = MessageDialog( "01", "削除確認", getString( R.string.msg_data_confirm01 ), "はい", "いいえ" )
    dialog.show( supportFragmentManager, "simple" )
  }
}
