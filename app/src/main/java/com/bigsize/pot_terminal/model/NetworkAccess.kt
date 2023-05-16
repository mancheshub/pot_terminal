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

class CollationReceivingAPI {
  private val model01:AppUtility = AppUtility()

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout( 10, TimeUnit.SECONDS )
    .writeTimeout( 10, TimeUnit.SECONDS )
    .readTimeout( 30, TimeUnit.SECONDS )
    .build()

  /**
   * 返品入庫の単位データを取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [floorID] 階ID
   * @return 伝発グループデータ
   */
  public suspend fun pickH_UnitList( accessURL:String, floorID:String ):Pair<String,MutableList<HashItem>> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "HU" )
      .add( "floorID", floorID )
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

    val tempList:MutableList<HashItem> = mutableListOf()
    val apiResponseBody:APIHashItemModel = Json.decodeFromString<APIHashItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( HashItem( it.id, it.item ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * 返品入庫対象となる商品データを取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [floorID] 階ID
   * @param [unitID] 単位ID
   * @return 商品データ
   */
  public suspend fun pickH_ItemList( accessURL:String, floorID:String, unitID:String ):Pair<String,MutableList<PotDataModel04>> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "HI" )
      .add( "floorID", floorID )
      .add( "unitID", unitID )
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

    val tempList:MutableList<PotDataModel04> = mutableListOf()
    val apiResponseBody:APIMcsItemModel = Json.decodeFromString<APIMcsItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      val location:String = it.ssb + it.ssh + it.ssf + "-" + it.sss + "-" + it.sst + "-" + it.sso

      tempList.add( PotDataModel04( model01.eightdigitsCd(it.cd), it.cn.padStart( 2, '0' ), it.sz, it.cs, it.itn, location, "0", it.ssa ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * Fキャンセル入庫対象となる商品データを取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [floorID] 階ID
   * @return 商品データ
   */
  public suspend fun pickF_ItemList( accessURL:String, floorID:String ):Pair<String,MutableList<PotDataModel04>> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "FI" )
      .add( "floorID", floorID )
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

    val tempList:MutableList<PotDataModel04> = mutableListOf()
    val apiResponseBody:APIMcsItemModel = Json.decodeFromString<APIMcsItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      val location:String = it.ssb + it.ssh + it.ssf + "-" + it.sss + "-" + it.sst + "-" + it.sso

      tempList.add( PotDataModel04( model01.eightdigitsCd(it.cd), it.cn.padStart( 2, '0' ), it.sz, it.cs, it.itn, location, "0", it.ssa ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }
}

class BoxOperationAPI {
  private val model01:AppUtility = AppUtility()

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout( 10, TimeUnit.SECONDS )
    .writeTimeout( 10, TimeUnit.SECONDS )
    .readTimeout( 30, TimeUnit.SECONDS )
    .build()

  /**
   * 箱ラベルの情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [boxno] 物理箱ラベル
   * @return 店舗名
   */
  public suspend fun pickBoxInfomation( accessURL:String, boxno:String ):Pair<String,String> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "S" )
      .add( "boxno", boxno )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
  }

  /**
   * 箱ラベルからから商品を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [boxno] 物理箱ラベル
   * @return 商品データ
   */
  public suspend fun pickItemList( accessURL:String, boxno:String ):Pair<String,MutableList<PotDataModel01>> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "I" )
      .add( "boxno", boxno )
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

    val tempList:MutableList<PotDataModel01> = mutableListOf()
    val apiResponseBody:APIMcsItemModel = Json.decodeFromString<APIMcsItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( PotDataModel01( model01.eightdigitsCd(it.cd), it.cn.padStart( 2, '0' ), it.sz, "0", it.ssa ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * 箱付替を完了します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [boxno01] 付替前箱ラベル
   * @param [boxno02] 付替後箱ラベル
   * @return エラー状況
   */
  public suspend fun finishReplace( accessURL:String, boxno01:String, boxno02:String ):Pair<String,String> {
    val formBody = FormBody.Builder()
      .add( "mode", "U" )
      .add( "kind", "01" )
      .add( "boxno01", boxno01 )
      .add( "boxno02", boxno02 )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
  }
}

class LocationConfirmAPI {
  private val model01:AppUtility = AppUtility()

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout( 10, TimeUnit.SECONDS )
    .writeTimeout( 10, TimeUnit.SECONDS )
    .readTimeout( 30, TimeUnit.SECONDS )
    .build()

  /**
   * 品番・色番・サイズから商品のロケーションを取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [cd] 品番
   * @param [cn] 色番
   * @param [sz] サイズ
   *
   * @return ロケーションデータ
   */
  public suspend fun pickLocation( accessURL:String, cd:String, cn:String, sz:String ):Pair<String,MutableList<PotDataModel04>> {
    val formBody = FormBody.Builder()
      .add( "cd", cd )
      .add( "cn", cn )
      .add( "sz", sz )
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

    val tempList:MutableList<PotDataModel04> = mutableListOf()
    val apiResponseBody:APIMcsItemModel = Json.decodeFromString<APIMcsItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      val location:String = it.ssb + it.ssh + it.ssf + "-" + it.sss + "-" + it.sst + "-" + it.sso

      tempList.add( PotDataModel04( model01.eightdigitsCd(it.cd), it.cn.padStart( 2, '0' ), it.sz, it.cs, it.itn, location, "0", it.ssa ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }
}

class BoxReceivingAPI {
  private val model01:AppUtility = AppUtility()

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout( 10, TimeUnit.SECONDS )
    .writeTimeout( 10, TimeUnit.SECONDS )
    .readTimeout( 30, TimeUnit.SECONDS )
    .build()

  /**
   * 箱ラベルを決定します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [cd] 品番
   * @param [cn] 色番
   * @param [sz] サイズ
   * @return 箱ラベルデータ
   */
  public suspend fun pickBoxNO( accessURL:String, cd:String, cn:String, sz:String ):Pair<String,String> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "B" )
      .add( "cd", cd )
      .add( "cn", cn )
      .add( "sz", sz )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
  }
}

class BoxShippingAPI {
  private val model01:AppUtility = AppUtility()

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout( 10, TimeUnit.SECONDS )
    .writeTimeout( 10, TimeUnit.SECONDS )
    .readTimeout( 30, TimeUnit.SECONDS )
    .build()

  /**
   * 伝発グループ情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @return 伝発グループデータ
   */
  public suspend fun pickGroupList( accessURL:String ):Pair<String,MutableList<HashItem>> {
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

    val tempList:MutableList<HashItem> = mutableListOf()
    val apiResponseBody:APIHashItemModel = Json.decodeFromString<APIHashItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( HashItem( it.id, it.item ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * 店舗情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [groupID] 伝発グループID
   * @return 店舗データ
   */
  public suspend fun pickShopList( accessURL:String, groupID:String ):Pair<String,MutableList<HashItem>> {
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

    val tempList:MutableList<HashItem> = mutableListOf()
    val apiResponseBody:APIHashItemModel = Json.decodeFromString<APIHashItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( HashItem( it.id, it.item ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * 商品情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [groupID] 伝発グループID
   * @param [shopID] 店舗ID
   * @return 商品データ
   */
  public suspend fun pickItemList( accessURL:String, groupID:String, shopID:String ):Pair<String,MutableList<PotDataModel01>> {
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

    val tempList:MutableList<PotDataModel01> = mutableListOf()
    val apiResponseBody:APIMcsItemModel = Json.decodeFromString<APIMcsItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( PotDataModel01( model01.eightdigitsCd(it.cd), it.cn.padStart( 2, '0' ), it.sz, "0", it.ssa ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * キャンセル商品情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @return 商品データ
   */
  public suspend fun pickCancelItemList( accessURL:String ):Pair<String,MutableList<PotDataModel05>> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "C" )
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

    val tempList:MutableList<PotDataModel05> = mutableListOf()
    val apiResponseBody:APIMcsItemModel = Json.decodeFromString<APIMcsItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( PotDataModel05( it.ssb, model01.eightdigitsCd(it.cd), it.cn.padStart( 2, '0' ), it.sz, it.ssh, it.ssf, "0", it.ssa ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * 先送商品情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @return 商品データ
   */
  public suspend fun pickPostponeItemList( accessURL:String ):Pair<String,MutableList<PotDataModel05>> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "P" )
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

    val tempList:MutableList<PotDataModel05> = mutableListOf()
    val apiResponseBody:APIMcsItemModel = Json.decodeFromString<APIMcsItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( PotDataModel05( it.ssb, model01.eightdigitsCd(it.cd), it.cn.padStart( 2, '0' ), it.sz, it.ssh, it.ssf, "0", it.ssa ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * 店舗の箱ラベルを取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [shopID] 店舗ID
   * @return 箱ラベルデータ
   */
  public suspend fun pickBoxNO( accessURL:String, shopID:String ):Pair<String,String> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "B" )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
  }

  /**
   * 照合箱出を完了します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [groupID] 伝発グループID
   * @param [shopID] 店舗ID
   * @return エラー状況
   */
  public suspend fun finishShipping( accessURL:String, groupID:String, shopID:String ):Pair<String,String> {
    val formBody = FormBody.Builder()
      .add( "mode", "U" )
      .add( "kind", "01" )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
  }

  /**
   * キャンセル箱出を完了します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [i_id] 明細番号
   * @return エラー状況
   */
  public suspend fun finishCancelShipping( accessURL:String, i_id:String ):Pair<String,String> {
    val formBody = FormBody.Builder()
      .add( "mode", "U" )
      .add( "kind", "02" )
      .add( "i_id", i_id )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
  }

  /**
   * 先送箱出を完了します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [i_id] 明細番号
   * @return エラー状況
   */
  public suspend fun finishPostponeShipping( accessURL:String, i_id:String ):Pair<String,String> {
    val formBody = FormBody.Builder()
      .add( "mode", "U" )
      .add( "kind", "03" )
      .add( "i_id", i_id )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
  }
}

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
  public suspend fun pickGroupList( accessURL:String ):Pair<String,MutableList<HashItem>> {
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

    val tempList:MutableList<HashItem> = mutableListOf()
    val apiResponseBody:APIHashItemModel = Json.decodeFromString<APIHashItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( HashItem( it.id, it.item ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * 店舗情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [groupID] 作業グループID
   * @return 店舗データ
   */
  public suspend fun pickShopList( accessURL:String, groupID:String ):Pair<String,MutableList<HashItem>> {
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

    val tempList:MutableList<HashItem> = mutableListOf()
    val apiResponseBody:APIHashItemModel = Json.decodeFromString<APIHashItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( HashItem( it.id, it.item ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * 商品情報を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [groupID] 作業グループID
   * @param [shopID] 店舗ID
   * @return 店舗データ
   */
  public suspend fun pickItemList( accessURL:String, groupID:String, shopID:String ):Pair<String,MutableList<PotDataModel03>> {
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

    val tempList:MutableList<PotDataModel03> = mutableListOf()
    val apiResponseBody:APIFoelItemModel = Json.decodeFromString<APIFoelItemModel>( resJSON!! )

    apiResponseBody.itemArray.forEach {
      tempList.add( PotDataModel03( model01.eightdigitsCd(it.cd), it.cn, it.sz, it.asn21, it.asn22, it.asn23, it.asn24, it.asn25, it.asn53, it.bf0, it.asn30_n.toInt().toString(), it.asn30_p.toInt().toString() ) )
    }

    return Pair( apiResponseBody.status, tempList )
  }

  /**
   * 検品担当状況を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [groupID] 作業グループID
   * @param [shopID] 店舗ID
   * @param [staffID] スタッフID
   * @return 店舗データ
   */
  public suspend fun pickSICondition( accessURL:String, groupID:String, shopID:String, staffID:String ):Pair<String,String> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "SI" )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
  }

  /**
   * SCMラベル印刷状況を取得します
   *
   * @param [accessURL] サーバプログラムのURL
   * @param [groupID] 作業グループID
   * @param [shopID] 店舗ID
   * @return 店舗データ
   */
  public suspend fun pickSMCondition( accessURL:String, groupID:String, shopID:String ):Pair<String,String> {
    val formBody = FormBody.Builder()
      .add( "mode", "S" )
      .add( "kind", "SCM" )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )
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
  public suspend fun updateSituation( accessURL:String, kind:String, groupID:String, shopID:String, staffID:String ):Pair<String,String> {
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )

    return Pair( apiResponseBody.status, apiResponseBody.text01 )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )
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

    val apiResponseBody:APILineModel = Json.decodeFromString<APILineModel>( resJSON!! )
  }
}
