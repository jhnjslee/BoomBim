package com.bb.boombim

import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bb.boombim.data.ResultSearchKeyword
import kotlinx.android.synthetic.main.search_result_layout.view.*

class SearchRecyclerAdapter(private val myData: ResultSearchKeyword) :
            RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder>() {
    private val mDataset: ResultSearchKeyword = myData


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val searchName: TextView = view.findViewById(R.id.search_name)
            val roadName : TextView = view.findViewById(R.id.search_road_name)
//            val rangeTo : TextView = view.findViewById(R.id.range_to)
//            val etcBtn : Button = view.findViewById(R.id.search_etc_btn)

            init {
                // Define click listener for the ViewHolder's View.
            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.search_result_layout, viewGroup, false)
//            view.search_name.text =
            Log.d("test","Test0")
            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.searchName.text = mDataset.documents[position].address_name
            viewHolder.roadName.text = mDataset.documents[position].road_address_name
//            viewHolder.rangeTo.text = mDataset.documents[position].
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = mDataset.documents.size

    }