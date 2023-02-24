package com.bigsize.pot_terminal.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bigsize.pot_terminal.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class ItemInspectionAPI {
  private val model01:AppUtility = AppUtility()

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout( 10, TimeUnit.SECONDS )
    .writeTimeout( 10, TimeUnit.SECONDS )
    .readTimeout( 30, TimeUnit.SECONDS )
    .build()

  /**
   * 作業グループ情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @return 作業グループデータ
   */
  public suspend fun pickGroupList( accessURL:String ):MutableList<HashItem> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "G" )
      .build()

    val request = Request.Builder()
      .url( accessURL )
      .post( formBody )
      .build()

    val resJSON = withContext( Dispatchers.IO ) {
      httpClient.newCall( request ).execute().use { response ->
        if( ! response.isSuccessful ) { throw IOException( "$response" ) }

        response.body?.string()
      }
    }

    var tempList:MutableList<HashItem> = mutableListOf()
    val apiResponseBody:APIHashItemModel = Json.decodeFromString<APIHashItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( HashItem( it.id, it.item ) )
    }

    return tempList
  }

  /**
   * 店舗情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [groupID] 作業グループID
   * @return 店舗データ
   */
  public suspend fun pickShopList( accessURL:String, groupID:String ):MutableList<HashItem> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "S" )
      .add( "groupID", groupID )
      .build()

    val request = Request.Builder()
      .url( accessURL )
      .post( formBody )
      .build()

    val resJSON = withContext( Dispatchers.IO ) {
      httpClient.newCall( request ).execute().use { response ->
        if( ! response.isSuccessful ) { throw IOException( "$response" ) }

        response.body?.string()
      }
    }

    val apiResponseBody:APIHashItemModel = Json.decodeFromString<APIHashItemModel>( resJSON!! )

    val tempList:MutableList<HashItem> = mutableListOf()

    apiResponseBody.itemArray.forEach {
      tempList.add( HashItem( it.id, it.item ) )
    }

    return tempList
  }

  /**
   * 商品情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [groupID] 作業グループID
   * @param [shopID] 店舗ID
   * @return 店舗データ
   */
  public suspend fun pickItemList( accessURL:String, groupID:String, shopID:String ):MutableList<PotDataModel03> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "I" )
      .add( "groupID", groupID )
      .add( "shopID", shopID )
      .build()

    val request = Request.Builder()
      .url( accessURL )
      .post( formBody )
      .build()

    val resJSON = withContext( Dispatchers.IO ) {
      httpClient.newCall( request ).execute().use { response ->
        if( ! response.isSuccessful ) { throw IOException( "$response" ) }

        response.body?.string()
      }
    }

    val apiResponseBody:APIFoelItemModel = Json.decodeFromString<APIFoelItemModel>( resJSON!! )

    val tempList:MutableList<PotDataModel03> = mutableListOf()

    apiResponseBody.itemArray.forEach {
      tempList.add( PotDataModel03( model01.eightdigitsCd(it.cd), it.cn, it.sz, it.asn21, it.asn22, it.asn23, it.asn24, it.asn25, it.asn53, it.bf0, it.asn30_n.toInt().toString(), it.asn30_p.toInt().toString() ) )
    }

    return tempList
  }

  /**
   * 更新処理を行います
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [kind] 処理区分 01 : クリア 02 : 箱確定 03 : 確定
   * @param [groupID] 作業グループID
   * @param [shopID] 店舗ID
   * @param [boxID] 箱ID
   * @param [printID] 印刷機ID
   * @param [itemList] 商品データ
   * @return 店舗データ
   */
  public suspend fun deceded( accessURL:String, kind:String, groupID:String, shopID:String, boxID:String, printID:String, itemList:MutableList<PotDataModel03> ) {
    // ■ itemListをシリアライズ可能な形式に変換します

    var resItemPart:MutableList<APIFoelPartModel> = mutableListOf()

    itemList.forEach {
      resItemPart.add( APIFoelPartModel( it.cd, it.cn, it.sz, it.hcd, it.hcn, it.hcz, it.asn24, it.asn25, it.asn53, it.bf0, it.amt_n, it.amt_p ) )
    }

    val reqJSON = Json.encodeToString( APIFoelItemModel( "OK", resItemPart ) )

    // ■ APIリクエストを送ります

    val formBody = FormBody.Builder()
      .add( "mode", "U" )
      .add( "kind", kind )
      .add( "groupID", groupID )
      .add( "shopID", shopID )
      .add( "boxID", boxID )
      .add( "printID", printID )
      .add( "itemList", reqJSON )
      .build()

    val request = Request.Builder()
      .url( accessURL )
      .post( formBody )
      .build()

    val resJSON = withContext( Dispatchers.IO ) {
      httpClient.newCall( request ).execute().use { response ->
        if( ! response.isSuccessful ) { throw IOException( "$response" ) }

        response.body?.string()
      }
    }

    val apiResponseBody:APITextModel = Json.decodeFromString<APITextModel>( resJSON!! )
  }

  /**
   * 排他処理を行います
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [kind] 処理区分 01 : クリア 02 : 箱確定 03 : 確定
   * @param [groupID] 作業グループID
   * @param [shopID] 店舗ID
   * @param [staffID] スタッフID
   * @return 排他結果
   */
  public suspend fun updateSituation( accessURL:String, kind:String, groupID:String, shopID:String, staffID:String ):String {
    val formBody = FormBody.Builder()
      .add( "mode", "U" )
      .add( "kind", kind )
      .add( "groupID", groupID )
      .add( "shopID", shopID )
      .add( "staffID", staffID )
      .build()

    val request = Request.Builder()
      .url( accessURL )
      .post( formBody )
      .build()

    val resJSON = withContext( Dispatchers.IO ) {
      httpClient.newCall( request ).execute().use { response ->
        if( ! response.isSuccessful ) { throw IOException( "$response" ) }

        response.body?.string()
      }
    }

    val apiResponseBody:APITextModel = Json.decodeFromString<APITextModel>( resJSON!! )

    return apiResponseBody.text
  }

}

class DataTransferAPI {
  private val httpClient = OkHttpClient.Builder()
    .connectTimeout( 10, TimeUnit.SECONDS )
    .writeTimeout( 10, TimeUnit.SECONDS )
    .readTimeout( 30, TimeUnit.SECONDS )
    .build()

  /**
   * POTデータの転送処理を開始します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [potType] POT種別＝POT区分＋端末番号
   * @param [fileKey] POTファイル名に含む一意文字列
   */
  public suspend fun startPOTData( accessURL:String, potType:String, fileKey:String ) {
    if( BuildConfig.DEBUG ) Log.d( "APP-NetworkAccess", "POTデータ転送開始" )

    // フォームデータを作成します

    val formHash:MutableMap<String,String> = mutableMapOf()
    val formBuilder:FormBody.Builder = FormBody.Builder()

    formHash["mode"] = "start"
    formHash["potType"] = potType
    formHash["fileKey"] = fileKey

    formHash.forEach { name:String, value:String -> formBuilder.add( name, value ) }

    val request = Request.Builder()
      .url( accessURL )
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

    val apiResponse:APITextModel = Json.decodeFromString<APITextModel>( resJSON!! )
  }

  /**
   * POTデータの転送処理を実施します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [potType] POT種別＝POT区分＋端末番号
   * @param [fileKey] POTファイル名に含む一意文字列
   * @param [potData] POTデータ
   */
  public suspend fun execPOTData( accessURL:String, potType:String, fileKey:String, potData:String ) {
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
      .url( accessURL )
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

    val apiResponse:APITextModel = Json.decodeFromString<APITextModel>( resJSON!! )
  }

  /**
   * POTデータの転送処理を終了します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [potType] POT種別＝POT区分＋端末番号
   * @param [fileKey] POTファイル名に含む一意文字列
   */
  public suspend fun finishPOTData( accessURL:String, potType:String, fileKey:String ) {
    if( BuildConfig.DEBUG ) Log.d( "APP-NetworkAccess", "POTデータ転送終了" )

    // フォームデータを作成します

    val formHash:MutableMap<String,String> = mutableMapOf()
    val formBuilder:FormBody.Builder = FormBody.Builder()

    formHash["mode"] = "finish"
    formHash["potType"] = potType
    formHash["fileKey"] = fileKey

    formHash.forEach { name:String, value:String -> formBuilder.add( name, value ) }

    val request = Request.Builder()
      .url( accessURL )
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

    val apiResponse:APITextModel = Json.decodeFromString<APITextModel>( resJSON!! )
  }
}
