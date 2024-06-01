package com.hoangdoviet.finaldoan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hoangdoviet.finaldoan.model.Event

class SharedViewModel : ViewModel() {
    private val _eventList = MutableLiveData<List<Event>>()
    val eventList: LiveData<List<Event>> get() = _eventList

    private val _eventDeleted = MutableLiveData<Boolean>()
    val eventDeleted: LiveData<Boolean> get() = _eventDeleted

    fun setEventList(events: List<Event>) {
        _eventList.value = events
    }

    fun setEventDeleted(deleted: Boolean) {
        _eventDeleted.value = deleted
    }
}