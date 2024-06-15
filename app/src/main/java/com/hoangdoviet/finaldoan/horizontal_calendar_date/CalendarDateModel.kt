package com.hoangdoviet.finaldoan.horizontal_calendar_date

import java.text.SimpleDateFormat
import java.util.*

data class CalendarDateModel(var data: Date, var isSelected: Boolean = false, var hasEvent: Boolean = false) {

    val calendarDay: String //Sử dụng SimpleDateFormat("EE", Locale.getDefault()) để định dạng ngày thành dạng viết tắt của ngày trong tuần (ví dụ: "Mon", "Tue").
        get() = SimpleDateFormat("EE", Locale("vi", "VN")).format(data)

    val calendarYear: String
        get() = SimpleDateFormat("yyyyMMdd", Locale("vi", "VN")).format(data)

    val calendarDate: String
        //        get() {
//            val cal = Calendar.getInstance()
//            cal.time = data
//            return cal[Calendar.DAY_OF_MONTH].toString()
//            //Truy cập và trả về ngày của tháng (DAY_OF_MONTH) từ đối tượng Calendar dưới dạng chuỗi.
//
//
//        }
        get() = SimpleDateFormat("dd", Locale("vi", "VN")).format(data)
}