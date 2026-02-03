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



        // ===============================
// ✅ QR Replay Protection (STATIC)
// ===============================


        /**
         * Check if QR signature was already scanned
         */
        fun isQrAlreadyScanned(context: Context, signature: String): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val set = prefs.getStringSet(SCANNED_QR_SIGNATURES, emptySet())
            return set?.contains(signature) == true
        }

        /**
         * Save scanned QR signature
         */
        fun saveScannedQr(context: Context, signature: String) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val currentSet = prefs
                .getStringSet(SCANNED_QR_SIGNATURES, emptySet())
                ?.toMutableSet() ?: mutableSetOf()

            currentSet.add(signature)

            prefs.edit()
                .putStringSet(SCANNED_QR_SIGNATURES, currentSet)
                .apply()
        }

        /**
         * Optional: clear all scanned QR signatures
         */
        fun clearScannedQrs(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit()
                .remove(SCANNED_QR_SIGNATURES)
                .apply()
        }

        fun getApiKey(context: Context): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(API_KEY_KEY, "") ?: ""

        }

        fun getTerminalSN(context: Context): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(TERMINAL_SN_KEY, "") ?: ""

        }

        fun getBaseURL(context: Context): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(SERVER_OPTION, "") ?: ""
        }

        fun getTotalBagsCount(context: Context): Int {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getInt(KEY_TOTAL_BAGS_COUNT, 0)
        }

        fun setTotalBagsCount(context: Context, value: Int) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit().putInt(KEY_TOTAL_BAGS_COUNT, value).apply()
        }



    }

}