package com.hoangdoviet.finaldoan.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentDeleteEventModeBinding
import com.hoangdoviet.finaldoan.databinding.FragmentRepeatModeBinding
import com.hoangdoviet.finaldoan.model.Event
import com.hoangdoviet.finaldoan.utils.RepeatMode
import com.hoangdoviet.finaldoan.utils.showToast
import com.hoangdoviet.finaldoan.viewmodel.SharedViewModel

class DeleteEventModeFragment : DialogFragment() {
    private lateinit var binding : FragmentDeleteEventModeBinding
    lateinit var eventDelete: Event
    companion object {
        private const val ARG_EVENT = "event"

        fun newInstance(event: Event): DeleteEventModeFragment {
            val fragment = DeleteEventModeFragment()
            val args = Bundle()
            args.putParcelable(ARG_EVENT, event)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDeleteEventModeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val event = it.getParcelable<Event>(ARG_EVENT)
            // Sử dụng dữ liệu event
            if (event != null) {
                eventDelete = event
            }
        }
        binding.title.text = "Xoá sự kiện"
        binding.cancel.setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
//            val selectedRadioButton = view.findViewById<RadioButton>(checkedId)
//            val selectedText = selectedRadioButton?.text
            //dismissAllowingStateLoss()
            // Do something with the selected text
            when (checkedId) {
                R.id.radioButton1 -> {
                    // Xử lý khi radioButton1 được chọn
                    //showToast(requireContext(), "xoa1")
                    deleteEvent(eventDelete.eventID)

                }

                R.id.radioButton2 -> {
                    //showToast(requireContext(), "xoa2")
                    // Xử lý khi radioButton2 được chọn
                    deleteAllRepeatingEvents(eventDelete.originalEventID)
                }

                else -> {
                    // Xử lý mặc định hoặc khi không có radioButton nào được chọn
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(350.dpToPx(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
    fun deleteEvent(eventID: String) {
        val db = FirebaseFirestore.getInstance()
        val eventRef = db.collection("Events").document(eventID)

        eventRef.delete().addOnSuccessListener {
            showToast(requireContext(), "Event deleted successfully")
            dismiss()
        }.addOnFailureListener { e ->
            showToast(requireContext(), "Error deleting event: $e")
        }
    }
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
                dismiss()
            }.addOnFailureListener { e ->
                showToast(requireContext(), "Error deleting repeating events: $e")
            }
        }.addOnFailureListener { e ->
            showToast(requireContext(), "Error fetching repeating events: $e")
        }
    }



}