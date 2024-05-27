package com.hoangdoviet.finaldoan.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentEventBinding
import com.hoangdoviet.finaldoan.databinding.FragmentOneDayBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventFragment : Fragment() , RepeatModeFragment.OnRepeatModeSelectedListener {
    private lateinit var binding: FragmentEventBinding
    private val datePicker by lazy {
        val picker = TimePickerBuilder(requireContext()) { date, v ->
            val formattedDate = formatDate(date)
            Log.i("pvTime", "$formattedDate")
            binding.valueDate.text = formattedDate
        }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .setCancelText("Huỷ")//取消按钮文字
            .setSubmitText("Xác nhận")//确认按钮文字
            .addOnCancelClickListener { Log.i("pvTime", "onCancelClickListener") }
            .setItemVisibleCount(5) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(4.0f)
            .isAlphaGradient(false)
            .build()

        val mDialog: Dialog = picker.dialog
        val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )
        params.leftMargin = 0
        params.rightMargin = 0
        picker.dialogContainerLayout.layoutParams = params
        mDialog.window?.apply {
            setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //修改动画样式
            setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
            setDimAmount(0.3f)
        }
        picker
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.date.setOnClickListener {
            datePicker.show()
        }
        binding.dateStart.setOnClickListener {
            val timePicker = createTimePicker(binding.valueDateStart)
            timePicker.show()
        }
        binding.dateEnd.setOnClickListener {
            val timePicker = createTimePicker(binding.valueDateEnd)
            timePicker.show()
        }
        binding.Repeat.setOnClickListener {
            val dialog = RepeatModeFragment()
            dialog.setTargetFragment(this, 0)
            dialog.show(parentFragmentManager, "RepeatModeFragment")
        }
    }
    private fun createTimePicker(view: TextView): TimePickerView {
        val picker = TimePickerBuilder(view.context) { date, _ ->
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            // Cập nhật giá trị lên TextView tương ứng
            view.text = timeFormat
        }
            .setType(booleanArrayOf(false, false, false, true, true, false))
            .setCancelText("Huỷ") // Nút Huỷ
            .setSubmitText("Xác nhận") // Nút Xác nhận
            .isDialog(true) // Hiển thị dưới dạng Dialog
            .addOnCancelClickListener { Log.i("pvTime", "onCancelClickListener") }
            .setItemVisibleCount(5) // Số lượng item hiển thị
            .setLineSpacingMultiplier(4.0f) // Khoảng cách giữa các dòng
            .isAlphaGradient(false) // Không sử dụng gradient alpha
            .build()

        val mDialog: Dialog = picker.dialog
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )
        params.leftMargin = 0
        params.rightMargin = 0
        picker.dialogContainerLayout.layoutParams = params
        mDialog.window?.apply {
            setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) // Hiệu ứng Animation
            setGravity(Gravity.BOTTOM) // Hiển thị ở phía dưới
            setDimAmount(0.3f) // Độ mờ của nền
        }
        return picker
    }
    fun formatDate(date: Date): String {
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val day = dayFormat.format(date)
        val month = monthFormat.format(date)
        val year = yearFormat.format(date)

        return "Ngày $day tháng $month năm $year"
    }

    override fun onRepeatModeSelected(mode: String) {
      binding.value4.text = mode
    }

}