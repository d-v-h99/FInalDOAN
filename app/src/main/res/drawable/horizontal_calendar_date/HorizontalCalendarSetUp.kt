package com.arjungupta08.horizontal_calendar_date

import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.hoangdoviet.finaldoan.horizontal_calendar_date.CalendarDateModel
import java.text.SimpleDateFormat
import java.util.*

class HorizontalCalendarSetUp {

    private val sdf = SimpleDateFormat("MMMM yyyy", Locale("vi", "VN"))
    private val cal = Calendar.getInstance(Locale("vi", "VN"))

    //    private val currentDate = Calendar.getInstance(Locale("vi", "VN"))
    private lateinit var adapter: HorizontalCalendarAdapter
    private val calendarList2 = ArrayList<CalendarDateModel>()

    /*
     * Set up click listener
     */
    fun setUpCalendarPrevNextClickListener(
        ivCalendarNext: ImageView,
        ivCalendarPrevious: ImageView,
        listener: HorizontalCalendarAdapter.OnItemClickListener,
        month: (String) -> Unit
    ) {
        ivCalendarNext.setOnClickListener {
            cal.add(Calendar.MONTH, 1)
            val monthDate = setUpCalendar(listener)
            month.invoke(monthDate)
        }
        ivCalendarPrevious.setOnClickListener {
            cal.add(Calendar.MONTH, -1)
            val monthDate = setUpCalendar(listener)
            month.invoke(monthDate)
        }
    }

    /*
     * Setting up adapter for recyclerview
     */
    fun setUpCalendarAdapter(
        recyclerView: RecyclerView,
        listener: HorizontalCalendarAdapter.OnItemClickListener
    ): String {
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        // Tạo một instance của HorizontalCalendarAdapter với một lambda function để đánh dấu mục được chọn.
        adapter = HorizontalCalendarAdapter { calendarDateModel: CalendarDateModel, position: Int ->
            calendarList2.forEachIndexed { index, calendarModel ->
                calendarModel.isSelected = index == position
            }

            // Sau khi cập nhật isSelected, cuộn đến vị trí được chọn.
            // Phương thức post của RecyclerView được sử dụng để đảm bảo rằng đoạn mã cuộn sẽ được thực thi sau khi RecyclerView đã hoàn thành việc đo và bố trí các mục.
//            // => Điều này là cần thiết vì nếu bạn gọi scrollToPositionWithOffset ngay lập tức, RecyclerView có thể chưa đo xong các mục và do đó không thể xác định đúng kích thước và vị trí của các mục.
//            recyclerView.post {
//                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                //Điều này là cần thiết vì phương thức scrollToPositionWithOffset chỉ có trong LinearLayoutManager.
//                // Phương thức này cuộn RecyclerView đến vị trí position và thêm một khoảng cách bù (offset).
//                layoutManager.scrollToPositionWithOffset(
//                    position,
//                    (recyclerView.width / 2) - (recyclerView.getChildAt(0)?.width ?: 0) / 2
//                    //recyclerView.width / 2: Lấy chiều rộng của RecyclerView và chia đôi để xác định trung tâm của RecyclerView.
//                    //recyclerView.getChildAt(0)?.width ?: 0: Lấy chiều rộng của mục đầu tiên trong RecyclerView. Nếu mục đầu tiên không tồn tại (tức là null), giá trị mặc định là 0.
//
//                    //Tính toán khoảng cách từ cạnh trái của RecyclerView đến trung tâm của RecyclerView, sau đó trừ đi một nửa chiều rộng của mục để đảm bảo rằng mục đó sẽ hiển thị ở giữa màn hình.
//                )
//            }
        }
        adapter.setData(calendarList2)
        adapter.setOnItemClickListener(listener)
        recyclerView.adapter = adapter

        val monthDate = setUpCalendar(listener)
        scrollToToday(recyclerView) // Cuộn đến ngày hôm nay khi thiết lập xong adapter
        triggerTodayClick()
        return monthDate
    }

    /*
     * Function to setup calendar for every month
     */
    private fun setUpCalendar(listener: HorizontalCalendarAdapter.OnItemClickListener): String {
        val calendarList = ArrayList<CalendarDateModel>()
        val monthCalendar = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val today = Calendar.getInstance()
//        today.set(Calendar.YEAR, 2024)
//        today.set(Calendar.MONTH, Calendar.JUNE)
//        today.set(Calendar.DAY_OF_MONTH, 21) // Xét ngày hiện tại là 1/6/2024

        for (day in 1..maxDaysInMonth) {
//            val isToday = (monthCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
//                    monthCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
//                    monthCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))
//
//            calendarList.add(CalendarDateModel(monthCalendar.time, isToday))
//            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
            if(day==today.get(Calendar.DAY_OF_MONTH)){
                calendarList.add(CalendarDateModel(monthCalendar.time, true))
            } else {
                calendarList.add(CalendarDateModel(monthCalendar.time))
            }
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendarList2.clear()
        calendarList2.addAll(calendarList)
        adapter.setOnItemClickListener(listener)
        adapter.setData(calendarList)
        Log.d("checkLich", sdf.format(cal.time))
        return sdf.format(cal.time)
    }

    /*
     * Scroll to today's date
     */
    private fun scrollToToday(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val todayPosition = calendarList2.indexOfFirst { it.isSelected }

        if (todayPosition != -1) {
            recyclerView.post {
                layoutManager.scrollToPositionWithOffset(
                    todayPosition,
                    (recyclerView.width / 2) - (recyclerView.getChildAt(0)?.width ?: 0) / 2
                )
            }
        }
    }
    private fun triggerTodayClick() {
        val todayPosition = calendarList2.indexOfFirst { it.isSelected }
        if (todayPosition != -1) {
            adapter.triggerOnItemClick(todayPosition)
        }
    }
}
