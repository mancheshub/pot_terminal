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
import com.bigsize.pot_terminal.R
import com.bigsize.pot_terminal.model.PotDataModel02
import com.bigsize.pot_terminal.databinding.DataConfirmListview01Binding

class DataConfirm( val context:Context?, var potDataArray:MutableList<PotDataModel02> ):BaseAdapter() {
  private val inflater = LayoutInflater.from( context )

  private val _chkCount:MutableLiveData<Int> = MutableLiveData( 0 )
  public val chkCount:LiveData<Int> get() = _chkCount

  override fun getCount():Int {
    return potDataArray.count()
  }

  override fun getItem( position:Int ):PotDataModel02 {
    return potDataArray[position]
  }

  override fun getItemId( position:Int ):Long {
    return position.toLong()
  }

  override fun getView( position:Int, convertView:View?, parent:ViewGroup ):View {
    var itemView:View? = convertView
    val potData:PotDataModel02 = getItem( position )
    lateinit var binding01:DataConfirmListview01Binding

    // 表示部品をなければ作ってあれば再利用します

    if( itemView == null ) {
      binding01 = DataBindingUtil.inflate( inflater, R.layout.data_confirm_listview01, parent, false );
      itemView = binding01.root;

      // 1行レイアウトをバインドしたbinding01をViewのtagに保管します
      itemView.tag = binding01;
    } else {
      // 1行レイアウトのbinding01をtagから復元します
      binding01 = itemView!!.tag as DataConfirmListview01Binding
    }

    binding01.time.setTextColor( Color.BLACK )
    binding01.cd.setTextColor( Color.BLACK )
    binding01.cn.setTextColor( Color.BLACK )
    binding01.sz.setTextColor( Color.BLACK )
    binding01.location01.setTextColor( Color.BLACK )
    binding01.location02.setTextColor( Color.BLACK )
    binding01.text01.setTextColor( Color.BLACK )
    binding01.text02.setTextColor( Color.BLACK )
    binding01.text04.setTextColor( Color.BLACK )
    binding01.text05.setTextColor( Color.BLACK )
    binding01.amt.setTextColor( Color.BLACK )

    binding01.check.setOnCheckedChangeListener{ _, isChecked ->
      val befChecked:Boolean = potData.isChecked
      val aftChecked:Boolean = isChecked

      potData.isChecked = isChecked
      potDataArray.set( position, potData )

      // チェックした行の数量を累計します
      if( befChecked != aftChecked && potData.isChecked == true ) _chkCount.value = chkCount.value!! + potData.amt.toInt()
      if( befChecked != aftChecked && potData.isChecked == false ) _chkCount.value = chkCount.value!! - potData.amt.toInt()
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
  fun refreshItem( newItem:MutableList<PotDataModel02> ) {
    potDataArray = newItem

    // 内容の変更をListViewに通知します
    notifyDataSetChanged()
  }
}
