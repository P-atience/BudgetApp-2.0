package com.budgettracker.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "BudgetTrackerSession"
        const val KEY_USER_ID = "userId"
        const val KEY_USERNAME = "username"
        const val KEY_IS_ADMIN = "isAdmin"
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }

    fun saveSession(userId: Int, username: String, isAdmin: Boolean) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_IS_ADMIN, isAdmin)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""
    fun isAdmin(): Boolean = prefs.getBoolean(KEY_IS_ADMIN, false)
}
