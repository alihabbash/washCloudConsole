package com.washcloud.consoleapplication.hardware

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

object BarcodeUtil {
    fun generateBarcode(data: String?, width: Int, height: Int): Bitmap? {
        val barcodeEncoder: BarcodeEncoder = BarcodeEncoder()
        var bitmap: Bitmap? = null
        try {
            val bitMatrix: BitMatrix =
                barcodeEncoder.encode(data, BarcodeFormat.CODE_128, width, height)
            bitmap = barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return bitmap
    }
}