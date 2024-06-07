package com.hoangdoviet.finaldoan.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentEventBinding
import com.hoangdoviet.finaldoan.databinding.FragmentFormTaskBinding
import com.hoangdoviet.finaldoan.model.Event
import com.hoangdoviet.finaldoan.model.Task
import com.hoangdoviet.finaldoan.utils.RepeatMode
import com.hoangdoviet.finaldoan.utils.addYearsToDate
import com.hoangdoviet.finaldoan.utils.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FormTaskFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFormTaskBinding
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    lateinit var textDatePicker: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val datePicker by lazy {
        val picker = TimePickerBuilder(requireContext()) { date, v ->
            val formattedDate = formatDate(date, true)
            Log.i("pvTime", "$formattedDate")
            binding.valueDate.text = formattedDate
            textDatePicker = formatDate(date, false)
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

    fun formatDate(date: Date, check: Boolean): String {
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val day = dayFormat.format(date)
        val month = monthFormat.format(date)
        val year = yearFormat.format(date)
        if (check == true) {
            return "Ngày $day tháng $month năm $year"
        } else return "$year$month$day"

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.date.setOnClickListener {
            datePicker.show()
        }
        binding.btnSave.setOnClickListener {
            try {
                val currentUserUid = mAuth.currentUser?.uid
                if (currentUserUid == null) {
                    showToast(requireContext(), "User is not logged in.")
                    return@setOnClickListener
                }
                addTask(currentUserUid,textDatePicker, binding.valueTitle.text.toString() )
//

            } catch (e: Exception) {
                Log.e("EventFragment", "Error creating event", e)
            }

        }


    }
    private fun addTask(userId: String, date: String, title: String) {
        val db = FirebaseFirestore.getInstance()
        val taskId = db.collection("Tasks").document().id
        val task = Task(id = taskId, title = title, status = "Chưa làm", userId = userId)

        // Thêm task vào collection Tasks
        db.collection("Tasks").document(taskId).set(task)
            .addOnSuccessListener {
                Log.d("TaskActivity", "Task added to Tasks collection successfully")

                // Kiểm tra xem tài liệu TasksByDate có tồn tại không
                val tasksByDateRef = db.collection("TasksByDate").document(date)
                tasksByDateRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            // Nếu tài liệu tồn tại, cập nhật taskIds
                            tasksByDateRef.update("taskIds", FieldValue.arrayUnion(taskId))
                                .addOnSuccessListener {
                                    Log.d(
                                        "TaskActivity",
                                        "Task added to TasksByDate collection successfully"
                                    )
                                    // Cập nhật taskIds trong User
                                    db.collection("User").document(userId)
                                        .update("taskIds", FieldValue.arrayUnion(taskId))
                                        .addOnSuccessListener {
                                            Log.d(
                                                "TaskActivity",
                                                "Task added to User's taskIds successfully"
                                            )
                                            dismiss()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "TaskActivity",
                                                "Failed to update user's taskIds",
                                                e
                                            )
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TaskActivity", "Failed to update TasksByDate", e)
                                }
                        } else {
                            // Nếu tài liệu không tồn tại, tạo mới và cập nhật taskIds
                            val newTaskDate = hashMapOf(
                                "taskIds" to arrayListOf(taskId)
                            )
                            tasksByDateRef.set(newTaskDate)
                                .addOnSuccessListener {
                                    Log.d(
                                        "TaskActivity",
                                        "TaskByDate document created successfully"
                                    )
                                    // Cập nhật taskIds trong User
                                    db.collection("User").document(userId)
                                        .update("taskIds", FieldValue.arrayUnion(taskId))
                                        .addOnSuccessListener {
                                            Log.d(
                                                "TaskActivity",
                                                "Task added to User's taskIds successfully"
                                            )
                                            dismiss()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "TaskActivity",
                                                "Failed to update user's taskIds",
                                                e
                                            )
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TaskActivity", "Failed to create TaskByDate document", e)
                                }
                        }
                    } else {
                        Log.e("TaskActivity", "Failed to get TaskByDate document", task.exception)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TaskActivity", "Failed to add task", e)
            }
    }
}