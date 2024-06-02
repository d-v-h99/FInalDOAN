package com.hoangdoviet.finaldoan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hoangdoviet.finaldoan.databinding.ItemEventBinding
import com.hoangdoviet.finaldoan.model.Event

class EventListAdapter(
    private val events: MutableList<Event>,
    private val eventClickListener: EventClickListener
) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
        holder.itemView.setOnClickListener {
            eventClickListener.onEventClick(events[position], position)
        }
    }

    override fun getItemCount() = events.size

    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.eventTitle.text = event.title
            binding.eventTime.text = "${event.timeStart} - ${event.timeEnd}"
        }
    }
    fun updateEvents(newEvents: List<Event>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }

    fun removeEventAt(position: Int) {
        events.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, events.size)
    }


    interface EventClickListener {
        fun onEventClick(event: Event, position: Int)
    }
}
