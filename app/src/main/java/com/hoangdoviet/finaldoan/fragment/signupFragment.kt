package com.hoangdoviet.finaldoan.fragment

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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentSignupBinding
import com.hoangdoviet.finaldoan.utils.InputValidation
import com.hoangdoviet.finaldoan.utils.LoadingDialog
import com.hoangdoviet.finaldoan.utils.addTextWatcher
import com.hoangdoviet.finaldoan.utils.clearText
import com.hoangdoviet.finaldoan.utils.getInputValue
import com.hoangdoviet.finaldoan.utils.showToast
import com.hoangdoviet.hoangfirebase.util.Status
import com.hoangdoviet.hoangfirebase.viewmodel.AuthViewModel


class signupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val authViewModel by viewModels<AuthViewModel>()
    private val loadingDialog : LoadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)

        setupUI()
        setupObserver()

        return binding.root
    }
    private fun setupObserver() {
        authViewModel.signupStatus.observe(viewLifecycleOwner){signupState ->
            when(signupState.status){
                Status.LOADING -> {
                    loadingDialog.show()
                }
                Status.SUCCESS -> {
                    loadingDialog.dismiss()
                    showToast(requireContext(), "Thanh cong")
                    findNavController().navigate(R.id.action_signupFragment2_to_loginFragment2)
                }
                Status.ERROR -> {
                    showToast(requireContext(), signupState.message.toString())
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun setupUI() {
        binding.apply {
            //tvLogin.text = createLoginText()
            tvLogin.setOnClickListener {
                findNavController().popBackStack(R.id.loginFragment2, false)
            }

            etUsernameContainer.addTextWatcher()
            etEmailContainer.addTextWatcher()
            etPasswordContainer.addTextWatcher()

            btnSignup.setOnClickListener {
                val username = binding.etUsername.getInputValue()
                val email = binding.etEmail.getInputValue()
                val password = binding.etPassword.getInputValue()
                if (detailVerification(username, email, password)) {
                    authViewModel.signup(username, email, password)
                    clearField()
                }
            }
        }
    }
//    private fun createLoginText(): SpannableString {
//        val loginText = SpannableString(getString(R.string.login_prompt))
//        val color = ContextCompat.getColor(requireActivity(), R.color.on_boarding_span_text_color)
//        val loginColor = ForegroundColorSpan(color)
//        loginText.setSpan(UnderlineSpan(), 25, loginText.length, 0)
//        loginText.setSpan(loginColor, 25, loginText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
//        return loginText
//    }
    private fun clearField() {
        binding.etUsername.clearText()
        binding.etEmail.clearText()
        binding.etPassword.clearText()
    }
    private fun detailVerification(
        username: String,
        email: String,
        password: String
    ): Boolean {
        binding.apply {
            val (isUsernameValid, usernameError) = InputValidation.isUsernameValid(username)
            if (isUsernameValid.not()){
                etUsernameContainer.error = usernameError
                return isUsernameValid
            }

            val (isEmailValid, emailError) = InputValidation.isEmailValid(email)
            if (isEmailValid.not()){
                etEmailContainer.error = emailError
                return isEmailValid
            }

            val (isPasswordValid, passwordError) = InputValidation.isPasswordValid(password)
            if (isPasswordValid.not()){
                etPasswordContainer.error = passwordError
                return isPasswordValid
            }
            return true
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }


}