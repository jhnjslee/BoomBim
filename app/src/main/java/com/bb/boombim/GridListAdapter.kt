package com.bb.boombim

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bb.boombim.data.MenuList


class GridListAdapter(private val context: Context) : BaseAdapter() {

    private val images = arrayOf(
            R.drawable.ic_heart,
            R.drawable.ic_heart,
            R.drawable.ic_heart,
            R.drawable.ic_heart,
            R.drawable.ic_heart
    )


    var items : ArrayList<MenuList> = arrayListOf()

    fun addItem(item: String, image: Drawable){
        val ml = MenuList(item, image)
        items.add(ml)

//        items.add(item,image)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val imageItem : ImageView
        var tempView = convertView
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tempView = inflater.inflate(R.layout.grid_item, parent, false)

        var textView: TextView? = tempView.findViewById(R.id.menutext)
        textView?.setText(items.get(position).title)

        var image : ImageView? = tempView.findViewById(R.id.menuicon)
        image?.setImageDrawable(items.get(position).menuImage)
        tempView.setOnClickListener({
            Log.d("menu","${items.get(position).title}")
        })

//
//        if(convertView == null){
//            imageItem = ImageView(context)
//            imageItem.run {
//                layoutParams = ViewGroup.LayoutParams(200, 200)
//                scaleType = ImageView.ScaleType.FIT_CENTER
//                setPadding(0, 0, 0, 0)
//            }
//        }else{
//            imageItem = convertView as ImageView
//        }
//        imageItem.setImageResource(images[position])

        return tempView
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}