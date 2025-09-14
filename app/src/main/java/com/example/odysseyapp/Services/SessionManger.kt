package com.example.odysseyapp.Utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "UserSession"
    private const val KEY_UID = "uid"
    private const val KEY_EMAIL = "email"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    //GeeksforGeeks (2022). Android Session Management with Kotlin
// Version not specified. Source code. Available at: https://www.geeksforgeeks.org/android-session-management-with-kotlin/ [Accessed 1 May 2025].
    // Save user session
    fun saveUser(context: Context, uid: String?, email: String?) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_UID, uid)
        editor.putString(KEY_EMAIL, email)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    // Get user ID
    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_UID, null)
    }

    // Get user email
    fun getUserEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_EMAIL, null)
    }

    // Check if user is logged in
    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Clear user session (logout)
    fun logout(context: Context) {
        val editor = getPrefs(context).edit()
        editor.clear()
        editor.apply()
    }
}