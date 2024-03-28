package com.washcloud.consoleapplication.remote.config

import retrofit2.Response


interface IApiProvider {

    suspend fun <T> proceedRequest(call: suspend () -> Response<T>): T
}