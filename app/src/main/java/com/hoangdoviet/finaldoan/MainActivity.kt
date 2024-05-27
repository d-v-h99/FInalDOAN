package com.hoangdoviet.finaldoan

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hoangdoviet.finaldoan.databinding.ActivityMainBinding
import com.hoangdoviet.finaldoan.fragment.DayFragment
import com.hoangdoviet.finaldoan.fragment.EventFragment
import com.hoangdoviet.finaldoan.fragment.MonthFragment
import com.hoangdoviet.finaldoan.fragment.RepeatModeFragment
import com.hoangdoviet.finaldoan.fragment.TaskFragment
import com.hoangdoviet.finaldoan.fragment.profileFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    //private var isFabOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

    }
    fun showDialogOne() {

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.sample_dialog_one)
        dialog.findViewById<RelativeLayout>(R.id.rl_chatbot)?.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
//        val btnDelete= dialog.findViewById<RelativeLayout>(R.id.rl_task)
//        val btnAdd= dialog.findViewById<RelativeLayout>(R.id.rl_event)


        dialog.show()


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