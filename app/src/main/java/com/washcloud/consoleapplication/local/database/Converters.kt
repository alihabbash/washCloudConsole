package com.washcloud.consoleapplication.local.database

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types


object Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val jsonAdapter = moshi.adapter<List<String>>(type)

        return jsonAdapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val jsonAdapter = moshi.adapter<List<String>>(type)
        return jsonAdapter.toJson(list)
    }
}