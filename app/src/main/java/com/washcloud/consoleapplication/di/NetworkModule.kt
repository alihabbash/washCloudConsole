package com.washcloud.consoleapplication.di

import android.app.Application
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.washcloud.consoleapplication.BuildConfig
import com.washcloud.consoleapplication.remote.config.HeadersInterceptors
import com.washcloud.consoleapplication.remote.config.IRetrofitService
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideClient(
        headersInterceptors: HeadersInterceptors
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder().apply {
            addInterceptor(headersInterceptors)
            if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor)
            connectTimeout(50, TimeUnit.SECONDS)
            readTimeout(50, TimeUnit.SECONDS)
            writeTimeout(50, TimeUnit.SECONDS)
            hostnameVerifier { hostname, session ->
                val verifier = HttpsURLConnection.getDefaultHostnameVerifier()
                if (hostname == "20.21.54.1") {
                    verifier.verify("washcloudproduction.azurewebsites.net", session)
                } else {
                    verifier.verify(hostname, session)
                }
            }
        }.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        application: Application
    ): Retrofit {
        val baseUrl = MainAdActivity.getBaseUrl(application)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun providerRetrofitService(retrofit: Retrofit): IRetrofitService {
        return retrofit.create(IRetrofitService::class.java)
    }
}
