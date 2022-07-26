package com.example.nailschedule.view.activities.data.service

import android.content.Context
import android.util.Log
import com.example.nailschedule.R
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.data.model.User.Companion.toUser
import com.example.nailschedule.view.activities.utils.showToast
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseUserService {
    private const val TAG = "UserService"
    suspend fun getUserData(context: Context, email: String): User? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("users")
                .document(email).get().await().toUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting User details", e)
            showToast(context, R.string.error_scheduling)
            null
        }
    }

    fun deleteUser(email: String): Task<Void>? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("users")
                .document(email).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting calendarField details", e)
            null
        }
    }

    fun updateUser(email: String, user: User): Task<Void>? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("users")
                .document(email).set(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting calendarField details", e)
            null
        }
    }
}