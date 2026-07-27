package com.washcloud.consoleapplication.workmanager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.washcloud.consoleapplication.local.database.dao.CustomerUserDao
import com.washcloud.consoleapplication.local.database.dto.CustomerUserDto
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import com.washcloud.consoleapplication.ui.mainad.RetrofitClient
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class CustomerSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val customerUserDao: CustomerUserDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!PrefsManager.isStaticQrOfflineEnabled(context)) {
                FileLogger.log(context, "CustomerSyncWorker", "Static QR Offline is disabled. Skipping sync.")
                return@withContext Result.success()
            }

            val apiKey = PrefsManager.getApiKey(context)
            val terminalSn = PrefsManager.getTerminalSN(context)
            
            val apiService = RetrofitClient.getApiService(context)

            FileLogger.log(context, "CustomerSyncWorker", "Starting customer data sync...")

            // Fetch local customers to sync to the server
            val localCustomers = customerUserDao.getAllCustomerUsers()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

            val syncDataList = localCustomers.map { dto ->
                // Ensure existing data stored as milliseconds is converted to ISO format
                val formattedLastUpdate = dto.consoleLastUpdate.toLongOrNull()?.let { timeInMillis ->
                    sdf.format(java.util.Date(timeInMillis))
                } ?: dto.consoleLastUpdate

                com.washcloud.consoleapplication.ui.mainad.CustomerData(
                    customerId = dto.userId,
                    customerName = dto.customerName,
                    phoneNumber = dto.phoneNumber,
                    password = dto.consolePassword,
                    lastUpdate = formattedLastUpdate
                )
            }

            val request = com.washcloud.consoleapplication.ui.mainad.CustomerSyncRequest(
                apiKey = apiKey,
                terminalSn = terminalSn,
                customers = syncDataList
            )

            val response = apiService.syncCustomerData(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status?.lowercase() == "success") {
                    val customers = body.customers
                    if (customers != null) {
                        FileLogger.log(context, "CustomerSyncWorker", "Received ${customers.size} customers from API.")
                        
                        // Insert or Update the database
                        for (data in customers) {
                            val existing = customerUserDao.getCustomerUser(data.customerId)
                            if (existing == null) {
                                val newDto = CustomerUserDto(
                                    userId = data.customerId,
                                    customerName = data.customerName,
                                    phoneNumber = data.phoneNumber,
                                    consolePassword = data.password,
                                    consoleLastUpdate = data.lastUpdate ?: ""
                                )
                                customerUserDao.insertCustomerUser(newDto)
                            } else {
                                // The backend is the source of truth after sync.
                                // We MUST overwrite with exactly what the backend sends, 
                                // even if it's null (e.g., backend resetting a password).
                                val updatedDto = existing.copy(
                                    customerName = data.customerName,
                                    phoneNumber = data.phoneNumber,
                                    consolePassword = data.password,
                                    consoleLastUpdate = data.lastUpdate ?: ""
                                )
                                customerUserDao.insertCustomerUser(updatedDto)
                            }
                        }
                        FileLogger.log(context, "CustomerSyncWorker", "Successfully synced customers to local DB.")
                        Result.success()
                    } else {
                        FileLogger.log(context, "CustomerSyncWorker", "Sync successful but no data returned.")
                        Result.success()
                    }
                } else {
                    FileLogger.log(context, "CustomerSyncWorker", "Sync failed: ${body?.message}")
                    Result.retry()
                }
            } else {
                FileLogger.log(context, "CustomerSyncWorker", "API Call failed with code ${response.code()}: ${response.errorBody()?.string()}")
                Result.retry()
            }
        } catch (e: Exception) {
            FileLogger.log(context, "CustomerSyncWorker", "Sync failed with exception: ${e.message}")
            Log.e("CustomerSyncWorker", "Exception during sync", e)
            Result.retry()
        }
    }
}
