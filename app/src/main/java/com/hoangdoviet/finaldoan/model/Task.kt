package com.hoangdoviet.finaldoan.model

data class Task(
    val id: String = "",
    val title: String = "",
    var status: String = "", // "Chưa làm", "Hoàn thành", "Quá hạn"
    val userId: String = ""
)
