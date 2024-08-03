package com.washcloud.consoleapplication.hardware


import android.content.Context
import android.hardware.usb.UsbDevice
import it.custom.printer.api.android.CustomAndroidAPI
import it.custom.printer.api.android.CustomException
import it.custom.printer.api.android.CustomPrinter
import it.custom.printer.api.android.PrinterFont

class PrintQR(context: Context?) {
    private val lock = "lockAccess"
    private val fntPrinterNormal: PrinterFont? = null
    private var context: Context? = null

    init {
        try {
            this.context = context
            val fntPrinterNormal: PrinterFont = PrinterFont()
            //Fill class: NORMAL
            fntPrinterNormal.setCharHeight(PrinterFont.FONT_SIZE_X2) //Height x2
            fntPrinterNormal.setCharWidth(PrinterFont.FONT_SIZE_X2) //Width x2
            fntPrinterNormal.setEmphasized(true) //Bold
            fntPrinterNormal.setItalic(false) //No Italic
            fntPrinterNormal.setUnderline(false) //No Underline
            fntPrinterNormal.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER) //Center
            fntPrinterNormal.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT) //Default International Chars
            usbDeviceList = CustomAndroidAPI.EnumUsbDevices(context)
            prnDevice = CustomAndroidAPI().getPrinterDriverUSB(usbDeviceList!![0], context)
        } catch (e: CustomException) {
        }
    }

    fun OpenDevice(): Boolean {
        if (prnDevice == null) {
            try {
                //Open and connect it
                prnDevice = CustomAndroidAPI().getPrinterDriverUSB(usbDeviceList!![0], context)
                return true
            } catch (e: CustomException) {
                return false
            } catch (e: Exception) {
                return false
            }
        }
        return true
    }

    fun PrintOrderQr(serialNumber: String?, consoleSN: String?) {
        if (!OpenDevice()) return
        synchronized(lock) {
            try {
                //Print Text (NORMAL)
                prnDevice?.present(40)
                prnDevice?.printText("SN:", fntPrinterNormal)
                prnDevice?.printTextLF(consoleSN, fntPrinterNormal)
                prnDevice?.feed(3)
                prnDevice?.printImage(
                    BarcodeUtil.generateBarcode(
                        serialNumber,
                        400,
                        170
                    ), CustomPrinter.IMAGE_ALIGN_TO_LEFT, CustomPrinter.IMAGE_SCALE_TO_FIT, 0
                )
                prnDevice?.feed(3)
                prnDevice?.printText("Order SO:", fntPrinterNormal)
                prnDevice?.printTextLF(serialNumber, fntPrinterNormal)
                //                prnDevice.feed(3);
                prnDevice?.cut(CustomPrinter.CUT_TOTAL)
            } catch (e: CustomException) {
            } catch (e: Exception) {
            }
        }
    }

    fun destroy() {
        if (prnDevice != null) {
            try {
                prnDevice?.close()
                prnDevice = null
            } catch (e: CustomException) {
            }
        }
    }

    companion object {
        var usbDeviceList: Array<UsbDevice>? = null
        var prnDevice: CustomPrinter? = null
    }
}
