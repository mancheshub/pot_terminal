package com.bigsize.pot_terminal.model

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.Failure
import com.bigsize.pot_terminal.model.FileOperation
import com.bigsize.pot_terminal.R

class DeviceNODialog( val deviceNO:String ):DialogFragment() {
  private lateinit var dialog:AlertDialog

  private val model01:FileOperation = FileOperation()

  override fun onCreateDialog( savedInstanceState:Bundle? ):Dialog {
    val builder = AlertDialog.Builder( activity )

    val txtView = TextView( activity )
    txtView.text = "端末設定"
    txtView.textSize = 20f
    txtView.setTextColor( Color.WHITE )
    txtView.setBackgroundColor( Color.rgb(74,138,245) )
    txtView.setPadding( 5,5,5,5 )
    txtView.gravity = Gravity.CENTER

    val inflater:LayoutInflater = requireActivity().layoutInflater
    val view:View = inflater.inflate( R.layout.dialog_setting, null )

    builder.setCustomTitle( txtView )
    builder.setView( view )

    if( deviceNO == "" ) {

    builder.setPositiveButton( "設定" ) { dialog, id ->
      val txtNumber = view.findViewById<EditText>( R.id.txt_number )

      try {
        model01.saveDeviceNO( "OVERWRITE", txtNumber.text.toString() )
      } catch( e:Exception ) {
        val intent = Intent( activity, Failure::class.java )
        intent.putExtra( "MESSAGE", "端末番号を保存できませんでした。" )
        startActivity( intent )
      }

      dialog.dismiss();
    }

    builder.setNegativeButton( "キャンセル" ) { dialog, id ->
      dialog.dismiss();
    }

    }

    dialog = builder.create()

    return dialog
  }
}

class MessageDialog:DialogFragment() {
  private lateinit var dialog:AlertDialog

  override fun onCreateDialog( savedInstanceState:Bundle? ):Dialog {
    val builder = AlertDialog.Builder( activity )

    val txtView = TextView( activity )
    txtView.text = arguments!!.getString( "title", "" )
    txtView.textSize = 24f
    txtView.setTextColor( Color.WHITE )
    txtView.setBackgroundColor( Color.rgb(74,138,245) )
    txtView.setPadding( 10,10,10,10 )
    txtView.gravity = Gravity.CENTER

    val msgView = TextView( activity )
    msgView.text = arguments!!.getString( "message", "" )
    msgView.textSize = 16f
    msgView.setTextColor( Color.BLACK )
    msgView.setPadding( 15, 10, 10, 40 )

    builder.setCustomTitle( txtView )
    builder.setView( msgView )

    builder.setPositiveButton( "OK" ) { dialog, id ->
      if( BuildConfig.DEBUG ) Log.d( "APP-MessageDialog", "終了" )

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
