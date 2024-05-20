package com.washcloud.consoleapplication.remote.config

const val BASE_URL = "https://devwashcloud.azurewebsites.net/"
const val PREFIX = "api/WinnsenMockIntegration/"
const val STAFF_LOGIN = PREFIX + "StaffVerification"
const val VERIFY_ORDER = PREFIX + "Verification/{serial}/{terminalSn}"
const val STAFF_DROP_OFF = PREFIX + "StaffDropOff"
const val STAFF_PICKUP = PREFIX + "StaffPickup"
