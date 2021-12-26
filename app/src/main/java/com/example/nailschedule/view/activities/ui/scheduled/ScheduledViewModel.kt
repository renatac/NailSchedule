package com.example.nailschedule.view.activities.ui.scheduled

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScheduledViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Scheduled Fragment"
    }
    val text: LiveData<String> = _text
}