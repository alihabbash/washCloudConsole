package com.washcloud.consoleapplication.remote.config

class ServerException : Exception {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}