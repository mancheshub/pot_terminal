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
import com.bigsize.pot_terminal.model.HashItem
import com.bigsize.pot_terminal.databinding.SortShippingListview01Binding

class SortShipping( val context:Context?, var itemDataArray:MutableList<HashItem> ):BaseAdapter() {
  private val inflater = LayoutInflater.from( context )

  override fun getCount():Int {
    return itemDataArray.count()
  }

  override fun getItem( position:Int ):HashItem {
    return itemDataArray[position]
  }

  override fun getItemId( position:Int ):Long {
    return position.toLong()
  }

  override fun getView( position:Int, convertView:View?, parent:ViewGroup ):View {
    var itemView:View? = convertView
    val itemData:HashItem = getItem( position )
    lateinit var binding01:SortShippingListview01Binding

    // 表示部品をなければ作ってあれば再利用します

    if( itemView == null ) {
      binding01 = DataBindingUtil.inflate( inflater, R.layout.sort_shipping_listview01, parent, false );
      itemView = binding01.root;

      // 1行レイアウトをバインドしたbinding01をViewのtagに保管します
      itemView.tag = binding01;
    } else {
      // 1行レイアウトのbinding01をtagから復元します
      binding01 = itemView!!.tag as SortShippingListview01Binding
    }

    binding01.item.setTextColor( Color.BLACK )
    binding01.id.setTextColor( Color.BLACK )

    // ViewModelをセットします
    binding01.viewmodel = itemData

    return itemView!!
  }

  /**
   * 更新したアダプタデータを適用します
   *
   * @param newItem
   */
  fun refreshItem( newItem:MutableList<HashItem> ) {
    itemDataArray = newItem

    // 内容の変更をListViewに通知します
    notifyDataSetChanged()
  }
}
