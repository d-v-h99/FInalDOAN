package com.hoangdoviet.finaldoan.model

import java.lang.Math.PI

class LunarCalendar {

    fun jdFromDate(dd: Int, mm: Int, yy: Int): Long {
        val a = Math.floor(((14 - mm) / 12).toDouble()).toInt()
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + Math.floor(((153 * m + 2) / 5).toDouble()).toInt() + 365 * y + Math.floor((y / 4).toDouble()).toInt() - Math.floor(
            (y / 100).toDouble()
        ).toInt() + Math.floor((y / 400).toDouble()).toInt() - 32045
        if (jd < 2299161) {
            jd = dd + Math.floor(((153 * m + 2) / 5).toDouble()).toInt() + 365 * y + Math.floor((y / 4).toDouble()).toInt() - 32083
        }
        return jd.toLong()
    }
    internal fun jdToDate(jd: Long): String {
        var a: Int
        var b: Int
        var c: Int
        var d: Int
        var e: Int
        var m: Int
        var day: Int
        var month: Int
        var year: Int
        if (jd > 2299160) {
            a = (jd + 32044).toInt()
            b = (4 * a + 3) / 146097
            c = a - (b * 146097) / 4
        } else {
            b = 0
            c = (jd + 32082).toInt()
        }
        d = (4 * c + 3) / 1461
        e = c - (1461 * d) / 4
        m = (5 * e + 2) / 153
        day = e - (153 * m + 2) / 5 + 1
        month = m + 3 - 12 * (m / 10)
        year = b * 100 + d - 4800 + (m / 10)

        return "$year-$month-$day"
    }
    private fun getNewMoonDay(k: Double, timeZone: Float): Long {
        val deltat: Double
        val T = k / 1236.85
        val T2 = T * T
        val T3 = T2 * T
        val dr = PI / 180
        var Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3
        Jd1 += 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr)
        val M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3
        val Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3
        val F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3
        var C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M)
        C1 -= 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(2 * dr * Mpr)
        C1 -= 0.0004 * Math.sin(3 * dr * Mpr)
        C1 += 0.0104 * Math.sin(2 * dr * F) - 0.0051 * Math.sin(dr * (M + Mpr))
        C1 -= 0.0074 * Math.sin(dr * (M - Mpr)) + 0.0004 * Math.sin(dr * (2 * F + M))
        C1 -= 0.0004 * Math.sin(dr * (2 * F - M)) - 0.0006 * Math.sin(dr * (2 * F + Mpr))
        C1 += 0.0010 * Math.sin(dr * (2 * F - Mpr)) + 0.0005 * Math.sin(dr * (2 * Mpr + M))
        if (T < -11) {
            deltat = 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3
        } else {
            deltat = -0.000278 + 0.000265 * T + 0.000262 * T2
        }
        val JdNew = Jd1 + C1 - deltat
        return Math.floor(JdNew + 0.5 + timeZone / 24).toLong()
    }

    private fun getSunLongitude(jdn: Long, timeZone: Float): Double {
        val T = (jdn - 2451545.5 - timeZone / 24) / 36525
        val T2 = T * T
        val dr = PI / 180
        val M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2
        val L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2
        var DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M)
        DL += (0.019993 - 0.000101 * T) * Math.sin(2 * dr * M) + 0.000290 * Math.sin(3 * dr * M)
        var L = L0 + DL
        L = L * dr
        L -= PI * 2 * (Math.floor(L / (PI * 2)))
        return Math.floor(L / PI * 6)
    }

    private fun getLunarMonth11(yy: Int, timeZone: Float): Long {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = Math.floor(off / 29.530588853).toInt()
        var nm = getNewMoonDay(k.toDouble(), timeZone)
        val sunLong = getSunLongitude(nm, timeZone)
        if (sunLong >= 9) {
            nm = getNewMoonDay((k - 1).toDouble(), timeZone)
        }
        return nm
    }

    private fun getLeapMonthOffset(a11: Long, timeZone: Float): Int {
        val k = Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5)
        var last: Double
        var i = 1
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        do {
            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        } while (arc != last && i < 14)
        return i - 1
    }

    fun convertSolar2Lunar(dd: Int, mm: Int, yy: Int, timeZone: Float): String {
        var lunarYear: Int
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = Math.floor((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = getNewMoonDay((k + 1).toDouble(), timeZone)

        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k.toDouble(), timeZone)
        }
        var a11 = getLunarMonth11(yy, timeZone)
        var b11 = a11
        if (a11 >= monthStart) {
            lunarYear = yy
            a11 = getLunarMonth11(yy - 1, timeZone)
        } else {
            lunarYear = yy + 1
            b11 = getLunarMonth11(yy + 1, timeZone)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = Math.floor(((monthStart - a11) / 29).toDouble()).toInt()
        var lunarLeap = false
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (leapMonthDiff.toDouble() == diff.toDouble()) {
                    lunarLeap = true
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth -= 12
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1
        }
        return "{'lunarDay': '$lunarDay', 'lunarMonth': '${lunarMonth.toInt()}', 'lunarYear': '$lunarYear', 'leap': '$lunarLeap'}"
    }

    internal fun INT(number: Number): Int {
        return number.toInt()
    }

    internal fun DOUBLE(i: Int): Double {
        return i.toDouble()
    }

    fun lunar2solar(lunarYear: Int, lunarMonth: Int, lunarDay: Int, lunarLeap: Boolean, timeZoneOffset: Float): String {
        var k: Int
        var a11: Long
        var b11: Long
        var off: Int
        var leapOff: Int
        var leapMonth: Int
        var monthStart: Long

        if (lunarMonth < 11) {
            a11 = getLunarMonth11(lunarYear - 1, timeZoneOffset)
            b11 = getLunarMonth11(lunarYear, timeZoneOffset)
        } else {
            a11 = getLunarMonth11(lunarYear, timeZoneOffset)
            b11 = getLunarMonth11(lunarYear + 1, timeZoneOffset)
        }
        k = INT(0.5 + (DOUBLE(a11.toInt()) - 2415021.076998695) / 29.530588853)
        off = lunarMonth - 11
        if (off < 0) {
            off += 12
        }
        if (b11 - a11 > 365) {
            leapOff = getLeapMonthOffset(a11, timeZoneOffset)
            leapMonth = leapOff - 2
            if (leapMonth < 0) {
                leapMonth += 12
            }
            if (lunarLeap && lunarMonth != leapMonth) {
                return "0/0/0"
            } else if (lunarLeap || off >= leapOff) {
                off += 1
            }
        }
        monthStart = getNewMoonDay((k + off).toDouble(), timeZoneOffset)

        return jdToDate(monthStart + lunarDay - 1)
    }
}
