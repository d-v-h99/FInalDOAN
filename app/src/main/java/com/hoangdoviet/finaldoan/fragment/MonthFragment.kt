package com.hoangdoviet.finaldoan.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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
import com.hoangdoviet.finaldoan.model.TopRightDotSpan
import com.hoangdoviet.finaldoan.utils.HolidayData
import com.hoangdoviet.finaldoan.utils.showToast
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import org.json.JSONObject
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    private lateinit var eventListAdapter: EventListAdapter
    private val events = mutableListOf<Event>()
    private val eventDates = HashSet<CalendarDay>()

    private val eventDecorators = mutableListOf<EventDecorator>()
    private val LunarDecorators = mutableListOf<DayViewDecorator>()
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var isWeekView = true
    private lateinit var dateSelect: CalendarDay
    private lateinit var todayDecorator: TodayDecorator
    private var currentDate: CalendarDay = CalendarDay.today()



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
//        arguments?.getString("EVENT_DATE")?.let { date ->
//            fetchEventsForDate(date)
//        }
        // Nhận dữ liệu từ MainActivity
        val eventDate = arguments?.getString("EVENT_DATE")
        Log.d("MonthFragment", "onCreateView: eventDate=$eventDate")
        eventDate?.let {
            fetchEventsForDate(it)
//            showToast(requireContext(), "THong baooo")
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val locale = Locale("vi")
        Locale.setDefault(locale)
        // Lấy dữ liệu từ Bundle và gọi hàm fetchEventsForDate

        binding.calendarView.setTitleFormatter { day ->
            val dateFormat: DateFormat = SimpleDateFormat("LLLL yyyy", locale) // tháng rồi năm
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
        Log.d("Checkckk", initialHolidays.toString())
        holidaysAdapter = HolidaysAdapter(initialHolidays)
        binding.holidaysRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.holidaysRecyclerView.adapter = holidaysAdapter
        //
        eventListAdapter = EventListAdapter(mutableListOf(), this)
        // Đặt listener sau khi view đã được tạo ra
//        parentFragmentManager.setFragmentResultListener("requestKey", viewLifecycleOwner) { requestKey, bundle ->
//            val position = bundle.getInt("position")
//            // Xóa sự kiện đã xóa khỏi danh sách và thông báo cho adapter
//            events.removeAt(position)
//            eventListAdapter.notifyItemRemoved(position)
//            Log.d("ktraa", position.toString() + " MonthFragment")
//        }
        if (mAuth.currentUser != null) {
        fetchAllEvents() }
        childFragmentManager.setFragmentResultListener("requestKey",viewLifecycleOwner){requestKey, bundle ->
            val position = bundle.getInt("position")
            // Gửi kết quả lại cho MonthFragment
            Log.d("ktraa", position.toString() + " MonthFragment")
            if(position==0){
                removeEventsForDate(dateSelect)
            }
            eventListAdapter.removeEventAt(position)



        }
        childFragmentManager.setFragmentResultListener("requestUpdate",viewLifecycleOwner){requestKey, bundle ->
            val position = bundle.getInt("position")
           val eventUpdate : Event? = bundle.getParcelable("event")
            Log.d("ktraa", position.toString() + " MonthFragment")
            if (eventUpdate != null) {
                eventListAdapter.updateEventAt(position, eventUpdate)
            }


        }

    }
    private fun toggleCalendarView() {
        if (isWeekView) {
            // Chuyển sang chế độ xem tháng
            binding.calendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit()
            showToast(requireContext(), "thang")
        } else {
            // Chuyển sang chế độ xem 7 ngày
            binding.calendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit()
            showToast(requireContext(), "tuan")
        }
        isWeekView = !isWeekView
    }
    private fun fetchAllEvents() {
        eventsRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val event = document.toObject(Event::class.java)
                try {
                    val date = dateFormat.parse(event.date)
                    if (date != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        val calendarDay = CalendarDay.from(calendar)
                        eventDates.add(calendarDay)
                        Log.d("fetchAllEvents", "Event date added: $calendarDay")
                    } else {
                        Log.e("fetchAllEvents", "Parsed date is null for event date: ${event.date}")
                    }
                } catch (e: ParseException) {
                    Log.e("fetchAllEvents", "Error parsing date: ${event.date}", e)
                }
            }
            val decorator = EventDecorator(Color.RED, eventDates)
            eventDecorators.add(decorator)
            binding.calendarView.addDecorator(decorator)
            Log.d("fetchAllEvents", "Total event dates added: ${eventDates.size}")
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error fetching events: $e", Toast.LENGTH_SHORT).show()
            Log.e("fetchAllEvents", "Error: ${e.message}")
        }
    }


    private fun fetchEventsForDate(date: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(context, "User is not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = FirebaseFirestore.getInstance().collection("User").document(currentUserUid)

        userRef.get().addOnSuccessListener { userDocument ->
            if (userDocument.exists()) {
                val eventIDs = userDocument.get("eventID") as? List<String> ?: emptyList()
                if (eventIDs.isEmpty()) {
                    Toast.makeText(context, "No events for this user.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val eventsRef = FirebaseFirestore.getInstance().collection("Events")
                val partitionedEventIDs = eventIDs.chunked(10) // Chia nhỏ thành từng phần 10 phần tử

                val tasks = partitionedEventIDs.map { part ->
                    eventsRef.whereIn("eventID", part)
                        .whereEqualTo("date", date)
                        .get()
                }

                Tasks.whenAllComplete(tasks).addOnSuccessListener { taskResults ->
                    val events = mutableListOf<Event>()
                    for (taskResult in taskResults) {
                        if (taskResult.isSuccessful) {
                            val documents = (taskResult.result as QuerySnapshot).documents
                            events.addAll(documents.map { it.toObject(Event::class.java)!! })
                        } else {
                            Log.d("Error", "Error fetching events: ${taskResult.exception}")
                        }
                    }

                    // Sắp xếp lại danh sách sự kiện theo timeStart
                    events.sortBy { it.timeStart }

                    if (events.isNotEmpty()) {
                        showEventsBottomSheet(events)
                    } else {
                        Toast.makeText(context, "No events for this date", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Error fetching events: $e", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error fetching user data: $e", Toast.LENGTH_SHORT).show()
        }
    }





    private fun showEventsBottomSheet(events: List<Event>) {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetEventListBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventListAdapter.apply {
                updateEvents(events)
            }
        }

        dialog.show()
    }



    private fun updateHolidays(month: Int) {
        //  val initialHolidays = HolidayData.holidays[m + 1] ?: emptyList()
        val holidaysForMonth = HolidayData.holidays[month] ?: emptyList()
        holidaysAdapter.updateHolidays(holidaysForMonth)
    }

    private fun setUI() {
        binding.calendarView.setTileSizeDp(45) // Set kích thước ô ngày

        val d = calendar.get(Calendar.DAY_OF_MONTH)
        val m = calendar.get(Calendar.MONTH) + 1
        val y = calendar.get(Calendar.YEAR)
        val lunarDate = LunarCalendar().convertSolar2Lunar(d, m, y, 7f)
        val jsonObject = JSONObject(lunarDate)
        val day = jsonObject.getString("lunarDay").toInt()
        val month = jsonObject.getString("lunarMonth").toInt()
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
        //todayDecorator.setDate(CalendarDay.today())
        todayDecorator = TodayDecorator(currentDate)
            binding.calendarView.addDecorator(todayDecorator)
        binding.calendarView.setWeekDayFormatter(CustomWeekDayFormatter())
        binding.today.setOnClickListener {
            binding.calendarView.setCurrentDate(calendar)  // Quay lại ngày hôm nay
            binding.calendarView.setSelectedDate(calendar) // Đánh dấu ngày hôm nay
            // Remove the old decorator
            binding.calendarView.removeDecorator(todayDecorator)
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
            val lunar =
                LunarDecorator(
                    calendarDay,
                    jsonObject.getString("lunarDay").toInt(),
                    jsonObject.getString("lunarMonth").toInt()
                )
            LunarDecorators.add(lunar)
            binding.calendarView.addDecorator(lunar)
            start.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    inner class TodayDecorator(private var date: CalendarDay) : DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day == date
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(resources.getDrawable(R.drawable.current_day))
        }
    }
   inner class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {
        private val dates: HashSet<CalendarDay> = HashSet(dates)
        private val drawable: Drawable = ColorDrawable(color)

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(TopRightDotSpan(10f, color)) // 5f là bán kính của chấm
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
    private fun removeEventsForDate(date: CalendarDay) {
        // Xóa các sự kiện từ eventDates cho ngày đã chọn
        eventDates.remove(date)

        // Xóa các decorator sự kiện hiện tại
        binding.calendarView.removeDecorators()

        // Thêm lại các decorator khác
        for (decorator in LunarDecorators) {
            binding.calendarView.addDecorator(decorator)
        }

        // Cập nhật lại danh sách decorator sự kiện
        eventDecorators.clear()
        val decorator = EventDecorator(Color.RED, eventDates)
        eventDecorators.add(decorator)
        binding.calendarView.addDecorator(decorator)
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
            val month = jsonObject.getString("lunarMonth").toInt()
            val year = jsonObject.getString("lunarYear").toInt()
            val ngayconvat: String? = thoiGianConVat?.getNamConVat(year)
            Log.d("abc", day.toString())
            Log.d("abc", ngayconvat.toString())
            binding.yyyyMAL.text = "$day Tháng $month Âm lịch, năm $ngayconvat"
            binding.yyyyM.text = "Tháng $m - $y"
           // binding.calendarView.addDecorators(TodayDecorator())
            if (mAuth.currentUser != null) {
            fetchEventsForDate(dateString)
                dateSelect = p1
            }
            if (p2){
                // Remove the old decorator
                binding.calendarView.removeDecorator(todayDecorator)

                // Update the current date
                currentDate = p1

                // Add the new decorator for the selected date
                todayDecorator = TodayDecorator(currentDate)
                    binding.calendarView.addDecorator(todayDecorator)
                        binding.calendarView.invalidateDecorators()
            }

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
            binding.yyyyM.text = "Tháng ${month + 1} - $year"
        }
    }

    inner class CustomWeekDayFormatter : WeekDayFormatter {
        private val weekDays = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")

        override fun format(dayOfWeek: Int): CharSequence {
            return weekDays[dayOfWeek - 1]
        }
    }

    inner class EmptyTitleFormatter : TitleFormatter {
        override fun format(day: CalendarDay?): CharSequence {
            return "" // Trả về chuỗi rỗng để ẩn tiêu đề tháng
        }
    }

    override fun onEventClick(event: Event, position: Int) {
        val fragment = EventFragment.newInstance(event, position)
        fragment.show(childFragmentManager, "EventFragment")
    }

}