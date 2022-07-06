package com.example.nailschedule.view.activities.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nailschedule.view.activities.data.model.CalendarField
import com.example.nailschedule.view.activities.data.service.FirebaseCalendarFieldService
import kotlinx.coroutines.launch

class CalendarFieldViewModel : ViewModel() {
    private val _calendarField = MutableLiveData<CalendarField>()
    val calendarField: LiveData<CalendarField> = _calendarField
   // private val _posts = MutableLiveData<List<Post>>()
   // val posts: LiveData<List<Post>> = _posts

    fun getCalendarFieldData(date: String) {
        viewModelScope.launch {
            _calendarField.value = FirebaseCalendarFieldService
                .getCalendarFieldData(date)
           // _posts.value = FirebaseProfileService.getPosts()
        }
    }
    //Rest of your viewmodel
}