package com.washcloud.consoleapplication.remote.config

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.washcloud.consoleapplication.local.preferences.IPrefsManager
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
    @ApplicationContext private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        var newRequest: Request = chain.request().newBuilder()
            .addHeader("Apikey", PrefsManager.getApiKey(context))
            .build()
            
        if (PrefsManager.isV2ApiEnabled(context)) {
            val url = newRequest.url
            val path = url.encodedPath

            if (path.startsWith("/api/LockerIntegration/") && !path.startsWith("/api/LockerIntegration/V2/")) {
                val lowerPath = path.lowercase()
                if (lowerPath.contains("/verification/") || 
                    lowerPath.endsWith("/heartbeat") || 
                    lowerPath.endsWith("/staffdropoff") || 
                    lowerPath.endsWith("/customerpickup")) {
                    
                    val newPath = path.replaceFirst("/api/LockerIntegration/", "/api/LockerIntegration/V2/")
                    val newUrl = url.newBuilder().encodedPath(newPath).build()
                    newRequest = newRequest.newBuilder().url(newUrl).build()
                }
            }
        }

        var response = chain.proceed(newRequest)

        when (response.code) {
            401 -> {
                // Show UnauthorizedError Message

            }


        }
        return response
    }
}