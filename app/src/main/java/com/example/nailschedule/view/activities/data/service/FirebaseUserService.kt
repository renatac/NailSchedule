package com.example.nailschedule.view.activities.data.service

import android.util.Log
import com.example.nailschedule.view.activities.data.model.CalendarField
import com.example.nailschedule.view.activities.data.model.CalendarField.Companion.toCalendarField
import com.example.nailschedule.view.activities.data.model.Time
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.data.model.User.Companion.toUser
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseUserService {
    private const val TAG = "UserService"
    suspend fun getUserData(email: String): User? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("users")
                .document(email).get().await().toUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting User details", e)
            //FirebaseCrashlytics.getInstance().log("Error getting user details")
            //FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
            //FirebaseCrashlytics.getInstance().recordException(e)
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
            //FirebaseCrashlytics.getInstance().log("Error getting user details")
            //FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
            //FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }
}