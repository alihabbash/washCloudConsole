package com.washcloud.consoleapplication.remote.config

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MoshiJsonMapper : JsonMapper {
    override fun <T : Any> toJson(value: T): String {
        return getJsonAdapter(value.javaClass).toJson(value)
    }

    override fun <T> fromJson(json: String, clz: Class<T>): T? {
        return getJsonAdapter(clz).fromJson(json)
    }

    private fun <T> getJsonAdapter(clz: Class<T>): JsonAdapter<T> {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build().adapter(clz)
    }
}