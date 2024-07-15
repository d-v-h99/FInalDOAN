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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hoangdoviet.finaldoan.MainActivity
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentLoginBinding
import com.hoangdoviet.finaldoan.utils.InputValidation
import com.hoangdoviet.finaldoan.utils.LoadingDialog
import com.hoangdoviet.finaldoan.utils.addTextWatcher
import com.hoangdoviet.finaldoan.utils.clearText
import com.hoangdoviet.finaldoan.utils.getInputValue
import com.hoangdoviet.finaldoan.utils.showToast
import com.hoangdoviet.hoangfirebase.util.Status
import com.hoangdoviet.hoangfirebase.viewmodel.AuthViewModel


class loginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        setupUI()
        setupObserver()

        return binding.root
    }

    private fun setupObserver() {
        authViewModel.loginStatus.observe(viewLifecycleOwner){loginState ->
            when(loginState.status){
                Status.LOADING -> {loadingDialog.show()}
                Status.SUCCESS -> {
                    val currentUser = loginState.data!!
                    loadingDialog.dismiss()
                    val intent = Intent(activity, MainActivity::class.java).apply {
                        putExtra("loginUiState", currentUser)
                    }
                    startActivity(intent)

                }
                Status.ERROR -> {
                    showToast(requireContext(), loginState.message.toString())
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun setupUI() {
        binding.apply {
            tvSignup.text = createSignupText()
            tvSignup.setOnClickListener {
                navigateToSignup()
            }
            tvForgetPassword.setOnClickListener {
                navigateToForgotPassword()
            }
            etEmailContainer.addTextWatcher()
            etPasswordContainer.addTextWatcher()
            btnLogin.setOnClickListener {
                val email = etEmail.getInputValue()
                val password = etPassword.getInputValue()
                if (detailVerification(email, password)) {
                    authViewModel.login(email, password)
                    clearField()
                }
            }
        }
    }
    private fun createSignupText(): SpannableString {
        val signupText = SpannableString(getString(R.string.sign_up_prompt))
        val color = ContextCompat.getColor(requireActivity(), R.color.on_boarding_span_text_color)
        val signupColor = ForegroundColorSpan(color)
        signupText.setSpan(UnderlineSpan(), 31, signupText.length, 0)
        signupText.setSpan(signupColor, 31, signupText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return signupText
    }

    private fun clearField() {
        binding.etEmail.clearText()
        binding.etPassword.clearText()
    }

    private fun navigateToForgotPassword() {
        findNavController().navigate(R.id.action_loginFragment2_to_forgotPassFragment2)
    }

    private fun navigateToSignup() {
        findNavController().navigate(R.id.action_loginFragment2_to_signupFragment2)
    }
    // xac minh nguoi dung thong bao loi neu co
    private fun detailVerification(
        email: String,
        password: String
    ): Boolean {
        binding.apply {
            val (isEmailValid, emailError) = InputValidation.isEmailValid(email)
            if (isEmailValid.not()) {
                etEmailContainer.error = emailError
                return isEmailValid
            }

            val (isPasswordValid, passwordError) = InputValidation.isPasswordValid(password)
            if (isPasswordValid.not()) {
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