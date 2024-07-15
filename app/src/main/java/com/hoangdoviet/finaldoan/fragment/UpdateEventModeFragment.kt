package com.hoangdoviet.finaldoan.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentUpdateEventModeBinding
import com.hoangdoviet.finaldoan.model.Event
import com.hoangdoviet.finaldoan.utils.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class UpdateEventModeFragment : DialogFragment() {
    private lateinit var binding: FragmentUpdateEventModeBinding
    private lateinit var eventUpdate: Event
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var position: Int = -1

    companion object {
        @JvmStatic
        fun newInstance(event: Event, position: Int) =
            UpdateEventModeFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("event", event)
                    putInt("position", position)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentUpdateEventModeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        arguments?.getParcelable<Event>("event")?.let { event ->
            this.eventUpdate = event
        }
        arguments?.let {
            position = it.getInt("position")
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(350.dpToPx(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setupUI() {
        binding.cancel.setOnClickListener {
            dismissWithMessage(getString(R.string.cancelled))
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButton1 ->updateEvent(eventUpdate)
                R.id.radioButton2 -> updateAllRepeatingEvents(eventUpdate.originalEventID, eventUpdate)
            }
        }
    }
    private fun updateEvent(event: Event) {
        val currentUserUid = mAuth.currentUser?.uid
        if (currentUserUid == null) {
            showToast(requireContext(), "User is not logged in.")
            return
        }

        val eventId = event.eventID

        mFirestore.collection("Events").document(eventId)
            .set(event)
            .addOnSuccessListener {
                mFirestore.collection("User").document(currentUserUid)
                    .update("eventID", FieldValue.arrayUnion(eventId))
                    .addOnSuccessListener {
                        showToast(requireContext(), "Event updated and user updated successfully")
                       dismiss()
                        // Gửi kết quả lại cho MonthFragment
                        setFragmentResult("requestKey1", Bundle().apply {
                            putInt("position", position)
                            putParcelable("event", event)
                            Log.d("ktraa", position.toString() + " Event updated")
                        })

                    }
                    .addOnFailureListener { e ->
                        showToast(requireContext(), "Error updating user: $e")
                    }
            }
            .addOnFailureListener { e ->
                showToast(requireContext(), "Error updating event: $e")
            }
    }
    private fun updateAllRepeatingEvents(originalEventID: String, newEventDetails: Event) {
        val db = FirebaseFirestore.getInstance()
        val eventsRef = db.collection("Events")

        eventsRef.whereEqualTo("originalEventID", originalEventID).get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                documents.forEach { document ->
                    val event = document.toObject(Event::class.java)
                    event.title = newEventDetails.title
                    event.timeStart = newEventDetails.timeStart
                    event.timeEnd = newEventDetails.timeEnd
                    event.repeat = newEventDetails.repeat
                    val dateStart = dateFormat.parse(event.date)
                    val newDate = addNextRepeatDate(dateStart, event.repeat)
                    event.date = dateFormat.format(newDate)
                    val eventRef = document.reference
                    batch.set(eventRef, event)
                }

                batch.commit().addOnSuccessListener {
                    showToast(requireContext(), "Update các bản ghi lặp thành công")
                    dismiss()
                    setFragmentResult("requestKey1", Bundle().apply {
                        putInt("position", position)
                        putParcelable("event", newEventDetails)
                        Log.d("ktraa", position.toString() + " updateEventModeFragment")
                    })
                }.addOnFailureListener { e ->
                    showToast(requireContext(), "Lỗi: ${e.message}")
                }
            }.addOnFailureListener { e ->
                showToast(requireContext(), getString(R.string.error_fetching_repeating_events, e.message))
            }
    }
    private fun addNextRepeatDate(dateStart: Date, repeatMode: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = dateStart
        }

        when (repeatMode) {
            1 -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            2 -> {
                do {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                } while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            }
            3 -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            4 -> calendar.add(Calendar.MONTH, 1)
            5 -> calendar.add(Calendar.YEAR, 1)
        }

        return calendar.time
    }



    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
    private fun dismissWithMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        dismissAllowingStateLoss()
    }


}