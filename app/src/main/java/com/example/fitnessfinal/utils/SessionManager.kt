package com.example.fitnessfinal.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.fitnessfinal.model.User

class SessionManager(context: Context) {
    private var preferences: SharedPreferences = context.getSharedPreferences("FitnessApp", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
    }

    fun createLoginSession(user: User) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.apply()

        println("✅ Session created for user: ${user.id}, ${user.name}")
    }

    fun getUserDetails(): Map<String, String> {
        return mapOf(
            KEY_USER_NAME to (preferences.getString(KEY_USER_NAME, "") ?: ""),
            KEY_USER_EMAIL to (preferences.getString(KEY_USER_EMAIL, "") ?: "")
        )
    }

    fun getUserId(): Long {
        val userId = preferences.getLong(KEY_USER_ID, -1)
        println("✅ SessionManager.getUserId(): $userId")
        return userId
    }

    fun isLoggedIn(): Boolean = preferences.getBoolean(KEY_IS_LOGGED_IN, false)

    fun logoutUser() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
        println("✅ User logged out")
    }
}