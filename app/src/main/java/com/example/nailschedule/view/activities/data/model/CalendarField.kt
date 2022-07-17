package com.example.nailschedule.view.activities.data.model

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CalendarField(
  private val date: String, // it is the actual id
  val timeList: List<String>
) : Parcelable {

    companion object {
        fun DocumentSnapshot.toCalendarField(date: String): CalendarField? {
            try {
                val timeList = get("timeList") as List<*>
                val mutableTimeList = mutableListOf<String>()
                timeList.forEach { time ->
                    time?.let { t ->
                        mutableTimeList.add(t.toString())
                    }
                }
                return CalendarField(date, timeList = mutableTimeList)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting calendarField", e)
                //FirebaseCrashlytics.getInstance().log("Error converting user profile")
                //FirebaseCrashlytics.getInstance().setCustomKey("userId", id)
                //FirebaseCrashlytics.getInstance().recordException(e)
                return null
            }
        }
        private const val TAG = "CalendarField"
    }
}