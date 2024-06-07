package com.hoangdoviet.finaldoan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event (
    val eventID: String = "",
    var date: String ="",
    var title: String ="",
    var timeStart: String = "",
    var timeEnd: String = "",
    var repeat: Int = 0,
    val originalEventID: String =""
) : Parcelable