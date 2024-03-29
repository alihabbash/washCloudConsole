package com.washcloud.consoleapplication.local.preferences

interface IPrefsManager {

    suspend fun putInt(key: String, value: Int)

    suspend fun getInt(key: String,): Int?

    suspend fun putString(key: String, value: String)

    suspend fun getString(key: String,): String?

    suspend fun putBoolean(key: String, value: Boolean)

    suspend fun getBoolean(key: String): Boolean?

    suspend fun <T> putObject(key: String, model: T)

    suspend fun <T> getObject(key: String, clz: Class<T>): T?

    suspend fun clearKey(key: String)

    suspend fun clearAllValues()
}