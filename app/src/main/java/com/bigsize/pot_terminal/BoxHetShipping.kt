package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.wada811.databinding.dataBinding
import com.densowave.bhtsdk.barcode.BarcodeScannerSettings
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.model.HashItem
import com.bigsize.pot_terminal.databinding.BoxHetShippingBinding
import com.bigsize.pot_terminal.adapter.BoxHetShipping as AD_BoxHetShipping
import com.bigsize.pot_terminal.viewmodel.BoxHetShipping as VM_BoxHetShipping

class BoxHetShipping:DensoWaveBase(),AdapterView.OnItemClickListener,DialogCallback {
  private val binding01:BoxHetShippingBinding by dataBinding()
  private val viewModel01:VM_BoxHetShipping by viewModels()

  private lateinit var adapter01:AD_BoxHetShipping
  private lateinit var adapter02:ArrayAdapter<String>
  private lateinit var adapter03:ArrayAdapter<String>

  private val model01:AppUtility = AppUtility()

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.box_het_shipping )

    // ■ スキャナを設定します

    isPointMode = true

    // ■ Wifi状態を確認します

    val rssi:Int = statusWifi.checkWifi()

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "電波強度 = " + rssi )

    if( rssi < AppBase.permitWifiLevel ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "04", "", getString( R.string.alt_wifi01 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
    }

    // ■ Activityをリストに加えます

    AppBase.activitySet.add( this )

    // ■ ActionBarを設定します

    supportActionBar?.title = "商品箱出"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ ListViewアダプタをセットします

    adapter01 = AD_BoxHetShipping( applicationContext, ( viewModel01.itemList.value as MutableList<PotDataModel01> ) )
    binding01.lstView01.adapter = adapter01

    // ■ 伝発グループデータを取得します

    viewModel01.pickGroupList()

    // ■ 変更を補足します

    viewModel01.apiCondition.observe( this, Observer<String> {
      if( ( viewModel01.apiCondition.value as String ) != "" ) {

        var regex:Regex? = null

        // observeの処理中に"viewModel01.apiCondition.value"の値が変更になると困るのでここで一旦記録します
        val apiCondition:String = viewModel01.apiCondition.value as String

        // プログレスバーを表示します

        if( apiCondition == "ST" ) {
          binding01.prgView01.visibility = android.widget.ProgressBar.VISIBLE
        }

        // プログレスバーを消します - 警告終了

        regex = Regex( "AL" )
        if( regex.containsMatchIn( apiCondition ) == true ) {
          binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

          // 警告ダイアログを表示します

          var message:String = ""

          if( apiCondition == "AL01" ) { message = getString( R.string.err_box_shipping02 ); }
          if( apiCondition == "AL02" ) { message = getString( R.string.alt_box_shipping01 ); }
          if( apiCondition == "AL03" ) { message = getString( R.string.alt_box_shipping02 ); }

          // AL03の場合は完了後の処理だから店舗の棚出状況に変化があったので店舗データと商品データを再取得します

          if( apiCondition == "AL03" ) {
            viewModel01.pickShopList()
            viewModel01.pickItemList()
          }

          claimSound( playSoundNG )
          claimVibration( AppBase.vibrationNG )

          val dialog:MessageDialog = MessageDialog( "00", "警告", message, "OK", "" )
          dialog.show( supportFragmentManager, "simple" )
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

        regex = Regex( "FN9" )
        if( regex.containsMatchIn( apiCondition ) == true ) {
          binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE
        }

        regex = Regex( "FN0" )
        if( regex.containsMatchIn( apiCondition ) == true ) {
          binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

          // いずれの処理も店舗の棚出状況に変化があったので店舗データと商品データを再取得します
          viewModel01.pickShopList()
          viewModel01.pickItemList()

          // 完了ダイアログを表示します

          claimSound( playSoundFN )
          claimVibration( AppBase.vibrationFN )

          val dialog:MessageDialog = MessageDialog( "00", "完了", getString( R.string.msg_box_shipping01 ), "OK", "" )
          dialog.show( supportFragmentManager, "simple" )
        }

      }
    })

    viewModel01.groupList.observe( this, Observer<MutableList<HashItem>> {
      if( ( viewModel01.groupList.value as MutableList<HashItem> ).size != 0 ) {

        if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "伝発グループセレクトボックス内容更新" )

        // 伝発グループ名を抽出します
        var menuItems:MutableList<String> = mutableListOf()
        for( _item in viewModel01.groupList.value as MutableList<HashItem> ) { menuItems.add( _item.item ) }

        adapter02 = ArrayAdapter( applicationContext, R.layout.box_het_shipping_popup01, menuItems )
        binding01.txtGroup.setAdapter( adapter02 )

      }
    })

    viewModel01.shopList.observe( this, Observer<MutableList<HashItem>> {
      if( ( viewModel01.shopList.value as MutableList<HashItem> ).size != 0 ) {

        if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "店舗セレクトボックス内容更新" )

        // 店舗名を抽出します
        var menuItems:MutableList<String> = mutableListOf()
        for( _item in viewModel01.shopList.value as MutableList<HashItem> ) { menuItems.add( _item.item ) }

        adapter03 = ArrayAdapter( applicationContext, R.layout.box_het_shipping_popup02, menuItems )
        binding01.txtShop.setAdapter( adapter03 )

      }
    })

    viewModel01.itemList.observe( this, Observer<MutableList<PotDataModel01>> {
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "商品データ内容更新" )

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntRead.value = "0"
      viewModel01.cntTotal.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel01> ).sumBy { it.amt_p.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "全データ数 POTで読んだデータ数 = " + viewModel01.cntTotal.value + " " + viewModel01.cntRead.value  )

      // ListViewの内容を更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel01> ) )
    })

    scanItemM.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "商品データ = " + scanItemM.value )

      readItem( scanItemM.value )
    })

    // ■ イベントを補足します

    binding01.txtGroup.setOnItemClickListener( this )
    binding01.txtShop.setOnItemClickListener( this )
  }

  override fun onDestroy() {
    super.onDestroy()

    binding01.lstView01.adapter = null
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
   * アイテムが選択された時に呼ばれるリスナー定義です
   */
  override fun onItemClick( parent:AdapterView<*>, v:View, position:Int, id:Long ) {
    var item:String? = null

    // アイテムが選択されたタイミングでエラーは解消します
    binding01.layGroup.error = null
    binding01.layShop.error = null

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    when( v.id ) {
      R.id.itm_group -> { // 伝発グループ選択
        item = adapter02.getItem( position )

        // 伝発グループIDを決定します
        var position:Int = ( viewModel01.groupList.value as MutableList<HashItem> ).indexOfFirst { it.item == item }
        var groupID:String = ( viewModel01.groupList.value as MutableList<HashItem> )[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "選択アイテム - グループ = " + groupID + " " + item )

        // 選択した伝発グループのIDを記録します
        viewModel01.selectedGroupID = groupID

        // 伝発グループが選択されたら店舗データを取得します
        viewModel01.pickShopList()

        // 伝発グループを切り替えたら空白の店舗データが選択されたとします
        binding01.txtShop.setText( "", false )
        viewModel01.selectedShopID = ""

        // 伝発グループ変更により店舗データがクリアされたので商品データを再取得します
        viewModel01.pickItemList()
      }
      R.id.itm_shop -> { // 店舗選択
        item = adapter03.getItem( position )

        // 店舗IDを決定します
        var position:Int = ( viewModel01.shopList.value as MutableList<HashItem> ).indexOfFirst { it.item == item }
        var shopID:String = ( viewModel01.shopList.value as MutableList<HashItem> )[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "選択アイテム - 店舗 = " + shopID + " " + item )

        // 選択した店舗のIDを記録します
        viewModel01.selectedShopID = shopID

        // 店舗が選択されたら商品データを取得します
        viewModel01.pickItemList()
      }
      else -> {}
    }
  }

  /**
   * 商品を読んだ時の処理を定義します
   *
   * @param [scanItem] 読み取ったQRデータ
   * @return 処理結果
   */
  private fun readItem( scanItem:String? ):Boolean {
    if( scanItem == null ) return false

    var position:Int = 0

    var cd:String = model01.convertTrueCd( scanItem.substring( 3, 13 ).toInt().toString() );
    var cn:String = scanItem.substring( 14, 16 );
    var sz:String = scanItem.substring( 17, 21 ).replace( " ", "" );

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "品番 色番 サイズ = " + cd + " " + cn + " " + sz )

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel01> ).indexOfFirst { model01.convertTrueCd(it.cd) == cd && it.cn == cn && it.sz == sz && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "00", "", getString( R.string.err_box_shipping01 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )
    } else {
      claimSound( playSoundOK )
      claimVibration( AppBase.vibrationOK )

      // 商品情報の検品数を更新します
      viewModel01.updateItemList( position )

      // ListViewの内容を更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel01> ) )

      // POTで読んだデータ数を更新します
      viewModel01.cntRead.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel01> ).sumBy { it.amt_n.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxHetShipping", "POTで読んだ件数 = " + viewModel01.cntRead.value )

      // 終了したらその旨を表示します
      position = ( viewModel01.itemList.value as MutableList<PotDataModel01> ).indexOfFirst { it.amt_n.toInt() < it.amt_p.toInt() }
      if( position == -1 ) { viewModel01.finishShipping() }
    }

    return true
  }
}
