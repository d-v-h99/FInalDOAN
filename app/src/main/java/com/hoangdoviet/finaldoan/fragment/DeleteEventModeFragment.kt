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
import androidx.fragment.app.setFragmentResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentDeleteEventModeBinding
import com.hoangdoviet.finaldoan.databinding.FragmentRepeatModeBinding
import com.hoangdoviet.finaldoan.model.Event
import com.hoangdoviet.finaldoan.utils.RepeatMode
import com.hoangdoviet.finaldoan.utils.showToast
import com.hoangdoviet.finaldoan.viewmodel.SharedViewModel

class DeleteEventModeFragment : DialogFragment() {

    private lateinit var binding: FragmentDeleteEventModeBinding
    private lateinit var eventToDelete: Event
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var position: Int = -1

    companion object {
        @JvmStatic
        fun newInstance(event: Event, position: Int) =
            DeleteEventModeFragment().apply {
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
        binding = FragmentDeleteEventModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        arguments?.getParcelable<Event>("event")?.let { event ->
            this.eventToDelete = event
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
        binding.title.text = getString(R.string.delete_event_title)
        binding.cancel.setOnClickListener {
            dismissWithMessage(getString(R.string.cancelled))
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButton1 -> eventToDelete.let { deleteEvent(it.eventID) }
                R.id.radioButton2 -> deleteAllRepeatingEvents(eventToDelete.originalEventID)
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun deleteEvent(eventID: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserUid = mAuth.currentUser?.uid

        if (currentUserUid != null) {
            db.collection("User").document(currentUserUid)
                .update("eventID", FieldValue.arrayRemove(eventID))
                .addOnSuccessListener {
                    db.collection("Events").document(eventID)
                        .delete()
                        .addOnSuccessListener {
                            showToast(requireContext(), getString(R.string.event_deleted))
                            dismiss()
                            // Gửi kết quả lại cho EventFragment mặc định c
                            setFragmentResult("deleteRequestKey", Bundle().apply {
                                putInt("position", position)
                                Log.d("ktraa", position.toString() + " deleteEventModeFragment")
                            })
                        }
                        .addOnFailureListener { e ->
                            showToast(requireContext(), getString(R.string.error_deleting_event, e))
                        }
                }
                .addOnFailureListener { e ->
                    showToast(requireContext(), getString(R.string.error_removing_event_from_user, e))
                }
        }
    }

    private fun deleteAllRepeatingEvents(originalEventID: String) {
        val db = FirebaseFirestore.getInstance()
        val eventsRef = db.collection("Events")

        eventsRef.whereEqualTo("originalEventID", originalEventID).get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                documents.forEach { document ->
                    batch.delete(document.reference)
                }
                batch.commit().addOnSuccessListener {
                    showToast(requireContext(), getString(R.string.repeating_events_deleted))
                    dismiss()
                    setFragmentResult("deleteRequestKey", Bundle().apply {
                        putInt("position", position)
                        Log.d("ktraa", position.toString() + " deleteEventModeFragment")
                    })
                }.addOnFailureListener { e ->
                    showToast(requireContext(), getString(R.string.error_deleting_repeating_events, e.message))
                }
            }.addOnFailureListener { e ->
                showToast(requireContext(), getString(R.string.error_fetching_repeating_events, e.message))
            }
    }

    private fun sendResultBackToParent() {
        val resultBundle = Bundle().apply {
            putParcelable("deletedEvent", eventToDelete)
        }
        parentFragmentManager.setFragmentResult("deleteEventRequest", resultBundle)
    }

    private fun dismissWithMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        dismissAllowingStateLoss()
    }
}