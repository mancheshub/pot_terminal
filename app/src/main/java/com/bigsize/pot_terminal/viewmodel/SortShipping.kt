package com.bigsize.pot_terminal.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigsize.pot_terminal.model.HashItem
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class SortShipping:ViewModel() {
  private lateinit var device:BluetoothDevice

  private var socket:BluetoothSocket? = null
  private var outStream:OutputStream? = null
  private val uuid = "00001101-0000-1000-8000-00805F9B34FB"

  // ソケット通信状況
  private val _socketCondition:MutableLiveData<String> = MutableLiveData()
  public val socketCondition:LiveData<String> get() = _socketCondition

  // デバイス名・MACアドレス
  public val txtName:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }
  public val txtAddress:MutableLiveData<String> by lazy { MutableLiveData<String>( "" ) }

  // 検索されたデータ
  public var itemDataArray:MutableList<HashItem> = mutableListOf<HashItem>()

  init {}

  /**
   * デバイスとペアリングします
   */
  public fun pairing( bluetooth:BluetoothAdapter ) {
    viewModelScope.launch {
      try {
        // デバイスオブジェクトを取得します
        device = bluetooth.getRemoteDevice( txtAddress.value.toString() )

        // ソケットオブジェクトを取得します
        socket = device.createRfcommSocketToServiceRecord( UUID.fromString( uuid ) )
      } catch( e:Exception ) {
        socket?.close()
      }
    }
  }

  /**
   * デバイスにソケット接続します
   */
  public fun connect() {
    // ソケット接続中の場合は切断します
    if( ( socketCondition.value as String ) == "CONNECTED" ) disconnect()

    try {
      socket?.connect()

      _socketCondition.value = "CONNECTED"
    } catch( e:IOException ) {
      if( socket != null ) socket?.close()

      _socketCondition.value = "CONNERROR"
    }
  }

  /**
   * デバイスからソケット切断します
   */
  public fun disconnect() {
    try {
      if( socket != null ) socket?.close()

      _socketCondition.value = "DISCONNECT"
    } catch( e:IOException ) {
      socket = null

      _socketCondition.value = "CONNERROR"
    }
  }

  /**
   * デバイスに書き込みします
   */
  public fun write( buffer:ByteArray ) {
    // ストリームでデータを送ります
    socket?.outputStream!!.write( buffer, 0, buffer.size )
  }
}
