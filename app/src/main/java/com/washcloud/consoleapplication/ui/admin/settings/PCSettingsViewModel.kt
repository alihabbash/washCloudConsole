package com.washcloud.consoleapplication.ui.admin.settings

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.local.database.ConsoleDatabase
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.preferences.*
import com.washcloud.consoleapplication.remote.config.BASE_URL_DEV
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PCSettingsViewModel @Inject constructor(
    application: Application,
    private val boxDao: BoxDao,
    private val transactionDao: TransactionDao
) : AndroidViewModel(application) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    var lockerName = mutableStateOf("")
    var rebootTime = mutableStateOf("")
    var isRebootEnabled = mutableStateOf(false)

    var apiKey = mutableStateOf("")
    var terminalSn = mutableStateOf("")
    var branchId = mutableStateOf("")
    var delayMillis = mutableStateOf("")
    var serverOption = mutableStateOf("")
    var customServer = mutableStateOf("")


    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            lockerName.value = sharedPreferences.getString(LOCKER_NAME_KEY, "22222213703.001") ?: ""
            rebootTime.value = sharedPreferences.getString(REBOOT_TIME_KEY, "13:00") ?: ""
            isRebootEnabled.value = sharedPreferences.getBoolean(IS_REBOOT_ENABLED_KEY, true)

            apiKey.value = sharedPreferences.getString(API_KEY_KEY, "cb71a12703264742b5b8") ?: ""
            terminalSn.value = sharedPreferences.getString(TERMINAL_SN_KEY, "21222213701A-001") ?: ""
            branchId.value = sharedPreferences.getString(BRANCH_ID_KEY, "28") ?: ""
            delayMillis.value = sharedPreferences.getString(DELAY_MILLIS_KEY, "10_000") ?: ""

            serverOption.value = sharedPreferences.getString(SERVER_OPTION, BASE_URL_DEV) ?: BASE_URL_DEV
            customServer.value = sharedPreferences.getString(CUSTOM_SERVER, "") ?: ""
        }
    }

    fun saveLockerName() {
        with(sharedPreferences.edit()) {
            putString(LOCKER_NAME_KEY, lockerName.value)
            apply()
        }
    }
    fun saveRebootTime() {

        with(sharedPreferences.edit()) {
            putString(REBOOT_TIME_KEY, rebootTime.value)
            apply()
        }
    }

    fun saveServerOption() {
        with(sharedPreferences.edit()) {
            putString(SERVER_OPTION, serverOption.value)
            putString(CUSTOM_SERVER, customServer.value)
            apply()
        }
    }

    fun saveIsRebootEnabled() {
        with(sharedPreferences.edit()) {
            putBoolean(IS_REBOOT_ENABLED_KEY, isRebootEnabled.value)
            apply()
        }
    }

    fun saveApiKey() {
        with(sharedPreferences.edit()) {
            putString(API_KEY_KEY, apiKey.value)
            apply()
        }
    }

    fun saveTerminalSn() {
        with(sharedPreferences.edit()) {
            putString(TERMINAL_SN_KEY, terminalSn.value)
            apply()
        }
    }

    fun saveBranchId() {
        with(sharedPreferences.edit()) {
            putString(BRANCH_ID_KEY, branchId.value)
            apply()
        }
    }

    fun saveDelayMillis() {
        with(sharedPreferences.edit()) {
            putString(DELAY_MILLIS_KEY, delayMillis.value)
            apply()
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
           boxDao.deleteAllBoxes()
            transactionDao.deleteAllTransactions()
        }
    }
}