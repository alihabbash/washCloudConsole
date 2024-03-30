package com.washcloud.consoleapplication.remote.config.exceptions

interface IExceptionHandler {
    fun error(code: Int, message: String? = null): Exception
}