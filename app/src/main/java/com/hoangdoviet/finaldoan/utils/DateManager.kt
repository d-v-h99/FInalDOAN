package com.hoangdoviet.finaldoan.utils

import android.content.Context
import org.json.JSONArray

object DateManager {
    lateinit var dates: ArrayList<String>

    fun loadDatesFromAssets(context: Context): ArrayList<String> {
        if (!::dates.isInitialized) {
            dates = ArrayList()
            try {
                val inputStream = context.assets.open("dates.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()

                val jsonString = String(buffer, Charsets.UTF_8)
                val jsonArray = JSONArray(jsonString)

                for (i in 0 until jsonArray.length()) {
                    dates.add(jsonArray.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return dates
    }
}
