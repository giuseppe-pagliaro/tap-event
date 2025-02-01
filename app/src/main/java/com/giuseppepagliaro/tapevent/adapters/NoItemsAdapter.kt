package com.giuseppepagliaro.tapevent.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giuseppepagliaro.tapevent.R

class NoItemsAdapter(
    private val context: Context,
    private val itemsName: String
) : RecyclerView.Adapter<NoItemsAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val setText: (String) -> Unit = { s -> (view as TextView).text = s }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_no_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setText(context.getString(R.string.no_items_available, itemsName))
    }

    override fun getItemCount(): Int = 1
}