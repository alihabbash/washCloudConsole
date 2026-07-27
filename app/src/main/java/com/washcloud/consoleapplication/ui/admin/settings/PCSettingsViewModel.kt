package com.washcloud.consoleapplication.ui.admin.settings

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.local.database.ConsoleDatabase
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.CustomerUserDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.preferences.*
import com.washcloud.consoleapplication.remote.config.BASE_URL_DEV
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PCSettingsViewModel @Inject constructor(
    application: Application,
    private val boxDao: BoxDao,
    private val transactionDao: TransactionDao,
    private val customerUserDao: CustomerUserDao
) : AndroidViewModel(application) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    var lockerName = mutableStateOf("")
    var rebootTime = mutableStateOf("")
    var isRebootEnabled = mutableStateOf(false)
    var isV2ApiEnabled = mutableStateOf(false)
    var isStaticQrOfflineEnabled = mutableStateOf(false)

    var branchApiKey = mutableStateOf("")
    var apiKey = mutableStateOf("")
    var terminalSn = mutableStateOf("")
    var branchId = mutableStateOf("")
    var delayMillis = mutableStateOf("")
    var serverOption = mutableStateOf("")
    var customServer = mutableStateOf("")

    var forceOpenBoxId = mutableStateOf("")
    var forceOpenStationId = mutableStateOf("")

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            lockerName.value = sharedPreferences.getString(LOCKER_NAME_KEY, "22222213703.001") ?: ""
            rebootTime.value = sharedPreferences.getString(REBOOT_TIME_KEY, "13:00") ?: ""
            isRebootEnabled.value = sharedPreferences.getBoolean(IS_REBOOT_ENABLED_KEY, true)
            isV2ApiEnabled.value = sharedPreferences.getBoolean(IS_V2_API_ENABLED_KEY, false)
            isStaticQrOfflineEnabled.value = sharedPreferences.getBoolean(IS_STATIC_QR_OFFLINE_ENABLED_KEY, false)

            branchApiKey.value = sharedPreferences.getString(BRANCH_API_KEY_KEY, "") ?: ""
            apiKey.value = sharedPreferences.getString(API_KEY_KEY, "cb71a12703264742b5b8") ?: ""
            terminalSn.value = sharedPreferences.getString(TERMINAL_SN_KEY, "21222213701A-001") ?: ""
            branchId.value = sharedPreferences.getString(BRANCH_ID_KEY, "28") ?: ""
            delayMillis.value = sharedPreferences.getString(DELAY_MILLIS_KEY, "600000") ?: ""

            serverOption.value = sharedPreferences.getString(SERVER_OPTION, BASE_URL_DEV) ?: BASE_URL_DEV
            customServer.value = sharedPreferences.getString(CUSTOM_SERVER, "") ?: ""
        }
    }


    fun saveIsRebootEnabled() {
        with(sharedPreferences.edit()) {
            putBoolean(IS_REBOOT_ENABLED_KEY, isRebootEnabled.value)
            apply()
        }
    }

    fun saveIsV2ApiEnabled() {
        with(sharedPreferences.edit()) {
            putBoolean(IS_V2_API_ENABLED_KEY, isV2ApiEnabled.value)
            apply()
        }
    }

    fun saveIsStaticQrOfflineEnabled() {
        with(sharedPreferences.edit()) {
            putBoolean(IS_STATIC_QR_OFFLINE_ENABLED_KEY, isStaticQrOfflineEnabled.value)
            apply()
        }
    }


    fun saveAllSettings() {

        println("serverOption: " + serverOption.value);
        with(sharedPreferences.edit()) {
            putString(LOCKER_NAME_KEY, lockerName.value)
            putString(REBOOT_TIME_KEY, rebootTime.value)
            putString(API_KEY_KEY, apiKey.value)
            putString(TERMINAL_SN_KEY, terminalSn.value)
            putString(BRANCH_ID_KEY, branchId.value)
            putString(DELAY_MILLIS_KEY, delayMillis.value)
            putString(SERVER_OPTION, serverOption.value)
            putString(CUSTOM_SERVER, customServer.value)
            putBoolean(IS_V2_API_ENABLED_KEY, isV2ApiEnabled.value)
            putBoolean(IS_STATIC_QR_OFFLINE_ENABLED_KEY, isStaticQrOfflineEnabled.value)
            putString(BRANCH_API_KEY_KEY, branchApiKey.value)
            apply()
        }
    }


    fun resetDatabase() {
        viewModelScope.launch {
            boxDao.deleteAllBoxes()
            transactionDao.deleteAllTransactions()
            customerUserDao.deleteAllCustomerUsers()
        }
    }

    fun forceOpenBox(context: android.content.Context) {
        val stationId = forceOpenStationId.value
        val boxId = forceOpenBoxId.value
        FileLogger.log(context, "PCSettings_ForceOpen", "Attempting to force open boxId $boxId at stationId $stationId")
        
        try {
            val intent = android.content.Intent("com.washcloud.open_door").apply {
                putExtra("stationId", stationId)
                putExtra("boxId", boxId)
            }
            context.sendBroadcast(intent)
            FileLogger.log(context, "PCSettings_ForceOpen", "Successfully sent force open command for boxId $boxId at stationId $stationId")
        } catch (e: Exception) {
            FileLogger.log(context, "PCSettings_ForceOpen", "FAILED to force open locker. Exception: ${e.message}")
        }
    }
}