package com.cibertec.pe.netshop.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

fun generarQRyGuardar(context: Context, contenido: String, nombreArchivo: String) {
    val writer = QRCodeWriter()
    try {
        val bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }

        // Guardar imagen en cache
        val archivo = File(context.cacheDir, "$nombreArchivo-qr.png")
        val output = FileOutputStream(archivo)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}