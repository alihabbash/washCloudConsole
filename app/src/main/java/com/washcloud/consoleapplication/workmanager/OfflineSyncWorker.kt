package com.washcloud.consoleapplication.workmanager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import com.washcloud.consoleapplication.ui.mainad.ApiService
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.washcloud.consoleapplication.ui.mainad.RetrofitClient

@HiltWorker
class OfflineSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val transactionDao: TransactionDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val pendingTransactions = transactionDao.getAllTransactions()
            if (pendingTransactions.isEmpty()) {
                Log.d("OfflineSyncWorker", "No pending offline transactions.")
                return@withContext Result.success()
            }

            FileLogger.log(context, "OfflineSyncWorker", "Found ${pendingTransactions.size} pending transactions to sync.")
            val apiKey = PrefsManager.getApiKey(context)
            val terminalSn = PrefsManager.getTerminalSN(context)
            val apiService = RetrofitClient.getApiService(context)

            var allSuccess = true

            for (transaction in pendingTransactions) {
                // Determine box type based on logic. Default to 1 (Box), conveyor is usually 2.
                val typeInt = if (transaction.boxId == 0L) 1 else 1 // Adjust if conveyor logic applies

                val response = if (transaction.trnasType == TransactionType.DROP_OFF) {
                    apiService.customerDropOff(
                        apiKey = apiKey,
                        wayBillNo = transaction.orderSerial,
                        terminalSn = terminalSn,
                        doorNo = transaction.boxId.toString().padStart(2, '0'),
                        type = typeInt 
                    )
                } else {
                    apiService.customerPickup(
                        apiKey = apiKey,
                        wayBillNo = transaction.orderSerial,
                        terminalSn = terminalSn,
                        doorNo = transaction.boxId.toString().padStart(2, '0'),
                        type = typeInt 
                    )
                }

                if (response.isSuccessful) {
                    transactionDao.deleteTransaction(transaction.id)
                    FileLogger.log(context, "OfflineSyncWorker", "Successfully synced transaction (ID: ${transaction.id}, Serial: ${transaction.orderSerial}, Type: ${transaction.trnasType})")
                } else {
                    FileLogger.log(context, "OfflineSyncWorker", "Failed to sync transaction (ID: ${transaction.id}, Serial: ${transaction.orderSerial}, Type: ${transaction.trnasType}): ${response.errorBody()?.string()}")
                    allSuccess = false
                }
            }

            if (allSuccess) {
                Result.success()
            } else {
                Result.retry()
            }

        } catch (e: Exception) {
            FileLogger.log(context, "OfflineSyncWorker", "Sync failed with exception: ${e.message}")
            Log.e("OfflineSyncWorker", "Exception during sync", e)
            Result.retry()
        }
    }
}
