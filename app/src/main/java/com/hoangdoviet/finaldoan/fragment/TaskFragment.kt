package com.hoangdoviet.finaldoan.fragment

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.style.ReplacementSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hoangdoviet.finaldoan.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale


class TaskFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.addDecorator(MonthYearDecorator())
    }

}
class MonthYearDecorator : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return true // Áp dụng cho tất cả các ngày
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(CustomSpan())
    }

    class CustomSpan : ReplacementSpan() {
        override fun getSize(
            paint: Paint,
            text: CharSequence?,
            start: Int,
            end: Int,
            fm: Paint.FontMetricsInt?
        ): Int {
            return paint.measureText("MM-yyyy").toInt()
        }

        override fun draw(
            canvas: Canvas,
            text: CharSequence?,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            val dateFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, text?.substring(start, end)?.toInt() ?: 0)
            val date = dateFormat.format(calendar.time)
            paint.color = Color.RED // Màu tùy chỉnh
            paint.isFakeBoldText = true
            canvas.drawText(date, x, y.toFloat(), paint)
        }
    }
}