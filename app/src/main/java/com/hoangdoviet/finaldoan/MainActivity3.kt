package com.hoangdoviet.finaldoan

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.hoangdoviet.finaldoan.databinding.ActivityMain3Binding
import com.hoangdoviet.finaldoan.fragment.RepeatModeFragment
import com.hoangdoviet.finaldoan.utils.RepeatMode
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MainActivity3 : AppCompatActivity(){
    lateinit var binding: ActivityMain3Binding
    private val datePicker by lazy {
        val picker = TimePickerBuilder(this) { date, v ->
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
    var remindMode: RepeatMode = RepeatMode.Never
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

      //  generateJsonFile()
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
            var dialog = RepeatModeFragment()
            dialog.show(supportFragmentManager, "tab")
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


    private fun generateJsonFile() {
        val arList = ArrayList<String>()
        val start = Calendar.getInstance().apply { set(1970, Calendar.JANUARY, 1) }
        val end = Calendar.getInstance().apply { set(2070, Calendar.DECEMBER, 31) }
        while (start <= end) {
            val getDate = getDate(start)
            arList.add(getDate)
            start.add(Calendar.DAY_OF_MONTH, 1)
        }

        val jsonArray = JSONArray(arList)
        val jsonString = jsonArray.toString()

        saveJsonToFile(jsonString)
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

    private fun getDate(calendar: Calendar): String {
        val wd = calendar.get(Calendar.DAY_OF_WEEK)
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        val m = calendar.get(Calendar.MONTH) + 1
        val y = calendar.get(Calendar.YEAR)
        return """{"weekday": "$wd", "day": "$d", "month": "$m", "year": "$y"}"""
    }

    private fun saveJsonToFile(jsonString: String) {
        try {
            val fileName = "dates.json"
            val file = File(filesDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(jsonString.toByteArray())
            fos.close()

            // Di chuyển file này vào thư mục assets sau khi tạo
            val assetsDir = File(filesDir, "../assets")
            if (!assetsDir.exists()) {
                assetsDir.mkdirs()
            }
            file.copyTo(File(assetsDir, fileName), overwrite = true)

            // Hiển thị Toast khi lưu thành công
            Toast.makeText(this, "JSON file saved successfully to assets.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            // Hiển thị Toast khi lưu thất bại
            Toast.makeText(this, "Error saving JSON file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

//    override fun onRepeatModeSelected(mode: String) {
//       binding.value4.text = mode
//    }
}
