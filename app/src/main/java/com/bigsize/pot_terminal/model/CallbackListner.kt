package com.bigsize.pot_terminal.model

interface DialogCallback {
  fun fromMessageDialog( callbackType:String )
}

interface ScanCallback {
  fun readBox( scanBox:String? ):Boolean
  fun readItem( scanItem:String? ):Boolean
}
