package com.example.nailschedule.view.activities.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences


object SharedPreferencesHelper {
    private var mSharedPref: SharedPreferences? = null
    const val EXTRA_DISPLAY_NAME = "extra_display_name"
    const val EXTRA_PHOTO_URL = "extra_photo_url"
    const val EXTRA_EMAIL = "extra_email"
    const val GOOGLE_ACCESS_TOKEN = "google_access_token"
    const val GOOGLE_TOKEN_ID = "google_token_id"
    const val FACEBOOK_ACCESS_TOKEN = "facebook_access_token"
    const val NAME = "name"
    const val SERVICE = "service"
    const val DATE = "date"
    const val TIME = "time"
    const val POSITION = "position"
    const val URI_STRING = "uri_string"
    const val MIN_DATE = "min_date"

    fun init(context: Context) {
        if (mSharedPref == null) mSharedPref =
            context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
    }

    fun read(key: String, defValue: String?): String? {
        return mSharedPref?.getString(key, defValue)
    }

    fun read(key: String, defValue: Int): Int? {
        return mSharedPref?.getInt(key, defValue)
    }

    fun write(key: String, value: String?) {
        val prefsEditor = mSharedPref?.edit()
        prefsEditor?.putString(key, value)
        prefsEditor?.apply()
    }

    fun write(key: String, value: Boolean) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putBoolean(key, value)
        prefsEditor.apply()
    }

    fun write(key: String, value: Int?) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putInt(key, value!!).apply()
    }
}