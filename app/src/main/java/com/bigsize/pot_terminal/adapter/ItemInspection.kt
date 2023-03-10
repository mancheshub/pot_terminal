package com.bigsize.pot_terminal.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import com.bigsize.pot_terminal.R
import com.bigsize.pot_terminal.model.PotDataModel03
import com.bigsize.pot_terminal.databinding.ItemInspectionListview01Binding

class ItemInspection( val context:Context?, var itemList:MutableList<PotDataModel03> ):BaseAdapter() {
  private val inflater = LayoutInflater.from( context )

  override fun getCount():Int {
    return itemList.count()
  }

  override fun getItem( position:Int ):PotDataModel03 {
    return itemList[position]
  }

  override fun getItemId( position:Int ):Long {
    return position.toLong()
  }

  override fun getView( position:Int, convertView:View?, parent:ViewGroup ):View {
    var itemView:View? = convertView
    val potData:PotDataModel03 = getItem( position )
    lateinit var binding01:ItemInspectionListview01Binding

    // 表示部品をなければ作ってあれば再利用します

    if( itemView == null ) {
      binding01 = DataBindingUtil.inflate( inflater, R.layout.item_inspection_listview01, parent, false );
      itemView = binding01.root;

      // 1行レイアウトをバインドしたbinding01をViewのtagに保管します
      itemView.tag = binding01;
    } else {
      // 1行レイアウトのbinding01をtagから復元します
      binding01 = itemView!!.tag as ItemInspectionListview01Binding
    }

    // 検品途中・検品完了の場合は背景色を変更します
    itemView.setBackgroundColor( Color.rgb( 255, 255, 255 ) );
    if( potData.amt_n != "0" && potData.amt_n.toInt() == potData.amt_p.toInt() ) { itemView.setBackgroundColor( Color.rgb( 230, 230, 230 ) ); }
    if( potData.amt_n != "0" && potData.amt_n.toInt() < potData.amt_p.toInt() ) { itemView.setBackgroundColor( Color.rgb( 255, 255, 204 ) ); }

    // ViewModelをセットします
    binding01.viewmodel = potData

    return itemView!!
  }

  /**
   * アダプタデータを更新します
   *
   * @param newItem
   */
  fun refreshItem( newItem:MutableList<PotDataModel03> ) {
    itemList = newItem

    // 内容の変更をListViewに通知します
    notifyDataSetChanged()
  }
}
