package com.bigsize.pot_terminal.adapter

import android.content.Context
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
import com.bigsize.pot_terminal.model.PotDataModel04
import com.bigsize.pot_terminal.databinding.ExamLocationListview01Binding

class ExamLocation( val context:Context?, var itemList:MutableList<PotDataModel04> ):BaseAdapter() {
  private val inflater = LayoutInflater.from( context )

  override fun getCount():Int {
    return itemList.count()
  }

  override fun getItem( position:Int ):PotDataModel04 {
    return itemList[position]
  }

  override fun getItemId( position:Int ):Long {
    return position.toLong()
  }

  override fun getView( position:Int, convertView:View?, parent:ViewGroup ):View {
    var itemView:View? = convertView
    val potData:PotDataModel04 = getItem( position )
    lateinit var binding01:ExamLocationListview01Binding

    // 表示部品をなければ作ってあれば再利用します

    if( itemView == null ) {
      binding01 = DataBindingUtil.inflate( inflater, R.layout.exam_location_listview01, parent, false );
      itemView = binding01.root;

      // 1行レイアウトをバインドしたbinding01をViewのtagに保管します
      itemView.tag = binding01;
    } else {
      // 1行レイアウトのbinding01をtagから復元します
      binding01 = itemView!!.tag as ExamLocationListview01Binding
    }

    // ViewModelをセットします
    binding01.viewmodel = potData

    return itemView!!
  }

  /**
   * 更新したアダプタデータを適用します
   *
   * @param newItem
   */
  fun refreshItem( newItem:MutableList<PotDataModel04> ) {
    itemList = newItem

    // 内容の変更をListViewに通知します
    notifyDataSetChanged()
  }
}
