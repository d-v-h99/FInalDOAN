package com.hoangdoviet.finaldoan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginUiState(
    val username: String = "",
    val email: String = "",
): Parcelable