package com.washcloud.consoleapplication.utils

interface UseCase<RESULT> {
    suspend operator fun invoke(): RESULT
}
