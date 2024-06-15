package com.hoangdoviet.finaldoan.fragment


import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.adapter.TaskAdapter
import com.hoangdoviet.finaldoan.databinding.FragmentTaskBinding
import com.hoangdoviet.finaldoan.horizontal_calendar_date.HorizontalCalendarAdapter
import com.hoangdoviet.finaldoan.horizontal_calendar_date.HorizontalCalendarSetUp
import com.hoangdoviet.finaldoan.model.Task
import com.hoangdoviet.finaldoan.utils.showToast
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TaskFragment : Fragment(), HorizontalCalendarAdapter.OnItemClickListener {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private val tasks = mutableListOf<Task>()
//    private var taskAdapter: TaskAdapter = TaskAdapter(tasks)
    private lateinit var taskAdapter: TaskAdapter

    private lateinit var userId: String
    private val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    private var vitri: Boolean = false
    private lateinit var DateDelete: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
//        Handler(Looper.getMainLooper()).postDelayed({
//            binding.animationView.visibility = View.VISIBLE
//            binding.animationView.playAnimation()
//        }, 0)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(binding.recyclerView, this)
        binding.textDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(
            binding.ivCalendarNext,
            binding.ivCalendarPrevious,
            this
        ) {
            binding.textDateMonth.text = it
        }
        // Lấy userId từ phiên đăng nhập hiện tại
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance().getReference()
        setupTaskAdapter()
        binding.recyclerView1.layoutManager = LinearLayoutManager(context)
        binding.recyclerView1.adapter = taskAdapter
//        val todayDate = "20240605"
        updateOverdueTasks()
        loadTasksByDate(todayDate)
      //  countTasksByDate(todayDate)
//        countCompletedTasksByDate(todayDate)
        loadTaskDatesForNextSevenDays()

//        binding.buttonAddTask.setOnClickListener {
//            Log.d("TaskActivity", userId + " " + todayDate)
//            addTask(userId, todayDate, binding.editTextTaskTitle.text.toString())
//        }
    }
    private fun setupTaskAdapter() {
        taskAdapter = TaskAdapter(tasks) { completedTasksCount, totalTasksCount ->
            updateProgress(completedTasksCount, totalTasksCount)
        }
        binding.recyclerView1.layoutManager = LinearLayoutManager(context)
        binding.recyclerView1.adapter = taskAdapter
    }
    private fun updateProgress(completedTasksCount: Int, totalTasksCount: Int) {
        if (completedTasksCount == 0 && totalTasksCount == 0) {
            binding.tvScore.text = "0%"
            binding.scoreProgressBar.progress = 0
        } else {
            val completionRate = if (totalTasksCount > 0) (completedTasksCount.toDouble() / totalTasksCount) * 100 else 0.0
            val completionRateText = if (completionRate % 1 == 0.0) {
                String.format("%d%%", completionRate.toInt())
            } else {
                String.format("%.1f%%", completionRate)
            }
            binding.tvScore.text = completionRateText
            binding.scoreProgressBar.progress = completionRate.toInt()
//            // Nếu ProgressBar đạt 100%, chạy animation
            if (completionRate.toInt() == 100) {
                // Hiển thị animation
                binding.animationView.visibility = View.VISIBLE
                binding.animationView.playAnimation()

                // Sau 8 giây, ẩn animation
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.animationView.visibility = View.GONE
                }, 3000)
            } else {
                // Ngược lại, ẩn animation
                binding.animationView.visibility = View.GONE
            }
        }
    }





    private fun loadTasksByDate(date: String) {
            val db = FirebaseFirestore.getInstance()
            val tasksByDateRef = db.collection("TasksByDate").document(date)
            tasks.clear()
            tasksByDateRef.get()
                .addOnSuccessListener { document ->
                    // Xóa các nhiệm vụ hiện tại trước khi thêm mới
                    if (document != null && document.exists()) {
                        val taskIds = document.get("taskIds") as? List<String> ?: emptyList()
                        if (taskIds.isEmpty()) {
                            Log.d("TaskActivity", "No tasks found for the given date")
                            taskAdapter.notifyDataSetChanged() // Thông báo adapter về thay đổi
                            return@addOnSuccessListener
                        }

                        for (taskId in taskIds) {
                            db.collection("Tasks").document(taskId).get()
                                .addOnSuccessListener { taskDocument ->
                                    val task = taskDocument.toObject(Task::class.java)
                                    if (task != null) {
                                        tasks.add(task)
                                        Log.d("checktask", task.toString())
                                        taskAdapter.notifyDataSetChanged() // Thông báo adapter về thay đổi sau khi thêm nhiệm vụ mới
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TaskActivity", "Failed to load task", e)
                                }
                        }
                    } else {
                        Log.d("TaskActivity", "No tasks found for the given date")
                        taskAdapter.notifyDataSetChanged() // Thông báo adapter về thay đổi nếu không có dữ liệu
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TaskActivity", "Failed to load task IDs by date", e)
                    taskAdapter.notifyDataSetChanged() // Thông báo adapter về thay đổi trong trường hợp thất bại
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

    private fun deleteTask(userId: String, date: String, taskId: String) {
        val db = FirebaseFirestore.getInstance()

        // Xóa task từ collection Tasks
        db.collection("Tasks").document(taskId).delete()
            .addOnSuccessListener {
                Log.d("TaskActivity", "Task deleted from Tasks collection successfully")
                Log.d("TaskActivity1", "$date")
                // Cập nhật taskIds trong TasksByDate
                val tasksByDateRef = db.collection("TasksByDate").document(date)
                tasksByDateRef.update("taskIds", FieldValue.arrayRemove(taskId))
                    .addOnSuccessListener {
                        Log.d("TaskActivity1", "Task ID removed from TasksByDate collection successfully $taskId")

                        // Kiểm tra nếu taskIds trống thì xoá luôn tài liệu
                        tasksByDateRef.get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val taskIds = document.get("taskIds") as? List<*>
                                    if (taskIds.isNullOrEmpty()) {
                                        tasksByDateRef.delete()
                                            .addOnSuccessListener {
                                                Log.d("TaskActivity", "Empty TasksByDate document deleted successfully")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("TaskActivity", "Failed to delete empty TasksByDate document", e)
                                            }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("TaskActivity", "Failed to check TasksByDate document", e)
                            }

                        // Cập nhật taskIds trong User
                        db.collection("User").document(userId)
                            .update("taskIds", FieldValue.arrayRemove(taskId))
                            .addOnSuccessListener {
                                Log.d("TaskActivity", "Task ID removed from User's taskIds successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("TaskActivity", "Failed to remove task ID from User's taskIds", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaskActivity", "Failed to remove task ID from TasksByDate collection", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("TaskActivity", "Failed to delete task from Tasks collection", e)
            }
    }

    private fun countTasksByDate(date: String) {
        val db = FirebaseFirestore.getInstance()
        val tasksByDateRef = db.collection("TasksByDate").document(date)

        tasksByDateRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val taskIds = document.get("taskIds") as? List<*>
                    val taskCount = taskIds?.size ?: 0
                    Log.d("TaskFragment", "Number of tasks for date $date: $taskCount")
                    binding.textView2.text = "$taskCount nhiệm vụ"
                } else {
                    Log.d("TaskFragment", "No tasks found for the given date")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TaskFragment", "Failed to load task IDs by date", e)
            }
    }
    private fun updateOverdueTasks() {
        val db = FirebaseFirestore.getInstance()
        val tasksByDateRef = db.collection("TasksByDate")

        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()

        tasksByDateRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val date = document.id.toInt()
                    if (date < currentDate) {
                        val taskIds = document.get("taskIds") as? List<String> ?: emptyList()
                        if (taskIds.isEmpty()) continue

                        val tasksCollection = db.collection("Tasks")
                        val batch = tasksCollection.whereIn(FieldPath.documentId(), taskIds)
                        batch.get()
                            .addOnSuccessListener { tasksSnapshot ->
                                for (taskDocument in tasksSnapshot) {
                                    val task = taskDocument.toObject(Task::class.java)
                                    if (task.status != "Hoàn thành") {
                                        task.status = "Quá hạn"
                                        tasksCollection.document(task.id).set(task)
                                            .addOnSuccessListener {
                                                Log.d("TaskUpdate", "Updated task ${task.id} to Quá hạn")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("TaskUpdate", "Failed to update task ${task.id}", e)
                                            }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("TaskUpdate", "Failed to load tasks", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TaskUpdate", "Failed to load tasks by date", e)
            }
    }

    private fun countCompletedTasksByDate(date: String) {
        val db = FirebaseFirestore.getInstance()
        val tasksByDateRef = db.collection("TasksByDate").document(date)

        tasksByDateRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Lấy mảng taskIds từ tài liệu
                    val taskIds = document.get("taskIds") as? List<String> ?: emptyList()
                    val taskCount = taskIds.size
                    Log.d("tasksize", "Number of tasks for date $date: $taskCount")
                    binding.textView2.text = "$taskCount nhiệm vụ"
                    binding.scoreProgressBar.progress = 0
                    Log.d("TaskFragment", "Task IDs for date $date: $taskIds")
                    if (taskIds.isEmpty()) {
                        Log.d("TaskFragment", "No tasks found for the given date")
                        updateProgress(0, 0) // Gọi hàm updateProgress với giá trị 0
                        return@addOnSuccessListener
                    }

                    // Tạo một danh sách các tác vụ để lấy chi tiết
                    val tasksCollection = db.collection("Tasks")
                    val batch = tasksCollection.whereIn(FieldPath.documentId(), taskIds)
                    var completedTasksCount = 0
                    batch.get()
                        .addOnSuccessListener { querySnapshot ->
                            Log.d("TaskFragment", "Found ${querySnapshot.size()} tasks for the given date")
                            for (taskDocument in querySnapshot) {
                                val task = taskDocument.toObject(Task::class.java)
                                if (task.status == "Hoàn thành") { // Giả sử "Hoàn thành" là mã cho "Hoàn thành"
                                    completedTasksCount++
                                    Log.d("CheckTask", "Added task with status 'Hoàn thành': $task")
                                    Log.d("CheckTask1", "$completedTasksCount - $taskCount")
                                }
                            }

                            Log.d("CheckTask1", "$completedTasksCount - $taskCount")
                            updateProgress(completedTasksCount, taskCount)
                        }
                        .addOnFailureListener { e ->
                            Log.e("TaskFragment", "Failed to load tasks", e)
                            updateProgress(0, 0) // Trong trường hợp lỗi, cũng cập nhật progress với giá trị 0
                            binding.textView2.text = "0 nhiệm vụ"
                        }
                } else {
                    Log.d("TaskFragment", "No tasks found for the given date")
                    updateProgress(0, 0) // Gọi hàm updateProgress với giá trị 0 khi không có tài liệu nào tồn tại
                    binding.textView2.text = "0 nhiệm vụ"
                }
            }
            .addOnFailureListener { e ->
                Log.e("TaskFragment", "Failed to load task IDs by date", e)
                updateProgress(0, 0) // Trong trường hợp lỗi, cũng cập nhật progress với giá trị 0
                binding.textView2.text = "0 nhiệm vụ"
            }
    }

    private fun loadTaskDatesForNextSevenDays() {
        val db = FirebaseFirestore.getInstance()
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()
        val endDate = currentDate + 7
        val taskDates = mutableListOf<String>()

        for (date in currentDate..endDate) {
            val tasksByDateRef = db.collection("TasksByDate").document(date.toString())
            tasksByDateRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val taskIds = document.get("taskIds") as? List<String> ?: emptyList()
                        if (taskIds.isNotEmpty()) {
                            taskDates.add(date.toString())
                        }
                    }
                    // Cập nhật adapter
                    val calendarList = // Danh sách ngày của bạn
                        HorizontalCalendarSetUp().setUpCalendarAdapter(binding.recyclerView, this, taskDates)
                }
                .addOnFailureListener { e ->
                    Log.e("TaskFragmentLoadTask", "Failed to load task IDs by date", e)
                }
        }
    }
    private fun setUpItemTouchHelper(ddMmYy: String) {
        val simpleCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val deleteData: Task? = tasks[position]
                    tasks.removeAt(position)
                    taskAdapter.notifyDataSetChanged()

                    deleteTask(userId, DateDelete, deleteData?.id ?: "")

                    deleteData?.let { finalDeleteData ->
                        Snackbar.make(
                            binding.recyclerView1,
                            "Xoá " + finalDeleteData.title,
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("Hoàn tác") {
                                tasks.add(position, finalDeleteData)
                                taskAdapter.notifyDataSetChanged()
                                addTask(userId, ddMmYy, finalDeleteData.title)
                            }
                            .setAnchorView(R.id.Fabb)
                            .show()
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(ContextCompat.getColor(context!!, R.color.color1))
                        .addSwipeLeftLabel("Xoá")
                        .create()
                        .decorate()
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView1)
    }



    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        DateDelete = ddMmYy
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            if(todayDate != ddMmYy){
                loadTasksByDate(ddMmYy)
                vitri = true
            }
            if(vitri == true && todayDate == ddMmYy){
                loadTasksByDate(todayDate)
            }
            updateOverdueTasks()
            setUpItemTouchHelper(ddMmYy)

            // countTasksByDate(ddMmYy)
            countCompletedTasksByDate(ddMmYy)
        }

    }

}
