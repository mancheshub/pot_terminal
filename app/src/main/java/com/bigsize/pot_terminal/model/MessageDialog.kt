package com.bigsize.pot_terminal.model

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bigsize.pot_terminal.*


class StaffNODialog():DialogFragment() {
  private lateinit var dialog:AlertDialog

  private val model01:PreferencesOperation = PreferencesOperation()

  override fun onCreateDialog( savedInstanceState:Bundle? ):Dialog {
    val builder = AlertDialog.Builder( activity )

    val txtView = TextView( activity )
    txtView.text = "ログアウト"
    txtView.textSize = 18f
    txtView.setTextColor( Color.WHITE )
    txtView.setBackgroundColor( Color.rgb( 74, 138, 245 ) )
    txtView.setPadding( 10,10,10,10 )
    txtView.gravity = Gravity.CENTER

    val inflater:LayoutInflater = requireActivity().layoutInflater
    val view:View = inflater.inflate( R.layout.dialog_staff, null )

    builder.setCustomTitle( txtView )
    builder.setView( view )

    view.findViewById<TextView>( R.id.txt_number ).text = "スタッフ番号 " + model01.readStaffNO() + " でログイン中 ..."

    builder.setNegativeButton( "閉じる" ) { dialog, id ->
      dialog.dismiss();
    }

    builder.setPositiveButton( "実行" ) { dialog, id ->
      val intent = Intent( activity, Entrance::class.java )
      startActivity( intent )

      // 全てのActivityを終了します
      AppBase.killApp()

      dialog.dismiss();
    }

    // 戻るボタンでダイアログを閉じないようにします
    setCancelable( false )

    dialog = builder.create()

    // ダイアログの外側をタップしてもダイアログを閉じないようにします
    dialog.setCanceledOnTouchOutside( false )

    // BUTTON_POSITIVEにフォーカスを当てます
    dialog.setOnShowListener {
      val negative:Button = dialog.getButton( DialogInterface.BUTTON_POSITIVE )
      negative.setFocusable( true )
      negative.setFocusableInTouchMode( true )
      negative.requestFocus()
    }

    return dialog
  }
}

class DeviceNODialog():DialogFragment() {
  private lateinit var dialog:AlertDialog

  private val model01:PreferencesOperation = PreferencesOperation()

  override fun onCreateDialog( savedInstanceState:Bundle? ):Dialog {
    val builder = AlertDialog.Builder( activity )

    val txtView = TextView( activity )
    txtView.text = "端末"
    txtView.textSize = 18f
    txtView.setTextColor( Color.WHITE )
    txtView.setBackgroundColor( Color.rgb( 74, 138, 245 ) )
    txtView.setPadding( 10,10,10,10 )
    txtView.gravity = Gravity.CENTER

    val inflater:LayoutInflater = requireActivity().layoutInflater
    val view:View = inflater.inflate( R.layout.dialog_device, null )

    builder.setCustomTitle( txtView )
    builder.setView( view )

    val txtNumber = view.findViewById<EditText>( R.id.txt_number )

    builder.setPositiveButton( "設定" ) { dialog, id ->
      try {
        model01.saveDeviceNO( txtNumber.text.toString() )
      } catch( e:Exception ) {
        val intent = Intent( activity, Failure::class.java )
        intent.putExtra( "MESSAGE", "端末番号を保存できませんでした。" )
        startActivity( intent )
      }

      dialog.dismiss();
    }

    builder.setNegativeButton( "閉じる" ) { dialog, id ->
      dialog.dismiss();
    }

    // 戻るボタンでダイアログを閉じないようにします
    setCancelable( false )

    dialog = builder.create()

    // ダイアログの外側をタップしてもダイアログを閉じないようにします
    dialog.setCanceledOnTouchOutside( false )

    return dialog
  }
}

/**
 * 汎用アクティビティダイアログを表示します
 *
 * @property [callbackType] コールバックメソッドの種類 - fromMessageDialog[callbackType]をコールバックとします
 * @property [title] ダイアログのタイトル
 * @property [message] ダイアログのテキスト
 * @property [yesText] ダイアログの YES テキスト
 * @property [canText] ダイアログの CANCEL テキスト
 */
class MessageDialog( val callbackType:String, val title:String, val message:String, val yesText:String, val canText:String ):DialogFragment() {
  private lateinit var listner:DialogCallback
  private lateinit var dialog:AlertDialog

  override fun onCreateDialog( savedInstanceState:Bundle? ):Dialog {
    val builder = AlertDialog.Builder( activity )

    if( title != "" ) {
      val txtView = TextView( activity )
      txtView.text = title
      txtView.textSize = 18f
      txtView.setTextColor( Color.WHITE )
      txtView.setBackgroundColor( Color.rgb( 74, 138, 245 ) )
      txtView.setPadding( 10,10,10,10 )
      txtView.gravity = Gravity.CENTER

      builder.setCustomTitle( txtView )
    }

    if( message != "" ) {
      val msgView = TextView( activity )
      msgView.text = message
      msgView.textSize = 15f
      msgView.setTextColor( Color.BLACK )
      msgView.setPadding( 20, 20, 20, 20 )

      builder.setView( msgView )
    }

    if( yesText != "" ) {
      builder.setPositiveButton( yesText ) { dialog, id ->
        if( callbackType != "00" ) listner.fromMessageDialog( callbackType )

        dialog.dismiss();
      }
    }

    if( canText != "" ) {
      builder.setNegativeButton( canText ) { dialog, id ->
        dialog.dismiss();
      }
    }

    // 戻るボタンでダイアログを閉じないようにします
    setCancelable( false )

    dialog = builder.create()

    // ダイアログの外側をタップしてもダイアログを閉じないようにします
    dialog.setCanceledOnTouchOutside( false )

    // ダイアログにフォーカスを当てません
    //dialog.window?.addFlags( WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE )

    // Activityのタッチ操作を禁止します
    //activity?.window?.addFlags( WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE )

    // BUTTON_POSITIVEにフォーカスを当てます
    dialog.setOnShowListener {
      val negative:Button = dialog.getButton( DialogInterface.BUTTON_POSITIVE )
      negative.setFocusable( true )
      negative.setFocusableInTouchMode( true )
      negative.requestFocus()
    }

    return dialog
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    // 呼び出し元のMainActivityをListenerに変換します

    try {
      listner = activity as DialogCallback
    } catch( e:ClassCastException ) {
      throw ClassCastException( context.toString() + " must implement NoticeDialogListener" )
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()

    // Activityのタッチ操作を許可します
    activity?.window?.clearFlags( WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE )
  }
}

/**
 * 汎用フラグメントダイアログを表示します
 *
 * @property [callbackType] コールバックメソッドの種類 - fromMessageDialog[callbackType]をコールバックとします
 * @property [title] ダイアログのタイトル
 * @property [message] ダイアログのテキスト
 * @property [yesText] ダイアログの YES テキスト
 * @property [canText] ダイアログの CANCEL テキスト
 */
class MessageDialogF( val callbackType:String, val title:String, val message:String, val yesText:String, val canText:String ):DialogFragment() {
  private lateinit var listner:DialogCallback
  private lateinit var dialog:AlertDialog

  override fun onCreateDialog( savedInstanceState:Bundle? ):Dialog {
    val builder = AlertDialog.Builder( activity )

    if( title != "" ) {
      val txtView = TextView( activity )
      txtView.text = title
      txtView.textSize = 18f
      txtView.setTextColor( Color.WHITE )
      txtView.setBackgroundColor( Color.rgb( 74, 138, 245 ) )
      txtView.setPadding( 10,10,10,10 )
      txtView.gravity = Gravity.CENTER

      builder.setCustomTitle( txtView )
    }

    if( message != "" ) {
      val msgView = TextView( activity )
      msgView.text = message
      msgView.textSize = 15f
      msgView.setTextColor( Color.BLACK )
      msgView.setPadding( 20, 20, 20, 20 )

      builder.setView( msgView )
    }

    if( yesText != "" ) {
      builder.setPositiveButton( yesText ) { dialog, id ->
        if( callbackType != "00" ) listner.fromMessageDialog( callbackType )

        dialog.dismiss();
      }
    }

    if( canText != "" ) {
      builder.setNegativeButton( canText ) { dialog, id ->
        dialog.dismiss();
      }
    }

    // 戻るボタンでダイアログを閉じないようにします
    setCancelable( false )

    dialog = builder.create()

    // ダイアログの外側をタップしてもダイアログを閉じないようにします
    dialog.setCanceledOnTouchOutside( false )

    // ダイアログにフォーカスを当てません
    //dialog.window?.addFlags( WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE )

    // Activityのタッチ操作を禁止します
    //activity?.window?.addFlags( WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE )

    // BUTTON_POSITIVEにフォーカスを当てます
    dialog.setOnShowListener {
      val negative:Button = dialog.getButton( DialogInterface.BUTTON_POSITIVE )
      negative.setFocusable( true )
      negative.setFocusableInTouchMode( true )
      negative.requestFocus()
    }

    return dialog
  }

  override fun onAttach( context:Context ) {
    super.onAttach( context )

    // 呼び出し元のMainActivityをListenerに変換します

    try {
      listner = parentFragment as DialogCallback
    } catch( e:ClassCastException ) {
      throw ClassCastException( context.toString() + " must implement NoticeDialogListener" )
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()

    // Activityのタッチ操作を許可します
    activity?.window?.clearFlags( WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE )
  }
}
