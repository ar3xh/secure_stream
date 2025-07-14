package com.arcane.securestream.utils

import android.content.Context
import android.content.Intent
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth

object AuthUtils {

    private const val PREF_NAME = "LoginPrefs"
    private const val KEY_REMEMBER = "rememberMe"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    fun saveLoginCredentials(context: Context, email: String, password: String, remember: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (remember) {
            editor.putBoolean(KEY_REMEMBER, true)
            editor.putString(KEY_EMAIL, email)
            editor.putString(KEY_PASSWORD, password)
        } else {
            editor.clear()
        }

        editor.apply()
    }

    fun getSavedCredentials(context: Context): Triple<Boolean, String, String> {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER, false)
        val email = sharedPreferences.getString(KEY_EMAIL, "") ?: ""
        val password = sharedPreferences.getString(KEY_PASSWORD, "") ?: ""

        return Triple(rememberMe, email, password)
    }

    fun logOut(context: Context, destinationClass: Class<*>) {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Clear saved credentials
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Redirect to login screen
        val intent = Intent(context, destinationClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}