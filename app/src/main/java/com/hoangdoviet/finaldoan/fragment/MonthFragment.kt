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
            val month = date.month + 1
            val year = date.year
            val dateString = String.format("%02d/%02d/%04d", day, month, year)
            Log.d("checkdatee",dateString)
            fetchEventsForDate(dateString)
        }
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
        binding.calendarView.setTitleFormatter { day ->
            val dateFormat: DateFormat = SimpleDateFormat("LLLL yyyy", locale)
            dateFormat.format(day.getDate())
        }
        binding.calendarView.setTitleFormatter(EmptyTitleFormatter())
        setUI()
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        start.set(y, m, 1)
        end.set(y, m, lastDayOfMonth)
        setLunar(start, end)
        seted["$m/$y"] = ""
        binding.calendarView.setOnDateChangedListener(DateSelectedListener())
        binding.calendarView.setOnMonthChangedListener(MonthChangeListener())
        // Lấy ngày lễ tĩnh
        val initialHolidays = HolidayData.holidays[m + 1]?.map {
            Holiday(it.date, it.description, false)
        } ?: emptyList()

        holidaysAdapter = HolidaysAdapter(initialHolidays, requireContext())
        binding.holidaysRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.holidaysRecyclerView.adapter = holidaysAdapter

        val month = (m + 1).toString().padStart(2, '0')

        loadMonthEventsAndHolidays(month, y.toString())
        //
        eventListAdapter = EventListAdapter(mutableListOf(), this)
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
        // Set up SwipeRefreshLayout
        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // Reload data when user performs swipe-to-refresh
            reloadData()
        }


    }
    private fun reloadData() {
        fetchAllEvents()
        val month = (m + 1).toString().padStart(2, '0')
        loadMonthEventsAndHolidays(month, y.toString())
        // Hide the refresh indicator once data is loaded
        binding.swipeRefreshLayout.isRefreshing = false
    }
    private fun loadMonthEventsAndHolidays(month: String, year: String) {
        val db = FirebaseFirestore.getInstance()
        val eventsInMonth = mutableListOf<Holiday>()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(context, "User is not logged in.", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("User").document(currentUserUid).get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val eventIDs = userDocument.get("eventID") as? List<String> ?: emptyList()
                    if (eventIDs.isEmpty()) {
                        updateHolidaysList(eventsInMonth, month.toInt())
                        return@addOnSuccessListener
                    }
                    val eventIDChunks = eventIDs.chunked(30)
                    val tasks = eventIDChunks.map { chunk ->
                        db.collection("Events").whereIn("eventID", chunk).get()
                    }
                    Tasks.whenAllComplete(tasks).addOnSuccessListener { taskResults ->
                        val allEvents = mutableListOf<Event>()
                        for (taskResult in taskResults) {
                            if (taskResult.isSuccessful) {
                                val documents = (taskResult.result as QuerySnapshot).documents
                                allEvents.addAll(documents.map { it.toObject(Event::class.java)!! })
                            } else {
                                Log.e("MonthFragment", "Error fetching events: ${taskResult.exception}")
                            }
                        }

                        val dateToEventMap = mutableMapOf<String, MutableSet<String>>()

                        for (event in allEvents) {
                            val eventDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(event.date)
                            if (eventDate != null) {
                                val cal = Calendar.getInstance()
                                cal.time = eventDate
                                val repeatMode = event.repeat
                                val eventMonth = cal.get(Calendar.MONTH) + 1
                                val eventYear = cal.get(Calendar.YEAR)

                                when (repeatMode) {
                                    0 -> {
                                        if (eventMonth.toString().padStart(2, '0') == month && eventYear.toString() == year) {
                                            addEventToMap(cal, event.title, dateToEventMap)
                                        }
                                    }
                                    1 -> {
                                        while (cal.get(Calendar.MONTH) + 1 == month.toInt() && cal.get(Calendar.YEAR) == year.toInt()) {
                                            addEventToMap(cal, event.title, dateToEventMap)
                                            cal.add(Calendar.DAY_OF_YEAR, 1)
                                        }
                                    }
                                    2 -> {
                                        while (cal.get(Calendar.MONTH) + 1 == month.toInt() && cal.get(Calendar.YEAR) == year.toInt()) {
                                            if (cal.get(Calendar.DAY_OF_WEEK) in Calendar.MONDAY..Calendar.FRIDAY) {
                                                addEventToMap(cal, event.title, dateToEventMap)
                                            }
                                            cal.add(Calendar.DAY_OF_YEAR, 1)
                                        }
                                    }
                                    3 -> {
                                        while (cal.get(Calendar.MONTH) + 1 == month.toInt() && cal.get(Calendar.YEAR) == year.toInt()) {
                                            addEventToMap(cal, event.title, dateToEventMap)
                                            cal.add(Calendar.WEEK_OF_YEAR, 1)
                                        }
                                    }
                                    4 -> {
                                        while (cal.get(Calendar.YEAR) == year.toInt() && (cal.get(Calendar.MONTH) + 1 <= month.toInt())) {
                                            if (cal.get(Calendar.MONTH) + 1 == month.toInt()) {
                                                addEventToMap(cal, event.title, dateToEventMap)
                                            }
                                            cal.add(Calendar.MONTH, 1)
                                        }
                                    }
                                    5 -> {
                                        while (cal.get(Calendar.YEAR) <= year.toInt()) {
                                            if (cal.get(Calendar.YEAR) == year.toInt() && cal.get(Calendar.MONTH) + 1 == month.toInt()) {
                                                addEventToMap(cal, event.title, dateToEventMap)
                                            }
                                            cal.add(Calendar.YEAR, 1)
                                        }
                                    }
                                }
                            }
                        }
                        // Chuyển đổi map sang danh sách Holiday
                        for ((date, titlesSet) in dateToEventMap) {
                            val titles = titlesSet.joinToString("\n")
                            eventsInMonth.add(Holiday(date, titles, false))
                        }
                        // Kết hợp ngày lễ tĩnh và sự kiện của người dùng, hợp nhất nếu trùng ngày
                        val initialHolidays = HolidayData.holidays[month.toInt()] ?: emptyList()
                        val combinedItems = mutableListOf<Holiday>()
                        // Thêm các ngày lễ tĩnh vào danh sách kết hợp
                        for (holiday in initialHolidays) {
                            if (dateToEventMap.containsKey(holiday.date)) {
                                val combinedTitle = holiday.description + "\n" + dateToEventMap[holiday.date]!!.joinToString("\n")
                                combinedItems.add(Holiday(holiday.date, combinedTitle, true))
                                dateToEventMap.remove(holiday.date)
                            } else {
                                combinedItems.add(holiday)
                            }
                        }
                        // Thêm các sự kiện còn lại không trùng với ngày lễ tĩnh
                        for ((date, titlesSet) in dateToEventMap) {
                            val titles = titlesSet.joinToString("\n")
                            combinedItems.add(Holiday(date, titles, true))
                        }
                        // Sắp xếp danh sách combinedItems theo ngày
                        combinedItems.sortBy { SimpleDateFormat("d/M", Locale.getDefault()).parse(it.date) }
                        holidaysAdapter.updateHolidays(combinedItems)
                    }.addOnFailureListener { e ->
                        Log.e("MonthFragment", "Error loading events", e)
                    }
                } else {
                    updateHolidaysList(eventsInMonth, month.toInt())
                    Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching user data: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addEventToMap(cal: Calendar, title: String, dateToEventMap: MutableMap<String, MutableSet<String>>) {
        val newDateFormat = SimpleDateFormat("d/M", Locale.getDefault())
        val formattedDate = newDateFormat.format(cal.time)
        if (dateToEventMap.containsKey(formattedDate)) {
            dateToEventMap[formattedDate]!!.add(title)
        } else {
            dateToEventMap[formattedDate] = mutableSetOf(title)
        }
    }
    private fun updateHolidaysList(eventsInMonth: MutableList<Holiday>, month: String) {
        val initialHolidays = HolidayData.holidays[month.toInt()] ?: emptyList()
        val combinedItems = initialHolidays + eventsInMonth
        holidaysAdapter.updateHolidays(combinedItems)
    }
    private fun fetchAllEvents() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            context?.let {
                Toast.makeText(it, "User is not logged in.", Toast.LENGTH_SHORT).show()
            }
            return
        }
        db.collection("User").document(currentUserUid).get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val eventIDs = userDocument.get("eventID") as? List<String> ?: emptyList()
                    if (eventIDs.isEmpty()) {
                        return@addOnSuccessListener
                    }
                    val eventIDChunks = eventIDs.chunked(30)
                   // val allEvents = mutableListOf<Event>()
                    eventIDChunks.forEachIndexed { index, chunk ->
                        db.collection("Events").whereIn("eventID", chunk).get()
                            .addOnSuccessListener { documents ->
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
                                if (index == eventIDChunks.lastIndex && isAdded) {
                                    context?.let {
                                        val decorator = EventDecorator(ContextCompat.getColor(it, R.color.grey), eventDates)
                                        eventDecorators.add(decorator)
                                        binding.calendarView.addDecorator(decorator)
                                        Log.d("fetchAllEvents", "Total event dates added: ${eventDates.size}")
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                context?.let {
                                    Toast.makeText(it, "Error fetching events: $e", Toast.LENGTH_SHORT).show()
                                }
                                Log.e("fetchAllEvents", "Error: ${e.message}")
                            }
                    }
                } else {
                    context?.let {
                        Toast.makeText(it, "User data not found.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                context?.let {
                    Toast.makeText(it, "Error fetching user data: $e", Toast.LENGTH_SHORT).show()
                }
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
                    return@addOnSuccessListener
                }
                val eventsRef = FirebaseFirestore.getInstance().collection("Events")
                val partitionedEventIDs = eventIDs.chunked(10)
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
                    events.sortBy { it.timeStart }
                    if (events.isNotEmpty()) {
                        showEventsBottomSheet(events)
                    } else {
                        //Toast.makeText(context, "No events for this date", Toast.LENGTH_SHORT).show()
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


    private fun updateHolidays1(month: Int) {
        val holidaysForMonth = HolidayData.holidays[month] ?: emptyList()
        holidaysAdapter.updateHolidays(holidaysForMonth)
    }
    private fun updateHolidays(month: Int, year: String) {
        val db = FirebaseFirestore.getInstance()
        val eventsInMonth = mutableListOf<Holiday>()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(context, "User is not logged in.", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("User").document(currentUserUid).get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val eventIDs = userDocument.get("eventID") as? List<String> ?: emptyList()
                    if (eventIDs.isEmpty()) {
                        updateHolidaysList(eventsInMonth, month)
                        return@addOnSuccessListener
                    }
                    val eventIDChunks = eventIDs.chunked(30)
                    val tasks = eventIDChunks.map { chunk ->
                        db.collection("Events").whereIn("eventID", chunk).get()
                    }
                    Tasks.whenAllComplete(tasks).addOnSuccessListener { taskResults ->
                        val allEvents = mutableListOf<Event>()
                        for (taskResult in taskResults) {
                            if (taskResult.isSuccessful) {
                                val documents = (taskResult.result as QuerySnapshot).documents
                                allEvents.addAll(documents.map { it.toObject(Event::class.java)!! })
                            } else {
                                Log.e("MonthFragment", "Error fetching events: ${taskResult.exception}")
                            }
                        }

                        val dateToEventMap = mutableMapOf<String, MutableSet<String>>()

                        for (event in allEvents) {
                            val eventDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(event.date)
                            if (eventDate != null) {
                                val cal = Calendar.getInstance()
                                cal.time = eventDate
                                val repeatMode = event.repeat
                                val eventMonth = cal.get(Calendar.MONTH) + 1
                                val eventYear = cal.get(Calendar.YEAR)

                                when (repeatMode) {
                                    0 -> {
                                        if (eventMonth == month && eventYear.toString() == year) {
                                            addEventToMap(cal, event.title, dateToEventMap)
                                        }
                                    }
                                    1 -> {
                                        while (cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year.toInt()) {
                                            addEventToMap(cal, event.title, dateToEventMap)
                                            cal.add(Calendar.DAY_OF_YEAR, 1)
                                        }
                                    }
                                    2 -> {
                                        while (cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year.toInt()) {
                                            if (cal.get(Calendar.DAY_OF_WEEK) in Calendar.MONDAY..Calendar.FRIDAY) {
                                                addEventToMap(cal, event.title, dateToEventMap)
                                            }
                                            cal.add(Calendar.DAY_OF_YEAR, 1)
                                        }
                                    }
                                    3 -> {
                                        while (cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year.toInt()) {
                                            addEventToMap(cal, event.title, dateToEventMap)
                                            cal.add(Calendar.WEEK_OF_YEAR, 1)
                                        }
                                    }
                                    4 -> {
                                        while (cal.get(Calendar.YEAR) == year.toInt() && (cal.get(Calendar.MONTH) + 1 <= month)) {
                                            if (cal.get(Calendar.MONTH) + 1 == month) {
                                                addEventToMap(cal, event.title, dateToEventMap)
                                            }
                                            cal.add(Calendar.MONTH, 1)
                                        }
                                    }
                                    5 -> {
                                        while (cal.get(Calendar.YEAR) <= year.toInt()) {
                                            if (cal.get(Calendar.YEAR) == year.toInt() && cal.get(Calendar.MONTH) + 1 == month) {
                                                addEventToMap(cal, event.title, dateToEventMap)
                                            }
                                            cal.add(Calendar.YEAR, 1)
                                        }
                                    }
                                }
                            }
                        }
                        for ((date, titlesSet) in dateToEventMap) {
                            val titles = titlesSet.joinToString("\n")
                            eventsInMonth.add(Holiday(date, titles, false))
                        }
                        val initialHolidays = HolidayData.holidays[month] ?: emptyList()
                        val combinedItems = mutableListOf<Holiday>()
                        for (holiday in initialHolidays) {
                            if (dateToEventMap.containsKey(holiday.date)) {
                                val combinedTitle = holiday.description + "\n" + dateToEventMap[holiday.date]!!.joinToString("\n")
                                combinedItems.add(Holiday(holiday.date, combinedTitle, true))
                                dateToEventMap.remove(holiday.date)
                            } else {
                                combinedItems.add(holiday)
                            }
                        }
                        for ((date, titlesSet) in dateToEventMap) {
                            val titles = titlesSet.joinToString("\n")
                            combinedItems.add(Holiday(date, titles, true))
                        }
                        combinedItems.sortBy { SimpleDateFormat("d/M", Locale.getDefault()).parse(it.date) }
                        holidaysAdapter.updateHolidays(combinedItems)
                    }.addOnFailureListener { e ->
                        Log.e("MonthFragment", "Error loading events", e)
                    }
                } else {
                    updateHolidaysList(eventsInMonth, month)
                    Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching user data: $e", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateHolidaysList(eventsInMonth: MutableList<Holiday>, month: Int) {
        val initialHolidays = HolidayData.holidays[month] ?: emptyList()
        val combinedItems = initialHolidays + eventsInMonth
        holidaysAdapter.updateHolidays(combinedItems)
    }



    private fun setUI() {
        binding.calendarView.setTileSizeDp(45)
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

        todayDecorator = TodayDecorator(currentDate)
        if (isAdded) {
            binding.calendarView.addDecorator(todayDecorator)
        }
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
            if (isAdded) {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.current_day)
                drawable?.let {
                    view.setBackgroundDrawable(it)
                }
            }
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
        if (isAdded) {
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
            if (p2 && isAdded){
                binding.calendarView.removeDecorator(todayDecorator)
                currentDate = p1
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
            if (mAuth.currentUser != null) {
            updateHolidays(month + 1, year.toString())}
            else {
                updateHolidays1(month+1)
            }
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
            return ""
        }
    }

    override fun onEventClick(event: Event, position: Int) {
        val fragment = EventFragment.newInstance(event, position)
        fragment.show(childFragmentManager, "EventFragment")
    }

}