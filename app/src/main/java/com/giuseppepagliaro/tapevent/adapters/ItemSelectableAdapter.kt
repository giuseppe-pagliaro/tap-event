package com.giuseppepagliaro.tapevent.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.giuseppepagliaro.tapevent.R
import com.giuseppepagliaro.tapevent.models.Selectable

class ItemSelectableAdapter(
    private val context: Context,
    private var selectable: List<Selectable>,
    private val onSelect: (Int) -> Unit
) : RecyclerView.Adapter<ItemSelectableAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.iv_thumbnail)
        val tvTitle: TextView = view.findViewById(R.id.tv_items_title)
        val tvPrice: TextView = view.findViewById(R.id.tv_price)
        val setOnClickListener: (View.OnClickListener) -> Unit = { l -> view.setOnClickListener(l) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newSelectable: List<Selectable>) {
        selectable = newSelectable
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item_selectable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selectable = selectable[position]

        Glide.with(context).load(selectable.thumbnail).into(holder.ivThumbnail)
        holder.tvTitle.text = selectable.name

        holder.tvPrice.text = selectable.getPrice()

        holder.setOnClickListener {
            onSelect(position)
        }
    }

    override fun getItemCount(): Int = selectable.size
}