package com.hoangdoviet.finaldoan.utils

import com.hoangdoviet.finaldoan.model.LunarCalendar
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.regex.Pattern
object DateScheduler {
    private var currentDate = Calendar.getInstance()
    private var d = currentDate.get(Calendar.DAY_OF_MONTH)
    private var m = currentDate.get(Calendar.MONTH) + 1
    private var y = currentDate.get(Calendar.YEAR)
    var lunarDate = LunarCalendar().convertSolar2Lunar(d, m, y, 7f)
    var jsonObject = JSONObject(lunarDate)
    val dayLunar = jsonObject.getString("lunarDay").toInt()
    val monthLunar = jsonObject.getString("lunarMonth").toInt()
    val yearLunar = jsonObject.getString("lunarYear").toInt()
    val leap = true
    fun extractLunarDate(sentence: String): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val lunarDatePattern = Pattern.compile("\\b(rằm\\s+tháng\\s+giêng|rằm\\s+tháng\\s+chạp|rằm\\s+tháng\\s+\\d+|mùng\\s\\d+\\s+tháng\\s\\d+\\s+âm\\s+lịch|\\d+\\s+âm\\s+lịch\\s+tháng\\s+này|\\d+\\s+âm\\s+lịch\\s+tháng\\s+\\d+|mùng\\s+\\d+\\s+âm\\s+lịch\\s+tháng\\s+này|rằm\\s+tháng\\s+này|ngày\\s+rằm\\s+tháng\\s+này|\\d+\\s+tháng\\s+\\d+\\s+âm\\s+lịch)\\b", Pattern.CASE_INSENSITIVE)
        val matcher = lunarDatePattern.matcher(sentence)

        while (matcher.find()) {
            val matchedPhrase = matcher.group().toLowerCase()

            return when {
                matchedPhrase.contains("rằm tháng giêng") -> {
                    LunarCalendar().lunar2solar(currentYear, 1, 15, false, 7f)
                }
                matchedPhrase.contains("rằm tháng chạp") -> {
                    LunarCalendar().lunar2solar(currentYear, 12, 15, false, 7f)
                }
                matchedPhrase.contains("rằm tháng này") || matchedPhrase.contains("ngày rằm tháng này") -> {
                    val currentLunarMonth = monthLunar
                    LunarCalendar().lunar2solar(currentYear, currentLunarMonth, 15, false, 7f)
                }
                matchedPhrase.contains("rằm tháng") -> {
                    val parts = matchedPhrase.split(" ")
                    val month = parts[2].toInt()
                    LunarCalendar().lunar2solar(currentYear, month, 15, false, 7f)
                }
                matchedPhrase.contains("mùng") && matchedPhrase.contains("tháng này") -> {
                    val currentLunarMonth = monthLunar
                    val day = matchedPhrase.split(" ")[1].toInt()
                    LunarCalendar().lunar2solar(currentYear, currentLunarMonth, day, false, 7f)
                }
                matchedPhrase.contains("tháng này") -> {
                    val currentLunarMonth = monthLunar
                    val day = matchedPhrase.split(" ")[0].toInt()
                    LunarCalendar().lunar2solar(currentYear, currentLunarMonth, day, false, 7f)
                }
                matchedPhrase.contains("mùng") -> {
                    val dayMonth = extractDayMonth(matchedPhrase)
                    LunarCalendar().lunar2solar(currentYear, dayMonth.second, dayMonth.first, false, 7f)
                }
                matchedPhrase.contains("tháng") -> {
                    val dayMonth = extractDayMonth(matchedPhrase)
                    LunarCalendar().lunar2solar(currentYear, dayMonth.second, dayMonth.first, false, 7f)
                }
                else -> "Không thể xác định ngày âm lịch"
            }
        }
        return "Không tìm thấy ngày âm lịch"
    }




    fun extractTemporalWords(sentence: String): List<String> {
        // Biểu thức chính quy để phát hiện các từ chỉ thời gian và ngày
        val sentence = sentence.replace("tư", "tu")
        val temporalPattern =
            "\\b(?:mai|ngày mai|ngày kia|hôm nay|chiều nay|tối nay|buổi sáng|buổi trưa|buổi chiều|buổi tối|sáng mai|chiều mai|tối mai|sáng ngày kia|chiều ngày kia|tối ngày kia|thứ hai|thứ ba|thứ tu|thứ năm|thứ sáu|thứ bảy|chủ nhật|tháng một|tháng hai|tháng ba|tháng tư|tháng năm|tháng sáu|tháng bảy|tháng tám|tháng chín|tháng mười|tháng mười một|tháng mười hai|tuần này|tuần sau|tuần tới)\\b"

        val pattern = Pattern.compile(temporalPattern, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(sentence)
        val temporalWords = mutableListOf<String>()

        while (matcher.find()) {
            temporalWords.add(matcher.group())
        }

        return temporalWords
    }

    fun checktime(temporalWords: List<String>): String? {
        if (temporalWords.isEmpty()) {
            return null
        }

        val currentDate = LocalDate.now()
        val currentDayOfWeek = currentDate.dayOfWeek

        val convertedDates = mutableListOf<LocalDate?>()
        temporalWords.forEachIndexed { index, temporalWord ->
            val referenceDate = if (index == 0) currentDate else convertedDates[index - 1]
            val convertedDate = when (temporalWord.toLowerCase()) {
                "hôm nay" -> currentDate
                "ngày mai" -> referenceDate?.plusDays(1)
                "ngày kia" -> referenceDate?.plusDays(2)
                "buổi sáng", "buổi chiều", "buổi tối" -> referenceDate
                "sáng mai", "chiều mai", "tối mai" -> referenceDate?.plusDays(1)
                "sáng ngày kia", "chiều ngày kia", "tối ngày kia" -> referenceDate?.plusDays(2)
                "thứ hai" -> referenceDate?.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
                "thứ ba" -> referenceDate?.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
                "thứ tu" -> referenceDate?.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY))
                "thứ năm" -> referenceDate?.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY))
                "thứ sáu" -> referenceDate?.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                "thứ bảy" -> referenceDate?.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
                "chủ nhật" -> referenceDate?.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                else -> null
            }
            convertedDates.add(convertedDate)
        }

        val temporalWord = temporalWords[0]
        val convertedDate = convertedDates[0]
        val pastDaysOfWeek = calculatePastDaysOfWeek()
        val listTuan = listOf("tuần sau", "tuần tới")

        return if (convertedDate != null) {
            when {
                temporalWords.contains("tuần này") -> {
                    if (pastDaysOfWeek.contains(temporalWord)) {
                        "Lỗi vì ngày đã trôi qua"
                    } else {
                        "$convertedDate"
                    }
                }
                temporalWords.any { it in listTuan } -> {
                    if (pastDaysOfWeek.contains(temporalWord)) {
                        "${convertedDate.plusDays(7)}"
                    } else {
                        "${convertedDate.plusDays(7)}"
                    }
                }
                else -> {
                    "$convertedDate"
                }
            }
        } else {
            "Không thể xác định ngày cho từ '$temporalWord'"
        }
    }

    fun findTimeReferences(sentence: String): String? {
        val specificTimePattern = Pattern.compile("\\b\\d{1,2}:\\d{2}\\b", Pattern.CASE_INSENSITIVE)
        val specificTimeMatcher = specificTimePattern.matcher(sentence)

        while (specificTimeMatcher.find()) {
            return specificTimeMatcher.group()
        }

        val specialCasesPattern = Pattern.compile("\\b(\\d{1,2})\\s*giờ\\s*(sáng|chiều|tối|đêm)\\b", Pattern.CASE_INSENSITIVE)
        val specialCasesMatcher = specialCasesPattern.matcher(sentence)

        while (specialCasesMatcher.find()) {
            val hour = specialCasesMatcher.group(1)?.toIntOrNull()
            val period = specialCasesMatcher.group(2)?.toLowerCase()

            if (hour != null && period != null) {
                return when (period) {
                    "sáng" -> String.format("%02d:00", hour)
                    "chiều", "tối", "đêm" -> String.format("%02d:00", if (hour < 12) hour + 12 else hour)
                    else -> null
                }
            }
        }

        val simpleHourPattern = Pattern.compile("\\b(\\d{1,2})\\s*giờ\\b", Pattern.CASE_INSENSITIVE)
        val simpleHourMatcher = simpleHourPattern.matcher(sentence)

        while (simpleHourMatcher.find()) {
            val hour = simpleHourMatcher.group(1)?.toIntOrNull()

            if (hour != null) {
                return String.format("%02d:00", hour)
            }
        }

        return null
    }


    fun getStringAfterKeyword(sentence: String, keyword: String = "nội dung"): String {
        val index = sentence.indexOf(keyword)
        if (index != -1) {
            val startIndex = index + keyword.length
            if (startIndex < sentence.length) {
                return sentence.substring(startIndex).trim()
            }
        }
        return ""
    }
    fun check(sentence: String){
        if(sentence.toLowerCase().contains("đặt lịch")){
            val temporalWords = extractTemporalWords(sentence)
            println("Các từ chỉ ngày: $temporalWords")
            findTimeReferences(sentence)
            val result = getStringAfterKeyword(sentence)
            println(result)
        }
    }
    fun calculatePastDaysOfWeek() : List<String>{
        val currentDate = LocalDate.now()
        val pastDaysOfWeek = mutableListOf<String>()
        val currentDayOfWeek = currentDate.dayOfWeek
        for (dayOfWeek in DayOfWeek.values().takeWhile { it != currentDayOfWeek }) {
            val vietnameseDayOfWeekMap = mapOf(
                "thứ hai" to DayOfWeek.MONDAY,
                "thứ ba" to DayOfWeek.TUESDAY,
                "thứ tu" to DayOfWeek.WEDNESDAY,
                "thứ năm" to DayOfWeek.THURSDAY,
                "thứ sáu" to DayOfWeek.FRIDAY,
                "thứ bảy" to DayOfWeek.SATURDAY,
                "chủ nhật" to DayOfWeek.SUNDAY
            )
            val vietnameseDay = vietnameseDayOfWeekMap.entries.find { it.value == dayOfWeek }?.key
            if (vietnameseDay != null) {
                pastDaysOfWeek.add(vietnameseDay)
            }
        }

        return pastDaysOfWeek

    }

    fun extractDayMonth(phrase: String): Pair<Int, Int> {
        val parts = phrase.split(" ")
        return when {
            phrase.contains("mùng") -> {
                val day = parts[1].toInt()
                val month = parts[3].toInt()
                Pair(day, month)
            }
            phrase.contains("rằm") -> {
                val day = 15
                val month = when {
                    phrase.contains("tháng giêng") -> 1
                    phrase.contains("tháng chạp") -> 12
                    parts.size == 3 -> parts[2].toInt() // "rằm tháng X"
                    else -> parts[3].toInt() // "rằm tháng Y âm lịch"
                }
                Pair(day, month)
            }
            phrase.contains("tháng") -> {
                val day = parts[0].toInt()
                val month = when {
                    parts.size >= 3 && parts[2].toIntOrNull() != null -> parts[2].toInt() // "X tháng Y âm lịch"
                    parts.size >= 5 && parts[4].toIntOrNull() != null -> parts[4].toInt() // "X âm lịch tháng Y"
                    else -> throw NumberFormatException("Invalid month format in phrase: $phrase")
                }
                Pair(day, month)
            }
            else -> {
                throw NumberFormatException("Invalid format in phrase: $phrase")
            }
        }
    }



}