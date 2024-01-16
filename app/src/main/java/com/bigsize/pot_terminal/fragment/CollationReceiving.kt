package com.bigsize.pot_terminal.fragment

import android.content.Context
import android.content.Intent
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
import com.bigsize.pot_terminal.*
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.databinding.CollationReceivingPage01Binding
import com.bigsize.pot_terminal.databinding.CollationReceivingPage02Binding
import com.bigsize.pot_terminal.databinding.CollationReceivingPage03Binding
import com.bigsize.pot_terminal.model.*
import com.bigsize.pot_terminal.viewmodel.CollationReceivingPage01 as VM_CollationReceivingPage01
import com.bigsize.pot_terminal.viewmodel.CollationReceivingPage02 as VM_CollationReceivingPage02
import com.bigsize.pot_terminal.viewmodel.CollationReceivingPage03 as VM_CollationReceivingPage03
import com.bigsize.pot_terminal.adapter.CollationReceivingPage01 as AD_CollationReceivingPage01
import com.bigsize.pot_terminal.adapter.CollationReceivingPage02 as AD_CollationReceivingPage02

class CollationReceivingPage01:Fragment(),AdapterView.OnItemClickListener,ScanCallback,KeyCallback {
  private lateinit var activity01:CollationReceiving

  private val binding01:CollationReceivingPage01Binding by dataBinding()
  private val viewModel01:VM_CollationReceivingPage01 by viewModels()

  private lateinit var adapter01:ArrayAdapter<String>
  private lateinit var adapter02:ArrayAdapter<String>
  private lateinit var adapter03:AD_CollationReceivingPage01

  private val model01:AppUtility = AppUtility()
  private val model02:FileOperation = FileOperation()
  private val model03:PreferencesOperation = PreferencesOperation()

  private var dialogFIN:MessageDialog? = null
  private var dialogERR:MessageDialog? = null

  override fun onCreateView( inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle? ):View? {
    return inflater.inflate( R.layout.collation_receiving_page01, container, false )
  }

  override fun onViewCreated( view:View, savedInstanceState:Bundle? ) {
    super.onViewCreated( view, savedInstanceState )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    // 階を抽出します
    var menuItems01:MutableList<String> = mutableListOf()
    for( _item in viewModel01.floorList ) { menuItems01.add( _item.item ) }

    adapter01 = ArrayAdapter( context!!, R.layout.collation_receiving_page01_popup01, menuItems01 )
    binding01.txtFloor.setAdapter( adapter01 )

    adapter02 = ArrayAdapter( context!!, R.layout.collation_receiving_page01_popup02, mutableListOf() )
    binding01.txtUnit.setAdapter( adapter02 )

    adapter03 = AD_CollationReceivingPage01( context!!, mutableListOf() )
    binding01.lstView01.adapter = adapter03

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

      regex = Regex( "FN0" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        // 完了ダイアログを表示します

        activity01.claimSound( activity01.playSoundFN )
        activity01.claimVibration( AppBase.vibrationFN )

        val dialog:MessageDialog = MessageDialog( "00", "完了", getString( R.string.msg_collation_receiving01 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      }
    })

    viewModel01.unitList.observe( this, Observer<MutableList<HashItem>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "単位セレクトボックス内容更新" )

      // 単位名を抽出します
      var menuItems:MutableList<String> = mutableListOf()
      for( _item in viewModel01.unitList.value as MutableList<HashItem> ) { menuItems.add( _item.item ) }

      // アダプタデータを更新します
      adapter02.clear()
      adapter02.addAll( menuItems )
      adapter02.notifyDataSetChanged()
    })

    viewModel01.itemList.observe( this, Observer<MutableList<PotDataModel04>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "商品データ内容更新" )

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntRead.value = "0"
      viewModel01.cntTotal.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel04> ).sumBy { it.amt_p.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "全データ数 POTで読んだデータ数 = " + viewModel01.cntTotal.value + " " + viewModel01.cntRead.value  )

      // アダプタデータを更新します
      adapter03.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel04> ) )
    })

    // ■ イベントを補足します

    binding01.txtFloor.setOnItemClickListener( this )
    binding01.txtUnit.setOnItemClickListener( this )
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    try {
      activity01 = activity as CollationReceiving
    } catch( e:ClassCastException ) { throw ClassCastException( getString( R.string.err_communication03 ) ) }
  }

  override fun onResume() {
    super.onResume()

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    // "階・単位"の選択をクリアします
    binding01.txtFloor.setText( "", false )
    binding01.txtUnit.setText( "", false )

    // "入庫ロケーション・ロケーション・商品・全データ数・POTで読んだデータ数"の表示をクリアします
    viewModel01.txtAddress.value = "入庫場所を表示"
    viewModel01.txtLocation.value = ""
    viewModel01.txtCd.value = ""
    viewModel01.txtCn.value = ""
    viewModel01.txtSz.value = ""
    viewModel01.cntTotal.value = "0"
    viewModel01.cntRead.value = "0"

    // アダプタデータを更新します
    adapter02.clear()
    adapter02.notifyDataSetChanged()

    // アダプタデータを更新します
    adapter03.refreshItem( mutableListOf<PotDataModel04>() )
  }

  override fun onPause() {
    super.onPause()

    // 選択した"階・単位"をクリアします
    viewModel01.selectedFloorID = ""
    viewModel01.selectedUnitID = ""

    // 入力した"ロケーション・商品"をクリアします
    viewModel01.inputedLocation = ""
    viewModel01.inputedCd = ""
    viewModel01.inputedCn = ""
    viewModel01.inputedSz = ""

    // 商品と合致した位置をクリアします
    viewModel01.memPosition = ""
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
    binding01.layFloor.error = null
    binding01.layUnit.error = null

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    when( v.id ) {
      R.id.itm_floor -> { // 階選択
        item = adapter01.getItem( position )

        // 階IDを決定します
        var position:Int = ( viewModel01.floorList as MutableList<HashItem> ).indexOfFirst { it.item == item }
        var floorID:String = ( viewModel01.floorList as MutableList<HashItem> )[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "選択アイテム - 階 = " + floorID + " " + item )

        // 選択した階のIDを記録します
        viewModel01.selectedFloorID = floorID

        // 階が選択されたら単位データを取得します
        viewModel01.pickH_UnitList()

        // 階を選択したら強制的に全部の単位を選択したものとします
        viewModel01.selectedUnitID = ""

        // 選択した単位と入力した"ロケーション・商品"をクリアします
        viewModel01.inputedLocation = ""
        viewModel01.inputedCd = ""
        viewModel01.inputedCn = ""
        viewModel01.inputedSz = ""

        // 単位の選択をクリアします
        binding01.txtUnit.setText( "全部", false )

        // "入庫ロケーション・ロケーション・商品・全データ数・POTで読んだデータ数"の表示をクリアします
        viewModel01.txtAddress.value = ""
        viewModel01.txtLocation.value = ""
        viewModel01.txtCd.value = ""
        viewModel01.txtCn.value = ""
        viewModel01.txtSz.value = ""

        // 商品と合致した位置をクリアします
        viewModel01.memPosition = ""

        // 階変更により単位データがクリアされたので商品データを再取得します
        viewModel01.pickH_ItemList()
      }
      R.id.itm_unit -> { // 単位選択
        item = adapter02.getItem( position )

        // 単位IDを決定します
        var position:Int = ( viewModel01.unitList.value as MutableList<HashItem> ).indexOfFirst { it.item == item }
        var unitID:String = ( viewModel01.unitList.value as MutableList<HashItem> )[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "選択アイテム - 単位 = " + unitID + " " + item )

        // 選択した単位のIDを記録します
        viewModel01.selectedUnitID = unitID

        // 単位が選択されたら商品データを取得します
        viewModel01.pickH_ItemList()

        // 選択した単位と入力した"ロケーション・商品"をクリアします
        viewModel01.inputedLocation = ""
        viewModel01.inputedCd = ""
        viewModel01.inputedCn = ""
        viewModel01.inputedSz = ""

        // "入庫ロケーション・ロケーション・商品・全データ数・POTで読んだデータ数"の表示をクリアします
        viewModel01.txtAddress.value = ""
        viewModel01.txtLocation.value = ""
        viewModel01.txtCd.value = ""
        viewModel01.txtCn.value = ""
        viewModel01.txtSz.value = ""

        // 商品と合致した位置をクリアします
        viewModel01.memPosition = ""
      }
      else -> {}
    }
  }

  /**
   * 棚を読んだ時の処理を定義します
   *
   * @param [scanBox] 読み取った箱QRデータ
   * @return 処理結果
   */
  override fun readShelf( scanShelf:String? ):Boolean {
    if( scanShelf == null ) return false

    // 入力チェックを行います
    if( inputCheck( "01" ) == false ) return false

    // ダイアログが表示されていれば閉じます
    dialogFIN?.dismiss(); dialogFIN = null;
    dialogERR?.dismiss(); dialogERR = null;

    // 今回読んだロケーションと照合対象のロケーションの完全版を作成します
    var fullLocation01:String = scanShelf.substring( 3 )
    var fullLocation02:String = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).get( viewModel01.memPosition.toInt() ).location.replace( "-", "" )

    // 今回読んだロケーションと照合対象のロケーションの箱を除いた版を作成します(箱を除いたロケーションで比較するため)
    val compLocation01:String = scanShelf.substring( 3,11 )
    val compLocation02:String = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).get( viewModel01.memPosition.toInt() ).location.replace( "-", "" ).substring( 0, 8 )

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "POTで読んだロケーション = " + fullLocation01 + " " + compLocation01 )
    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", " 照合対象のロケーション = " + fullLocation02 + " " + compLocation02 )

    // 今回読んだロケーションを記録します
    viewModel01.inputedLocation = scanShelf.substring( 3 )

    // ロケーションを表示します
    viewModel01.txtLocation.value = fullLocation01.substring( 0, 1 ) + fullLocation01.substring( 1, 2 ) + fullLocation01.substring( 2, 4 ) + "-" +
                                    fullLocation01.substring( 4, 7 ) + "-" + fullLocation01.substring( 7, 8 ) + "-" + fullLocation01.substring( 8 )

    // 見取図参照のケースで棚QRを読んだらそのまま終了します
    if( fullLocation02 == "00000000000" ) {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )

      return true
    }

    if( fullLocation01 != fullLocation02 ) {
      if( compLocation01 != compLocation02 ) {
        activity01.claimSound( activity01.playSoundNG )
        activity01.claimVibration( AppBase.vibrationNG )

        // 箱を除いたロケーションすら合致していなければ今回読んだロケーションの記録と表示を元に戻します
        viewModel01.inputedLocation = ""
        viewModel01.txtLocation.value = ""
      } else {
        activity01.claimSound( activity01.playSoundOK )
        activity01.claimVibration( AppBase.vibrationOK )
      }
    }

    // 商品情報の検品数を更新します
    if( fullLocation01 == fullLocation02 ) updateItem()

    return true
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
    if( inputCheck( "02" ) == false ) return false

    // ダイアログが表示されていれば閉じます
    dialogFIN?.dismiss(); dialogFIN = null;
    dialogERR?.dismiss(); dialogERR = null;

    // 今回読んだロケーションと照合対象のロケーションの完全版を作成します
    var fullLocation01:String = viewModel01.inputedLocation.substring( 0, 8 ) + scanBox.substring( 3 )
    var fullLocation02:String = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).get( viewModel01.memPosition.toInt() ).location.replace( "-", "" )

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "POTで読んだロケーション = " + fullLocation01 )
    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", " 照合対象のロケーション = " + fullLocation02 )

    // 今回読んだロケーションを記録します
    viewModel01.inputedLocation = viewModel01.inputedLocation.substring( 0, 8 ) + scanBox.substring( 3 )

    // ロケーションを表示します
    viewModel01.txtLocation.value = fullLocation01.substring( 0, 1 ) + fullLocation01.substring( 1, 2 ) + fullLocation01.substring( 2, 4 ) + "-" +
                                    fullLocation01.substring( 4, 7 ) + "-" + fullLocation01.substring( 7, 8 ) + "-" + fullLocation01.substring( 8 )

    // 見取図参照のケースで棚QRを読んだらそのまま終了します
    if( fullLocation02 == "00000000000" ) {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )

      return true
    }

    if( fullLocation01 != fullLocation02 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      // 今回読んだロケーションの記録と表示を元に戻します
      viewModel01.inputedLocation = fullLocation01.substring( 0, 1 ) + fullLocation01.substring( 1, 2 ) + fullLocation01.substring( 2, 4 ) +
                                    fullLocation01.substring( 4, 7 ) + fullLocation01.substring( 7, 8 ) + "000"
      viewModel01.txtLocation.value = fullLocation01.substring( 0, 1 ) + fullLocation01.substring( 1, 2 ) + fullLocation01.substring( 2, 4 ) + "-" +
                                      fullLocation01.substring( 4, 7 ) + "-" + fullLocation01.substring( 7, 8 ) + "-000"
    }

    // 商品情報の検品数を更新します
    if( fullLocation01 == fullLocation02 ) updateItem()

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

    lateinit var potData:PotDataModel04
    var position:Int = 0

    // 入力チェックを行います
    if( inputCheck( "03" ) == false ) return false

    // ダイアログが表示されていれば閉じます
    dialogFIN?.dismiss(); dialogFIN = null;
    dialogERR?.dismiss(); dialogERR = null;

    // 入力したロケーションをクリアします
    viewModel01.inputedLocation = ""

    // ロケーションの表示をクリアします
    viewModel01.txtLocation.value = ""

    // 商品と合致した位置をクリアします
    viewModel01.memPosition = ""

    var cd:String = scanItem.substring( 3, 13 );
    var cn:String = scanItem.substring( 14, 16 );
    var sz:String = scanItem.substring( 17, 21 );

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "品番 色番 サイズ = " + cd + " " + cn + " " + sz )

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).indexOfFirst { it.cd == model01.eightdigitsCd(cd) && it.cn == cn && it.sz == sz.replace(" ","") && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "", getString( R.string.err_collation_receiving01 ), "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )
    } else {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )

      // 今回読んだ商品を表示します
      viewModel01.txtCd.value = model01.eightdigitsCd( scanItem.substring( 3, 13 ) )
      viewModel01.txtCn.value = scanItem.substring( 14, 16 )
      viewModel01.txtSz.value = scanItem.substring( 17, 21 ).replace( " ", "" )

      // 今回読んだ商品を記録します
      viewModel01.inputedCd = scanItem.substring( 3, 13 )
      viewModel01.inputedCn = scanItem.substring( 14, 16 )
      viewModel01.inputedSz = scanItem.substring( 17, 21 )

      // ロケーション照合のために商品と合致した位置を記録します
      viewModel01.memPosition = position.toString()

      // 入庫ロケーションを表示します
      if( ( viewModel01.itemList.value as MutableList<PotDataModel04> )[position].location.replace( "-", "" ) == "00000000000" ) {
        viewModel01.txtAddress.value = "見取図参照"
      } else {
        viewModel01.txtAddress.value = ( viewModel01.itemList.value as MutableList<PotDataModel04> )[position].location
      }
    }

    return true
  }

  /**
   * エンターキーを押した時の処理を定義します
   */
  override fun enterEvent():Boolean {
    // 商品とロケーションが読まれている時にのみ処理します
    if( viewModel01.inputedCd == "" || viewModel01.inputedLocation == "" ) return false

    var fullLocation:String = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).get( viewModel01.memPosition.toInt() ).location.replace( "-", "" )

    // 照合対象のロケーションが見取図参照である時のみ処理します
    if( fullLocation != "00000000000" ) return false

    // 商品情報の検品数を更新します
    updateItem()

    return true
  }

  /**
   * 入力チェックを行います
   *
   * @param [execSubject] チェックモード 01 : 棚読み取り時 02 : 箱読み取り時 03 : 商品読み取り時
   * @return 入力チェック結果
   */
  private fun inputCheck( execSubject:String ):Boolean {
    var msgError01:String = ""

    if( msgError01 == "" && viewModel01.itemList.value == null ) {
      msgError01 = getString( R.string.err_collation_receiving04 )
    }

    if( msgError01 == "" && ( execSubject == "01" || execSubject == "02" ) && viewModel01.inputedCd == "" ) {
      msgError01 = getString( R.string.err_collation_receiving02 )
    }

    if( msgError01 == "" && execSubject == "02" && viewModel01.inputedLocation == "" ) {
      msgError01 = getString( R.string.err_collation_receiving03 )
    }

    if( msgError01 != "" ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "エラー", msgError01, "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )

      return false
    }

    return true
  }

  /**
   * 商品情報の検品数を更新します
   */
  private fun updateItem() {
    // 商品情報の検品数を更新します
    viewModel01.updateItemList( viewModel01.memPosition.toInt() )

    // アダプタデータを更新します
    adapter03.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel04> ) )

    // POTで読んだデータ数を更新します
    viewModel01.cntRead.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel04> ).sumBy { it.amt_n.toInt() } ).toString()

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "POTで読んだ件数 = " + viewModel01.cntRead.value )

    // POTデータは照合が完了した都度作成します
    execDataSave( "1" )

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "データ更新" )

    // 入力した"ロケーション・商品"をクリアします
    viewModel01.inputedLocation = ""
    viewModel01.inputedCd = ""
    viewModel01.inputedCn = ""
    viewModel01.inputedSz = ""

    // 商品と合致した位置をクリアします
    viewModel01.memPosition = ""

    // 入庫したらその旨を入庫ロケーションに表示します
    viewModel01.txtAddress.value = "入庫しました"

    // 終了したらその旨を表示します

    val position:Int = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).indexOfFirst { it.amt_n.toInt() < it.amt_p.toInt() }
    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundFN )
      activity01.claimVibration( AppBase.vibrationFN )

      dialogFIN = MessageDialog( "00", "完了", getString( R.string.msg_collation_receiving01 ), "OK", "" )
      dialogFIN?.show( parentFragmentManager, "simple" )
    } else {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )
    }
  }

  /**
   * POTデータを作成します
   */
  private fun execDataSave( amt:String ) {
    val devision = "1"
    val dataArray:MutableList<PotDataModel02> = mutableListOf()
    val dateHash:Map<String,String> = model01.returnPRecodeDate()

    dataArray.add( PotDataModel02(
      model03.readDeviceNO(), dateHash["date"]!!, dateHash["time"]!!, model03.readStaffNO(), devision,
      viewModel01.inputedCd, viewModel01.inputedCn, viewModel01.inputedSz,
      "00000000000", viewModel01.inputedLocation, amt, false,
    ) )

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "登録" )

    try {
      model02.savePotData( "APPEND", devision, dataArray )
    } catch( e:Exception ) {
      val intent = Intent( context!!, Failure::class.java )
      intent.putExtra( "MESSAGE", e.message )
      startActivity( intent )
    }
  }
}

class CollationReceivingPage02:Fragment(),AdapterView.OnItemClickListener,ScanCallback,KeyCallback {
  private lateinit var activity01:CollationReceiving

  private val binding01:CollationReceivingPage02Binding by dataBinding()
  private val viewModel01:VM_CollationReceivingPage02 by viewModels()

  private lateinit var adapter01:ArrayAdapter<String>
  private lateinit var adapter03:AD_CollationReceivingPage02

  private val model01:AppUtility = AppUtility()
  private val model02:FileOperation = FileOperation()
  private val model03:PreferencesOperation = PreferencesOperation()

  private var dialogFIN:MessageDialog? = null
  private var dialogERR:MessageDialog? = null

  override fun onCreateView( inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle? ):View? {
    return inflater.inflate( R.layout.collation_receiving_page02, container, false )
  }

  override fun onViewCreated( view:View, savedInstanceState:Bundle? ) {
    super.onViewCreated( view, savedInstanceState )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    // 階を抽出します
    var menuItems01:MutableList<String> = mutableListOf()
    for( _item in viewModel01.floorList ) { menuItems01.add( _item.item ) }

    adapter01 = ArrayAdapter( context!!, R.layout.collation_receiving_page02_popup01, menuItems01 )
    binding01.txtFloor.setAdapter( adapter01 )

    adapter03 = AD_CollationReceivingPage02( context!!, mutableListOf() )
    binding01.lstView01.adapter = adapter03

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

      regex = Regex( "FN0" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        // 完了ダイアログを表示します

        activity01.claimSound( activity01.playSoundFN )
        activity01.claimVibration( AppBase.vibrationFN )

        val dialog:MessageDialog = MessageDialog( "00", "完了", getString( R.string.msg_collation_receiving01 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      }
    })

    viewModel01.itemList.observe( this, Observer<MutableList<PotDataModel04>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "商品データ内容更新" )

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntRead.value = "0"
      viewModel01.cntTotal.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel04> ).sumBy { it.amt_p.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "全データ数 POTで読んだデータ数 = " + viewModel01.cntTotal.value + " " + viewModel01.cntRead.value  )

      // アダプタデータを更新します
      adapter03.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel04> ) )
    })

    // ■ イベントを補足します

    binding01.txtFloor.setOnItemClickListener( this )
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    try {
      activity01 = activity as CollationReceiving
    } catch( e:ClassCastException ) { throw ClassCastException( getString( R.string.err_communication03 ) ) }
  }

  override fun onResume() {
    super.onResume()

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    // "階"の選択をクリアします
    binding01.txtFloor.setText( "", false )

    // "入庫ロケーション・ロケーション・商品・全データ数・POTで読んだデータ数"の表示をクリアします
    viewModel01.txtAddress.value = "入庫場所を表示"
    viewModel01.txtLocation.value = ""
    viewModel01.txtCd.value = ""
    viewModel01.txtCn.value = ""
    viewModel01.txtSz.value = ""
    viewModel01.cntTotal.value = "0"
    viewModel01.cntRead.value = "0"

    // アダプタデータを更新します
    adapter03.refreshItem( mutableListOf<PotDataModel04>() )
  }

  override fun onPause() {
    super.onPause()

    // 選択した"階"をクリアします
    viewModel01.selectedFloorID = ""

    // 入力した"ロケーション・商品"をクリアします
    viewModel01.inputedLocation = ""
    viewModel01.inputedCd = ""
    viewModel01.inputedCn = ""
    viewModel01.inputedSz = ""

    // 商品と合致した位置をクリアします
    viewModel01.memPosition = ""
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
    binding01.layFloor.error = null

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    when( v.id ) {
      R.id.itm_floor -> { // 階選択
        item = adapter01.getItem( position )

        // 階IDを決定します
        var position:Int = ( viewModel01.floorList as MutableList<HashItem> ).indexOfFirst { it.item == item }
        var floorID:String = ( viewModel01.floorList as MutableList<HashItem> )[position].id

        if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "選択アイテム - 階 = " + floorID + " " + item )

        // 選択した階のIDを記録します
        viewModel01.selectedFloorID = floorID

        // 選択した単位と入力した"ロケーション・商品"をクリアします
        viewModel01.inputedLocation = ""
        viewModel01.inputedCd = ""
        viewModel01.inputedCn = ""
        viewModel01.inputedSz = ""

        // "入庫ロケーション・ロケーション・商品・全データ数・POTで読んだデータ数"の表示をクリアします
        viewModel01.txtAddress.value = ""
        viewModel01.txtLocation.value = ""
        viewModel01.txtCd.value = ""
        viewModel01.txtCn.value = ""
        viewModel01.txtSz.value = ""

        // 商品と合致した位置をクリアします
        viewModel01.memPosition = ""

        // 階データがクリアされたので商品データを再取得します
        viewModel01.pickF_ItemList()
      }
      else -> {}
    }
  }

  /**
   * 棚を読んだ時の処理を定義します
   *
   * @param [scanBox] 読み取った箱QRデータ
   * @return 処理結果
   */
  override fun readShelf( scanShelf:String? ):Boolean {
    if( scanShelf == null ) return false

    // 入力チェックを行います
    if( inputCheck( "01" ) == false ) return false

    // ダイアログが表示されていれば閉じます
    dialogFIN?.dismiss(); dialogFIN = null;
    dialogERR?.dismiss(); dialogERR = null;

    // 今回読んだロケーションと照合対象のロケーションの完全版を作成します
    var fullLocation01:String = scanShelf.substring( 3 )
    var fullLocation02:String = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).get( viewModel01.memPosition.toInt() ).location.replace( "-", "" )

    // 今回読んだロケーションと照合対象のロケーションの箱を除いた版を作成します(箱を除いたロケーションで比較するため)
    val compLocation01:String = scanShelf.substring( 3,11 )
    val compLocation02:String = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).get( viewModel01.memPosition.toInt() ).location.replace( "-", "" ).substring( 0, 8 )

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "POTで読んだロケーション = " + fullLocation01 + " " + compLocation01 )
    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", " 照合対象のロケーション = " + fullLocation02 + " " + compLocation02 )

    // 今回読んだロケーションを記録します
    viewModel01.inputedLocation = scanShelf.substring( 3 )

    // ロケーションを表示します
    viewModel01.txtLocation.value = fullLocation01.substring( 0, 1 ) + fullLocation01.substring( 1, 2 ) + fullLocation01.substring( 2, 4 ) + "-" +
                                    fullLocation01.substring( 4, 7 ) + "-" + fullLocation01.substring( 7, 8 ) + "-" + fullLocation01.substring( 8 )

    // 見取図参照のケースで棚QRを読んだらそのまま終了します
    if( fullLocation02 == "00000000000" ) {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )

      return true
    }

    if( fullLocation01 != fullLocation02 ) {
      if( compLocation01 != compLocation02 ) {
        activity01.claimSound( activity01.playSoundNG )
        activity01.claimVibration( AppBase.vibrationNG )

        // 箱を除いたロケーションすら合致していなければ今回読んだロケーションの記録と表示を元に戻します
        viewModel01.inputedLocation = ""
        viewModel01.txtLocation.value = ""
      } else {
        activity01.claimSound( activity01.playSoundOK )
        activity01.claimVibration( AppBase.vibrationOK )
      }
    }

    // 商品情報の検品数を更新します
    if( fullLocation01 == fullLocation02 ) updateItem()

    return true
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
    if( inputCheck( "02" ) == false ) return false

    // ダイアログが表示されていれば閉じます
    dialogFIN?.dismiss(); dialogFIN = null;
    dialogERR?.dismiss(); dialogERR = null;

    // 今回読んだロケーションと照合対象のロケーションの完全版を作成します
    var fullLocation01:String = viewModel01.inputedLocation.substring( 0, 8 ) + scanBox.substring( 3 )
    var fullLocation02:String = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).get( viewModel01.memPosition.toInt() ).location.replace( "-", "" )

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "POTで読んだロケーション = " + fullLocation01 )
    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", " 照合対象のロケーション = " + fullLocation02 )

    // 今回読んだロケーションを記録します
    viewModel01.inputedLocation = viewModel01.inputedLocation.substring( 0, 8 ) + scanBox.substring( 3 )

    // ロケーションを表示します
    viewModel01.txtLocation.value = fullLocation01.substring( 0, 1 ) + fullLocation01.substring( 1, 2 ) + fullLocation01.substring( 2, 4 ) + "-" +
                                    fullLocation01.substring( 4, 7 ) + "-" + fullLocation01.substring( 7, 8 ) + "-" + fullLocation01.substring( 8 )

    // 見取図参照のケースで棚QRを読んだらそのまま終了します
    if( fullLocation02 == "00000000000" ) {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )

      return true
    }

    if( fullLocation01 != fullLocation02 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      // 今回読んだロケーションの記録と表示を元に戻します
      viewModel01.inputedLocation = fullLocation01.substring( 0, 1 ) + fullLocation01.substring( 1, 2 ) + fullLocation01.substring( 2, 4 ) +
                                    fullLocation01.substring( 4, 7 ) + fullLocation01.substring( 7, 8 ) + "000"
      viewModel01.txtLocation.value = fullLocation01.substring( 0, 1 ) + fullLocation01.substring( 1, 2 ) + fullLocation01.substring( 2, 4 ) + "-" +
                                      fullLocation01.substring( 4, 7 ) + "-" + fullLocation01.substring( 7, 8 ) + "-000"
    }

    // 商品情報の検品数を更新します
    if( fullLocation01 == fullLocation02 ) updateItem()

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

    lateinit var potData:PotDataModel04
    var position:Int = 0

    // 入力チェックを行います
    if( inputCheck( "03" ) == false ) return false

    // ダイアログが表示されていれば閉じます
    dialogFIN?.dismiss(); dialogFIN = null;
    dialogERR?.dismiss(); dialogERR = null;

    // 入力したロケーションをクリアします
    viewModel01.inputedLocation = ""

    // ロケーションの表示をクリアします
    viewModel01.txtLocation.value = ""

    // 商品と合致した位置をクリアします
    viewModel01.memPosition = ""

    var cd:String = scanItem.substring( 3, 13 );
    var cn:String = scanItem.substring( 14, 16 );
    var sz:String = scanItem.substring( 17, 21 );

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "品番 色番 サイズ = " + cd + " " + cn + " " + sz )

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).indexOfFirst { it.cd == model01.eightdigitsCd(cd) && it.cn == cn && it.sz == sz.replace(" ","") && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "", getString( R.string.err_collation_receiving01 ), "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )
    } else {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )

      // 今回読んだ商品を表示します
      viewModel01.txtCd.value = model01.eightdigitsCd( scanItem.substring( 3, 13 ) )
      viewModel01.txtCn.value = scanItem.substring( 14, 16 )
      viewModel01.txtSz.value = scanItem.substring( 17, 21 ).replace( " ", "" )

      // 今回読んだ商品を記録します
      viewModel01.inputedCd = scanItem.substring( 3, 13 )
      viewModel01.inputedCn = scanItem.substring( 14, 16 )
      viewModel01.inputedSz = scanItem.substring( 17, 21 )

      // ロケーション照合のために商品と合致した位置を記録します
      viewModel01.memPosition = position.toString()

      // 入庫ロケーションを表示します
      if( ( viewModel01.itemList.value as MutableList<PotDataModel04> )[position].location.replace( "-", "" ) == "00000000000" ) {
        viewModel01.txtAddress.value = "見取図参照"
      } else {
        viewModel01.txtAddress.value = ( viewModel01.itemList.value as MutableList<PotDataModel04> )[position].location
      }
    }

    return true
  }

  /**
   * エンターキーを押した時の処理を定義します
   */
  override fun enterEvent():Boolean {
    // 商品とロケーションが読まれている時にのみ処理します
    if( viewModel01.inputedCd == "" || viewModel01.inputedLocation == "" ) return false

    var fullLocation:String = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).get( viewModel01.memPosition.toInt() ).location.replace( "-", "" )

    // 照合対象のロケーションが見取図参照である時のみ処理します
    if( fullLocation != "00000000000" ) return false

    // 商品情報の検品数を更新します
    updateItem()

    return true
  }

  /**
   * 入力チェックを行います
   *
   * @param [execSubject] チェックモード 01 : 棚読み取り時 02 : 箱読み取り時 03 : 商品読み取り時
   * @return 入力チェック結果
   */
  private fun inputCheck( execSubject:String ):Boolean {
    var msgError01:String = ""

    if( msgError01 == "" && viewModel01.itemList.value == null ) {
      msgError01 = getString( R.string.err_collation_receiving04 )
    }

    if( msgError01 == "" && ( execSubject == "01" || execSubject == "02" ) && viewModel01.inputedCd == "" ) {
      msgError01 = getString( R.string.err_collation_receiving02 )
    }

    if( msgError01 == "" && execSubject == "02" && viewModel01.inputedLocation == "" ) {
      msgError01 = getString( R.string.err_collation_receiving03 )
    }

    if( msgError01 != "" ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "エラー", msgError01, "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )

      return false
    }

    return true
  }

  /**
   * 商品情報の検品数を更新します
   */
  private fun updateItem() {
    // 商品情報の検品数を更新します
    viewModel01.updateItemList( viewModel01.memPosition.toInt() )

    // アダプタデータを更新します
    adapter03.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel04> ) )

    // POTで読んだデータ数を更新します
    viewModel01.cntRead.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel04> ).sumBy { it.amt_n.toInt() } ).toString()

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "POTで読んだ件数 = " + viewModel01.cntRead.value )

    // POTデータは照合が完了した都度作成します
    execDataSave( "1" )

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "データ更新" )

    // 入力した"ロケーション・商品"をクリアします
    viewModel01.inputedLocation = ""
    viewModel01.inputedCd = ""
    viewModel01.inputedCn = ""
    viewModel01.inputedSz = ""

    // 商品と合致した位置をクリアします
    viewModel01.memPosition = ""

    // 入庫したらその旨を入庫ロケーションに表示します
    viewModel01.txtAddress.value = "入庫しました"

    // 終了したらその旨を表示します

    val position:Int = ( viewModel01.itemList.value as MutableList<PotDataModel04> ).indexOfFirst { it.amt_n.toInt() < it.amt_p.toInt() }
    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundFN )
      activity01.claimVibration( AppBase.vibrationFN )

      dialogFIN = MessageDialog( "00", "完了", getString( R.string.msg_collation_receiving01 ), "OK", "" )
      dialogFIN?.show( parentFragmentManager, "simple" )
    } else {
      activity01.claimSound( activity01.playSoundOK )
      activity01.claimVibration( AppBase.vibrationOK )
    }
  }

  /**
   * POTデータを作成します
   */
  private fun execDataSave( amt:String ) {
    val devision = "1"
    val dataArray:MutableList<PotDataModel02> = mutableListOf()
    val dateHash:Map<String,String> = model01.returnPRecodeDate()

    dataArray.add( PotDataModel02(
      model03.readDeviceNO(), dateHash["date"]!!, dateHash["time"]!!, model03.readStaffNO(), devision,
      viewModel01.inputedCd, viewModel01.inputedCn, viewModel01.inputedSz,
      "00000000000", viewModel01.inputedLocation, amt, false,
    ) )

    if( BuildConfig.DEBUG ) Log.d( "APP-CollationReceiving", "登録" )

    try {
      model02.savePotData( "APPEND", devision, dataArray )
    } catch( e:Exception ) {
      val intent = Intent( context!!, Failure::class.java )
      intent.putExtra( "MESSAGE", e.message )
      startActivity( intent )
    }
  }
}

class CollationReceivingPage03:Fragment() {
  private lateinit var activity01:CollationReceiving

  private val binding01:CollationReceivingPage03Binding by dataBinding()
  private val viewModel01:VM_CollationReceivingPage03 by viewModels()

  override fun onCreateView( inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle? ):View? {
    return inflater.inflate( R.layout.collation_receiving_page03, container, false )
  }

  override fun onViewCreated( view:View, savedInstanceState:Bundle? ) {
    super.onViewCreated( view, savedInstanceState )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    try {
      activity01 = activity as CollationReceiving
    } catch( e:ClassCastException ) { throw ClassCastException( getString( R.string.err_communication03 ) ) }
  }

  override fun onResume() {
    super.onResume()

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )
  }
}
