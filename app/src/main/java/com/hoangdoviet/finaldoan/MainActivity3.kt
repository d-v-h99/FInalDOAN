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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        //  generateJsonFile()
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

}
