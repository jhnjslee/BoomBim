package com.bb.boombim

    import android.app.Activity
    import android.content.Context
    import android.content.Intent
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.bb.boombim.data.ListLayout


class SearchListAdapter(val itemList: ArrayList<ListLayout>, mActivity: SearchActivity): RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {
        val SEARCH_CLICK_RESULT_CODE = 200
        var parentActivity : SearchActivity = mActivity
        lateinit var mContext : Context

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.search_result_layout, parent, false)

            mContext = parent.context
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

        override fun onBindViewHolder(holder: SearchListAdapter.ViewHolder, position: Int) {

            val layoutParams = holder.itemView.layoutParams
            layoutParams.height = 100
            holder.itemView.requestLayout()
            if (itemList[position].road == ""){
                holder.road.text = itemList[position].address
            }else{
                holder.road.text = itemList[position].road
            }
            holder.name.text = itemList[position].name


            if (itemList[position].distance == ""){
                holder.range.text = "?"
            }else{
                holder.range.text = itemList[position].road
            }
            // 아이템 클릭 이벤트

            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, position)
//                val array = arrayOf(itemList[position].name, itemList[position].address, itemList[position].road, itemList[position].x, itemList[position].y)
//                val intent : Intent  = Intent()
//                intent.putExtra("key", array)
//                parentActivity.setResult(SEARCH_CLICK_RESULT_CODE,intent)
//                parentActivity.finish()
                parentActivity.clickLocation()
            }
        }

        class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.search_name)
            val road: TextView = itemView.findViewById(R.id.search_road_name)
            val range: TextView = itemView.findViewById(R.id.range_to)
        }

        interface OnItemClickListener {
            fun onClick(v: View, position: Int)
        }

        fun setItemClickListener(onItemClickListener: OnItemClickListener) {
            this.itemClickListener = onItemClickListener
        }

        private lateinit var itemClickListener : OnItemClickListener
}