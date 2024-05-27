package com.hoangdoviet.finaldoan.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hoangdoviet.finaldoan.fragment.OneDayFragment
import java.util.ArrayList

class OneDayAdapter(fragmentManager: FragmentManager, val data: ArrayList<String>) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        //Log.d("nowPos", "$position")
        //Log.d("dataNow", data[position])
        val bundle = Bundle()
        bundle.putString("demo", data[position])
        val fragment = OneDayFragment()
        fragment.arguments = bundle
        return fragment
    }

    override fun getCount(): Int {
        return data.size
    }

}