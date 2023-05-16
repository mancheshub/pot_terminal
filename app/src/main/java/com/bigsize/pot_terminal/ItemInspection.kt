package com.bigsize.pot_terminal

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.HashItem
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.model.PotDataModel03
import com.bigsize.pot_terminal.databinding.ItemInspectionBinding
import com.bigsize.pot_terminal.viewmodel.ItemInspection as VM_ItemInspection
import com.bigsize.pot_terminal.adapter.ItemInspection as AD_ItemInspection

class ItemInspection:DensoWaveBase(),View.OnClickListener,AdapterView.OnItemClickListener,DialogCallback {
  private val binding01:ItemInspectionBinding by dataBinding()
  private val viewModel01:VM_ItemInspection by viewModels()

  private lateinit var adapter01:AD_ItemInspection
  private lateinit var adapter02:ArrayAdapter<String>
  private lateinit var adapter03:ArrayAdapter<String>
  private lateinit var adapter04:ArrayAdapter<String>
  private lateinit var adapter05:ArrayAdapter<String>

  private var dialogERR:MessageDialog? = null

  override fun onCreate( savedInstanceState:Bundle? ) {
    super.onCreate( savedInstanceState )

    setContentView( R.layout.item_inspection )

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

    supportActionBar?.title = "商品検品"
    supportActionBar?.setDisplayHomeAsUpEnabled( true )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    adapter02 = ArrayAdapter( applicationContext, R.layout.item_inspection_popup01, mutableListOf() )
    binding01.txtGroup.setAdapter( adapter02 )

    adapter03 = ArrayAdapter( applicationContext, R.layout.item_inspection_popup02, mutableListOf() )
    binding01.txtShop.setAdapter( adapter03 )

    adapter01 = AD_ItemInspection( applicationContext, mutableListOf() )
    binding01.lstView01.adapter = adapter01

    // 箱名を抽出します
    var menuItems01:MutableList<String> = mutableListOf()
    for( _item in viewModel01.boxList ) { menuItems01.add( _item.item ) }

    adapter04 = ArrayAdapter( applicationContext, R.layout.item_inspection_popup03, menuItems01 )
    binding01.txtBox.setAdapter( adapter04 )

    // 初期値を"箱01"として箱のIDを記録します
    binding01.txtBox.setText( "箱01", false )
    viewModel01.selectedBoxID = "01"

    // 印刷機名を抽出します
    var menuItems02:MutableList<String> = mutableListOf()
    for( _item in viewModel01.printList ) { menuItems02.add( _item.item ) }

    adapter05 = ArrayAdapter( applicationContext, R.layout.item_inspection_popup04, menuItems02 )
    binding01.txtPrint.setAdapter( adapter05 )

    // 初期値を"印刷機01"として印刷機のIDを記録します
    binding01.txtPrint.setText( "印刷機01", false )
    viewModel01.selectedPrintID = "ELS_FEL_P01"

    // 作業グループデータを取得します
    viewModel01.pickGroupList()

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

      regex = Regex( "AL0" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        // 警告ダイアログを表示します

        var message:String = ""
        var callbackFlag:String = ""

        if( apiCondition == "AL01" ) { callbackFlag = "00"; message = getString( R.string.err_item_inspection05 ); }
        if( apiCondition == "AL03" ) { callbackFlag = "05"; message = getString( R.string.msg_item_inspection07 ); }

        claimSound( playSoundNG )
        claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( callbackFlag, "警告", message, "OK", "" )
        dialog.show( supportFragmentManager, "simple" )
      }

      // プログレスバーを消します - 正常終了

      // 終了ステータスでどのような処理が完了したかを判定します
      // FN01 → クリア FN02 → 箱確定 FN03 → 確定 FN** → 左記以外

      // 誤って確定(欠品)した時に引き続き同店舗で検品するために、確定後は店舗セレクトボックスの内容をそのままとします

      regex = Regex( "FN9" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE
      }

      regex = Regex( "FN0" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        if( apiCondition == "FN01" ) {
          // 選択した"作業グループ・店舗"をクリアします
          viewModel01.selectedGroupID = ""
          viewModel01.selectedShopID = ""
          viewModel01.selectedBoxID = "01"
          viewModel01.selectedPrintID = "01"

          // "作業グループ・店舗・箱・印刷機"の選択をクリアします
          binding01.txtGroup.setText( "", false )
          binding01.txtShop.setText( "", false )
          binding01.txtBox.setText( "箱01", false )
          binding01.txtPrint.setText( "印刷機01", false )
        }

        if( apiCondition == "FN02" ) {
          viewModel01.selectedBoxID = ( viewModel01.selectedBoxID.toInt() + 1 ).toString().padStart( 2, '0' )

          // 箱確定したら次の箱が選択されたとします
          binding01.txtBox.setText( "箱"+viewModel01.selectedBoxID, false )
        }

        // いずれの処理も店舗の検品状況に変化があったので店舗データと商品データを再取得します
        viewModel01.pickShopList()
        viewModel01.pickItemList( "nonExclusive" )

        // 原則全量検品が完了した店舗は店舗リストに表示されませんが、確定によって全量検品完了後に該当店舗は選択状態となっています
        // 該当店舗をあえて選択状態のままとしているのは、誤って確定(欠品)した時に引き続き同店舗で検品するためです。

        // 完了ダイアログを表示します

        var message:String = ""
        if( apiCondition == "FN01" ) message = getString( R.string.msg_item_inspection01 )
        if( apiCondition == "FN02" ) message = getString( R.string.msg_item_inspection02 )
        if( apiCondition == "FN03" ) message = getString( R.string.msg_item_inspection03 )

        claimSound( playSoundFN )
        claimVibration( AppBase.vibrationFN )

        val dialog:MessageDialog = MessageDialog( "00", "完了", message, "OK", "" )
        dialog.show( supportFragmentManager, "simple" )
      }

      // プログレスバーを消します - 排他制御終了

      regex = Regex( "SI_" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        var staffID:String = apiCondition.substring( 3, 6 )

        claimSound( playSoundNG )
        claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( "00", "排他エラー", "ｽﾀｯﾌ " + staffID + " " + getString( R.string.err_item_inspection01 ), "OK", "" )
        dialog.show( supportFragmentManager, "simple" )
      }
    })

    viewModel01.groupList.observe( this, Observer<MutableList<HashItem>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "作業グループセレクトボックス内容更新" )

      // 作業グループ名を抽出します
      var menuItems:MutableList<String> = mutableListOf()
      for( _item in viewModel01.groupList.value as MutableList<HashItem> ) { menuItems.add( _item.item ) }

      // アダプタデータを更新します
      adapter02.clear()
      adapter02.addAll( menuItems )
      adapter02.notifyDataSetChanged()
    })

    viewModel01.shopList.observe( this, Observer<MutableList<HashItem>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "店舗セレクトボックス内容更新" )

      // 店舗名を抽出します
      var menuItems:MutableList<String> = mutableListOf()
      for( _item in viewModel01.shopList.value as MutableList<HashItem> ) { menuItems.add( _item.item ) }

      // アダプタデータを更新します
      adapter03.clear()
      adapter03.addAll( menuItems )
      adapter03.notifyDataSetChanged()
    })

    viewModel01.itemList.observe( this, Observer<MutableList<PotDataModel03>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "商品データ内容更新" )

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntRead.value = "0"
      viewModel01.cntTotal.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel03> ).sumBy { it.amt_p.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "全データ数 POTで読んだデータ数 = " + viewModel01.cntTotal.value + " " + viewModel01.cntRead.value  )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel03> ) )
    })

    scanItemH.observe( this, Observer<String> {
      if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "商品データ = " + scanItemH.value )

      readItem( scanItemH.value )
    })

    // ■ イベントを補足します

    binding01.exeButton01.setOnClickListener( this )
    binding01.exeButton02.setOnClickListener( this )
    binding01.exeButton03.setOnClickListener( this )
    binding01.txtGroup.setOnItemClickListener( this )
    binding01.txtShop.setOnItemClickListener( this )
    binding01.txtBox.setOnItemClickListener( this )
    binding01.txtPrint.setOnItemClickListener( this )
  }

  override fun onDestroy() {
    super.onDestroy()

    binding01.lstView01.adapter = null
  }

  /**
   * ダイアログで実行する処理を実装します
   */
  override fun fromMessageDialog( callbackType:String ) {
    // クリアボタンを押した場合
    if( callbackType == "01" && inputCheck( "01" ) == true ) viewModel01.deceded( "01" )

    // 箱確定ボタンを押した場合
    if( callbackType == "02" && inputCheck( "02" ) == true ) viewModel01.deceded( "02" )

    // 確定ボタンを押した場合
    if( callbackType == "03" && inputCheck( "03" ) == true ) viewModel01.deceded( "03" )
    if( callbackType == "05" ) { viewModel01.isExecute03 = "0"; viewModel01.deceded( "03" ); }

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
    binding01.layBox.error = null
    binding01.layPrint.error = null

    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    when( v.id ) {
      R.id.itm_group -> { // 作業グループ選択
        item = adapter02.getItem( position )

        // 作業グループIDを決定します
        var position:Int = ( viewModel01.groupList.value as MutableList<HashItem> ).indexOfFirst { it.item == item }
        var groupID:String = ( viewModel01.groupList.value as MutableList<HashItem> )[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "選択アイテム - グループ = " + groupID + " " + item )

        // 選択した作業グループのIDを記録します
        viewModel01.selectedGroupID = groupID

        // 作業グループが選択されたら店舗データを取得します
        viewModel01.pickShopList()

        // 選択した"店舗・箱・印刷機"をクリアします
        viewModel01.selectedShopID = ""
        viewModel01.selectedBoxID = "01"
        viewModel01.selectedPrintID = "ELS_FEL_P01"

        // "店舗・箱・印刷機"の選択をクリアします
        binding01.txtShop.setText( " ", false )
        binding01.txtBox.setText( "箱01", false )
        binding01.txtPrint.setText( "印刷機01", false )

        // 作業グループ変更により店舗データがクリアされたので商品データを再取得します
        viewModel01.pickItemList( "nonExclusive" )
      }
      R.id.itm_shop -> { // 店舗選択
        item = adapter03.getItem( position )

        // 店舗IDを決定します
        var position:Int = ( viewModel01.shopList.value as MutableList<HashItem> ).indexOfFirst { it.item == item }
        var shopID:String = ( viewModel01.shopList.value as MutableList<HashItem> )[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "選択アイテム - 店舗 = " + shopID + " " + item )

        // 選択した店舗のIDを記録します
        viewModel01.selectedShopID = shopID

        // 店舗が選択されたら商品データを取得します
        viewModel01.pickItemList( "exeExclusive" )

        // 店舗を切り替えたら箱01が選択されたとします
        binding01.txtBox.setText( "箱01", false )
        viewModel01.selectedBoxID = "01"
      }
      R.id.itm_box -> { // 箱選択
        item = adapter04.getItem( position )

        // 箱IDを決定します
        var position:Int = viewModel01.boxList.indexOfFirst { it.item == item }
        var boxID:String = viewModel01.boxList[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "選択アイテム - 箱 = " + boxID + " " + item )

        // 選択した箱のIDを記録します
        viewModel01.selectedBoxID = boxID
      }
      R.id.itm_print -> { // 印刷機選択
        item = adapter05.getItem( position )

        // 印刷機IDを決定します
        var position:Int = viewModel01.printList.indexOfFirst { it.item == item }
        var printID:String = viewModel01.printList[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "選択アイテム - 印刷機 = " + printID + " " + item )

        // 選択した印刷機のIDを記録します
        viewModel01.selectedPrintID = printID
      }
      else -> {}
    }
  }

  /**
   * ボタンが押された時に呼ばれるリスナー定義です
   */
  override fun onClick( v:View ) {
    claimSound( playSoundOK )
    claimVibration( AppBase.vibrationOK )

    when( v.id ) {
      R.id.exe_button01 -> { // クリアボタン
        val dialog:MessageDialog = MessageDialog( "01", "クリア確認", getString( R.string.msg_item_inspection04 ), "はい", "いいえ" )
        dialog.show( supportFragmentManager, "simple" )
      }
      R.id.exe_button02 -> { // 箱確定ボタン
        val dialog:MessageDialog = MessageDialog( "02", "箱確定確認", getString( R.string.msg_item_inspection05 ), "はい", "いいえ" )
        dialog.show( supportFragmentManager, "simple" )
      }
      R.id.exe_button03 -> { // 確定ボタン
        val dialog:MessageDialog = MessageDialog( "03", "確定確認", getString( R.string.msg_item_inspection06 ), "はい", "いいえ" )
        dialog.show( supportFragmentManager, "simple" )
      }
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

    // ダイアログが表示されていれば閉じます
    dialogERR?.dismiss()

    var hcd:String = scanItem.substring( 1, 11 );
    var hcn:String = scanItem.substring( 11, 13 );
    var hcz:String = scanItem.substring( 13, 16 );

    if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "は品番 は色番 はサイズ = " + hcd + " " + hcn + " " + hcz )

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel03> ).indexOfFirst { it.hcd == hcd && it.hcn == hcn && it.hcz == hcz && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-ItemVerification", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "", getString( R.string.err_item_inspection02 ), "OK", "" )
      dialogERR?.show( supportFragmentManager, "simple" )
    } else {
      claimSound( playSoundOK )
      claimVibration( AppBase.vibrationOK )

      // 商品情報の検品数を更新します
      viewModel01.updateItemList( position )

      // ListViewの内容を更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel03> ) )

      // POTで読んだデータ数を更新します
      viewModel01.cntRead.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel03> ).sumBy { it.amt_n.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "POTで読んだ件数 = " + viewModel01.cntRead.value )

      // 終了したらその旨を表示します
      position = ( viewModel01.itemList.value as MutableList<PotDataModel03> ).indexOfFirst { it.amt_n.toInt() < it.amt_p.toInt() }
      if( position == -1 ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-ItemInspection", "完了" )

        val dialog:MessageDialog = MessageDialog( "03", "確定確認", getString( R.string.msg_item_inspection06 ), "はい", "いいえ" )
        dialog.show( supportFragmentManager, "simple" )
      }
    }

    return true
  }

  /**
   * 入力チェックを行います - 入力チェックは処理区分に応じた内容で実施されます
   *
   * @param [kind] 処理区分 01 : クリア 02 : 箱確定 03 : 確定
   * @return 入力チェック結果
   */
  private fun inputCheck( kind:String ):Boolean {
    var msgError01:String = ""
    var msgError02:String = ""
    var msgError03:String = ""
    var msgError04:String = ""

    binding01.layGroup.error = null
    binding01.layShop.error = null
    binding01.layBox.error = null
    binding01.layPrint.error = null

    if( msgError01 == "" && ( binding01.txtGroup as TextView ).text.toString() == " " ) {
      msgError01 = getString( R.string.err_txt_number01 )
    }

    if( msgError02 == "" && ( binding01.txtShop as TextView ).text.toString() == " " ) {
      msgError02 = getString( R.string.err_txt_number01 )
    }

    if( kind != "01" && msgError03 == "" && ( binding01.txtBox as TextView ).text.toString() == " " ) {
      msgError03 = getString( R.string.err_txt_number01 )
    }

    if( kind != "01" && msgError04 == "" && ( binding01.txtPrint as TextView ).text.toString() == " " ) {
      msgError04 = getString( R.string.err_txt_number01 )
    }

    if( msgError01 != "" || msgError02 != "" || msgError03 != "" || msgError04 != "" ) {
      if( msgError01 != "" ) { binding01.layGroup.error = msgError01 }
      if( msgError02 != "" ) { binding01.layShop.error = msgError02 }
      if( msgError03 != "" ) { binding01.layBox.error = msgError03 }
      if( msgError04 != "" ) { binding01.layPrint.error = msgError04 }

      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      return false
    }

    var itemList:MutableList<PotDataModel03>? = viewModel01.itemList.value

    if( kind == "01" && ( itemList == null || itemList.size == 0 || itemList.sumBy { it.amt_n.toInt() } == 0 ) ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "00", "エラー", getString( R.string.err_item_inspection06 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )

      return false
    }

    if( kind == "02" && ( itemList == null || itemList.size == 0 || itemList.sumBy { it.amt_n.toInt() } == 0 ) ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "00", "エラー", getString( R.string.err_item_inspection03 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )

      return false
    }

    if( kind == "03" && ( itemList == null || itemList.size == 0 ) ) {
      claimSound( playSoundNG )
      claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "00", "エラー", getString( R.string.err_item_inspection04 ), "OK", "" )
      dialog.show( supportFragmentManager, "simple" )

      return false
    }

    return true
  }
}
