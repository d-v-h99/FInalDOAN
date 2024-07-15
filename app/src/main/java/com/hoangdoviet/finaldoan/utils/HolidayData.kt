package com.hoangdoviet.finaldoan.utils

import android.util.Log
import com.hoangdoviet.finaldoan.model.Holiday
import com.hoangdoviet.finaldoan.model.LunarCalendar
import java.text.SimpleDateFormat
import java.util.*

object HolidayData {
    private var currentDate = Calendar.getInstance()
    private var y = currentDate.get(Calendar.YEAR)
    val holidays: MutableMap<Int, MutableList<Holiday>> = mutableMapOf(
        1 to mutableListOf(
            Holiday("1/1", "Tết Dương lịch"),
            Holiday("9/1", "Ngày Học sinh – Sinh viên Việt Nam")
        ),
        2 to mutableListOf(
            Holiday("3/2", "Ngày thành lập Đảng Cộng sản Việt Nam"),
            Holiday("14/2", "Lễ tình nhân (Valentine, Valentine đỏ)"),
            Holiday("27/2", "Ngày thầy thuốc Việt Nam")
        ),
        3 to mutableListOf(
            Holiday("8/3", "Ngày Quốc tế Phụ nữ"),
            Holiday("14/3", "Ngày Valentine Trắng (White day)"),
            Holiday("20/3", "Ngày Quốc tế Hạnh phúc"),
            Holiday("22/3", "Ngày Nước sạch Thế giới"),
            Holiday("26/3", "Ngày thành lập Đoàn TNCS Hồ Chí Minh"),
            Holiday("27/3", "Ngày Thể thao Việt Nam"),
            Holiday("28/3", "Ngày thành lập lực lượng Dân quân tự vệ")
        ),
        4 to mutableListOf(
            Holiday("1/4", "Ngày Cá tháng Tư"),
            Holiday("6/4", "Ngày Quốc tế Thể thao"),
            Holiday("7/4", "Ngày Sức khỏe Thế giới"),
            Holiday("14/4", "Valentine Đen (Black Day)"),
            Holiday("21/4", "Ngày Sách Việt Nam"),
            Holiday("22/4", "Ngày Trái Đất"),
            Holiday("30/4", "Ngày giải phóng miền Nam")
        ),
        5 to mutableListOf(
            Holiday("1/5", "Ngày Quốc tế Lao động"),
            Holiday("7/5", "Ngày chiến thắng Điện Biên Phủ"),
            Holiday("8/5", "Ngày của mẹ (Ngày của Mẹ được tính là Chủ nhật thứ hai của tháng 5 và trong năm 2022 này thì rơi vào ngày 08/05)"),
            Holiday("15/5", "Ngày thành lập Đội Thiếu niên Tiền phong Hồ Chí Minh"),
            Holiday("19/5", "Ngày sinh chủ tịch Hồ Chí Minh")
        ),
        6 to mutableListOf(
            Holiday("1/6", "Ngày Quốc tế thiếu nhi"),
            Holiday("5/6", "Ngày Bác Hồ ra đi tìm đường cứu nước và ngày Môi trường Thế giới"),
            Holiday("17/6", "Ngày của cha (Ngày của Cha là được quy ước ngày Chủ nhật thứ ba của tháng 6 và trong năm 2022 thì rơi vào ngày 19/06)"),
            Holiday("21/6", "Ngày Báo chí Việt Nam"),
            Holiday("28/6", "Ngày Gia đình Việt Nam")
        ),
        7 to mutableListOf(
            Holiday("6/7", "Ngày Quốc tế Nụ hôn"),
            Holiday("11/7", "Ngày dân số thế giới"),
            Holiday("27/7", "Ngày Thương binh liệt sĩ"),
            Holiday("28/7", "Ngày thành lập Công đoàn Việt Nam")
        ),
        8 to mutableListOf(
            Holiday("19/8", "Ngày Cách mạng tháng Tám thành công và ngày truyền thống Công an nhân dân")
        ),
        9 to mutableListOf(
            Holiday("2/9", "Ngày Quốc Khánh"),
            Holiday("10/9", "Ngày thành lập Mặt trận Tổ quốc Việt Nam"),
            Holiday("21/9", "Ngày Quốc tế Hòa bình")
        ),
        10 to mutableListOf(
            Holiday("1/10", "Ngày Quốc tế Người cao tuổi"),
            Holiday("10/10", "Ngày Giải phóng Thủ đô và ngày truyền thống Luật sư Việt Nam"),
            Holiday("13/10", "Ngày Doanh nhân Việt Nam"),
            Holiday("14/10", "Ngày thành lập Hội Nông dân Việt Nam"),
            Holiday("15/10", "Ngày thành lập Hội Liên hiệp Thanh niên Việt Nam"),
            Holiday("20/10", "Ngày Phụ nữ Việt Nam"),
            Holiday("26/10", "Ngày Điều dưỡng Việt Nam"),
            Holiday("31/10", "Ngày Halloween")
        ),
        11 to mutableListOf(
            Holiday("9/11", "Ngày Pháp luật Việt Nam"),
            Holiday("19/11", "Ngày Quốc tế Nam giới"),
            Holiday("20/11", "Ngày Nhà giáo Việt Nam"),
            Holiday("23/11", "Ngày thành lập Hội chữ thập đỏ Việt Nam")
        ),
        12 to mutableListOf(
            Holiday("1/12", "Ngày thế giới phòng chống AIDS"),
            Holiday("6/12", "Ngày thành lập Hội Cựu chiến binh Việt Nam"),
            Holiday("19/12", "Ngày toàn quốc kháng chiến"),
            Holiday("22/12", "Ngày thành lập Quân đội nhân dân Việt Nam"),
            Holiday("24/12", "Ngày lễ Giáng sinh")
        )
    )

    init {
        // Danh sách các ngày lễ âm lịch
        val lunarHolidays = listOf(
            "1/1" to "Tết Nguyên Đán\nÂm lịch 1/1",
            "15/1" to "Tết Nguyên Tiêu (Lễ Thượng Nguyên)\nÂm lịch 15/1",
            "3/3" to "Tết Hàn Thực\nÂm lịch 3/3",
            "10/3" to "Giỗ Tổ Hùng Vương\nÂm lịch 10/3",
            "15/4" to "Lễ Phật Đản\nÂm lịch 15/4",
            "5/5" to "Tết Đoan Ngọ\nÂm lịch 5/5",
            "15/7" to "Lễ Vu Lan\nÂm lịch 15/7",
            "15/8" to "Tết Trung Thu\nÂm lịch 15/8",
            "10/10" to "Tết Thường Tân (Tết Cơm mới)\nÂm lịch 10/10",
            "15/10" to "Tết Hạ Nguyên\nÂm lịch 15/10",
            "23/12" to "Tiễn Táo Quân về trời\nÂm lịch 23/12",
            "30/12" to "Lễ Tất Niên\nÂm lịch 30/12"
        )

        lunarHolidays.forEach { (lunarDate, description) ->
            val (lunarDay, lunarMonth) = lunarDate.split("/").map { it.toInt() }
            val solarDateString = LunarCalendar().lunar2solar(y, lunarMonth, lunarDay, false, 7f)
            // Log.d("checkarrAL", solarDateString.toString())

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val date = inputFormat.parse(solarDateString)
            val formattedDate = outputFormat.format(date)

            val (day, month) = formattedDate.split("/").map { it.toInt() }
            val solarDate = "$day/$month"
            val holiday = Holiday(solarDate, description)
            Log.d("checkarrAL", solarDateString.toString() + holiday.toString())

            holidays.getOrPut(month) { mutableListOf() }.add(holiday)
        }

        holidays.forEach { (month, holidayList) ->
            holidayList.sortBy { holiday ->
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                dateFormat.parse(holiday.date)
            }
        }
    }
}
