package com.washcloud.consoleapplication.utils

import android.content.Context

import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory
import org.acra.config.CoreConfiguration
import com.google.auto.service.AutoService


class FileReportSender(private val context: Context) : ReportSender {
    override fun send(context: Context, errorContent: CrashReportData) {
        try {
            val reportText = errorContent.toJSON()
            FileLogger.log(context, "CRASH_REPORT_JSON", reportText)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


@AutoService(ReportSenderFactory::class)
class FileReportSenderFactory : ReportSenderFactory {

    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        return FileReportSender(context)
    }

    override fun enabled(config: CoreConfiguration): Boolean {
        return true
    }
}
