package com.washcloud.consoleapplication.di

import com.washcloud.consoleapplication.remote.datasource.HeartbeatRemoteDataSource
import com.washcloud.consoleapplication.remote.datasource.IHeartbeatRemoteDataSource
import com.washcloud.consoleapplication.remote.datasource.IStaffPickupRemoteDataSource
import com.washcloud.consoleapplication.remote.datasource.IStaffRemoteDataSource
import com.washcloud.consoleapplication.remote.datasource.StaffPickupRemoteDataSource
import com.washcloud.consoleapplication.remote.datasource.StaffRemoteDataSource
import com.washcloud.consoleapplication.repository.HeartbeatRepository
import com.washcloud.consoleapplication.repository.IHeartbeatRepository
import com.washcloud.consoleapplication.repository.IStaffPickupRepository
import com.washcloud.consoleapplication.repository.IStaffRepository
import com.washcloud.consoleapplication.repository.StaffPickupRepository
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

    @Binds
    fun bindsHeartbeatRemoteDataSource(heartbeatRemoteDataSource: HeartbeatRemoteDataSource): IHeartbeatRemoteDataSource

    @Binds
    fun bindHeartbeatRepository(heartbeatRepository: HeartbeatRepository): IHeartbeatRepository
    @Binds
    fun bindStaffPickupDataSource(staffPickupRemoteDataSource: StaffPickupRemoteDataSource): IStaffPickupRemoteDataSource

    @Binds
    fun bindsStaffPickupRepository(staffPickupRepository: StaffPickupRepository): IStaffPickupRepository
}