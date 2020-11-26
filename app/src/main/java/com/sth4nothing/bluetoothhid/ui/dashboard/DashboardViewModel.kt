package com.sth4nothing.bluetoothhid.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {
    val script = MutableLiveData<String>().apply {
        value = "move 100, 100\nclick 1,1"
    }
}