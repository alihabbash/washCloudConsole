package com.washcloud.consoleapplication.remote.usecase

import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse
import com.washcloud.consoleapplication.repository.StaffRepository
import com.washcloud.consoleapplication.utils.ParamsUseCase
import javax.inject.Inject

class StaffLoginUseCase @Inject constructor(
    private val staffRepository: StaffRepository
) : ParamsUseCase<StaffLoginUseCase.Params, StaffLoginResponse> {

    override suspend fun invoke(params: Params): StaffLoginResponse {
        return staffRepository.login(params.account, params.password)
    }

    data class Params(
        val account: String,
        val password: String
    )
}