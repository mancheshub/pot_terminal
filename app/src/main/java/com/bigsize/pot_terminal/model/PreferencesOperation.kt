package com.bigsize.pot_terminal.model

import android.content.Context.MODE_PRIVATE
import com.bigsize.pot_terminal.AppBase

class PreferencesOperation() {
  /**
   * 端末番号を保存します
   *
   * @param [deviceNO] 端末番号
   */
  public fun saveDeviceNO( deviceNO:String ) {
    val sharedPref = AppBase.app.getSharedPreferences( "com.bigsize.pot_terminal.is_preferences", MODE_PRIVATE )

    val editor = sharedPref.edit()

    if( deviceNO == "" ) {
      editor.remove("deviceNO").apply()
    } else {
      editor.putString( "deviceNO", deviceNO ).apply()
    }
  }

  /**
   * 端末番号を取得します
   *
   * @return 端末番号
   */
  public fun readDeviceNO():String {
    val sharedPref = AppBase.app.getSharedPreferences( "com.bigsize.pot_terminal.is_preferences", MODE_PRIVATE )

    return sharedPref.getString( "deviceNO", "" ) ?: ""
  }

  /**
   * スタッフ番号を保存します
   *
   * @param [staffNO] 端末番号
   */
  public fun saveStaffNO( staffNO:String ) {
    val sharedPref = AppBase.app.getSharedPreferences( "com.bigsize.pot_terminal.is_preferences", MODE_PRIVATE )

    val editor = sharedPref.edit()

    if( staffNO == "" ) {
      editor.remove("staffNO").apply()
    } else {
      editor.putString( "staffNO", staffNO ).apply()
    }
  }

  /**
   * スタッフ番号を取得します
   *
   * @return スタッフ番号
   */
  public fun readStaffNO():String {
    val sharedPref = AppBase.app.getSharedPreferences( "com.bigsize.pot_terminal.is_preferences", MODE_PRIVATE )

    return sharedPref.getString( "staffNO", "" ) ?: ""
  }
}
