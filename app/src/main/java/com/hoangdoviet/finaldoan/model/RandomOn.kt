package com.hoangdoviet.finaldoan.model

import java.util.*

class RandomOn {
    fun random(from: Int, to: Int): Int {
        return Random().nextInt(to - from) + from
    }
}