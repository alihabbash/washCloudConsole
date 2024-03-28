package com.washcloud.consoleapplication.remote.config

class UnAuthorizedException : Exception {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}