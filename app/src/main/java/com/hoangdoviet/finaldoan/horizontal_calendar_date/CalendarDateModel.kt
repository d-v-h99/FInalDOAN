package com.hoangdoviet.finaldoan.horizontal_calendar_date

import java.text.SimpleDateFormat
import java.util.*

data class CalendarDateModel(var data: Date, var isSelected: Boolean = false, var hasEvent: Boolean = false) {

    val calendarDay: String
        get() = SimpleDateFormat("EE", Locale("vi", "VN")).format(data)

    val calendarYear: String
        get() = SimpleDateFormat("yyyyMMdd", Locale("vi", "VN")).format(data)

    val calendarDate: String
        get() = SimpleDateFormat("dd", Locale("vi", "VN")).format(data)
}