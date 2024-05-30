package com.hoangdoviet.finaldoan.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.hoangdoviet.finaldoan.AuthActivity
import com.hoangdoviet.finaldoan.databinding.FragmentProfileBinding
import com.hoangdoviet.finaldoan.utils.showToast

class profileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        val strtext = arguments?.getString("ten")
        if (strtext != null) {
            binding.username.text = strtext
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()

        binding.btnLogin.setOnClickListener {
            val intent = Intent(activity, AuthActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            if (mAuth.currentUser != null) {
                mAuth.signOut()
                showToast(requireContext(), "Thoát tài khoản thành công")
                updateUI() // Cập nhật lại giao diện sau khi đăng xuất
            }
        }
    }

    private fun updateUI() {
        if (mAuth.currentUser != null) {
            binding.btnLogout.visibility = View.VISIBLE
            binding.username.text = mAuth.currentUser?.displayName ?: "Người dùng"
        } else {
            binding.btnLogout.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}