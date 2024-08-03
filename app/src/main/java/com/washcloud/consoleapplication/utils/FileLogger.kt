package com.washcloud.consoleapplication.utils

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {

    private const val LOG_TAG = "FileLogger"
    private const val LOG_FILE_NAME = "app_log.txt"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun log(context: Context, tag: String, message: String) {
        val logMessage = "${dateFormat.format(Date())} $tag: $message\n"
        writeLogToFile(context, logMessage)
    }

    private fun writeLogToFile(context: Context, logMessage: String) {
        val logFile = getLogFile(context)
        try {
            FileWriter(logFile, true).use { writer ->
                writer.append(logMessage)
                writer.flush()
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Error writing log to file", e)
        }
    }


    fun clearLogFile(context: Context) {
        val logFile = getLogFile(context)
        if (logFile.exists()) {
            logFile.delete()
        }
    }

    private fun getLogFile(context: Context): File {
        return if (isExternalStorageWritable() && isPermissionGranted(context)) {
            File(context.getExternalFilesDir(null), LOG_FILE_NAME)
        } else {
            File(context.filesDir, LOG_FILE_NAME)
        }
    }

    private fun isPermissionGranted(context: Context): Boolean {
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}
