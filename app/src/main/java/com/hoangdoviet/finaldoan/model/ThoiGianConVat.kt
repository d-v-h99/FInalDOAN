package com.hoangdoviet.finaldoan.model

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class ThoiGianConVat(millisTime: Long?) {

    @SuppressLint("SimpleDateFormat")
    private val hoursFormat = SimpleDateFormat("HH:mm")
    @SuppressLint("SimpleDateFormat")
    private val yearFormat = SimpleDateFormat("yyyy")
    private lateinit var date: Date
    private lateinit var hours: String
    private var year = 0

    private lateinit var arrStart: ArrayList<String>
    private lateinit var arrEnd: ArrayList<String>

    private val arrName = arrayListOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    private val arrChi = arrayListOf("Thân", "Dậu", "Tuất", "Hợi", "Tý", "Sửu", "Dần", "Mẹo", "Thìn", "Tỵ", "Ngọ", "Mùi")
    private val arrCanYear = arrayListOf("Canh", "Tân", "Nhâm", "Quý", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ")
    private val arrChiMonth = arrayListOf("Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi", "Tý", "Sửu")
    private val arrayCanMonth = arrayListOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    private val arrChiDay = arrayListOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    private val arrayCanDay = arrayListOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")

    init {
        if (millisTime != null) {
            date = Date(millisTime)
            hours = hoursFormat.format(date)
            year = yearFormat.format(date).toInt()
        }
    }

    fun getCanhGio(month: Int): String {
        var hours1: Long //hours start in array
        var hours2: Long //hours now
        var hours3: Long //hours end in arrar

        when (month) {
            1 -> {
                arrStart = arrayListOf("23:30", "01:30", "03:30", "05:30", "07:30", "09:30", "11:30", "13:30", "15:30", "17:30", "19:30", "21:30")
                arrEnd = arrayListOf("01:30", "03:30", "05:30", "07:30", "09:30", "11:30", "13:30", "15:30", "17:30", "19:30", "21:30", "23:30")
            }
            2 -> {
                arrStart = arrayListOf("23:40", "01:40", "03:40", "05:40", "07:40", "09:40", "11:40", "13:40", "15:40", "17:40", "19:40", "21:40")
                arrEnd = arrayListOf("01:40", "03:40", "05:40", "07:40", "09:40", "11:40", "13:40", "15:40", "17:40", "19:40", "21:40", "23:40")
            }
            3 -> {
                arrStart = arrayListOf("23:50", "01:50", "03:50", "05:50", "07:50", "09:50", "11:50", "13:50", "15:50", "17:50", "19:50", "21:50")
                arrEnd = arrayListOf("01:50", "03:50", "05:50", "07:50", "09:50", "11:50", "13:50", "15:50", "17:50", "19:50", "21:50", "23:50")
            }
            4 -> {
                arrStart = arrayListOf("00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00")
                arrEnd = arrayListOf("02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00", "00:00")
            }
            5 -> {
                arrStart = arrayListOf("00:10", "02:10", "04:10", "06:10", "08:10", "10:10", "12:10", "14:10", "16:10", "18:10", "20:10", "22:00")
                arrEnd = arrayListOf("02:10", "04:10", "06:10", "08:10", "10:10", "12:10", "14:10", "16:10", "18:10", "20:10", "22:10", "00:10")
            }
            6 -> {
                arrStart = arrayListOf("00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00")
                arrEnd = arrayListOf("02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00", "00:00")
            }
            7 -> {
                arrStart = arrayListOf("23:50", "01:50", "03:50", "05:50", "07:50", "09:50", "11:50", "13:50", "15:50", "17:50", "19:50", "21:50")
                arrEnd = arrayListOf("01:50", "03:50", "05:50", "07:50", "09:50", "11:50", "13:50", "15:50", "17:50", "19:50", "21:50", "23:50")
            }
            8 -> {
                arrStart = arrayListOf("23:40", "01:40", "03:40", "05:40", "07:40", "09:40", "11:40", "13:40", "15:40", "17:40", "19:40", "21:40")
                arrEnd = arrayListOf("01:40", "03:40", "05:40", "07:40", "09:40", "11:40", "13:40", "15:40", "17:40", "19:40", "21:40", "23:40")
            }
            9 -> {
                arrStart = arrayListOf("23:30", "01:30", "03:30", "05:30", "07:30", "09:30", "11:30", "13:30", "15:30", "17:30", "19:30", "21:30")
                arrEnd = arrayListOf("01:30", "03:30", "05:30", "07:30", "09:30", "11:30", "13:30", "15:30", "17:30", "19:30", "21:30", "23:30")
            }
            10 -> {
                arrStart = arrayListOf("23:20", "01:20", "03:20", "05:20", "07:20", "09:20", "11:20", "13:20", "15:20", "17:20", "19:20", "21:20")
                arrEnd = arrayListOf("01:20", "03:20", "05:20", "07:20", "09:20", "11:20", "13:20", "15:20", "17:20", "19:20", "21:20", "23:20")
            }
            11 -> {
                arrStart = arrayListOf("23:10", "01:10", "03:10", "05:10", "07:10", "09:10", "11:10", "13:10", "15:10", "17:10", "19:10", "21:10")
                arrEnd = arrayListOf("01:10", "03:10", "05:10", "07:10", "09:10", "11:10", "13:10", "15:10", "17:10", "19:10", "21:10", "23:10")
            }
            12 -> {
                arrStart = arrayListOf("23:30", "01:30", "03:30", "05:30", "07:30", "09:30", "11:30", "13:30", "15:30", "17:30", "19:30", "21:30")
                arrEnd = arrayListOf("01:30", "03:30", "05:30", "07:30", "09:30", "11:30", "13:30", "15:30", "17:30", "19:30", "21:30", "23:30")
            }
            else -> return "err"
        }

        var finded = 0

        for (i in 0 until arrStart.size) { // lap tu 0 den arrrStart.size - 1
            hours1 = hoursFormat.parse(arrStart[i]).time //hours start in array
            hours2 = hoursFormat.parse(hours).time //hours current
            hours3 = hoursFormat.parse(arrEnd[i]).time //hours end in array
            if (hours2 >= hours1 && hours2 < hours3) {
                finded = 1
                //Log.d("Gio"," ${arrName[i]}")
                return arrName[i]
            }
        }
        if (finded == 0) {
            if (month == 4 || month == 5 || month == 6) {
                //Log.d("Gio", arrName[arrStart.size - 1])
                return arrName[arrStart.size - 1]
            } else {
                return arrName[0]
            }
        }
        return "null"
    }

    fun getNamConVat(year: Int): String {
        val can = arrCanYear[year % 10]
        val chi = arrChi[year % 12]
        //Log.d("Nam:", "$can $chi")
        return "$can $chi"
    }

    fun getNamConVat(): String {
        val can = arrCanYear[this.year % 10]
        val chi = arrChi[this.year % 12]
        return "$can $chi"
    }

    fun getThangConVat(month: Int, year: Int): String {
        val can = (year * 12 + month + 3) % 10
        //Log.d("Thang", "${arrayCanMonth[can]} ${arrChiMonth[month-1]}")
        return "${arrayCanMonth[can]} ${arrChiMonth[month - 1]}"
    }

    fun getNgayConVat(day: Int, month: Int, year: Int): String {
        val jd = LunarCalendar().jdFromDate(day, month, year)
        val can = ((jd + 9) % 10).toInt()
        val chi = ((jd + 1) % 12).toInt()
        //Log.d("Ngay", "${arrayCanDay[can]} ${arrChiDay[chi]}")
        return "${arrayCanDay[can]} ${arrChiDay[chi]}"
    }

}