package com.hms.socialsteps.core.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.hms.socialsteps.data.model.Users
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreference @Inject constructor(@ApplicationContext context: Context) {

    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun getStoredUser(): Users {
        val gson = Gson()
        val json = prefs.getString("LoggedUser","{}")
        return gson.fromJson(json, Users::class.java)
    }

    fun setStoredUser(user: Users) {
        val prefsEditor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(user)
        prefsEditor.putString("LoggedUser", json)
        prefsEditor.apply()
    }

    fun setIsFirst(boolean: Boolean){
        val prefsEditor = prefs.edit()
        prefsEditor.putBoolean("isFirstSet", boolean)
        prefsEditor.apply()
    }

    fun getIsFirst(): Boolean {
        return prefs.getBoolean("isFirstSet", false)
    }
}