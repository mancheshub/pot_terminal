package com.bigsize.pot_terminal.model

import kotlinx.serialization.Serializable

class PotDivision( var name:String , var division:String, var fileName:String, var amt:String, var isChecked:Boolean ) {}
class HashItem( var id:String, var item:String ) {}
class AssortHistory( var cd:String, var cn:String, var sz:String, var boxno:String ) {}
class PotDataModel01( var cd:String, var cn:String, var sz:String, var amt_n:String, var amt_p:String ) {}
class PotDataModel02( var deviceNO:String, var date:String, var time:String, var staffNO:String, var mode:String, var cd:String, var cn:String, var sz:String, var location01:String, var location02:String, var amt:String, var isChecked:Boolean  ) {}
class PotDataModel03( var cd:String, var cn:String, var sz:String, var hcd:String, var hcn:String, var hcz:String, var asn24:String, var asn25:String, var asn53:String, var bf0:String, var amt_n:String, var amt_p:String ) {}
class PotDataModel04( var cd:String, var cn:String, var sz:String, var cs:String, var itn:String, var location:String, var amt_n:String, var amt_p:String ) {}
class PotDataModel05( var i_id:String, var cd:String, var cn:String, var sz:String, var boxno:String, var color:String, var amt_n:String, var amt_p:String ) {}

/**
 * -- APIとのデータ交換フォーマット
 */

@Serializable
data class APILineModel(
  val status:String,
  val text01:String,
  val text02:String,
  val text03:String,
)

// 選択項目データ交換モデル

@Serializable
data class APIHashItemModel(
  val status:String,
  val itemArray:List<APIHashPartModel>,
)

@Serializable
data class APIHashPartModel(
  val id:String,
  val item:String,
)

// はるやま商品データ交換モデル

@Serializable
data class APIFoelItemModel(
  val status:String,
  val itemArray:List<APIFoelPartModel>,
)

@Serializable
data class APIFoelPartModel(
  val cd:String,
  val cn:String,
  val sz:String,
  val asn21:String,
  val asn22:String,
  val asn23:String,
  val asn24:String,
  val asn25:String,
  val asn53:String,
  val bf0:String,
  val asn30_n:String,
  val asn30_p:String,
)

// マンチェス商品データ交換モデル

@Serializable
data class APIMcsItemModel(
  val status:String,
  val itemArray:List<APIMcsPartModel>,
)

@Serializable
data class APIMcsPartModel(
  val cd:String,
  val cn:String,
  val sz:String,
  val cs:String,
  val itn:String,
  val ssb:String,
  val ssh:String,
  val ssf:String,
  val sss:String,
  val sst:String,
  val sso:String,
  val ssa:String,
)
