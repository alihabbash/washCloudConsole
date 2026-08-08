package com.washcloud.consoleapplication.hardware


import android.content.Context
import android.hardware.usb.UsbDevice
import com.washcloud.consoleapplication.utils.FileLogger
import it.custom.printer.api.android.CustomAndroidAPI
import it.custom.printer.api.android.CustomException
import it.custom.printer.api.android.CustomPrinter
import it.custom.printer.api.android.PrinterFont

class PrintQR(context: Context?) {
    private val lock = "lockAccess"
    private lateinit var fntPrinterNormal: PrinterFont
    private var context: Context? = null

    init {
        try {
            this.context = context
            fntPrinterNormal = PrinterFont()
            //Fill class: NORMAL
            fntPrinterNormal.setCharHeight(PrinterFont.FONT_SIZE_X2) //Height x2
            fntPrinterNormal.setCharWidth(PrinterFont.FONT_SIZE_X2) //Width x2
            fntPrinterNormal.setEmphasized(true) //Bold
            fntPrinterNormal.setItalic(false) //No Italic
            fntPrinterNormal.setUnderline(false) //No Underline
            fntPrinterNormal.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER) //Center
            fntPrinterNormal.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT) //Default International Chars
            usbDeviceList = CustomAndroidAPI.EnumUsbDevices(context)
            if (!usbDeviceList.isNullOrEmpty()) {
                prnDevice = CustomAndroidAPI().getPrinterDriverUSB(usbDeviceList!![0], context)
            } else {
                context?.let { FileLogger.log(it, "Init PrintQR", "No USB printer devices found.") }
            }
        } catch (e: CustomException) {
            context?.let { FileLogger.log(it, "Init PrintQR", e.message.toString()) };

        }
    }

    fun openDevice(): Boolean {
        if (prnDevice == null) {
            try {
                //Open and connect it
                context?.let { FileLogger.log(it, "PrintQR", "Device was opened successfully") }
                context?.let { FileLogger.log(it, "PrintQR", "${usbDeviceList?.size}") }
                if (!usbDeviceList.isNullOrEmpty()) {
                    prnDevice = CustomAndroidAPI().getPrinterDriverUSB(usbDeviceList!![0], context)
                    return true
                } else {
                    context?.let {
                        FileLogger.log(
                            it,
                            "PrintQR",
                            "Cannot open device: No USB printer devices found."
                        )
                    }
                    return false
                }
            } catch (e: CustomException) {
                context?.let {
                    FileLogger.log(
                        it,
                        "CustomException-PrintQR",
                        e.message.toString() + e.stackTrace.toString()
                    )
                };
                return false
            } catch (e: Exception) {

                context?.let { FileLogger.log(it, "Exception-PrintQR", e.message.toString()) };
                return false
            }
        }
        return true
    }

    fun printOrderQr(serialNumber: String?, consoleSN: String?) {
        if (!openDevice()) return
        synchronized(lock) {
            try {
                //Print Text (NORMAL)
                //  prnDevice?.present(40)
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
                /*
                 prnDevice?.printBarcode(
                    serialNumber ?: "",
                    CustomPrinter.BARCODE_TYPE_CODE128,
                    CustomPrinter.BARCODE_HRI_NONE, // brcHriType: Hide the text below the barcode
                    CustomPrinter.BARCODE_ALIGN_TO_CENTER, // brcJustification: Center the barcode
                    2, // brcWidth: Width multiplier (2 is standard)
                    120 // brcHeight: Height in dots
                )
                 */
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
