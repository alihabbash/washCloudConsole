package com.washcloud.consoleapplication.utils

interface ParamsUseCase<PARAMS, RESULT> {
    suspend operator fun invoke(params: PARAMS): RESULT
}