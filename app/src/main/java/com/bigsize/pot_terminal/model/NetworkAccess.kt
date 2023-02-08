package com.bigsize.pot_terminal.model

import android.content.Intent
import android.util.Log
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.Failure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
data class APIResponse(
  val status:String,
  val message:String
)

class POTAccessAPI {
  private val httpClient = OkHttpClient.Builder()
    .connectTimeout( 10, TimeUnit.SECONDS )
    .writeTimeout( 10, TimeUnit.SECONDS )
    .readTimeout( 30, TimeUnit.SECONDS )
    .build()

  /**
   * POTデータの転送処理を開始します
   *
   * @param [transferURL] POTデータを受け取るサーバプログラムのURL
   * @param [potType] POT種別＝POT区分＋端末番号
   * @param [fileKey] POTファイル名に含む一意文字列
   */
  suspend fun startPOTData( transferURL:String, potType:String, fileKey:String ) {
    if( BuildConfig.DEBUG ) Log.d( "APP-NetworkAccess", "POTデータ転送開始" )

    // フォームデータを作成します

    val formHash:MutableMap<String,String> = mutableMapOf()
    val formBuilder:FormBody.Builder = FormBody.Builder()

    formHash["mode"] = "start"
    formHash["potType"] = potType
    formHash["fileKey"] = fileKey

    formHash.forEach { name:String, value:String -> formBuilder.add( name, value ) }

    val request = Request.Builder()
      .url( transferURL )
      .post( formBuilder.build() )
      .build()

    val resJSON = withContext( Dispatchers.IO ) {
      httpClient.newCall( request ).execute().use { response ->
        if( response.isSuccessful ) {
          response.body?.string()
        } else {
          ""
        }
      }
    }

    val apiResponse:APIResponse = Json.decodeFromString<APIResponse>( resJSON!! )

    if( BuildConfig.DEBUG ) Log.d( "APP-NetworkAccess", "startPOTDataデータ = " + apiResponse.message )
  }

  /**
   * POTデータの転送処理を実施します
   *
   * @param [transferURL] POTデータを受け取るサーバプログラムのURL
   * @param [potType] POT種別＝POT区分＋端末番号
   * @param [fileKey] POTファイル名に含む一意文字列
   * @param [potData] POTデータ
   */
  suspend fun execPOTData( transferURL:String, potType:String, fileKey:String, potData:String ) {
    if( BuildConfig.DEBUG ) Log.d( "APP-NetworkAccess", "POTデータ転送実施" )

    // フォームデータを作成します

    val formHash:MutableMap<String,String> = mutableMapOf()
    val formBuilder:FormBody.Builder = FormBody.Builder()

    formHash["mode"] = "continue"
    formHash["potType"] = potType
    formHash["fileKey"] = fileKey
    formHash["potData"] = potData

    formHash.forEach { name:String, value:String -> formBuilder.add( name, value ) }

    val request = Request.Builder()
      .url( transferURL )
      .post( formBuilder.build() )
      .build()

    val resJSON = withContext( Dispatchers.IO ) {
      httpClient.newCall( request ).execute().use { response ->
        if( response.isSuccessful ) {
          response.body?.string()
        } else {
          ""
        }
      }
    }

    val apiResponse:APIResponse = Json.decodeFromString<APIResponse>( resJSON!! )

    if( BuildConfig.DEBUG ) Log.d( "APP-NetworkAccess", "execPOTDataデータ = " + apiResponse.message )
  }

  /**
   * POTデータの転送処理を終了します
   *
   * @param [transferURL] POTデータを受け取るサーバプログラムのURL
   * @param [potType] POT種別＝POT区分＋端末番号
   * @param [fileKey] POTファイル名に含む一意文字列
   */
  suspend fun finishPOTData( transferURL:String, potType:String, fileKey:String ) {
    if( BuildConfig.DEBUG ) Log.d( "APP-NetworkAccess", "POTデータ転送終了" )

    // フォームデータを作成します

    val formHash:MutableMap<String,String> = mutableMapOf()
    val formBuilder:FormBody.Builder = FormBody.Builder()

    formHash["mode"] = "finish"
    formHash["potType"] = potType
    formHash["fileKey"] = fileKey

    formHash.forEach { name:String, value:String -> formBuilder.add( name, value ) }

    val request = Request.Builder()
      .url( transferURL )
      .post( formBuilder.build() )
      .build()

    val resJSON = withContext( Dispatchers.IO ) {
      httpClient.newCall( request ).execute().use { response ->
        if( response.isSuccessful ) {
          response.body?.string()
        } else {
          ""
        }
      }
    }

    val apiResponse:APIResponse = Json.decodeFromString<APIResponse>( resJSON!! )

    if( BuildConfig.DEBUG ) Log.d( "APP-NetworkAccess", "finishPOTDataデータ = " + apiResponse.message )
  }
}
