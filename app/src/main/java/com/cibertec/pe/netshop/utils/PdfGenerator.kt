package com.cibertec.pe.netshop.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.cibertec.pe.netshop.data.entity.DetalleParaPdf
import com.cibertec.pe.netshop.data.entity.Venta
import com.cibertec.pe.netshop.data.entity.DetalleVenta
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun generateTicket(context: Context, venta: Venta, detalles: List<DetalleParaPdf>): File {
        val document = PdfDocument()
        val paint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = 60f

        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val title = "COMPROBANTE DE VENTA"
        val titleWidth = paint.measureText(title)
        canvas.drawText(title, (pageInfo.pageWidth - titleWidth) / 2, y, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        y += 40f
        canvas.drawText("ID Venta: ${venta.id}", 50f, y, paint)
        y += 20f
        canvas.drawText("Fecha: ${venta.fecha}  Hora: ${venta.hora}", 50f, y, paint)
        y += 20f
        canvas.drawText("Cliente ID: ${venta.clienteId}", 50f, y, paint)
        y += 20f
        canvas.drawText("Empleado ID: ${venta.empleadoId}", 50f, y, paint)
        y += 20f
        canvas.drawText("Método de Pago: ${venta.metodoPago}", 50f, y, paint)

        y += 30f
        canvas.drawLine(50f, y, pageInfo.pageWidth - 50f, y, paint)
        y += 20f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Cant", 50f, y, paint)
        canvas.drawText("Producto", 110f, y, paint)
        canvas.drawText("Precio", 350f, y, paint)
        canvas.drawText("Subtotal", 450f, y, paint)

        paint.typeface = Typeface.DEFAULT
        detalles.forEach {
            y += 20f
            canvas.drawText("${it.cantidad}", 50f, y, paint)
            canvas.drawText("${it.productoNombre}", 110f, y, paint)
            canvas.drawText("S/.${"%.2f".format(it.precioUnitario)}", 350f, y, paint)
            canvas.drawText("S/.${"%.2f".format(it.subtotal)}", 450f, y, paint)
        }

        y += 30f
        canvas.drawLine(50f, y, pageInfo.pageWidth - 50f, y, paint)
        y += 30f
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL: S/.${"%.2f".format(venta.total)}", 400f, y, paint)

        document.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "tickets")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "ticket_${venta.id}.pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return file
    }}