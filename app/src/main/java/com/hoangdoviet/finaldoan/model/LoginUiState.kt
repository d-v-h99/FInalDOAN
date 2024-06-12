package com.hoangdoviet.finaldoan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginUiState(
    val username: String = "",
    val email: String = "",
    val eventID: List<String> = listOf(),
    val taskIds: List<String> = listOf(),
): Parcelable