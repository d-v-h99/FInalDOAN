package com.hoangdoviet.finaldoan.utils

import java.util.Calendar
import java.util.Date



fun addDaysSkippingWeekends(date: Date, days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    var daysAdded = 0
    while (daysAdded < days) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            daysAdded++
        }
    }
    return calendar.time
}

fun addDaysToDate(date: Date, days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return calendar.time
}

fun addMonthsToDate(date: Date, months: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.MONTH, months)
    return calendar.time
}

fun addYearsToDate(date: Date, years: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.YEAR, years)
    return calendar.time
}

sealed interface RepeatMode : java.io.Serializable {
    val repeatModeInt: Int // id đại diện chế độ lặp
    val name: String
    fun repeatBeginTimeByIndex(beginTime: Long, index: Int): Long // tính toán thời gian bắt đầu cho lần lặp tiếp theo dựa trên thời gian bắt đầu hiện tại và chỉ số lặp.

    object Never : RepeatMode {
        override val repeatModeInt: Int = 0
        override val name: String = "Không bao giờ"

        override fun repeatBeginTimeByIndex(beginTime: Long, index: Int): Long {
            return beginTime
        }
    }

    object Day : RepeatMode {
        override val repeatModeInt: Int = 1
        override val name: String = "Hàng ngày"

        override fun repeatBeginTimeByIndex(beginTime: Long, index: Int): Long {
            val newDate = addDaysToDate(Date(beginTime), index)
            return newDate.time
        }
    }

    object WorkDay : RepeatMode {
        override val repeatModeInt: Int = 2
        override val name: String = "Ngày làm việc"

        override fun repeatBeginTimeByIndex(beginTime: Long, index: Int): Long {
            val newDate = addDaysSkippingWeekends(Date(beginTime), index)
            return newDate.time
        }
    }

    object Week : RepeatMode {
        override val repeatModeInt: Int = 3
        override val name: String = "Hàng tuần"

        override fun repeatBeginTimeByIndex(beginTime: Long, index: Int): Long {
            val newDate = addDaysToDate(Date(beginTime), index * 7)
            return newDate.time
        }
    }

    object Month : RepeatMode {
        override val repeatModeInt: Int = 4
        override val name: String = "Hàng tháng"

        override fun repeatBeginTimeByIndex(beginTime: Long, index: Int): Long {
            val newDate = addMonthsToDate(Date(beginTime), index)
            return newDate.time
        }
    }

    object Year : RepeatMode {
        override val repeatModeInt: Int = 5
        override val name: String = "Hàng năm"

        override fun repeatBeginTimeByIndex(beginTime: Long, index: Int): Long {
            val newDate = addYearsToDate(Date(beginTime), index)
            return newDate.time
        }
    }

    companion object {
        val Pair<Int, Int>.parseLocalRepeatMode: RepeatMode
            get() = when (first) {
                1 -> Day
                2 -> WorkDay
                3 -> Week
                4 -> Month
                5 -> Year
                else -> Never
            }
    }
}
