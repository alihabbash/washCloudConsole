package com.washcloud.consoleapplication.remote.config.exceptions

import com.washcloud.consoleapplication.remote.config.ResponseCode
import javax.inject.Inject

class ExceptionHandler @Inject constructor() : IExceptionHandler {

    override fun error(code: Int, message: String?): Exception {
        return when (code) {
            ResponseCode.NETWORK_ERROR -> NoInternetException(message)
            ResponseCode.SERVER_ERROR -> ServerException(message)
            ResponseCode.UNAUTHORIZED -> UnAuthorizedException(message)
            else -> Exception(message)
        }
    }
}