package com.hoangdoviet.finaldoan.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.adapter.EventListAdapter
import com.hoangdoviet.finaldoan.adapter.HolidaysAdapter
import com.hoangdoviet.finaldoan.databinding.BottomSheetEventListBinding
import com.hoangdoviet.finaldoan.databinding.FragmentMonthBinding
import com.hoangdoviet.finaldoan.model.DrawLableForDate
import com.hoangdoviet.finaldoan.model.Event
import com.hoangdoviet.finaldoan.model.Holiday
import com.hoangdoviet.finaldoan.model.LunarCalendar
import com.hoangdoviet.finaldoan.model.ThoiGianConVat
import com.hoangdoviet.finaldoan.model.holidays
import com.hoangdoviet.finaldoan.utils.HolidayData
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
import java.util.Calendar
import java.util.Locale


class MonthFragment : Fragment(), EventListAdapter.EventClickListener {
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
    private val db = FirebaseFirestore.getInstance()
    private val eventsRef = db.collection("Events")
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMonthBinding.inflate(inflater, container, false)
        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            val day = date.day
            val month = date.month + 1 // CalendarDay month is 0-based, so add 1
            val year = date.year
            val dateString = String.format("%02d/%02d/%04d", day, month, year)
            fetchEventsForDate(dateString)
        }

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
        val initialHolidays = HolidayData.holidays[m + 1] ?: emptyList()
//        val initialHolidays = holidays[m + 1]
        Log.d("Checkckk", initialHolidays.toString())
        holidaysAdapter = initialHolidays?.let { HolidaysAdapter(it) }!!
        binding.holidaysRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.holidaysRecyclerView.adapter = holidaysAdapter



    }
    private fun fetchEventsForDate(date: String) {
        eventsRef.whereEqualTo("date", date).get()
            .addOnSuccessListener { documents ->
                val events = documents.toObjects(Event::class.java)
                if (events.isNotEmpty()) {
                    showEventsBottomSheet(events)
                } else {
                    Toast.makeText(context, "No events for this date", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching events: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEventsBottomSheet(events: List<Event>) {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetEventListBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = EventListAdapter(events, this@MonthFragment)
        }

        dialog.show()
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
            val dateString = String.format("%02d/%02d/%04d", d, m, y)
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
            fetchEventsForDate(dateString)
//            holidaysAdapter.clearData()
//            //
//
//                val title: String? = arguments?.getString("title")
//                val textDatePicker: String? = arguments?.getString("textDatePicker")
//                val timeStart: String? = arguments?.getString("timeStart")
//                val timeEnd: String? = arguments?.getString("timeEnd")
//                val repeat: String? = arguments?.getString("repeat")
//                val holidayItem = listOf(Holiday(title.toString(), timeStart+ " - "+ timeEnd))
//                Log.d("aaa", holidayItem.toString())
//                holidaysAdapter.updateHolidays(holidayItem)




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

    override fun onEventClick(event: Event) {
        val fragment = EventFragment.newInstance(event)
        fragment.show(childFragmentManager, "EventFragment")
    }

}