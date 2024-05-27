package com.hoangdoviet.finaldoan.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.hoangdoviet.finaldoan.MainActivity
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentOneDayBinding
import com.hoangdoviet.finaldoan.model.RandomOn
import org.json.JSONObject


class OneDayFragment : Fragment() {
    private lateinit var arrStringFromXml: Array<String>
    private lateinit var binding: FragmentOneDayBinding
    private val arrayWeekDay = mapOf(
        "2" to "Thứ hai",
        "3" to "Thứ ba", "4" to "Thứ tư",
        "5" to "Thứ năm", "6" to "Thứ sáu",
        "7" to "Thứ Bảy", "1" to "Chủ Nhật"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOneDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val random = RandomOn()

        arrStringFromXml = resources.getStringArray(R.array.quotes)
        binding.quotes.text = arrStringFromXml[random.random(0, arrStringFromXml.size)]
        val bundle = arguments
        val data = bundle!!.getString("demo")
        val jsonObject = JSONObject(data)
        var wd = jsonObject.getString("weekday")
        val d = jsonObject.getString("day")

        binding.day.text = d
        for (i in arrayWeekDay) {
            if (i.key == wd)
                wd = i.value
        }
        binding.weekdayString.text = wd
        // Truy cập Activity binding
        binding.relativelayoutfragmentoneday.setOnClickListener {
            //Toast.makeText(requireContext(), "Dang cham", Toast.LENGTH_SHORT).show()
            val activityBinding = (activity as? MainActivity)?.getActivityBinding()
//            activityBinding?.let {
//                it.fabOption1.visibility = View.GONE
//                it.fabOption2.visibility = View.GONE
//                it.fab.visibility = View.VISIBLE
//                it.fabBackground.visibility = View.GONE
//            }
        }
    }
}