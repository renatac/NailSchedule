package com.example.nailschedule.view.activities.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nailschedule.view.activities.data.model.CalendarField
import com.example.nailschedule.view.activities.data.model.Time
import com.example.nailschedule.view.activities.data.service.FirebaseCalendarFieldService
import kotlinx.coroutines.launch

class CalendarFieldViewModel : ViewModel() {
    private val _calendarField = MutableLiveData<CalendarField>()
    val calendarField: LiveData<CalendarField> = _calendarField

    fun getCalendarFieldData(context: Context, date: String) {
        viewModelScope.launch {
            _calendarField.value = FirebaseCalendarFieldService
                .getCalendarFieldData(context, date)
        }
    }

    fun updateCalendarField(date: String, hoursList: Time) {
        viewModelScope.launch {
             FirebaseCalendarFieldService
                .updateCalendarFieldData(date, hoursList)
        }
    }

    fun deleteCalendarField(date: String) {
        viewModelScope.launch {
            FirebaseCalendarFieldService
                .deleteCalendarFieldData(date)
        }
    }
}