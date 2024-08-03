package com.washcloud.consoleapplication.hardware


import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.washcloud.consoleapplication.utils.FileLogger
import it.custom.printer.api.android.CustomAndroidAPI
import it.custom.printer.api.android.CustomException

import it.custom.printer.api.android.CustomPrinter
import it.custom.printer.api.android.PrinterFont

class CustomPrinterHelper(private val context: Context) {

    private var prnDevice: CustomPrinter? = null
    private val lock = Any()
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var usbDeviceList: Array<UsbDevice>? = null

    init {
        try {
            FileLogger.log(context, "CustomPrinterHelper", "Getting USB devices")
            usbDeviceList = CustomAndroidAPI.EnumUsbDevices(context)
        } catch (e: CustomException) {
            FileLogger.log(context, "CustomPrinterHelper", "Error getting USB devices: $e")
            e.printStackTrace()
        }
    }

    fun openDevice(deviceSelected: Int): Boolean {
        if (deviceSelected == -1) {
            return false
        }

        if (prnDevice == null) {
            try {
                FileLogger.log(context, "CustomPrinterHelper", "Opening device: $deviceSelected")
                prnDevice = CustomAndroidAPI().getPrinterDriverUSB(usbDeviceList!![deviceSelected], context)
                return true
            } catch (e: CustomException) {
                FileLogger.log(context, "CustomPrinterHelper", "Error opening device: $e")
                e.printStackTrace()
                return false
            }
        }
        return true
    }

    fun printImage(image: Bitmap) {
        synchronized(lock) {
            try {
                FileLogger.log(context, "CustomPrinterHelper", "Printing image")
                prnDevice?.printImage(image, CustomPrinter.IMAGE_ALIGN_TO_LEFT, CustomPrinter.IMAGE_SCALE_TO_FIT, 0)
                prnDevice?.feed(3)
                prnDevice?.cut(CustomPrinter.CUT_TOTAL)
                prnDevice?.present(40)
            } catch (e: CustomException) {
                FileLogger.log(context, "CustomPrinterHelper", "Error printing image: $e")
                e.printStackTrace()
            }
        }
    }

    fun printText(text: String) {
        val fntPrinterBold2X = PrinterFont()
        try {
            FileLogger.log(context, "CustomPrinterHelper", "Printing text")
            fntPrinterBold2X.setCharHeight(PrinterFont.FONT_SIZE_X2)
            fntPrinterBold2X.setCharWidth(PrinterFont.FONT_SIZE_X2)
            fntPrinterBold2X.setEmphasized(true)
            fntPrinterBold2X.setItalic(false)
            fntPrinterBold2X.setUnderline(false)
            fntPrinterBold2X.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER)
            fntPrinterBold2X.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT)
        } catch (e: CustomException) {
            FileLogger.log(context, "CustomPrinterHelper", "Error setting font: $e")
            e.printStackTrace()
        }

        synchronized(lock) {
            try {
                FileLogger.log(context, "CustomPrinterHelper", "Printing text")
                prnDevice?.printTextLF("SN:", fntPrinterBold2X)
                prnDevice?.printText(text, fntPrinterBold2X)
                prnDevice?.cut(CustomPrinter.CUT_TOTAL)
            } catch (e: CustomException) {
                FileLogger.log(context, "CustomPrinterHelper", "Error printing text: $e")
                e.printStackTrace()
            }
        }
    }

    fun closeDevice() {
        try {
            FileLogger.log(context, "CustomPrinterHelper", "Closing device")
            prnDevice?.close()
        } catch (e: CustomException) {
            FileLogger.log(context, "CustomPrinterHelper", "Error closing device: $e")
            e.printStackTrace()
        }
    }
}