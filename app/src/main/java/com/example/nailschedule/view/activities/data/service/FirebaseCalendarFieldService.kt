package com.example.nailschedule.view.activities.data.service

import android.util.Log
import com.example.nailschedule.view.activities.data.model.CalendarField
import com.example.nailschedule.view.activities.data.model.CalendarField.Companion.toCalendarField
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseCalendarFieldService {
    private const val TAG = "CalendarFieldService"
    suspend fun getCalendarFieldData(date: String): CalendarField? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("calendarField")
                .document(date).get().await().toCalendarField(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting calendarField details", e)
            //FirebaseCrashlytics.getInstance().log("Error getting user details")
            //FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
            //FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }
}