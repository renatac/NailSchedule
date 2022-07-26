package com.example.nailschedule.view.activities.data.service

import android.content.Context
import android.util.Log
import com.example.nailschedule.R
import com.example.nailschedule.view.activities.data.model.CalendarField
import com.example.nailschedule.view.activities.data.model.CalendarField.Companion.toCalendarField
import com.example.nailschedule.view.activities.data.model.Time
import com.example.nailschedule.view.activities.utils.showToast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.android.gms.tasks.Task

object FirebaseCalendarFieldService {
    private const val TAG = "CalendarFieldService"
    suspend fun getCalendarFieldData(context: Context, date: String): CalendarField? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("calendarField")
                .document(date).get().await().toCalendarField(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting calendarField details", e)
            showToast(context, R.string.error_scheduling)
            null
        }
    }

   fun updateCalendarFieldData(date: String, hoursList: Time):
    Task<Void>?{
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("calendarField")
                .document(date).set(hoursList)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating calendarField", e)
            null
        }
    }

    fun deleteCalendarFieldData(date: String):
            Task<Void>?{
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("calendarField")
                .document(date).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error delete calendarField", e)
            null
        }
    }
}