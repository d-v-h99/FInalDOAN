package com.hoangdoviet.finaldoan.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.hoangdoviet.finaldoan.fragment.profileFragment

class UserGoogleViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        profileFragment.PREFS_NAME, Context.MODE_PRIVATE)

    private val _isGoogleLoggedIn = MutableLiveData<Boolean>()
    val isGoogleLoggedIn: LiveData<Boolean> get() = _isGoogleLoggedIn
    private val _credential = MutableLiveData<GoogleAccountCredential?>()
    val credential: LiveData<GoogleAccountCredential?> get() = _credential


    init {
        _isGoogleLoggedIn.value = sharedPreferences.getBoolean(profileFragment.GOOGLE_LOGIN_STATUS, false)
    }

    fun setGoogleLoggedIn(isLoggedIn: Boolean) {
        _isGoogleLoggedIn.value = isLoggedIn
        sharedPreferences.edit().putBoolean(profileFragment.GOOGLE_LOGIN_STATUS, isLoggedIn).apply()
    }
    fun setCredential(credential: GoogleAccountCredential?) {
        _credential.value = credential
    }
}

