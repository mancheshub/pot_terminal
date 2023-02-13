package com.bigsize.pot_terminal.model

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.media.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.R
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 無線LAN管理クラス
 */
class StatusWifi( val context:Context ) {
  private var wifi:WifiManager? = null

  init {
    wifi = context.getSystemService( WIFI_SERVICE ) as WifiManager
  }

  /**
   * 無線LANの接続状況を調べます
   *
   * 電波強度は -100 から 0 dBm で 0 に近づくほど強度が高くなります。
   *
   * @return 電波強度
   */
  public fun checkWifi():Int {
    val info:WifiInfo? = wifi?.getConnectionInfo()

    val rssi:Int = info?.rssi ?: -100

    return rssi
  }

  /**
   * 終了します
   */
  public fun release() {
    wifi = null
  }
}

/**
 * バイブレーション管理クラス
 */
class PlayVibration( val context:Context ) {
  private var vibration:Vibrator? = null

  init {
    vibration = context.getSystemService( Context.VIBRATOR_SERVICE ) as Vibrator
  }

  /**
   * 振動させます
   *
   * @param [rate] 振動パターン
   * @param [volume] 振動の大きさ
   */
  public fun play( rate:Array<String>, volume:Array<String> ) {
    val rateArray:LongArray = rate.map { it.toLong() }.toLongArray()
    val volumeArray:IntArray = volume.map { it.toInt() }.toIntArray()

    // 第一引数 : 振動パターン
    //            偶数インデックスは待ち時間、奇数インデックスは振動が発生する時間です
    // 第二引数 : 振動の大きさ
    // 第三引数 : 繰り返し指示
    //            -1 : 繰り返さない 0 : 繰り返す
    val vibrationEffect = VibrationEffect.createWaveform( rateArray!!, volumeArray, -1 )

    vibration?.vibrate( vibrationEffect )
  }

  /**
   * 終了します
   */
  public fun release() {
    vibration = null
  }
}

/**
 * サウンド管理クラス
 */
class PlaySound( var frequency:String, var sinDuration:String, var resDuration:String ) {
  private val SAMPLE_RATE:Int = 44100
  private val GAIN:Int = 1

  private var audioTrack:AudioTrack? = null
  private var sinBuffer:ShortArray? = null
  private var resBuffer:ShortArray? = null

  init {
    // 正弦波用のバッファを作ります
    createBufferSin()

    // 休符波用のバッファを作ります
    createBufferRes()

    // AudioTrackオブジェクトを作成します

    val attributes: AudioAttributes = AudioAttributes.Builder()
      .setLegacyStreamType( AudioManager.STREAM_MUSIC )
      .build()

    val format: AudioFormat = AudioFormat.Builder()
      .setEncoding( AudioFormat.ENCODING_PCM_16BIT )
      .setSampleRate( SAMPLE_RATE )
      .setChannelMask( AudioFormat.CHANNEL_OUT_MONO )
      .build()

    // AudioTrackをサンプリングレートで初期化します - MODE_STREAMをセットしてバッファへ書き込みながら再生します
    audioTrack = AudioTrack( attributes, format, SAMPLE_RATE, AudioTrack.MODE_STREAM, 0 )

    // ゲインの最大値を音量にセットします
    audioTrack?.setVolume( AudioTrack.getMaxVolume() )
  }

  /**
   * サウンドを再生します
   *
   * AudioTrackはplay()後stop()またはpause()をしない限りステータスがPLAYSTATE_PLAYINGのままなので注意します。
   */
  public fun play() {
    if( audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING ) {
      // 再生中の場合は停止します
      audioTrack?.stop()
    } else {
      // 再生を開始します
      audioTrack?.play()

      // 正弦波をSTREAMに書き込みながら再生します
      writeStreamSin()

      // 休符波を挟んで正弦波をSTREAMに書き込みながら再生します
      writeStreamRes()
    }
  }

  /**
   * サウンドを停止します
   */
  public fun stop() {
    if( audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING ) {
      audioTrack?.stop()
    }
  }

  /**
   * 終了します
   */
  public fun release() {
    stop()

    if( sinBuffer != null ) sinBuffer = null
    if( resBuffer != null ) resBuffer = null

    audioTrack?.release()
    audioTrack = null
  }

  /**
   * 正弦波用のバッファを作ります
   */
  private fun createBufferSin() {
    val sinBufferSize = sinDuration.toInt() * SAMPLE_RATE * 2 / 1000
    sinBuffer = ShortArray( sinBufferSize * 2 )

    val param: Double = ( 2.0 * Math.PI / ( SAMPLE_RATE / frequency.toDouble() ) )
    for( i in 0 until sinBufferSize ) {
      val value = ( sin( param * i ) * Short.MAX_VALUE * GAIN ).roundToInt().toLong()
      if( value > Short.MAX_VALUE ) {
        sinBuffer!![i] = Short.MAX_VALUE
      } else if( value < Short.MIN_VALUE ) {
        sinBuffer!![i] = Short.MIN_VALUE
      } else {
        sinBuffer!![i] = value.toShort()
      }
    }
  }

  /**
   * 休符波用のバッファを作ります
   */
  private fun createBufferRes() {
    if( resDuration == "0" ) return

    val resBufferSize = resDuration.toInt() * SAMPLE_RATE * 2 / 1000
    resBuffer = ShortArray( resBufferSize * 2 )

    for( i in 0 until resBufferSize ) {
      resBuffer!![i] = 0
    }
  }

  /**
   * 正弦波をSTREAMに書き込みながら再生します
   */
  private fun writeStreamSin() {
    audioTrack?.write( sinBuffer!!, 0, sinBuffer!!.size )
  }

  /**
   * 休符波を挟んで正弦波をSTREAMに書き込みながら再生します
   */
  private fun writeStreamRes() {
    if( resDuration == "0" ) return

    audioTrack?.write( resBuffer!!, 0, resBuffer!!.size )
    audioTrack?.write( sinBuffer!!, 0, sinBuffer!!.size )
    audioTrack?.write( resBuffer!!, 0, resBuffer!!.size )
    audioTrack?.write( sinBuffer!!, 0, sinBuffer!!.size )
  }
}
