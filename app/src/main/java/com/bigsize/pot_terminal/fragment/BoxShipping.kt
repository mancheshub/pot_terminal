package com.bigsize.pot_terminal.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.R
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BoxShipping
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.model.HashItem
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.model.PotDataModel05
import com.bigsize.pot_terminal.model.ScanCallback
import com.bigsize.pot_terminal.databinding.BoxShippingPage01Binding
import com.bigsize.pot_terminal.databinding.BoxShippingPage02Binding
import com.bigsize.pot_terminal.databinding.BoxShippingPage03Binding
import com.bigsize.pot_terminal.viewmodel.BoxShippingPage01 as VM_BoxShippingPage01
import com.bigsize.pot_terminal.viewmodel.BoxShippingPage02 as VM_BoxShippingPage02
import com.bigsize.pot_terminal.viewmodel.BoxShippingPage03 as VM_BoxShippingPage03
import com.bigsize.pot_terminal.adapter.BoxShippingPage01 as AD_BoxShippingPage01
import com.bigsize.pot_terminal.adapter.BoxShippingPage02 as AD_BoxShippingPage02
import com.bigsize.pot_terminal.adapter.BoxShippingPage03 as AD_BoxShippingPage03

class BoxShippingPage01:Fragment(),AdapterView.OnItemClickListener,ScanCallback {
  private lateinit var activity01:BoxShipping

  private val binding01:BoxShippingPage01Binding by dataBinding()
  private val viewModel01:VM_BoxShippingPage01 by viewModels()

  private lateinit var adapter01:AD_BoxShippingPage01
  private lateinit var adapter02:ArrayAdapter<String>
  private lateinit var adapter03:ArrayAdapter<String>

  private val model01:AppUtility = AppUtility()

  private var dialogERR:MessageDialog? = null

  override fun onCreateView( inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle? ):View? {
    return inflater.inflate( R.layout.box_shipping_page01, container, false )
  }

  override fun onViewCreated( view:View, savedInstanceState:Bundle? ) {
    super.onViewCreated( view, savedInstanceState )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    adapter03 = ArrayAdapter( context!!, R.layout.box_shipping_page01_popup02, mutableListOf( " " ) )
    binding01.txtShop.setAdapter( adapter03 )
    binding01.txtShop.setText( " ", false )

    adapter01 = AD_BoxShippingPage01( context!!, mutableListOf() )
    binding01.lstView01.adapter = adapter01

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

        activity01.claimSound( activity01.playSoundNG )
        activity01.claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( "00", "警告", message, "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      }

      // プログレスバーを消します - 異常終了

      if( apiCondition == "ER" ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        activity01.claimSound( activity01.playSoundNG )
        activity01.claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( "00", "通信エラー", getString( R.string.err_communication01 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
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

        activity01.claimSound( activity01.playSoundFN )
        activity01.claimVibration( AppBase.vibrationFN )

        val dialog:MessageDialog = MessageDialog( "00", "完了", getString( R.string.msg_box_shipping01 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      }
    })

    viewModel01.groupList.observe( this, Observer<MutableList<HashItem>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "伝発グループセレクトボックス内容更新" )

      // 伝発グループ名を抽出します
      var menuItems:MutableList<String> = mutableListOf()
      for( _item in viewModel01.groupList.value as MutableList<HashItem> ) { menuItems.add( _item.item ) }

      // アダプタデータを更新します
      adapter02 = ArrayAdapter( context!!, R.layout.box_shipping_page01_popup01, menuItems )
      binding01.txtGroup.setAdapter( adapter02 )
      binding01.txtGroup.setText( " ", false )
    })

    viewModel01.shopList.observe( this, Observer<MutableList<HashItem>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "店舗セレクトボックス内容更新" )

      // 店舗名を抽出します
      var menuItems:MutableList<String> = mutableListOf()
      for( _item in viewModel01.shopList.value as MutableList<HashItem> ) { menuItems.add( _item.item ) }

      // アダプタデータを更新します
      adapter03.clear()
      adapter03.addAll( menuItems )
      adapter03.notifyDataSetChanged()
    })

    viewModel01.itemList.observe( this, Observer<MutableList<PotDataModel01>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "商品データ内容更新" )

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntRead.value = "0"
      viewModel01.cntTotal.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel01> ).sumBy { it.amt_p.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "全データ数 POTで読んだデータ数 = " + viewModel01.cntTotal.value + " " + viewModel01.cntRead.value  )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel01> ) )
    })

    // ■ イベントを補足します

    binding01.txtGroup.setOnItemClickListener( this )
    binding01.txtShop.setOnItemClickListener( this )
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    try {
      activity01 = activity as BoxShipping
    } catch( e:ClassCastException ) { throw ClassCastException( getString( R.string.err_communication03 ) ) }
  }

  override fun onResume() {
    super.onResume()

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    // 伝発グループデータを取得します
    viewModel01.pickGroupList()

    // 空の伝発グループが選択されたとします
    viewModel01.selectedGroupID = " "

    // 箱ラベル背景色をクリアします
    viewModel01.bkgBoxno.value = "N"

    // 伝発グループが選択されたら店舗データを取得します
    viewModel01.pickShopList()

    // 伝発グループを切り替えたら空白の店舗データが選択されたとします
    binding01.txtShop.setText( " ", false )
    viewModel01.selectedShopID = " "
    viewModel01.selectedBoxID = " "

    // 伝発グループ変更により店舗データがクリアされたので商品データを再取得します
    viewModel01.pickItemList()
  }

  override fun onDestroyView() {
    super.onDestroyView()

    binding01.lstView01.adapter = null
  }

  /**
   * アイテムが選択された時に呼ばれるリスナー定義です
   */
  override fun onItemClick( parent:AdapterView<*>, v:View, position:Int, id:Long ) {
    var item:String? = null

    // アイテムが選択されたタイミングでエラーは解消します
    binding01.layGroup.error = null
    binding01.layShop.error = null

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    when( v.id ) {
      R.id.itm_group -> { // 伝発グループ選択
        item = adapter02.getItem( position )

        // 伝発グループIDを決定します
        var position:Int = ( viewModel01.groupList.value as MutableList<HashItem> ).indexOfFirst { it.item == item }
        var groupID:String = ( viewModel01.groupList.value as MutableList<HashItem> )[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "選択アイテム - グループ = " + groupID + " " + item )

        // 選択した伝発グループのIDを記録します
        viewModel01.selectedGroupID = groupID

        // 伝発グループが選択されたら店舗データを取得します
        viewModel01.pickShopList()

        // 伝発グループを切り替えたら空白の店舗データが選択されたとします
        binding01.txtShop.setText( " ", false )
        viewModel01.selectedShopID = " "
        viewModel01.selectedBoxID = " "

        // 伝発グループ変更により店舗データがクリアされたので商品データを再取得します
        viewModel01.pickItemList()
      }
      R.id.itm_shop -> { // 店舗選択
        item = adapter03.getItem( position )

        // 店舗IDを決定します
        var position:Int = ( viewModel01.shopList.value as MutableList<HashItem> ).indexOfFirst { it.item == item }
        var shopID:String = ( viewModel01.shopList.value as MutableList<HashItem> )[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "選択アイテム - 店舗 = " + shopID + " " + item )

        // 選択した店舗のIDを記録します
        viewModel01.selectedShopID = shopID

        // 店舗が選択されたら商品データを取得します
        viewModel01.pickItemList()
      }
      else -> {}
    }
  }

  /**
   * 箱を読んだ時の処理を定義します
   *
   * @param [scanBox] 読み取った箱QRデータ
   * @return 処理結果
   */
  override fun readBox( scanBox:String? ):Boolean { return true }

  /**
   * 商品を読んだ時の処理を定義します
   *
   * @param [scanItem] 読み取ったQRデータ
   * @return 処理結果
   */
  override fun readItem( scanItem:String? ):Boolean {
    if( scanItem == null ) return false

    var position:Int = 0

    // ダイアログが表示されていれば閉じます
    dialogERR?.dismiss()

    var cd:String = model01.convertTrueCd( scanItem.substring( 3, 13 ).toInt().toString() );
    var cn:String = scanItem.substring( 14, 16 );
    var sz:String = scanItem.substring( 17, 21 ).replace( " ", "" );

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "品番 色番 サイズ = " + cd + " " + cn + " " + sz )

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel01> ).indexOfFirst { model01.convertTrueCd(it.cd) == cd && it.cn == cn && it.sz == sz && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "", getString( R.string.err_box_shipping01 ), "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )
    } else {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )

      // 商品情報の検品数を更新します
      viewModel01.updateItemList( position )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel01> ) )

      // POTで読んだデータ数を更新します
      viewModel01.cntRead.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel01> ).sumBy { it.amt_n.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "POTで読んだ件数 = " + viewModel01.cntRead.value )

      // 終了したらその旨を表示します
      position = ( viewModel01.itemList.value as MutableList<PotDataModel01> ).indexOfFirst { it.amt_n.toInt() < it.amt_p.toInt() }
      if( position == -1 ) { viewModel01.finishShipping() }
    }

    return true
  }
}

class BoxShippingPage02:Fragment(),ScanCallback {
  private lateinit var activity01:BoxShipping

  private val binding01:BoxShippingPage02Binding by dataBinding()
  private val viewModel01:VM_BoxShippingPage02 by viewModels()

  private lateinit var adapter01:AD_BoxShippingPage02

  private val model01:AppUtility = AppUtility()

  private var dialogERR:MessageDialog? = null

  override fun onCreateView( inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle? ):View? {
    return inflater.inflate( R.layout.box_shipping_page02, container, false )
  }

  override fun onViewCreated( view:View, savedInstanceState:Bundle? ) {
    super.onViewCreated( view, savedInstanceState )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    adapter01 = AD_BoxShippingPage02( context!!, mutableListOf() )
    binding01.lstView01.adapter = adapter01

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

        activity01.claimSound( activity01.playSoundNG )
        activity01.claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( "00", "通信エラー", getString( R.string.err_communication01 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      }

      // プログレスバーを消します - 正常終了

      regex = Regex( "FN9" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE
      }
    })

    viewModel01.itemList.observe( this, Observer<MutableList<PotDataModel05>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "商品データ内容更新" )

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntRead.value = "0"
      viewModel01.cntTotal.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel05> ).sumBy { it.amt_p.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "全データ数 POTで読んだデータ数 = " + viewModel01.cntTotal.value + " " + viewModel01.cntRead.value  )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel05> ) )
    })
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    try {
      activity01 = activity as BoxShipping
    } catch( e:ClassCastException ) { throw ClassCastException( getString( R.string.err_communication03 ) ) }
  }

  override fun onResume() {
    super.onResume()

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    // 箱ラベルと店舗名と箱ラベル背景色をクリアします
    viewModel01.txtBoxno.value = ""
    viewModel01.txtShopname.value = ""
    viewModel01.bkgBoxno.value = "N"

    // 入力した箱ラベルをクリアします
    viewModel01.inputedBoxno = ""

    // キャンセル商品データを取得します
    viewModel01.pickItemList()
  }

  override fun onDestroyView() {
    super.onDestroyView()

    binding01.lstView01.adapter = null
  }

  /**
   * 箱を読んだ時の処理を定義します
   *
   * @param [scanBox] 読み取った箱QRデータ
   * @return 処理結果
   */
  override fun readBox( scanBox:String? ):Boolean {
    if( scanBox == null ) return false

    // 入力チェックを行います
    if( inputCheck( "01" ) == false ) return false

    var position:Int = 0

    // ダイアログが表示されていれば閉じます
    dialogERR?.dismiss()

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).indexOfFirst { it.boxno == scanBox.substring( 3 ) && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "", getString( R.string.err_box_shipping04 ), "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )
    } else {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )

      // 今回読んだ箱ラベルを表示します
      viewModel01.txtBoxno.value = scanBox.substring( 3 )

      // 箱ラベル背景色を決定します
      if( scanBox.substring( 3, 4 ) == "A" ) viewModel01.bkgBoxno.value = "A"
      if( scanBox.substring( 3, 4 ) == "B" ) viewModel01.bkgBoxno.value = "B"
      if( scanBox.substring( 3, 4 ) == "C" ) viewModel01.bkgBoxno.value = "C"
      if( scanBox.substring( 3, 4 ) == "D" ) viewModel01.bkgBoxno.value = "D"
      if( scanBox.substring( 3, 4 ) == "E" ) viewModel01.bkgBoxno.value = "E"

      // 今回読んだ箱ラベルを記録します
      viewModel01.inputedBoxno = scanBox.substring( 3 )

      // 箱ラベルの情報を取得します
      viewModel01.pickBoxInfomation()
    }

    return true
  }

  /**
   * 商品を読んだ時の処理を定義します
   *
   * @param [scanItem] 読み取ったQRデータ
   * @return 処理結果
   */
  override fun readItem( scanItem:String? ):Boolean {
    if( scanItem == null ) return false

    // 入力チェックを行います
    if( inputCheck( "02" ) == false ) return false

    var position:Int = 0

    // ダイアログが表示されていれば閉じます
    dialogERR?.dismiss()

    var cd:String = model01.convertTrueCd( scanItem.substring( 3, 13 ).toInt().toString() );
    var cn:String = scanItem.substring( 14, 16 );
    var sz:String = scanItem.substring( 17, 21 ).replace( " ", "" );

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "箱ラベル 品番 色番 サイズ = " + viewModel01.inputedBoxno + " " + cd + " " + cn + " " + sz )

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).indexOfFirst { it.boxno == viewModel01.inputedBoxno && model01.convertTrueCd(it.cd) == cd && it.cn == cn && it.sz == sz && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "", getString( R.string.err_box_shipping01 ), "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )
    } else {
      // 商品情報の検品数を更新します
      viewModel01.updateItemList( position )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel05> ) )

      // POTで読んだデータ数を更新します
      viewModel01.cntRead.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel05> ).sumBy { it.amt_n.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "POTで読んだ件数 = " + viewModel01.cntRead.value )

      // 該当商品の明細番号を取得します
      val potData:PotDataModel05 = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).get( position )
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "更新する明細番号 = " + potData.i_id )

      // 明細番号に合致する商品が検品完了状態である場合はデータ更新します
      position = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).indexOfFirst { it.i_id == potData.i_id && it.amt_n.toInt() < it.amt_p.toInt() }
      if( position == -1 ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "データ更新" )
        viewModel01.finishShipping( potData.i_id )
      }

      // 全て照合が終了したらその旨を表示します
      position = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).indexOfFirst { it.amt_n.toInt() < it.amt_p.toInt() }
      if( position == -1 ) {
        activity01.claimSound( activity01.playSoundFN )
        activity01.claimVibration( AppBase.vibrationFN )

        val dialog:MessageDialog = MessageDialog( "00", "完了", getString( R.string.msg_box_shipping01 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      } else {
        activity01.claimSound( activity01.playSoundOK )
        activity01.claimVibration( AppBase.vibrationOK )
      }
    }

    return true
  }

  /**
   * 入力チェックを行います
   *
   * @param [execSubject] チェックモード 01 : 箱読み取り時 02 : 商品読み取り時
   * @return 入力チェック結果
   */
  private fun inputCheck( execSubject:String ):Boolean {
    var msgError01:String = ""

    if( msgError01 == "" && execSubject == "02" && viewModel01.inputedBoxno == "" ) {
      msgError01 = getString( R.string.err_box_shipping03 )
    }

    if( msgError01 != "" ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "00", "エラー", getString( R.string.err_box_shipping03 ), "OK", "" )
      dialog.show( parentFragmentManager, "simple" )

      return false
    }

    return true
  }
}

class BoxShippingPage03:Fragment(),ScanCallback {
  private lateinit var activity01:BoxShipping

  private val binding01:BoxShippingPage03Binding by dataBinding()
  private val viewModel01:VM_BoxShippingPage03 by viewModels()

  private lateinit var adapter01:AD_BoxShippingPage03

  private val model01:AppUtility = AppUtility()

  private var dialogERR:MessageDialog? = null

  override fun onCreateView( inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle? ):View? {
    return inflater.inflate( R.layout.box_shipping_page03, container, false )
  }

  override fun onViewCreated( view:View, savedInstanceState:Bundle? ) {
    super.onViewCreated( view, savedInstanceState )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    adapter01 = AD_BoxShippingPage03( context!!, mutableListOf() )
    binding01.lstView01.adapter = adapter01

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

        activity01.claimSound( activity01.playSoundNG )
        activity01.claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( "00", "通信エラー", getString( R.string.err_communication01 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      }

      // プログレスバーを消します - 正常終了

      regex = Regex( "FN9" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE
      }
    })

    viewModel01.itemList.observe( this, Observer<MutableList<PotDataModel05>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "商品データ内容更新" )

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntRead.value = "0"
      viewModel01.cntTotal.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel05> ).sumBy { it.amt_p.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "全データ数 POTで読んだデータ数 = " + viewModel01.cntTotal.value + " " + viewModel01.cntRead.value  )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel05> ) )
    })
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    try {
      activity01 = activity as BoxShipping
    } catch( e:ClassCastException ) { throw ClassCastException( getString( R.string.err_communication03 ) ) }
  }

  override fun onResume() {
    super.onResume()

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    // 箱ラベルと店舗名と箱ラベル背景色をクリアします
    viewModel01.txtBoxno.value = ""
    viewModel01.txtShopname.value = ""
    viewModel01.bkgBoxno.value = "N"

    // 入力した箱ラベルをクリアします
    viewModel01.inputedBoxno = ""

    // 先送商品データを取得します
    viewModel01.pickItemList()
  }

  override fun onDestroyView() {
    super.onDestroyView()

    binding01.lstView01.adapter = null
  }

  /**
   * 箱を読んだ時の処理を定義します
   *
   * @param [scanBox] 読み取った箱QRデータ
   * @return 処理結果
   */
  override fun readBox( scanBox:String? ):Boolean {
    if( scanBox == null ) return false

    // 入力チェックを行います
    if( inputCheck( "01" ) == false ) return false

    var position:Int = 0

    // ダイアログが表示されていれば閉じます
    dialogERR?.dismiss()

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).indexOfFirst { it.boxno == scanBox.substring( 3 ) && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "", getString( R.string.err_box_shipping04 ), "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )
    } else {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )

      // 今回読んだ箱ラベルを表示します
      viewModel01.txtBoxno.value = scanBox.substring( 3 )

      // 箱ラベル背景色を決定します
      if( scanBox.substring( 3, 4 ) == "A" ) viewModel01.bkgBoxno.value = "A"
      if( scanBox.substring( 3, 4 ) == "B" ) viewModel01.bkgBoxno.value = "B"
      if( scanBox.substring( 3, 4 ) == "C" ) viewModel01.bkgBoxno.value = "C"
      if( scanBox.substring( 3, 4 ) == "D" ) viewModel01.bkgBoxno.value = "D"
      if( scanBox.substring( 3, 4 ) == "E" ) viewModel01.bkgBoxno.value = "E"

      // 今回読んだ箱ラベルを記録します
      viewModel01.inputedBoxno = scanBox.substring( 3 )

      // 箱ラベルの情報を取得します
      viewModel01.pickBoxInfomation()
    }

    return true
  }

  /**
   * 商品を読んだ時の処理を定義します
   *
   * @param [scanItem] 読み取ったQRデータ
   * @return 処理結果
   */
  override fun readItem( scanItem:String? ):Boolean {
    if( scanItem == null ) return false

    // 入力チェックを行います
    if( inputCheck( "02" ) == false ) return false

    var position:Int = 0

    // ダイアログが表示されていれば閉じます
    dialogERR?.dismiss()

    var cd:String = model01.convertTrueCd( scanItem.substring( 3, 13 ).toInt().toString() );
    var cn:String = scanItem.substring( 14, 16 );
    var sz:String = scanItem.substring( 17, 21 ).replace( " ", "" );

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "箱ラベル 品番 色番 サイズ = " + viewModel01.inputedBoxno + " " + cd + " " + cn + " " + sz )

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).indexOfFirst { it.boxno == viewModel01.inputedBoxno && model01.convertTrueCd(it.cd) == cd && it.cn == cn && it.sz == sz && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "", getString( R.string.err_box_shipping01 ), "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )
    } else {
      // 商品情報の検品数を更新します
      viewModel01.updateItemList( position )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel05> ) )

      // POTで読んだデータ数を更新します
      viewModel01.cntRead.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel05> ).sumBy { it.amt_n.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "POTで読んだ件数 = " + viewModel01.cntRead.value )

      // 該当商品の明細番号を取得します
      val potData:PotDataModel05 = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).get( position )
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "更新する明細番号 = " + potData.i_id )

      // 明細番号に合致する商品が検品完了状態である場合はデータ更新します
      position = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).indexOfFirst { it.i_id == potData.i_id && it.amt_n.toInt() < it.amt_p.toInt() }
      if( position == -1 ) {
        if( BuildConfig.DEBUG ) Log.d( "APP-BoxShipping", "データ更新" )
        viewModel01.finishShipping( potData.i_id )
      }

      // 全て照合が終了したらその旨を表示します
      position = ( viewModel01.itemList.value as MutableList<PotDataModel05> ).indexOfFirst { it.amt_n.toInt() < it.amt_p.toInt() }
      if( position == -1 ) {
        activity01.claimSound( activity01.playSoundFN )
        activity01.claimVibration( AppBase.vibrationFN )

        val dialog:MessageDialog = MessageDialog( "00", "完了", getString( R.string.msg_box_shipping01 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      } else {
        activity01.claimSound( activity01.playSoundOK )
        activity01.claimVibration( AppBase.vibrationOK )
      }
    }

    return true
  }

  /**
   * 入力チェックを行います
   *
   * @param [execSubject] チェックモード 01 : 箱読み取り時 02 : 商品読み取り時
   * @return 入力チェック結果
   */
  private fun inputCheck( execSubject:String ):Boolean {
    var msgError01:String = ""

    if( msgError01 == "" && execSubject == "02" && viewModel01.inputedBoxno == "" ) {
      msgError01 = getString( R.string.err_box_shipping03 )
    }

    if( msgError01 != "" ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      val dialog:MessageDialog = MessageDialog( "00", "エラー", msgError01, "OK", "" )
      dialog.show( parentFragmentManager, "simple" )

      return false
    }

    return true
  }
}
