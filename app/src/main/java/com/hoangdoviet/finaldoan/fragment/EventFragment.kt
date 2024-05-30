package com.hoangdoviet.finaldoan.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentEventBinding
import com.hoangdoviet.finaldoan.databinding.FragmentOneDayBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventFragment : BottomSheetDialogFragment(), RepeatModeFragment.OnRepeatModeSelectedListener {
    private lateinit var binding: FragmentEventBinding
    lateinit var textDatePicker: String
    lateinit var timeStart: String
    lateinit var timeEnd: String
    private val datePicker by lazy {
        val picker = TimePickerBuilder(requireContext()) { date, v ->
            val formattedDate = formatDate(date)
            Log.i("pvTime", "$formattedDate")
            binding.valueDate.text = formattedDate
            textDatePicker = date.toString()
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
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
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
            val result = createTimePicker(binding.valueDateStart)
            val picker = result.picker
            picker.show()
           timeStart = result.timeFormat
        }
        binding.dateEnd.setOnClickListener {
            val result = createTimePicker(binding.valueDateEnd)
            val picker = result.picker
            picker.show()
           timeEnd = result.timeFormat
        }
        binding.Repeat.setOnClickListener {
            val dialog = RepeatModeFragment()
            dialog.setTargetFragment(this, 0)
            dialog.show(parentFragmentManager, "RepeatModeFragment")
        }
        binding.btnSave.setOnClickListener {
            val temp = binding.valueTitle.text.toString() +"\n"+ textDatePicker +"\n" + binding.valueDateStart.text +"\n"+binding.valueDateEnd.text+"\n"+binding.value4.text
            Log.d("Checkkkk", temp)
            val bundle = Bundle()
            bundle.putString("title", binding.valueTitle.text.toString())
            bundle.putString("textDatePicker", textDatePicker)
            bundle.putString("timeStart", binding.valueDateStart.text.toString())
            bundle.putString("timeEnd", binding.valueDateEnd.text.toString())
            bundle.putString("repeat", binding.value4.text.toString())
            MonthFragment().arguments = bundle
            dismiss()
        }
    }
    private fun createTimePicker(view: TextView): TimePickerResult {
        var timeFormat = ""
        val picker = TimePickerBuilder(view.context) { date, _ ->
             timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
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
        return TimePickerResult(picker, timeFormat)
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
    data class TimePickerResult(val picker: TimePickerView, val timeFormat: String)


}