package com.hoangdoviet.finaldoan


import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hoangdoviet.finaldoan.databinding.ActivityMainBinding
import com.hoangdoviet.finaldoan.fragment.DayFragment
import com.hoangdoviet.finaldoan.fragment.EventFragment
import com.hoangdoviet.finaldoan.fragment.FormTaskFragment
import com.hoangdoviet.finaldoan.fragment.MonthFragment
import com.hoangdoviet.finaldoan.fragment.TaskFragment
import com.hoangdoviet.finaldoan.fragment.profileFragment
import com.hoangdoviet.finaldoan.model.LoginUiState

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    //private var isFabOpen = false
    private val permissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.SET_ALARM
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermission()
        binding.bottomNavigationView.background = null
        //binding.navView.menu.getItem(1).isEnabled = false
        replaceFragment(DayFragment())
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.DayFragment) replaceFragment(DayFragment())
            else if (item.itemId == R.id.MonthFragment) replaceFragment(MonthFragment())
            else if (item.itemId == R.id.TaskFragment) replaceFragment(TaskFragment())
            else replaceFragment(
                profileFragment()
            )
            true
        }
        binding.Fabb.setOnClickListener {
            showDialogOne()
        }
        val loginUiState = intent.getParcelableExtra<LoginUiState>("loginUiState")
       if(loginUiState != null){
           val profileFragment = profileFragment()
           val bundle = Bundle()
           bundle.putString("ten", loginUiState.username.toString())
           profileFragment.arguments = bundle
           supportFragmentManager.beginTransaction().add(R.id.frame_layout, profileFragment).commit()

       }
        // Kiểm tra xem activity được mở từ thông báo hay không
        val eventDate = intent.getStringExtra("EVENT_DATE")
        val targetFragment = intent.getStringExtra("TARGET_FRAGMENT")
        Log.d("MainActivity", "onCreate: eventDate=$eventDate, targetFragment=$targetFragment")

        if (targetFragment == "MonthFragment" && eventDate != null) {
            openMonthFragment(eventDate)
        }

    }
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val remainingPermissions = mutableListOf<String>()
            for (permission in permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    remainingPermissions.add(permission)
                }
            }
            if (remainingPermissions.isNotEmpty()) {
                requestPermissions(remainingPermissions.toTypedArray(), 101)
            }
        }
    }

    private fun openMonthFragment(dateString: String) {
        val monthFragment = MonthFragment()

        // Truyền dữ liệu đến MonthFragment
        val bundle = Bundle()
        bundle.putString("EVENT_DATE", dateString)
        monthFragment.arguments = bundle

        Log.d("MainActivity", "openMonthFragment: dateString=$dateString")
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, monthFragment)
            .commit()
    }

    // }
    fun showDialogOne() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.sample_dialog_one)

        dialog.findViewById<RelativeLayout>(R.id.rl_chatbot)?.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
//        val btnDelete= dialog.findViewById<RelativeLayout>(R.id.rl_task)
//        val btnAdd= dialog.findViewById<RelativeLayout>(R.id.rl_event)
        dialog.findViewById<RelativeLayout>(R.id.rl_task)?.setOnClickListener {
            val eventFragment = EventFragment()
            eventFragment.show(supportFragmentManager, "EventFragment")
            dialog.dismiss()
//            val  dialog =  Dialog(this);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setContentView(R.layout.fragment_event);
//            dialog.show();
//            dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
//            dialog.getWindow()?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT));
//            dialog.getWindow()?.getAttributes()?.windowAnimations ?: R.style.DialogAnimation;
//            dialog.getWindow()?.setGravity(Gravity.BOTTOM);

        }
        dialog.findViewById<RelativeLayout>(R.id.rl_event)?.setOnClickListener {
            val formTask = FormTaskFragment()
            formTask.show(supportFragmentManager, "EventFragment")
            dialog.dismiss()

        }

        dialog.show()
        dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow()?.setGravity(Gravity.BOTTOM);


    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    // Phương thức trả về binding của Activity
    fun getActivityBinding(): ActivityMainBinding {
        return binding
    }
}