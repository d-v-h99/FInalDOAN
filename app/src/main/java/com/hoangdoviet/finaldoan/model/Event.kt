package com.hoangdoviet.finaldoan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event (
    val eventID: String = "",
    val date: String ="",
    val title: String ="",
    val timeStart: String = "",
    val timeEnd: String = "",
    val repeat: Int = 0,
    val originalEventID: String =""
) : Parcelable