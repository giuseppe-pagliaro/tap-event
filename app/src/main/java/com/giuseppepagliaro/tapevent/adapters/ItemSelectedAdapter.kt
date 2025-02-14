package com.giuseppepagliaro.tapevent.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giuseppepagliaro.tapevent.R
import com.giuseppepagliaro.tapevent.models.Selected

class ItemSelectedAdapter(
    private val context: Context,
    private var selected: List<Selected>,
    private val onItemAdded: (Int) -> Unit,
    private val onItemRemoved: (Int) -> Unit
) : RecyclerView.Adapter<ItemSelectedAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitleCount: TextView = view.findViewById(R.id.tv_selected_title_count)
        val tvTotalCost: TextView = view.findViewById(R.id.tv_selected_total_cost)
        val btnAdd: ImageButton = view.findViewById(R.id.btn_add_item)
        val btnRemove: ImageButton = view.findViewById(R.id.btn_remove_item)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newSelected: List<Selected>) {
        selected = newSelected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item_selected, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selected = selected[position]

        if (selected.count > 1)
            holder.tvTitleCount.text = context.getString(
                R.string.item_selected_name_count,
                selected.item.name,
                selected.count
            )
        else
            holder.tvTitleCount.text = selected.item.name

        holder.tvTotalCost.text = selected.getTotalPriceStr()

        holder.btnAdd.setOnClickListener {
            onItemAdded(position)
        }
        holder.btnRemove.setOnClickListener {
            onItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = selected.size
}