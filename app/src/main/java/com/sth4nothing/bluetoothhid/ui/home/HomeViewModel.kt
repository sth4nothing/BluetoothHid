package com.sth4nothing.bluetoothhid.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val bltConnect = MutableLiveData<Boolean>()
    val modifier_checked_state = MutableLiveData<Int>().apply {
        value = 0
    }
}