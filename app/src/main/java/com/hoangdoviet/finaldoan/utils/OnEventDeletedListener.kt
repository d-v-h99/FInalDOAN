package com.hoangdoviet.finaldoan.utils

import com.hoangdoviet.finaldoan.model.Event

interface OnEventDeletedListener {
    fun onEventDeleted(event: Event)
}
