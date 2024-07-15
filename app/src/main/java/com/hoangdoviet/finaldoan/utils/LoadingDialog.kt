package com.hoangdoviet.finaldoan.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import com.hoangdoviet.finaldoan.R

class LoadingDialog(context : Context) : Dialog(context) {
    init {
        setContentView(R.layout.loading_dialog)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        setCancelable(false)
    }
}