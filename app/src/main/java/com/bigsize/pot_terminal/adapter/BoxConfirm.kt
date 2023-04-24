package com.bigsize.pot_terminal.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.bigsize.pot_terminal.BuildConfig
import com.bigsize.pot_terminal.R
import com.bigsize.pot_terminal.model.PotDataModel01
import com.bigsize.pot_terminal.databinding.BoxConfirmListview01Binding
import com.bigsize.pot_terminal.model.PotDataModel03
import com.bigsize.pot_terminal.model.PotDivision

class BoxConfirm(val context:Context?,var itemList:MutableList<PotDataModel01> ):BaseAdapter() {
  private val inflater = LayoutInflater.from( context )

  override fun getCount():Int {
    return itemList.count()
  }

  override fun getItem( position:Int ):PotDataModel01 {
    return itemList[position]
  }

  override fun getItemId( position:Int ):Long {
    return position.toLong()
  }

  override fun getView( position:Int, convertView:View?, parent:ViewGroup ):View {
    var itemView:View? = convertView
    val potData:PotDataModel01 = getItem( position )
    lateinit var binding01:BoxConfirmListview01Binding

    // 表示部品をなければ作ってあれば再利用します

    if( itemView == null ) {
      binding01 = DataBindingUtil.inflate( inflater, R.layout.box_confirm_listview01, parent, false );
      itemView = binding01.root;

      // 1行レイアウトをバインドしたbinding01をViewのtagに保管します
      itemView.tag = binding01;
    } else {
      // 1行レイアウトのbinding01をtagから復元します
      binding01 = itemView!!.tag as BoxConfirmListview01Binding
    }

    binding01.text01.setTextColor( Color.BLACK )
    binding01.text02.setTextColor( Color.BLACK )
    binding01.amtN.setTextColor( Color.BLACK )
    binding01.amtP.setTextColor( Color.BLACK )
    binding01.cd.setTextColor( Color.BLACK )
    binding01.cn.setTextColor( Color.BLACK )
    binding01.sz.setTextColor( Color.BLACK )

    // 照合途中・照合完了の場合は背景色を変更します
    itemView.setBackgroundResource( R.drawable.border )
    if( potData.amt_n != "0" && potData.amt_n.toInt() == potData.amt_p.toInt() ) itemView.setBackgroundResource( R.drawable.line_finished )
    if( potData.amt_n != "0" && potData.amt_n.toInt() < potData.amt_p.toInt() ) itemView.setBackgroundResource( R.drawable.line_selected )

    // ViewModelをセットします
    binding01.viewmodel = potData

    return itemView!!
  }

  /**
   * アダプタデータを更新します
   */
  fun refreshItem( newItem:MutableList<PotDataModel01> ) {
    itemList = newItem

    // 内容の変更をListViewに通知します
    notifyDataSetChanged()
  }
}
