package com.hoangdoviet.finaldoan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hoangdoviet.finaldoan.databinding.HolidayItemBinding
import com.hoangdoviet.finaldoan.model.Holiday

class HolidaysAdapter(private var holidays: List<Holiday>) :
    RecyclerView.Adapter<HolidaysAdapter.HolidayViewHolder>() {

    inner class HolidayViewHolder(private val binding: HolidayItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(holiday: Holiday, isLastItem: Boolean) {
            binding.holidayDate.text = holiday.date
            binding.holidayDescription.text = holiday.description
            // Hide the view divider if it's the last item
            binding.viewDivider.visibility = if (isLastItem) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
        val binding = HolidayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HolidayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
        val isLastItem = position == holidays.size - 1
        holder.bind(holidays[position], isLastItem)
    }

    override fun getItemCount(): Int = holidays.size

    fun updateHolidays(newHolidays: List<Holiday>) {
        holidays = newHolidays
        notifyDataSetChanged()
    }

    fun clearData(){
        holidays = emptyList()
        notifyDataSetChanged()
    }
}
