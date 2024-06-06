package com.hoangdoviet.finaldoan.horizontal_calendar_date

import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import java.text.SimpleDateFormat
import java.util.*

class HorizontalCalendarSetUp {

    private val sdf = SimpleDateFormat("MMMM yyyy", Locale("vi", "VN"))
    private val cal = Calendar.getInstance(Locale("vi", "VN"))
    private lateinit var adapter: HorizontalCalendarAdapter
    private val calendarList2 = ArrayList<CalendarDateModel>()

    fun setUpCalendarPrevNextClickListener(
        ivCalendarNext: ImageView,
        ivCalendarPrevious: ImageView,
        listener: HorizontalCalendarAdapter.OnItemClickListener,
        month: (String) -> Unit
    ) {
        ivCalendarNext.setOnClickListener {
            cal.add(Calendar.MONTH, 1)
            val monthDate = setUpCalendar(listener)
            month.invoke(monthDate)
        }
        ivCalendarPrevious.setOnClickListener {
            cal.add(Calendar.MONTH, -1)
            val monthDate = setUpCalendar(listener)
            month.invoke(monthDate)
        }
    }

    fun setUpCalendarAdapter(
        recyclerView: RecyclerView,
        listener: HorizontalCalendarAdapter.OnItemClickListener
    ): String {
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        adapter = HorizontalCalendarAdapter(listener) { calendarDateModel, position ->
            calendarList2.forEachIndexed { index, calendarModel ->
                calendarModel.isSelected = index == position
            }

//            recyclerView.post {
//                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                layoutManager.scrollToPositionWithOffset(
//                    position,
//                    (recyclerView.width / 2) - (recyclerView.getChildAt(0)?.width ?: 0) / 2
//                )
//            }
        }
        adapter.setData(calendarList2)
        recyclerView.adapter = adapter

        val monthDate = setUpCalendar(listener)
        scrollToToday(recyclerView)
        triggerTodayClick()
        return monthDate
    }

    private fun setUpCalendar(listener: HorizontalCalendarAdapter.OnItemClickListener): String {
        val calendarList = ArrayList<CalendarDateModel>()
        val monthCalendar = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val today = Calendar.getInstance()
//        today.set(Calendar.YEAR, 2024)
//        today.set(Calendar.MONTH, Calendar.JUNE)
//        today.set(Calendar.DAY_OF_MONTH, 21)

        for (day in 1..maxDaysInMonth) {
            val isToday = (day == today.get(Calendar.DAY_OF_MONTH))
            calendarList.add(CalendarDateModel(monthCalendar.time, isToday))
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendarList2.clear()
        calendarList2.addAll(calendarList)
        adapter.setOnItemClickListener(listener)
        adapter.setData(calendarList)
        Log.d("checkLich", sdf.format(cal.time))
        return sdf.format(cal.time)
    }

    private fun scrollToToday(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val todayPosition = calendarList2.indexOfFirst { it.isSelected }

        if (todayPosition != -1) {
            recyclerView.post {
                layoutManager.scrollToPositionWithOffset(
                    todayPosition,
                    (recyclerView.width / 2) - (recyclerView.getChildAt(0)?.width ?: 0) / 2
                )
            }
        }
    }

    private fun triggerTodayClick() {
        val todayPosition = calendarList2.indexOfFirst { it.isSelected }
        if (todayPosition != -1) {
            adapter.triggerOnItemClick(todayPosition)
        }
    }
}

