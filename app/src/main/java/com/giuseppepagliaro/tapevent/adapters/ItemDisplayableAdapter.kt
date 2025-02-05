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
import com.giuseppepagliaro.tapevent.models.Displayable

class ItemDisplayableAdapter(
    private val context: Context,
    private var displayable: List<Displayable>
) : RecyclerView.Adapter<ItemDisplayableAdapter.ViewHolder>() {
    class ViewHolder(view:View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.iv_displayer_thumbnail)
        val tvTitle: TextView = view.findViewById(R.id.tv_displayer_title)
        val tvSoldIn: TextView = view.findViewById(R.id.tv_displayer_sold_in)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newDisplayable: List<Displayable>) {
        displayable = newDisplayable
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item_displayer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val displayable = displayable[position]

        Glide.with(context).load(displayable.thumbnail).into(holder.ivThumbnail)

        holder.tvTitle.text = displayable.title

        val soldIn = displayable.soldIn.toString()
        holder.tvSoldIn.text = context.getString(
            R.string.item_diplayer_sold_id,
            soldIn.substring(1, soldIn.length - 1)
        )
    }

    override fun getItemCount(): Int = displayable.size
}