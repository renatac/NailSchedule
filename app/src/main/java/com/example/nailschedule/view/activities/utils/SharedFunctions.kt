package com.example.nailschedule.view.activities.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nailschedule.view.activities.view.activities.LoginActivity
import com.facebook.AccessToken

const val semicolonTrue = ";true"
const val semicolonFalse = ";false"

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
    val intent = Intent(activity, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

fun isNotEmptyField(field: String?): Boolean {
    return field != null && field.isNotBlank() && field.isNotEmpty()
}

fun isEmptyField(field: String?): Boolean {
    return field == null || field.isBlank() || field.isEmpty()
}

fun String.hasTrueSemicolon() = this.contains("true;")

fun String.hasSemicolonTrue() = this.contains(";true")

fun String.hasFalseSemicolon() = this.contains("false;")

fun String.hasSemicolonFalse() = this.contains(";false")