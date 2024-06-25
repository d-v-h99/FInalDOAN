package com.hoangdoviet.finaldoan.model

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.google.ai.client.generativeai.type.content
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.hoangdoviet.finaldoan.MainActivity2
import com.hoangdoviet.finaldoan.utils.showToast

class EventCreator(
    private val context: Context,
    private val service: com.google.api.services.calendar.Calendar,
    private val calendarId: String,
    private val event: Event
) : AsyncTask<Void, Void, Event?>() {

    override fun doInBackground(vararg params: Void?): Event? {
        return try {
            service.events().insert(calendarId, event).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            cancel(true)
            null
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun onPostExecute(result: Event?) {
        super.onPostExecute(result)
      showToast(context, "Thêm sự kiện vào Google Lịch thành công")
    }

    override fun onCancelled() {
        super.onCancelled()
    }
}