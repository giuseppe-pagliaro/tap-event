package com.giuseppepagliaro.tapevent.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.giuseppepagliaro.tapevent.R
import com.giuseppepagliaro.tapevent.dto.EventDto
import com.giuseppepagliaro.tapevent.entities.Role
import java.util.Locale

class ItemEventAdapter(
    private val context: Context,
    private var events: List<EventDto>,
    private val getRoleFromEvent: (eventCod: Int) -> Role,
    private val getRoleColor: (Role) -> Int,
    private val openEventActivity: (event: EventDto) -> Unit
) : RecyclerView.Adapter<ItemEventAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val ivTitle: TextView = view.findViewById(R.id.tv_event_item_title)
        val ivDate: TextView = view.findViewById(R.id.tv_event_item_date)
        val ivRole: TextView = view.findViewById(R.id.tv_event_item_role)
        val cvRole: CardView = view.findViewById(R.id.cv_event_item_role)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newEvents: List<EventDto>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_event_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        val role = getRoleFromEvent(event.cod)

        holder.ivTitle.text = event.name
        holder.ivDate.text = event.date.toString()
        holder.ivRole.text = role.name.lowercase().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }

        holder.cvRole.setCardBackgroundColor(ContextCompat.getColor(context, getRoleColor(role)))

        holder.itemView.setOnClickListener {
            openEventActivity(event)
        }
    }

    override fun getItemCount(): Int = events.size
}