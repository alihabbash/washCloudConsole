package com.washcloud.consoleapplication.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.remote.config.JsonMapper
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity
import javax.inject.Inject

class PrefsManager constructor(
    private val jsonMapper: JsonMapper,
    context: Context
) : IPrefsManager {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val prefsEditor = prefs.edit()

    override suspend fun putInt(key: String, value: Int) {
        prefsEditor.putInt(key, value)
        prefsEditor.commit()
    }

    override suspend fun getInt(key: String): Int? {
        return if(prefs.contains(key)){
            prefs.getInt(key, -1)
        } else {
            null
        }
    }

    override suspend fun putString(key: String, value: String) {
        prefsEditor.putString(key, value)
        prefsEditor.commit()
    }

    override suspend fun getString(key: String): String? {
        return if(prefs.contains(key)){
            prefs.getString(key, null)
        } else {
            null
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        prefsEditor.putBoolean(key, value)
        prefsEditor.commit()
    }

    override suspend fun getBoolean(key: String): Boolean? {
        return if(prefs.contains(key)){
            prefs.getBoolean(key, false)
        } else {
            null
        }
    }

    override suspend fun <T> putObject(key: String, model: T) {
        val stringValue = model?.let { jsonMapper.toJson(it) }
        prefsEditor.putString(key, stringValue)
        prefsEditor.commit()
    }

    override suspend fun <T> getObject(key: String, clz: Class<T>): T? {
        val stringValue = getString(key) ?: return null
        return if (stringValue.isNotBlank()) jsonMapper.fromJson(stringValue, clz) else null
    }

    override suspend fun clearAllValues() {
        prefsEditor.clear()
    }

    override suspend fun clearKey(key: String) {
        prefsEditor.remove(key)
    }
    companion object {
        fun getApiKey(context: Context): String {
//            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
//            return sharedPreferences.getString(API_KEY_KEY, "") ?: ""
            return  "cb71aa3387256986324"
        }

        fun getTerminalSN(context: Context): String {
//            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
//            return sharedPreferences.getString(TERMINAL_SN_KEY, "") ?: ""
            return  "24222213703-003"
        }

        fun getBaseURL(context: Context): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(SERVER_OPTION, "") ?: ""
        }
    }

}