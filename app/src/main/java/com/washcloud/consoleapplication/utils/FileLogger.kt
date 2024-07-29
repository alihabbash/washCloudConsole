package com.washcloud.consoleapplication.utils


import android.content.Context
import android.util.Log
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
        val logFile = File(context.getExternalFilesDir(null), LOG_FILE_NAME)
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
        val logFile = File(context.getExternalFilesDir(null), LOG_FILE_NAME)
        if (logFile.exists()) {
            logFile.delete()
        }
    }
}
