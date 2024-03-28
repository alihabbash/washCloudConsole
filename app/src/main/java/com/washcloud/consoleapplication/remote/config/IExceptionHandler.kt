package com.washcloud.consoleapplication.remote.config

interface IExceptionHandler {
    fun error(code: Int, message: String? = null): Exception
}