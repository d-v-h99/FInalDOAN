package com.hoangdoviet.finaldoan.fragment

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup.LayoutParams
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentRepeatModeBinding
import com.hoangdoviet.finaldoan.utils.RepeatMode
import kotlin.math.roundToInt


class RepeatModeFragment : DialogFragment() {
    private lateinit var binding: FragmentRepeatModeBinding
    private var listener: OnRepeatModeSelectedListener? = null
override fun onAttach(context: Context) {
    super.onAttach(context)
    val parent = targetFragment
    if (parent is OnRepeatModeSelectedListener) {
        listener = parent
    } else {
        throw RuntimeException("$context must implement OnRepeatModeSelectedListener")
    }
}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!::binding.isInitialized) {
            binding = FragmentRepeatModeBinding.inflate(inflater)
        }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = "Đặt lập lại"
        val options = listOf(RepeatMode.Never.name, RepeatMode.Day.name, RepeatMode.WorkDay.name,RepeatMode.Week.name,RepeatMode.Month.name,RepeatMode.Year.name) // Your dynamic options
        var i = 0
        for (option in options) {
            val radioButton = RadioButton(context).apply {
                text = option
                id = i// Generate unique ID
                i++
                Log.d("idradio", id.toString())
            }
                binding.radioGroup.addView(radioButton)
        }
        binding.cancel.setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = view.findViewById<RadioButton>(checkedId)
            val selectedText = selectedRadioButton?.text
            selectedText?.let {
                listener?.onRepeatModeSelected(it.toString(), selectedRadioButton.id)
            }
            dismissAllowingStateLoss()
        }

    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(350.dpToPx(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
    interface OnRepeatModeSelectedListener {
        fun onRepeatModeSelected(mode: String, id: Int)
    }



}