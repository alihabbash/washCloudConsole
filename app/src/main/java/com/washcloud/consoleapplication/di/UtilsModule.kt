package com.washcloud.consoleapplication.di

import com.washcloud.consoleapplication.remote.config.ApiProvider
import com.washcloud.consoleapplication.remote.config.exceptions.ExceptionHandler
import com.washcloud.consoleapplication.remote.config.IApiProvider
import com.washcloud.consoleapplication.remote.config.exceptions.IExceptionHandler
import com.washcloud.consoleapplication.remote.config.JsonMapper
import com.washcloud.consoleapplication.remote.config.MoshiJsonMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideApiProvider(exceptionHandler: IExceptionHandler): IApiProvider {
        return ApiProvider(exceptionHandler)
    }

    @Provides
    @Singleton
    fun provideExceptionHandler(): IExceptionHandler {
        return ExceptionHandler()
    }

    @Provides
    @Singleton
    fun provideJsonMapper(): JsonMapper {
        return MoshiJsonMapper()
    }
}