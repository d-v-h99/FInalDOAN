package com.hoangdoviet.finaldoan.adapter

import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.HolidayItemBinding
import com.hoangdoviet.finaldoan.model.Holiday

class HolidaysAdapter(private var holidays: List<Holiday>, private val context: Context) :
    RecyclerView.Adapter<HolidaysAdapter.HolidayViewHolder>() {

    inner class HolidayViewHolder(private val binding: HolidayItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(holiday: Holiday, isLastItem: Boolean) {
            binding.holidayDate.text = holiday.date
            binding.holidayDescription.text = holiday.description
            binding.holidayDescription.setTextColor(if (holiday.isHoliday) ContextCompat.getColor(context, R.color.grey) else Color.BLACK)
            //
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

    fun clearData() {
        holidays = emptyList()
        notifyDataSetChanged()
    }
}
