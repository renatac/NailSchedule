package com.example.nailschedule.view.activities.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nailschedule.view.activities.view.activities.LoginActivity
import com.facebook.AccessToken

fun isLoggedInFacebook(): Boolean {
    val accessToken = AccessToken.getCurrentAccessToken()
    return accessToken != null
}

fun showToast(context: Context, stringId: Int) {
    Toast.makeText(
        context,
        context.getString(stringId),
        Toast.LENGTH_LONG
    ).show()
}

fun showLoginScreen(activity: AppCompatActivity) = activity.apply {
    startActivity(Intent(activity, LoginActivity::class.java))
    finish()
}

fun isNotEmptyField(field: String?): Boolean {
    return field != null && field.isNotBlank() && field.isNotEmpty()
}

fun isEmptyField(field: String?): Boolean {
    return field == null || field.isBlank() || field.isEmpty()
}