package com.example.fitnessfinal.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.fitnessfinal.model.User

class SessionManager(context: Context) {
    private var preferences: SharedPreferences = context.getSharedPreferences("FitnessApp", Context.MODE_PRIVATE)

    companion object {
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_USER_ID = "userId"
        const val KEY_USER_NAME = "userName"
        const val KEY_USER_EMAIL = "userEmail"
    }

    fun createLoginSession(user: User) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.apply()
    }

    fun getUserDetails(): HashMap<String, String> {
        val user = HashMap<String, String>()
        user[KEY_USER_NAME] = preferences.getString(KEY_USER_NAME, "") ?: ""
        user[KEY_USER_EMAIL] = preferences.getString(KEY_USER_EMAIL, "") ?: ""
        return user
    }

    fun getUserId(): Long = preferences.getLong(KEY_USER_ID, -1)

    fun isLoggedIn(): Boolean = preferences.getBoolean(KEY_IS_LOGGED_IN, false)

    fun logoutUser() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}