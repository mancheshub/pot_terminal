package com.bigsize.pot_terminal.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.wada811.databinding.dataBinding
import com.bigsize.pot_terminal.R
import com.bigsize.pot_terminal.AppBase
import com.bigsize.pot_terminal.BoxOperation
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.model.AppUtility
import com.bigsize.pot_terminal.model.MessageDialog
import com.bigsize.pot_terminal.model.DialogCallback
import com.bigsize.pot_terminal.model.ScanCallback
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.databinding.BoxOperationPage01Binding
import com.bigsize.pot_terminal.databinding.BoxOperationPage02Binding
import com.bigsize.pot_terminal.viewmodel.BoxOperationPage01 as VM_BoxOperationPage01
import com.bigsize.pot_terminal.viewmodel.BoxOperationPage02 as VM_BoxOperationPage02
import com.bigsize.pot_terminal.adapter.BoxOperationPage01 as AD_BoxOperationPage01
import com.bigsize.pot_terminal.adapter.BoxOperationPage02 as AD_BoxOperationPage02

class BoxOperationPage01:Fragment(),DialogCallback,ScanCallback {
  private lateinit var activity01:BoxOperation

  private val binding01:BoxOperationPage01Binding by dataBinding()
  private val viewModel01:VM_BoxOperationPage01 by viewModels()

  private lateinit var adapter01:AD_BoxOperationPage01
  private lateinit var adapter02:ArrayAdapter<String>
  private lateinit var adapter03:ArrayAdapter<String>

  private val model01:AppUtility = AppUtility()

  private var dialogFIN:MessageDialog? = null
  private var dialogERR:MessageDialog? = null

  override fun onCreateView( inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle? ):View? {
    return inflater.inflate( R.layout.box_operation_page01, container, false )
  }

  override fun onViewCreated( view:View, savedInstanceState:Bundle? ) {
    super.onViewCreated( view, savedInstanceState )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    adapter01 = AD_BoxOperationPage01( context!!, mutableListOf() )
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

      regex = Regex( "FN" )
      if( regex.containsMatchIn( apiCondition ) == true ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE
      }
    })

    viewModel01.itemList.observe( this, Observer<MutableList<PotDataModel01>> {
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "商品データ内容更新" )

      // 全データ数とPOTで読んだデータ数を更新します
      viewModel01.cntRead.value = "0"
      viewModel01.cntTotal.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel01> ).sumBy { it.amt_p.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "全データ数 POTで読んだデータ数 = " + viewModel01.cntTotal.value + " " + viewModel01.cntRead.value  )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel01> ) )
    })
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    try {
      activity01 = activity as BoxOperation
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

    // 全データ数とPOTで読んだデータ数を更新します
    viewModel01.cntTotal.value = "0"
    viewModel01.cntRead.value = "0"

    // アダプタデータを更新します
    adapter01.refreshItem( mutableListOf<PotDataModel01>() )
  }

  override fun onDestroyView() {
    super.onDestroyView()

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
   * 箱を読んだ時の処理を定義します
   *
   * @param [scanBox] 読み取った箱QRデータ
   * @return 処理結果
   */
  override fun readBox( scanBox:String? ):Boolean {
    if( scanBox == null ) return false

    // 入力チェックを行います
    if( inputCheck( "01" ) == false ) return false

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

    // 箱ラベルから店舗名と商品を取得します
    viewModel01.pickItemList()

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

    lateinit var potData:PotDataModel01
    var position:Int = 0

    // 入力チェックを行います
    if( inputCheck( "02" ) == false ) return false

    // ダイアログが表示されていれば閉じます
    dialogFIN?.dismiss()
    dialogERR?.dismiss()

    var cd:String = scanItem.substring( 3, 13 );
    var cn:String = scanItem.substring( 14, 16 );
    var sz:String = scanItem.substring( 17, 21 );

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "品番 色番 サイズ = " + cd + " " + cn + " " + sz )

    // 該当商品を検索します
    position = ( viewModel01.itemList.value as MutableList<PotDataModel01> ).indexOfFirst { it.cd == model01.eightdigitsCd(cd) && it.cn == cn && it.sz == sz.replace(" ","") && it.amt_n.toInt() < it.amt_p.toInt() }

    if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "検索位置 = " + position.toString() )

    if( position == -1 ) {
      activity01.claimSound( activity01.playSoundNG )
      activity01.claimVibration( AppBase.vibrationNG )

      dialogERR = MessageDialog( "00", "", getString( R.string.err_box_operation01 ), "OK", "" )
      dialogERR?.show( parentFragmentManager, "simple" )
    } else {
      // 商品情報の検品数を更新します
      viewModel01.updateItemList( position )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel01> ) )

      // POTで読んだデータ数を更新します
      viewModel01.cntRead.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel01> ).sumBy { it.amt_n.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "POTで読んだ件数 = " + viewModel01.cntRead.value )

      // 終了したらその旨を表示します

      position = ( viewModel01.itemList.value as MutableList<PotDataModel01> ).indexOfFirst { it.amt_n.toInt() < it.amt_p.toInt() }
      if( position == -1 ) {
        activity01.claimSound( activity01.playSoundFN )
        activity01.claimVibration( AppBase.vibrationFN )

        dialogFIN = MessageDialog( "00", "完了", getString( R.string.msg_box_operation01 ), "OK", "" )
        dialogFIN?.show( parentFragmentManager, "simple" )
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
      msgError01 = getString( R.string.err_box_operation02 )
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

class BoxOperationPage02:Fragment(),ScanCallback {
  private lateinit var activity01:BoxOperation

  private val binding01:BoxOperationPage02Binding by dataBinding()
  private val viewModel01:VM_BoxOperationPage02 by viewModels()

  private lateinit var adapter01:AD_BoxOperationPage02

  override fun onCreateView( inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle? ):View? {
    return inflater.inflate( R.layout.box_operation_page02, container, false )
  }

  override fun onViewCreated( view:View, savedInstanceState:Bundle? ) {
    super.onViewCreated( view, savedInstanceState )

    // ■ バインディングしたレイアウトにデータをセットします

    binding01.viewmodel = viewModel01

    // ■ アダプタを初期化します

    adapter01 = AD_BoxOperationPage02( context!!, mutableListOf() )
    binding01.lstView01.adapter = adapter01

    // ■ 変更を補足します

    viewModel01.apiCondition.observe( this, Observer<String> {
      it ?: return@Observer

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

      // プログレスバーを消します - 警告終了

      if( apiCondition == "AL" ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        activity01.claimSound( activity01.playSoundNG )
        activity01.claimVibration( AppBase.vibrationNG )

        val dialog:MessageDialog = MessageDialog( "00", "警告", getString( R.string.alt_box_operation01 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      }

      // プログレスバーを消します - 正常終了

      if( apiCondition == "FN" ) {
        binding01.prgView01.visibility = android.widget.ProgressBar.INVISIBLE

        activity01.claimSound( activity01.playSoundFN )
        activity01.claimVibration( AppBase.vibrationFN )

        val dialog:MessageDialog = MessageDialog( "00", "完了", getString( R.string.msg_box_operation02 ), "OK", "" )
        dialog.show( parentFragmentManager, "simple" )
      }
    })

    viewModel01.itemList.observe( this, Observer<MutableList<PotDataModel01>> {
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "AAAA" )
      it ?: return@Observer
      if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "商品データ内容更新" )

      // 全データ数を更新します
      viewModel01.cntTotal.value = ( ( viewModel01.itemList.value as MutableList<PotDataModel01> ).sumBy { it.amt_p.toInt() } ).toString()

      if( BuildConfig.DEBUG ) Log.d( "APP-BoxOperation", "全データ数 = " + viewModel01.cntTotal.value  )

      // アダプタデータを更新します
      adapter01.refreshItem( ( viewModel01.itemList.value as MutableList<PotDataModel01> ) )
    })
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    try {
      activity01 = activity as BoxOperation
    } catch( e:ClassCastException ) { throw ClassCastException( getString( R.string.err_communication03 ) ) }
  }

  override fun onResume() {
    super.onResume()

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    // 箱ラベルと箱ラベル背景色をクリアします
    viewModel01.txtBoxno01.value = ""
    viewModel01.txtBoxno02.value = ""
    viewModel01.bkgBoxno01.value = "N"
    viewModel01.bkgBoxno02.value = "N"

    // 全データ数を更新します
    viewModel01.cntTotal.value = "0"

    // アダプタデータを更新します
    adapter01.refreshItem( mutableListOf<PotDataModel01>() )
  }

  /**
   * 箱を読んだ時の処理を定義します
   *
   * @param [scanBox] 読み取った箱QRデータ
   * @return 処理結果
   */
  override fun readBox( scanBox:String? ):Boolean {
    if( scanBox == null ) return false

    activity01.claimSound( activity01.playSoundOK )
    activity01.claimVibration( AppBase.vibrationOK )

    // すでに出箱と入箱が読まれていた場合はクリアします
    if( viewModel01.inputedBoxno01 != "" && viewModel01.inputedBoxno02 != "" ) {
      viewModel01.inputedBoxno01 = ""
      viewModel01.inputedBoxno02 = ""
      viewModel01.txtBoxno01.value = ""
      viewModel01.txtBoxno02.value = ""
      viewModel01.bkgBoxno01.value = "N"
      viewModel01.bkgBoxno02.value = "N"
    }

    if( viewModel01.inputedBoxno01 == "" && viewModel01.inputedBoxno02 == "" ) {
      // 付替前の箱ラベルを表示します
      viewModel01.txtBoxno01.value = scanBox.substring( 3 )

      // 付替前の箱ラベル背景色を決定します
      if( scanBox.substring( 3, 4 ) == "A" ) viewModel01.bkgBoxno01.value = "A"
      if( scanBox.substring( 3, 4 ) == "B" ) viewModel01.bkgBoxno01.value = "B"
      if( scanBox.substring( 3, 4 ) == "C" ) viewModel01.bkgBoxno01.value = "C"
      if( scanBox.substring( 3, 4 ) == "D" ) viewModel01.bkgBoxno01.value = "D"
      if( scanBox.substring( 3, 4 ) == "E" ) viewModel01.bkgBoxno01.value = "E"

      // 付替前の箱ラベルを記録します
      viewModel01.inputedBoxno01 = scanBox.substring( 3 )

      return true
    }

    if( viewModel01.inputedBoxno01 != "" && viewModel01.inputedBoxno02 == "" ) {
      // 付替後の箱ラベルを表示します
      viewModel01.txtBoxno02.value = scanBox.substring( 3 )

      // 付替後の箱ラベル背景色を決定します
      if( scanBox.substring( 3, 4 ) == "A" ) viewModel01.bkgBoxno02.value = "A"
      if( scanBox.substring( 3, 4 ) == "B" ) viewModel01.bkgBoxno02.value = "B"
      if( scanBox.substring( 3, 4 ) == "C" ) viewModel01.bkgBoxno02.value = "C"
      if( scanBox.substring( 3, 4 ) == "D" ) viewModel01.bkgBoxno02.value = "D"
      if( scanBox.substring( 3, 4 ) == "E" ) viewModel01.bkgBoxno02.value = "E"

      // 付替後の箱ラベルを記録します
      viewModel01.inputedBoxno02 = scanBox.substring( 3 )

      // 箱付替を実施します
      viewModel01.finishReplace()

      return true
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
    activity01.claimSound( activity01.playSoundNG )
    activity01.claimVibration( AppBase.vibrationNG )

    return true
  }
}
