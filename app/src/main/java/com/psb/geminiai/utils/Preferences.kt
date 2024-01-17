package com.psb.geminiai.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.Locale

class Preferences(private val parentActivity: Context) {

    private val pref: SharedPreferences by lazy { parentActivity.getSharedPreferences(appKey, Context.MODE_PRIVATE) }
    private val appKey: String = parentActivity.packageName.replace("\\.".toRegex(), "_")
        .lowercase(Locale.getDefault())

    fun setString(key: String, value: String) {
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String? {
        return pref.getString(key, "")
    }

    fun setDouble(key: String, value: Double) {
        val editor = pref.edit()
        editor.putString(key, "$value")
        editor.apply()
    }

    fun getDouble(key: String): Double? {
        return if (pref.getString(key, "")!!.isNotEmpty()) {
            pref.getString(key, "")!!.toDouble()
        } else {
            null
        }
    }

    fun setBoolean(key: String, value: Boolean) {
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        return pref.getBoolean(key, false)
    }

    fun setInt(key: String, value: Int) {
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String): Int {
        return pref.getInt(key, 0)
    }

    fun <T> setJson(key: String, model: T) {
        val editor = pref.edit()
        editor.putString(key, Gson().toJson(model))
        editor.apply()
    }

    fun <T> getJson(key: String, _class: Class<T>): T {
        return Gson().fromJson(pref.getString(key, "{}"), _class)
    }

    fun setLong(key: String, value: Long) {
        val editor = pref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String): Long {
        return pref.getLong(key, 0)
    }

    fun isExist(key: String): Boolean {
        return pref.contains(key)
    }

    fun clearData() {
        pref.edit().clear().apply()
    }
}