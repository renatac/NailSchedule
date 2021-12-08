package com.example.nailschedule.view.activities.utils

import com.facebook.AccessToken

fun isLoggedInFacebook(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null
}