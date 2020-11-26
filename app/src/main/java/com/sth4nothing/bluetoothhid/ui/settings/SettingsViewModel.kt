package com.sth4nothing.bluetoothhid.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    val autoPair = MutableLiveData<Boolean>().apply {
        value = true
    }

    val screenOn = MutableLiveData<Boolean>().apply {
        value = true
    }
    var hasInit: Boolean = false
}