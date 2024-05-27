package com.hoangdoviet.finaldoan.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.adapter.OneDayAdapter
import com.hoangdoviet.finaldoan.databinding.FragmentDayBinding
import com.hoangdoviet.finaldoan.model.LunarCalendar
import com.hoangdoviet.finaldoan.model.ThoiGianConVat
import com.hoangdoviet.finaldoan.utils.DateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class DayFragment : Fragment() {

    private var _binding: FragmentDayBinding? = null
    private val binding get() = _binding!!

    private var currentDate = Calendar.getInstance()
    private lateinit var threadTime: CountTime
    private var wd = 0
    private var d = 0
    private var m = 0
    private var y = 0
    private lateinit var today: String
    private lateinit var thoiGianConVat: ThoiGianConVat
    private lateinit var ngayConVat: String
    private lateinit var thangconvat: String
    private var fixMonthLunar = 0
    private var indexFinded = 0
    private lateinit var start: Calendar
    private lateinit var end: Calendar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDate()
    }

    private fun setDate() {
        CoroutineScope(Dispatchers.Main).launch {
            val dates = withContext(Dispatchers.IO) {
                DateManager.loadDatesFromAssets(requireContext())
            }
            // Tính toán indexFinded sau khi dates được tải
            wd = currentDate.get(Calendar.DAY_OF_WEEK)
            d = currentDate.get(Calendar.DAY_OF_MONTH)
            m = currentDate.get(Calendar.MONTH) + 1
            y = currentDate.get(Calendar.YEAR)
            thoiGianConVat = ThoiGianConVat(null)
            ngayConVat = thoiGianConVat.getNgayConVat(d, m, y)
            today = """{"weekday": "$wd", "day": "$d", "month": "$m", "year": "$y"}"""
            indexFinded = findIndexFromPosition(today, 19868, dates)
            Log.d("vi tri", indexFinded.toString())
            setupViewPager(dates)
        }
    }

    private fun findIndexFromPosition(target: String, startPos: Int, list: List<String>): Int {
        for (i in startPos until list.size) {
            if (list[i] == target) {
                return i
            }
        }
        return 0 // Nếu không tìm thấy, trả về 0 hoặc bạn có thể xử lý khác nếu muốn
    }

    private fun setupViewPager(dates: ArrayList<String>) {
        binding.viewpager.adapter = OneDayAdapter(childFragmentManager, dates)
        binding.viewpager.currentItem = indexFinded
        binding.dayYearInOneDay.text = "$m - $y"

        var lunarDate = LunarCalendar().convertSolar2Lunar(d, m, y, 7f)
        var jsonObject = JSONObject(lunarDate)
        d = jsonObject.getString("lunarDay").toInt()
        m = jsonObject.getString("lunarMonth").toInt()
        fixMonthLunar = m
        y = jsonObject.getString("lunarYear").toInt()
        thangconvat = thoiGianConVat.getThangConVat(m, y)
        binding.ngayAmLichInOneDay.text = "Ngày\n$d\n$ngayConVat"
        binding.thangAmLichInOneDay.text = "Tháng\n$m\n$thangconvat"

        var lastPosition = indexFinded
        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                // Xử lý lựa chọn trang ở đây
                jsonObject = JSONObject(dates[position])
                d = jsonObject.getString("day").toInt()
                m = jsonObject.getString("month").toInt()
                y = jsonObject.getString("year").toInt()
                binding.dayYearInOneDay.text = "$m-$y"
                ngayConVat = thoiGianConVat.getNgayConVat(d, m, y)
                lunarDate = LunarCalendar().convertSolar2Lunar(jsonObject.getString("day").toInt(),
                    jsonObject.getString("month").toInt(),
                    jsonObject.getString("year").toInt(),
                    7f)
                jsonObject = JSONObject(lunarDate)
                d = jsonObject.getString("lunarDay").toInt()
                m = jsonObject.getString("lunarMonth").toInt()
                y = jsonObject.getString("lunarYear").toInt()

                thangconvat = thoiGianConVat.getThangConVat(m, y) //m, y lunar

                binding.ngayAmLichInOneDay.text = "Ngày\n$d\n$ngayConVat"
                binding.thangAmLichInOneDay.text = "Tháng\n$m\n$thangconvat"
                if (position > lastPosition) {
                    //Log.d("aaaaaaaaaa", "left")
                }
                if (position < lastPosition) {
                    //Log.d("aaaaaaaaaa", "right")
                }
                lastPosition = position
            }
        })

        binding.toDayInOneDay.setOnClickListener {
            indexFinded = dates.indexOf(today)
            binding.viewpager.currentItem = indexFinded
        }
    }

    private fun getDate(calendar: Calendar): String {
        val wd = calendar.get(Calendar.DAY_OF_WEEK)
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        val m = calendar.get(Calendar.MONTH) + 1
        val y = calendar.get(Calendar.YEAR)
        return "{'weekday': '$wd' ,'day': '$d', 'month': '$m', 'year': '$y'}"
    }

    override fun onResume() {
        super.onResume()
        threadTime = CountTime()
        threadTime.start()
    }

    override fun onPause() {
        super.onPause()
        threadTime.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class CountTime : Runnable {

        private val thread = Thread(this)
        private val timer = Timer()
        private var minute = 0

        fun start() {
            thread.start()
        }

        fun cancel() {
            thread.join()
        }

        @SuppressLint("SetTextI18n")
        override fun run() {
            timer.scheduleAtFixedRate(0, 2000) {
                val currentDatee = Calendar.getInstance()
                val hourss = currentDatee.get(Calendar.HOUR_OF_DAY)
                val minutee = currentDatee.get(Calendar.MINUTE)
                val thoiGianConVatt = ThoiGianConVat(currentDatee.timeInMillis)
                val canhgio = thoiGianConVatt.getCanhGio(m)
                activity?.runOnUiThread {
                    if (minutee < 10)
                        binding.timeNowInOneDay.text = "Giờ\n$hourss:0$minutee\n$canhgio"
                    else
                        binding.timeNowInOneDay.text = "Giờ\n$hourss:$minutee\n$canhgio"
                }
            }
        }
    }
}
