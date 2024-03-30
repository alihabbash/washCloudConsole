package com.washcloud.consoleapplication.remote.config.exceptions

class UnAuthorizedException : Exception {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}