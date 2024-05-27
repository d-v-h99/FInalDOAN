package com.hoangdoviet.finaldoan.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.adapter.HolidaysAdapter
import com.hoangdoviet.finaldoan.databinding.FragmentDayBinding
import com.hoangdoviet.finaldoan.databinding.FragmentMonthBinding
import com.hoangdoviet.finaldoan.model.DrawLableForDate
import com.hoangdoviet.finaldoan.model.Holiday
import com.hoangdoviet.finaldoan.model.LunarCalendar
import com.hoangdoviet.finaldoan.model.ThoiGianConVat
import com.hoangdoviet.finaldoan.model.holidays
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale


class MonthFragment : Fragment() {
    private var _binding: FragmentMonthBinding? = null
    private var thoiGianConVat: ThoiGianConVat? = ThoiGianConVat(null)
    private val binding get() = _binding!!
    private val start = Calendar.getInstance()
    private val end = Calendar.getInstance()
    private val seted = HashMap<String, String>()

    private val calendar = Calendar.getInstance()
    val d = calendar.get(Calendar.DAY_OF_MONTH)
    val m = calendar.get(Calendar.MONTH)
    val y = calendar.get(Calendar.YEAR)
    private lateinit var holidaysAdapter: HolidaysAdapter
    val holidays = mapOf(
        1 to listOf(
            Holiday("1/1", "Tết Dương lịch"),
            Holiday("9/1", "Ngày Học sinh – Sinh viên Việt Nam")
        ),
        2 to listOf(
            Holiday("3/2", "Ngày thành lập Đảng Cộng sản Việt Nam"),
            Holiday("14/2", "Lễ tình nhân (Valentine, Valentine đỏ)"),
            Holiday("27/2", "Ngày thầy thuốc Việt Nam")
        ),
        3 to listOf(
            Holiday("8/3", "Ngày Quốc tế Phụ nữ"),
            Holiday("14/3", "Ngày Valentine Trắng (White day)"),
            Holiday("20/3", "Ngày Quốc tế Hạnh phúc"),
            Holiday("22/3", "Ngày Nước sạch Thế giới"),
            Holiday("26/3", "Ngày thành lập Đoàn TNCS Hồ Chí Minh"),
            Holiday("27/3", "Ngày Thể thao Việt Nam"),
            Holiday("28/3", "Ngày thành lập lực lượng Dân quân tự vệ")
        ),
        4 to listOf(
            Holiday("1/4", "Ngày Cá tháng Tư"),
            Holiday("6/4", "Ngày Quốc tế Thể thao"),
            Holiday("7/4", "Ngày Sức khỏe Thế giới"),
            Holiday("14/4", "Valentine Đen (Black Day)"),
            Holiday("21/4", "Ngày Sách Việt Nam"),
            Holiday("22/4", "Ngày Trái Đất"),
            Holiday("30/4", "Ngày giải phóng miền Nam")
        ),
        5 to listOf(
            Holiday("1/5", "Ngày Quốc tế Lao động"),
            Holiday("7/5", "Ngày chiến thắng Điện Biên Phủ"),
            Holiday("8/5", "Ngày của mẹ (Ngày của Mẹ được tính là Chủ nhật thứ hai của tháng 5 và trong năm 2022 này thì rơi vào ngày 08/05)"),
            Holiday("15/5", "Ngày thành lập Đội Thiếu niên Tiền phong Hồ Chí Minh"),
            Holiday("19/5", "Ngày sinh chủ tịch Hồ Chí Minh")
        ),
        6 to listOf(
            Holiday("1/6", "Ngày Quốc tế thiếu nhi"),
            Holiday("5/6", "Ngày Bác Hồ ra đi tìm đường cứu nước và ngày Môi trường Thế giới"),
            Holiday("17/6", "Ngày của cha (Ngày của Cha là được quy ước ngày Chủ nhật thứ ba của tháng 6 và trong năm 2022 thì rơi vào ngày 19/06)"),
            Holiday("21/6", "Ngày Báo chí Việt Nam"),
            Holiday("28/6", "Ngày Gia đình Việt Nam")
        ),
        7 to listOf(
            Holiday("6/7", "Ngày Quốc tế Nụ hôn"),
            Holiday("11/7", "Ngày dân số thế giới"),
            Holiday("27/7", "Ngày Thương binh liệt sĩ"),
            Holiday("28/7", "Ngày thành lập Công đoàn Việt Nam")
        ),
        8 to listOf(
            Holiday("19/8", "Ngày Cách mạng tháng Tám thành công và ngày truyền thống Công an nhân dân")
        ),
        9 to listOf(
            Holiday("2/9", "Ngày Quốc Khánh"),
            Holiday("10/9", "Ngày thành lập Mặt trận Tổ quốc Việt Nam"),
            Holiday("21/9", "Ngày Quốc tế Hòa bình")
        ),
        10 to listOf(
            Holiday("1/10", "Ngày Quốc tế Người cao tuổi"),
            Holiday("10/10", "Ngày Giải phóng Thủ đô và ngày truyền thống Luật sư Việt Nam"),
            Holiday("13/10", "Ngày Doanh nhân Việt Nam"),
            Holiday("14/10", "Ngày thành lập Hội Nông dân Việt Nam"),
            Holiday("15/10", "Ngày thành lập Hội Liên hiệp Thanh niên Việt Nam"),
            Holiday("20/10", "Ngày Phụ nữ Việt Nam"),
            Holiday("26/10", "Ngày Điều dưỡng Việt Nam"),
            Holiday("31/10", "Ngày Halloween")
        ),
        11 to listOf(
            Holiday("9/11", "Ngày Pháp luật Việt Nam"),
            Holiday("19/11", "Ngày Quốc tế Nam giới"),
            Holiday("20/11", "Ngày Nhà giáo Việt Nam"),
            Holiday("23/11", "Ngày thành lập Hội chữ thập đỏ Việt Nam")
        ),
        12 to listOf(
            Holiday("1/12", "Ngày thế giới phòng chống AIDS"),
            Holiday("6/12", "Ngày thành lập Hội Cựu chiến binh Việt Nam"),
            Holiday("19/12", "Ngày toàn quốc kháng chiến"),
            Holiday("22/12", "Ngày thành lập Quân đội nhân dân Việt Nam"),
            Holiday("24/12", "Ngày lễ Giáng sinh")
        )
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMonthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val locale = Locale("vi")
        Locale.setDefault(locale)
        binding.calendarView.setTitleFormatter { day ->
            val dateFormat: DateFormat = SimpleDateFormat("LLLL yyyy", locale) // tháng rồi năm
            //MM/dd/yyyy
            dateFormat.format(day.getDate())
        }
        binding.calendarView.setTitleFormatter(EmptyTitleFormatter()) // Sử dụng EmptyTitleFormatter để ẩn tiêu đề
        setUI()
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        start.set(y, m, 1)
        end.set(y, m, lastDayOfMonth)
        setLunar(start, end)
        seted["$m/$y"] = ""
        binding.calendarView.setOnDateChangedListener(DateSelectedListener())
        binding.calendarView.setOnMonthChangedListener(MonthChangeListener())
        //
        val initialHolidays = holidays[m + 1] ?: emptyList()
//        val initialHolidays = holidays[m + 1]
        Log.d("Checkckk", initialHolidays.toString())
        holidaysAdapter = initialHolidays?.let { HolidaysAdapter(it) }!!
        binding.holidaysRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.holidaysRecyclerView.adapter = holidaysAdapter

    }
    private fun updateHolidays(month: Int) {
        val holidaysForMonth = holidays[month] ?: emptyList()
        holidaysAdapter.updateHolidays(holidaysForMonth)
    }

    private fun setUI() {
        binding.calendarView.setTileSizeDp(45) // Set kích thước ô ngày

        val d = calendar.get(Calendar.DAY_OF_MONTH)
        val m = calendar.get(Calendar.MONTH)+1
        val y = calendar.get(Calendar.YEAR)
        val lunarDate = LunarCalendar().convertSolar2Lunar(d, m, y, 7f)
        val jsonObject = JSONObject(lunarDate)
        val day = jsonObject.getString("lunarDay").toInt()
        val month =jsonObject.getString("lunarMonth").toInt()
        val year = jsonObject.getString("lunarYear").toInt()
        val ngayconvat: String? = thoiGianConVat?.getNamConVat(year)
        Log.d("abc", day.toString())
        Log.d("abc", ngayconvat.toString())
        binding.yyyyMAL.text = "$day Tháng $month Âm lịch, năm $ngayconvat"
        binding.yyyyM.text = "Tháng $m - $y"


        val maxDate = Calendar.getInstance()
        maxDate.set(2070, 12, 31)
        val minDate = Calendar.getInstance()
        minDate.set(1930, 1, 1)

        binding.calendarView.state().edit()
            .setMinimumDate(minDate)
            .setMaximumDate(maxDate)
            .commit()

        binding.calendarView.setHeaderTextAppearance(R.style.DateTextAppearance)
        binding.calendarView.setDateTextAppearance(R.style.DateTextAppearance)
        binding.calendarView.setWeekDayTextAppearance(R.style.WeekDayTextAppearance)
        binding.calendarView.addDecorators(TodayDecorator())
        binding.calendarView.setWeekDayFormatter(CustomWeekDayFormatter())
        binding.today.setOnClickListener {
            binding.calendarView.setCurrentDate(calendar)  // Quay lại ngày hôm nay
            binding.calendarView.setSelectedDate(calendar) // Đánh dấu ngày hôm nay
        }
    }

    private fun setLunar(start: Calendar, end: Calendar) {

        while (start <= end) {
            val calendarDay = CalendarDay(
                start.get(Calendar.YEAR),
                start.get(Calendar.MONTH),
                start.get(Calendar.DAY_OF_MONTH)
            )
            val lunarDate = LunarCalendar().convertSolar2Lunar(
                start.get(Calendar.DAY_OF_MONTH),
                start.get(Calendar.MONTH) + 1,
                start.get(Calendar.YEAR),
                7f
            )
            val jsonObject = JSONObject(lunarDate)
            binding.calendarView.addDecorator(
                LunarDecorator(
                    calendarDay,
                    jsonObject.getString("lunarDay").toInt(),
                    jsonObject.getString("lunarMonth").toInt()
                )
            )
            start.add(Calendar.DAY_OF_MONTH, 1)
        }
    }


    inner class TodayDecorator : DayViewDecorator {

        val calendar = Calendar.getInstance()

        override fun shouldDecorate(day: CalendarDay): Boolean {
            day.copyTo(calendar)
            val dd = calendar.get(Calendar.DAY_OF_MONTH)
            val mm = calendar.get(Calendar.MONTH) + 1
            val yy = calendar.get(Calendar.YEAR)
            return dd == d && mm == m + 1 && yy == y
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(resources.getDrawable(R.drawable.current_day))
        }
    }

    inner class LunarDecorator(val dates: CalendarDay, val lunarDay: Int, val lunarMonth: Int) :
        DayViewDecorator {

        val calendar = Calendar.getInstance()

        override fun shouldDecorate(day: CalendarDay): Boolean {
            day.copyTo(calendar)
            return dates == day
        }

        override fun decorate(view: DayViewFacade) {
            if (lunarDay > 9)
                view.addSpan(DrawLableForDate(Color.RED, "$lunarDay"))
            else
                view.addSpan(DrawLableForDate(Color.RED, "0$lunarDay"))
        }
    }

    inner class DateSelectedListener : OnDateSelectedListener {

        override fun onDateSelected(p0: MaterialCalendarView, p1: CalendarDay, p2: Boolean) {
            val cal = p1.calendar
            val wd = cal.get(Calendar.DAY_OF_WEEK)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val m = cal.get(Calendar.MONTH) + 1
            val y = cal.get(Calendar.YEAR)
            val result = "{'weekday': '$wd' ,'day': '$d', 'month': '$m', 'year': '$y'}"
            val lunarDate = LunarCalendar().convertSolar2Lunar(d, m, y, 7f)
            val jsonObject = JSONObject(lunarDate)
            val day = jsonObject.getString("lunarDay").toInt()
            val month =jsonObject.getString("lunarMonth").toInt()
            val year = jsonObject.getString("lunarYear").toInt()
            val ngayconvat: String? = thoiGianConVat?.getNamConVat(year)
            Log.d("abc", day.toString())
            Log.d("abc", ngayconvat.toString())
            binding.yyyyMAL.text = "$day Tháng $month Âm lịch, năm $ngayconvat"
            binding.yyyyM.text = "Tháng $m - $y"


        }
    }

    inner class MonthChangeListener : OnMonthChangedListener {

        override fun onMonthChanged(p0: MaterialCalendarView?, p1: CalendarDay) {
            val year = p1.year
            val month = p1.month
            val day = p1.day

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            val setKey = "$month/$year"

            if (!seted.containsKey(setKey)) {
                start.set(year, month, 1)
                end.set(year, month, lastDayOfMonth)
                setLunar(start, end)
                seted[setKey] = ""
            }
            updateHolidays(month + 1)
            binding.yyyyM.text = "Tháng ${month+1} - $year"
        }
    }
   inner class CustomWeekDayFormatter : WeekDayFormatter {

        private val weekDays = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")

        override fun format(dayOfWeek: Int): CharSequence {
            // Calendar.SUNDAY == 1, Calendar.MONDAY == 2, ..., Calendar.SATURDAY == 7
            return weekDays[dayOfWeek - 1]
        }
    }
    inner class EmptyTitleFormatter : TitleFormatter {
        override fun format(day: CalendarDay?): CharSequence {
            return "" // Trả về chuỗi rỗng để ẩn tiêu đề tháng
        }
    }

}