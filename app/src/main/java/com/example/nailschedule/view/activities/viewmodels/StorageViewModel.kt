package com.example.nailschedule.view.activities.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nailschedule.view.activities.data.service.FirebaseStorageService
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch


class StorageViewModel : ViewModel() {

    private val _listResult = MutableLiveData<ListResult>()
    val listResult: LiveData<ListResult> = _listResult

    fun getImagesList(context: Context, email: String) {
        viewModelScope.launch {
            _listResult.value = FirebaseStorageService.getImagesList(context, email)
        }
    }

    fun deleteImageReference(storageReference: StorageReference) {
        FirebaseStorageService.deleteImageReference(storageReference)
    }

    fun deleteChildImage(path: String) {
        FirebaseStorageService.deleteImageReference(path)
    }

    fun getImageReference(context: Context, email: String, filename: String): StorageReference? =
        FirebaseStorageService.getImageReference(context, email, filename)
}