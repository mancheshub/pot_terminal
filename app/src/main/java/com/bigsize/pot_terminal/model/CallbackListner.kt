package com.bigsize.pot_terminal.model

interface DialogCallback {
  fun fromMessageDialog( callbackType:String )
}

interface ScanCallback {
  fun readShelf( scanShelf:String? ):Boolean
  fun readBox( scanBox:String? ):Boolean
  fun readItem( scanItem:String? ):Boolean
}

interface KeyCallback {
  fun enterEvent():Boolean
}
