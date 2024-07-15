package com.hoangdoviet.finaldoan.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentEmailBinding


class emailFragment : Fragment() {
    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)

        setupUI()

        return binding.root
    }
    private fun setupUI() {
        binding.apply {

           // tvEmailResend.text = createResendText()

            btnBackToLogin.setOnClickListener {
                findNavController().popBackStack(R.id.loginFragment2, false)
            }

            btnOpenEmail.setOnClickListener {
                val mailIntent = Intent(Intent.ACTION_MAIN)
                mailIntent.addCategory(Intent.CATEGORY_APP_EMAIL)
                mailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity?.startActivity(mailIntent)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }


}