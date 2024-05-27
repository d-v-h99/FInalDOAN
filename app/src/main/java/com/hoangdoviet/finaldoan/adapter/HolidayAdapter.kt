package com.hoangdoviet.finaldoan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.HolidayItemBinding
import com.hoangdoviet.finaldoan.model.Holiday

// HolidaysAdapter.kt
class HolidaysAdapter(private var holidays: List<Holiday>) :
    RecyclerView.Adapter<HolidaysAdapter.HolidayViewHolder>() {

    inner class HolidayViewHolder(private val binding: HolidayItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(holiday: Holiday) {
            binding.holidayDate.text = holiday.date
            binding.holidayDescription.text = holiday.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
        val binding = HolidayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HolidayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
        holder.bind(holidays[position])
    }

    override fun getItemCount(): Int = holidays.size

    fun updateHolidays(newHolidays: List<Holiday>) {
        holidays = newHolidays
        notifyDataSetChanged()
    }
}
