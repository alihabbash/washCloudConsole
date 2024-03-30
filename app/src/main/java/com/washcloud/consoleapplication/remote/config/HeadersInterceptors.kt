package com.washcloud.consoleapplication.remote.config

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class HeadersInterceptors @Inject constructor(
    private val prefsManager: PrefsManager,
    @ApplicationContext private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        var newRequest: Request = chain.request().newBuilder()
            .addHeader("Apikey", API_KEY)
            .build()
        var response = chain.proceed(newRequest)

        when (response.code) {
            401 -> {
                // Show UnauthorizedError Message

            }


        }
        return response
    }
}