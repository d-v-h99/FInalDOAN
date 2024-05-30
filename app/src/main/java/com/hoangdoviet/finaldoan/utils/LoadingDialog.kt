package com.hoangdoviet.finaldoan.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import com.hoangdoviet.finaldoan.R

class LoadingDialog(context : Context) : Dialog(context) {
    init {
        // Set the layout for the dialog
        setContentView(R.layout.loading_dialog)

        // Set the dialog's window properties
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        setCancelable(false)
    }
}