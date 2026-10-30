package com.tether.app.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tether_prefs", Context.MODE_PRIVATE)

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var displayName: String
        get() = prefs.getString(KEY_DISPLAY_NAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_DISPLAY_NAME, value).apply()

    var avatarColor: Int
        get() = prefs.getInt(KEY_AVATAR_COLOR, 0)
        set(value) = prefs.edit().putInt(KEY_AVATAR_COLOR, value).apply()

    var isProfileCompleted: Boolean
        get() = prefs.getBoolean(KEY_PROFILE_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_PROFILE_COMPLETED, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_DISPLAY_NAME = "key_display_name"
        private const val KEY_AVATAR_COLOR = "key_avatar_color"
        private const val KEY_PROFILE_COMPLETED = "key_profile_completed"
    }
}