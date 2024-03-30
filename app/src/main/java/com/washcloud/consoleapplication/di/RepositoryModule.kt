package com.washcloud.consoleapplication.di

import com.washcloud.consoleapplication.remote.datasource.IStaffRemoteDataSource
import com.washcloud.consoleapplication.remote.datasource.StaffRemoteDataSource
import com.washcloud.consoleapplication.repository.IStaffRepository
import com.washcloud.consoleapplication.repository.StaffRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindsStaffRepository(staffRepository: StaffRepository): IStaffRepository
    @Binds
    fun bindsStaffRemoteDataSource(staffRemoteDataSource: StaffRemoteDataSource): IStaffRemoteDataSource
}