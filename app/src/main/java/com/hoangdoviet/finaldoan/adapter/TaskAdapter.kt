package com.hoangdoviet.finaldoan.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.model.Task

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskCompleted: (completedTasksCount: Int, totalTasksCount: Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        val radioButton: RadioButton = itemView.findViewById(R.id.radio_button)
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.titleTextView.text = task.title
        holder.radioButton.setOnCheckedChangeListener(null)

        when (task.status) {
            "Hoàn thành" -> {
                holder.radioButton.visibility = View.GONE
                holder.imageView.visibility = View.VISIBLE
                holder.titleTextView.paintFlags =
                    holder.titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.imageView.setImageResource(R.drawable.radiocheck) // Hoặc biểu tượng "Hoàn thành" khác nếu có
                holder.titleTextView.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.black
                    )
                )
            }

            "Quá hạn" -> {
                holder.radioButton.visibility = View.GONE
                holder.imageView.visibility = View.VISIBLE
                holder.imageView.setImageResource(R.drawable.ic_close)
                holder.titleTextView.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.color2
                    )
                )
                holder.titleTextView.paintFlags =
                    holder.titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            else -> {
                holder.radioButton.visibility = View.VISIBLE
                holder.imageView.visibility = View.GONE
                holder.titleTextView.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.black
                    )
                )
                holder.titleTextView.paintFlags =
                    holder.titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        holder.radioButton.isChecked = task.status == "Hoàn thành"

        holder.radioButton.setOnCheckedChangeListener { _, isChecked ->
            task.status = if (isChecked) "Hoàn thành" else "Chưa làm"
            notifyItemChanged(position)

            // Update task status in Firebase
            updateTaskStatus(task.id, task.status)

            // Recalculate and notify the completion rate
            val completedTasks = tasks.count { it.status == "Hoàn thành" }
            onTaskCompleted(completedTasks, tasks.size)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
        updateCompletionRate()
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
        updateCompletionRate()
    }

    private fun updateTaskStatus(taskId: String, status: String) {
        db.collection("Tasks").document(taskId)
            .update("status", status)
            .addOnSuccessListener {
                // Task status updated successfully
            }
            .addOnFailureListener { e ->
                // Failed to update task status
                e.printStackTrace()
            }
    }

    private fun updateCompletionRate() {
        val completedTasks = tasks.count { it.status == "Hoàn thành" }
        onTaskCompleted(completedTasks, tasks.size)
    }
}