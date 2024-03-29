package com.washcloud.consoleapplication.remote.config

import com.washcloud.consoleapplication.remote.config.exceptions.IExceptionHandler
import com.washcloud.consoleapplication.remote.config.exceptions.RequestTimedOutException
import com.washcloud.consoleapplication.remote.config.exceptions.ServerException
import org.json.JSONObject
import retrofit2.Response
import java.io.InterruptedIOException

class ApiProvider(
    private val exceptionHandler: IExceptionHandler
) : IApiProvider {

    override suspend fun <T> proceedRequest(call: suspend () -> Response<T>): T {
        val response = getResponseOrFail(call)
        return getResponseBodyOrFail(response)
    }

    private suspend fun <T> getResponseOrFail(call: suspend () -> Response<T>): Response<T> {
        return try {
            call.invoke()
        } catch (e: Exception) {
            if (e is InterruptedIOException) throw RequestTimedOutException(e)
            else throw ServerException(e)
        }
    }

    private fun <T> getResponseBodyOrFail(response: Response<T>): T {
        val body = response.body()
        response.message()
        if (response.isSuccessful && body != null) {
            return body
        } else {
            val errorBody = response.errorBody()?.string()
            val message = if (errorBody != null) {
                JSONObject(errorBody)
                    .optString("message", "")
                    .takeIf { it.isNotEmpty() }
            } else null
            throw exceptionHandler.error(response.code(), message ?: response.message())
        }
    }
}