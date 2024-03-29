package com.washcloud.consoleapplication.remote.config

interface JsonMapper {
    fun <T : Any> toJson(value: T): String
    fun <T> fromJson(json: String, clz: Class<T>): T?
}