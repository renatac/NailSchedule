package com.example.nailschedule.view.activities.data.service

import android.content.Context
import android.util.Log
import com.example.nailschedule.R
import com.example.nailschedule.view.activities.utils.showToast
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

object FirebaseStorageService {

    private const val TAG = "StorageService"
    val storage = Firebase.storage.reference


    suspend fun getImagesList(context: Context, email: String): ListResult? {
        return try {
            storage.child("/images")
                .child("/$email")
                .listAll().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting images list", e)
            showToast(context, R.string.error_storage)
            null
        }
    }

    fun getImageReference(context: Context, email: String, filename: String): StorageReference? {
        return try {
            FirebaseStorage.getInstance()
                .getReference("images/${email}/${filename}")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting images reference", e)
            showToast(context, R.string.error_storage_reference)
            null
        }
    }

    fun deleteImageReference(storageReference : StorageReference): Task<Void>? {
        return try {
            storageReference.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deletion image reference", e)
            null
        }
    }

   fun deleteImageReference(path: String): Task<Void>? {
        return try {
            storage.child("/images")
                .child(path)
                .delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deletion image", e)
            null
        }
    }
}