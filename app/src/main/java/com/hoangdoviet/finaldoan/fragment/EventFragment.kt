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
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentEventBinding
import com.hoangdoviet.finaldoan.databinding.FragmentOneDayBinding
import com.hoangdoviet.finaldoan.model.Event
import com.hoangdoviet.finaldoan.utils.RepeatMode
import com.hoangdoviet.finaldoan.utils.addYearsToDate
import com.hoangdoviet.finaldoan.utils.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EventFragment : BottomSheetDialogFragment(), RepeatModeFragment.OnRepeatModeSelectedListener {
    private lateinit var binding: FragmentEventBinding
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    lateinit var textDatePicker: String
    lateinit var timeStart: String
    lateinit var timeEnd: String
    var idRadio: Int = 0
    lateinit var eventDelete: Event
    private var position: Int = -1
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            eventDelete = it.getParcelable("event")!!
//            position = it.getInt("position")
//        }
//    }


    companion object {
        @JvmStatic
        fun newInstance(event: Event, position: Int) =
            EventFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("event", event)
                    putInt("position", position)
                }
            }
    }

    private val datePicker by lazy {
        val picker = TimePickerBuilder(requireContext()) { date, v ->
            val formattedDate = formatDate(date,true)
            Log.i("pvTime", "$formattedDate")
            binding.valueDate.text = formattedDate
            textDatePicker = formatDate(date,false)
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
    private fun populateEventDetails(event: Event) {
        binding.valueTitle.setText(event.title)
        binding.valueDate.text = event.date
        binding.valueDateStart.text = event.timeStart
        binding.valueDateEnd.text = event.timeEnd
        binding.value4.text = when(event.repeat){
            0->"Không bao giờ"
            1->"Hàng ngày"
            2->"Ngày làm việc"
            3->"Hàng tuần"
            4->"Hàng tháng"
            else -> "Hàng năm"
        }
        binding.btnSave.text = "Sửa sự kiện"
        binding.button2.text = "Xoá sự kiện"
        // Thiết lập các chi tiết sự kiện khác nếu cần
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<Event>("event")?.let { event ->
            populateEventDetails(event)
            eventDelete = event
        }
        arguments?.let {
            position = it.getInt("position")
        }

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
            try {
                val currentUserUid = mAuth.currentUser?.uid
                if (currentUserUid == null) {
                    showToast(requireContext(), "User is not logged in.")
                    return@setOnClickListener
                }

                val event = Event(
                    eventID = generateEventId(),
                    date = textDatePicker,
                    title = binding.valueTitle.text.toString(),
                    timeStart = binding.valueDateStart.text.toString(),
                    timeEnd = binding.valueDateEnd.text.toString(),
                    repeat = idRadio
                )

                if (idRadio == 0) {
                    // Sự kiện đơn lẻ
                    addSingleEvent(currentUserUid, event)
                } else {
                    // Sự kiện lặp lại
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dateStart = dateFormat.parse(event.date)
                    val dateEnd = addYearsToDate(dateStart, 1)
                    val endDateFormatted = dateFormat.format(dateEnd)
                    val repeatModeClick = when(event.repeat){
                        1->RepeatMode.Day
                        2->RepeatMode.WorkDay
                        3->RepeatMode.Week
                        4->RepeatMode.Month
                        else -> RepeatMode.Year
                    }

                    addEventWithRepeats(currentUserUid, event, repeatModeClick, endDateFormatted)
                }
//                deleteEvent("e0c6c8ef-f673-4d96-b4e8-01bb4aa3cfd0")

            } catch (e: Exception) {
                Log.e("EventFragment", "Error creating event", e)
            }
//            deleteEvent("779e81b9-e0a0-41e9-9cad-fd81c6e849ed")
//            deleteAllRepeatingEvents("2055ddb4-c907-4c5b-b71a-c496b8edc4a8")
//            deleteFutureRepeatingEvents("5197007d-1143-4182-aae0-5fdbcaf07483")
        }
        binding.button2.setOnClickListener {
            if(binding.button2.text == "Xoá sự kiện"){
                if(eventDelete.originalEventID.isNotEmpty()){
                    //showDeleteEventDialog(eventDelete)
                    eventDelete?.let { event ->
                        val dialog = DeleteEventModeFragment.newInstance(event, position)
                        dialog.setTargetFragment(this, 0)
                        dialog.show(parentFragmentManager, "DeleteEventModeFragment")
                    }
                }else {
                    deleteEvent(eventDelete.eventID)
                    showToast(requireContext(), "Xoá sự kiẹn thành công")
                    //dismiss()
                }

            }

        }
        // Lắng nghe kết quả từ DeleteEventModeFragment
        setFragmentResultListener("deleteRequestKey") { requestKey, bundle ->
            val position = bundle.getInt("position")
            // Gửi kết quả lại cho MonthFragment
                setFragmentResult("requestKey", Bundle().apply {
                putInt("position", position)
                Log.d("ktraa", position.toString() + " EventModeFragment")
            })
            dismiss()
        }



    }
//    private fun showDeleteEventDialog(event: Event) {
//        val dialog = DeleteEventModeFragment.newInstance(event)
//        dialog.setTargetFragment(this, 0)
//        dialog.show(parentFragmentManager, "DeleteEventModeFragment")
//    }
    fun addEventWithRepeats(userId: String, event: Event, repeatMode: RepeatMode, endDate: String) {
        val db = FirebaseFirestore.getInstance()
        val events = createRepeatingEvents(event, repeatMode, endDate)

        val batch = db.batch()
        val userRef = db.collection("User").document(userId)

        // Lưu sự kiện gốc
        val originalEvent = events.firstOrNull()
        originalEvent?.let {
            val originalEventRef = db.collection("Events").document(it.eventID)
            batch.set(originalEventRef, it)
            batch.update(userRef, "eventID", FieldValue.arrayUnion(it.eventID))
        }

        // Lưu các sự kiện lặp lại còn lại
        events.drop(1).forEach { repeatingEvent ->
            val eventRef = db.collection("Events").document(repeatingEvent.eventID)
            batch.set(eventRef, repeatingEvent)
            batch.update(userRef, "eventID", FieldValue.arrayUnion(repeatingEvent.eventID))
        }

        batch.commit().addOnSuccessListener {
            showToast(requireContext(), "Repeating events added and user updated successfully")
            dismiss() // Chỉ đóng giao diện khi lưu thành công
        }.addOnFailureListener { e ->
            showToast(requireContext(), "Error adding repeating events: $e")
        }
    }




    fun addSingleEvent(userId: String, event: Event) {
        val db = FirebaseFirestore.getInstance()
        val eventRef = db.collection("Events").document(event.eventID)

        eventRef.set(event)
            .addOnSuccessListener {
                // Thêm eventId vào danh sách eventID của người dùng
                db.collection("User").document(userId)
                    .update("eventID", FieldValue.arrayUnion(event.eventID))
                    .addOnSuccessListener {
                        showToast(requireContext(), "Event added and user updated successfully")
                        dismiss() // Chỉ đóng giao diện khi lưu thành công
                    }
                    .addOnFailureListener { e ->
                        showToast(requireContext(), "Error updating user: $e")
                    }
            }
            .addOnFailureListener { e ->
                showToast(requireContext(), "Error adding event: $e")
            }
    }
    private fun deleteEvent(eventId: String) {
        val currentUserUid = mAuth.currentUser?.uid
        if (currentUserUid == null) {
            showToast(requireContext(), "User is not logged in.")
            return
        }
        mFirestore.collection("Events").document(eventId).delete()
            .addOnSuccessListener {
                mFirestore.collection("User").document(currentUserUid)
                    .update("eventID", FieldValue.arrayRemove(eventId))
                    .addOnSuccessListener {
                        showToast(requireContext(), "Event deleted and user updated successfully")
                        setFragmentResult("event_update", Bundle().apply {
                            putString("deleted_event_id", eventId)
                        })
                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        showToast(requireContext(), "Error updating user: $e")
                    }
            }
            .addOnFailureListener { e ->
                showToast(requireContext(), "Error deleting event: $e")
            }
    }
    // xoa tat ca lich trinh lap giu lai lich trinh goc
    fun deleteAllRepeatingEvents(originalEventID: String) {
        val db = FirebaseFirestore.getInstance()
        val eventsRef = db.collection("Events")

        eventsRef.whereEqualTo("originalEventID", originalEventID).get().addOnSuccessListener { documents ->
            val batch = db.batch()
            documents.forEach { document ->
                batch.delete(document.reference)
            }
            batch.commit().addOnSuccessListener {
                showToast(requireContext(), "All repeating events deleted successfully")
            }.addOnFailureListener { e ->
                showToast(requireContext(), "Error deleting repeating events: $e")
            }
        }.addOnFailureListener { e ->
            showToast(requireContext(), "Error fetching repeating events: $e")
        }
    }
    // xoa chinh sua su kien nay va cac su kien tiep theo
    fun deleteFutureRepeatingEvents(eventID: String) {
        val db = FirebaseFirestore.getInstance()
        val eventsRef = db.collection("Events")

        eventsRef.document(eventID).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val event = document.toObject(Event::class.java)
                if (event != null) {
                    val originalEventID = event.originalEventID.ifEmpty { eventID }
                    eventsRef.whereEqualTo("originalEventID", originalEventID)
                        .whereGreaterThanOrEqualTo("date", event.date)
                        .get()
                        .addOnSuccessListener { documents ->
                            val batch = db.batch()
                            documents.forEach { doc ->
                                batch.delete(doc.reference)
                            }
                            batch.commit().addOnSuccessListener {
                                Log.d("checkdelete", "Future repeating events deleted successfully")
                            }.addOnFailureListener { e ->
                                Log.d("checkdelete", "Error deleting future repeating events: $e")
                            }
                        }.addOnFailureListener { e ->
                            Log.d("checkdelete", "Error fetching future repeating events: $e")
                        }
                } else {
                    Log.d("checkdelete", "Event is null")
                }
            } else {
                Log.d("checkdelete", "Event document does not exist")
            }
        }.addOnFailureListener { e ->
            Log.d("checkdelete", "Error fetching event: $e")
        }
    }






    fun addEvent(userId: String, event: Event) {
        val db = FirebaseFirestore.getInstance()
        val eventRef = db.collection("Events").document(event.eventID)

        eventRef.set(event)
            .addOnSuccessListener {
                // Thêm eventId vào danh sách eventID của người dùng
                db.collection("User").document(userId)
                    .update("eventID", FieldValue.arrayUnion(event.eventID))
                    .addOnSuccessListener {
                        showToast(requireContext(), "Event added and user updated successfully")
                        dismiss() // Chỉ đóng giao diện khi lưu thành công
                    }
                    .addOnFailureListener { e ->
                        showToast(requireContext(), "Error updating user: $e")
                    }
            }
            .addOnFailureListener { e ->
                showToast(requireContext(), "Error adding event: $e")
            }
    }
    fun createRepeatingEvents(event: Event, repeatMode: RepeatMode, endDate: String): List<Event> {
        val events = mutableListOf<Event>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val beginTime = dateFormat.parse(event.date)?.time ?: return events
        val endTime = dateFormat.parse(endDate)?.time ?: return events

        var currentIndex = 0
        var newBeginTime = beginTime
        var originalEventId = event.eventID // Gán eventID của sự kiện gốc

        while (newBeginTime <= endTime) {
            val newDate = dateFormat.format(Date(newBeginTime))
            val newEvent = if (currentIndex == 0) {
                event.copy(eventID = generateEventId(), date = newDate) // Sự kiện gốc
            } else {
                event.copy(eventID = generateEventId(), date = newDate, originalEventID = originalEventId)
            }
            if (currentIndex == 0) {
                originalEventId = newEvent.eventID // Cập nhật lại originalEventId
            }
            events.add(newEvent)

            currentIndex++
            newBeginTime = repeatMode.repeatBeginTimeByIndex(beginTime, currentIndex)
        }

        // Log ra toàn bộ mảng events
        for (e in events) {
            Log.d("CreateRepeatingEvents", e.toString())
        }

        return events
    }






    fun generateEventId(): String {
        return UUID.randomUUID().toString()
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
    fun formatDate(date: Date,check: Boolean): String {
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val day = dayFormat.format(date)
        val month = monthFormat.format(date)
        val year = yearFormat.format(date)
        if(check == true){
            return "Ngày $day tháng $month năm $year"
        }
        else return "$day/$month/$year"

    }

    override fun onRepeatModeSelected(mode: String, id: Int) {
        binding.value4.text = mode
        idRadio = id
    }
    data class TimePickerResult(val picker: TimePickerView, val timeFormat: String)


}
