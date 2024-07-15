package com.hoangdoviet.finaldoan.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.databinding.FragmentForgotPassBinding
import com.hoangdoviet.finaldoan.utils.InputValidation
import com.hoangdoviet.finaldoan.utils.LoadingDialog
import com.hoangdoviet.finaldoan.utils.addTextWatcher
import com.hoangdoviet.finaldoan.utils.clearText
import com.hoangdoviet.finaldoan.utils.getInputValue
import com.hoangdoviet.finaldoan.utils.showToast
import com.hoangdoviet.hoangfirebase.util.Status
import com.hoangdoviet.hoangfirebase.viewmodel.AuthViewModel


class forgotPassFragment : Fragment() {
    private var _binding: FragmentForgotPassBinding? = null
    private val binding get() = _binding!!

    private val authViewModel by viewModels<AuthViewModel>()
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPassBinding.inflate(inflater, container, false)

        setupUI()
        setupObserver()

        return binding.root
    }
    private fun setupObserver() {
        authViewModel.resendPasswordStatus.observe(viewLifecycleOwner){ resendPasswordState ->
            when(resendPasswordState.status){
                Status.LOADING -> {
                    loadingDialog.show()
                }
                Status.SUCCESS -> {
                    val successMessage = resendPasswordState.data!!
                    showToast(requireContext(), successMessage)
                    loadingDialog.dismiss()
                    findNavController().navigate(R.id.action_forgotPassFragment2_to_emailFragment2)
                }
                Status.ERROR -> {
                    val errorMessage = resendPasswordState.message!!
                    showToast(requireContext(), errorMessage)
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun setupUI() {
        binding.apply {
            btnBackToLogin.setOnClickListener {
                findNavController().popBackStack()
            }
            etEmailContainer.addTextWatcher()
            btnResetPassword.setOnClickListener {
                val email = etEmail.getInputValue()
                if(email.isEmpty()){
                    showToast(requireContext(), "Không được để trống email")
                    return@setOnClickListener
                }
                authViewModel.resendPassword(email)
                val (isEmailValid, emailError) = InputValidation.isEmailValid(email)
                if (isEmailValid.not()) {
                    authViewModel.resendPassword(email)
                    clearField()
                } else {
                    etEmailContainer.error = emailError
                }
            }
        }
    }
    private fun clearField() {
        binding.etEmail.clearText()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }


}