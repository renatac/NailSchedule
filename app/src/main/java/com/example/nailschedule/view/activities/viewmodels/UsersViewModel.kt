package com.example.nailschedule.view.activities.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nailschedule.R
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.data.service.FirebaseUserService
import com.example.nailschedule.view.activities.utils.showToast
import kotlinx.coroutines.launch

class UsersViewModel: ViewModel() {
    private val _users = MutableLiveData<User>()
    val users: LiveData<User> = _users

    fun getUserData(context: Context, email: String) {
        viewModelScope.launch {
            _users.value = FirebaseUserService
                .getUserData(context, email)
        }
    }

    fun deleteUser(email: String) {
        viewModelScope.launch {
            FirebaseUserService
                .deleteUser(email)
        }
    }

    fun updateUser(context: Context, email: String, user: User) {
        viewModelScope.launch {
            FirebaseUserService
                .updateUser(email, user)?.addOnSuccessListener {
                    showToast(context, R.string.successful_scheduling)
                }?.addOnFailureListener {
                    showToast(context, R.string.error_scheduling)
                }
        }
    }
}