package com.washcloud.consoleapplication.remote.config

class RequestTimedOutException : Exception {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}