package com.example.nailschedule.view.activities.utils

import android.content.Context
import android.widget.Toast
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