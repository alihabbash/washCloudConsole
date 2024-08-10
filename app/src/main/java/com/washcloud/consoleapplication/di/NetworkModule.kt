package com.washcloud.consoleapplication.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.washcloud.consoleapplication.BuildConfig
import com.washcloud.consoleapplication.remote.config.BASE_URL
import com.washcloud.consoleapplication.remote.config.HeadersInterceptors
import com.washcloud.consoleapplication.remote.config.IRetrofitService
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
            connectTimeout(3, TimeUnit.MINUTES)
            readTimeout(3, TimeUnit.MINUTES)
            writeTimeout(3, TimeUnit.MINUTES)
        }.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder().apply {
            baseUrl(BASE_URL)
            client(client)
            addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
        }.build()
    }

    @Provides
    @Singleton
    fun providerRetrofitService(retrofit: Retrofit): IRetrofitService {
        return retrofit.create(IRetrofitService::class.java)
    }

}