package com.example.nailschedule.view.activities.data.model

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val name: String,
    val service: String,
    val date: String,
    val time: String,
    val uriString: String,
    val email: String) : Parcelable {

    companion object {
        fun DocumentSnapshot.toUser(): User? {
            return try {
                val name = getString("name")!!
                val service = getString("service")!!
                val date = getString("date")!!
                val time = getString("time")!!
                val uriString = getString("uriString")!!
                val email = getString("email")!!
                User(name, service, date, time, uriString, email)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting User", e)
                //FirebaseCrashlytics.getInstance().log("Error converting user profile")
                //FirebaseCrashlytics.getInstance().setCustomKey("userId", id)
                //FirebaseCrashlytics.getInstance().recordException(e)
                null
            }
        }
        private const val TAG = "User"
    }
}