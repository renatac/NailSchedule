package com.example.nailschedule.view.activities.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.data.service.FirebaseUserService
import kotlinx.coroutines.launch

class UsersViewModel: ViewModel() {
    private val _users = MutableLiveData<User>()
    val users: LiveData<User> = _users
    // private val _posts = MutableLiveData<List<Post>>()
    // val posts: LiveData<List<Post>> = _posts

    fun getUserData(context: Context, email: String) {
        viewModelScope.launch {
            _users.value = FirebaseUserService
                .getUserData(context, email)
            // _posts.value = FirebaseProfileService.getPosts()
        }
    }

    fun deleteUser(email: String) {
        viewModelScope.launch {
            FirebaseUserService
                .deleteUser(email)
            // _posts.value = FirebaseProfileService.getPosts()
        }
    }


    //Rest of your viewmodel
}