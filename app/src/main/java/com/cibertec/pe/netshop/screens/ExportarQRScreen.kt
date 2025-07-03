package com.cibertec.pe.netshop.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cibertec.pe.netshop.data.entity.Producto
import com.cibertec.pe.netshop.viewmodel.ProductoViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.*

@Composable
fun ExportarQRScreen(
    onBack: () -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val productos by viewModel.productos.collectAsState()

    val productosConClave = productos.filter { it.clave.isNotBlank() }
    val productosSinClave = productos.filter { it.clave.isBlank() }

    var seleccionadosConClave by remember { mutableStateOf(productosConClave.associateWith { false }.toMutableMap()) }
    var seleccionadosSinClave by remember { mutableStateOf(productosSinClave.associateWith { false }.toMutableMap()) }

    var allSelectedConClave by remember { mutableStateOf(false) }
    var allSelectedSinClave by remember { mutableStateOf(false) }

    // Guarda referencia al PDF generado
    var pdfFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(productos) {
        seleccionadosConClave = productosConClave.associateWith { seleccionadosConClave[it] ?: false }.toMutableMap()
        seleccionadosSinClave = productosSinClave.associateWith { seleccionadosSinClave[it] ?: false }.toMutableMap()

        allSelectedConClave = seleccionadosConClave.values.all { it } && seleccionadosConClave.isNotEmpty()
        allSelectedSinClave = seleccionadosSinClave.values.all { it } && seleccionadosSinClave.isNotEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Seleccionar productos para exportar QR",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Sección Con Clave
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Checkbox(
                        checked = allSelectedConClave,
                        onCheckedChange = { checked ->
                            allSelectedConClave = checked
                            seleccionadosConClave = seleccionadosConClave.mapValues { checked }.toMutableMap()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "✅ Productos con clave",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Divider()
                Spacer(Modifier.height(8.dp))

                val listHeight = 200.dp
                if (productosConClave.isEmpty()) {
                    Text(
                        "No hay productos con clave",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(listHeight)
                    ) {
                        items(productosConClave) { producto ->
                            val selected = seleccionadosConClave[producto] == true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newValue = !selected
                                        seleccionadosConClave = seleccionadosConClave.toMutableMap().apply {
                                            this[producto] = newValue
                                        }
                                        allSelectedConClave = seleccionadosConClave.values.all { it }
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (selected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    producto.nombre.take(30),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Divider()
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Sección Sin Clave
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Checkbox(
                        checked = allSelectedSinClave,
                        onCheckedChange = { checked ->
                            allSelectedSinClave = checked
                            seleccionadosSinClave = seleccionadosSinClave.mapValues { checked }.toMutableMap()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "❌ Productos sin clave",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Divider()
                Spacer(Modifier.height(8.dp))

                val listHeight = 200.dp
                if (productosSinClave.isEmpty()) {
                    Text(
                        "No hay productos sin clave",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(listHeight)
                    ) {
                        items(productosSinClave) { producto ->
                            val selected = seleccionadosSinClave[producto] == true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newValue = !selected
                                        seleccionadosSinClave = seleccionadosSinClave.toMutableMap().apply {
                                            this[producto] = newValue
                                        }
                                        allSelectedSinClave = seleccionadosSinClave.values.all { it }
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (selected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    producto.nombre.take(30),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Divider()
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val productosSeleccionados = (seleccionadosConClave + seleccionadosSinClave)
                        .filterValues { it }
                        .keys
                        .toList()

                    if (productosSeleccionados.isEmpty()) {
                        Toast.makeText(context, "Selecciona al menos un producto", Toast.LENGTH_SHORT).show()
                    } else {
                        val file = generarPDFconQRs(context, productosSeleccionados)
                        if (file != null) {
                            pdfFile = file
                            // Abrir PDF directamente
                            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(openIntent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No hay app para abrir PDF instalada", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Error generando PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generar QR", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
            }

            Button(
                onClick = {
                    if (pdfFile != null && pdfFile!!.exists()) {
                        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile!!)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
                    } else {
                        Toast.makeText(context, "Primero genera el PDF", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Compartir PDF", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

fun generarPDFconQRs(context: Context, productos: List<Producto>): File? {
    val pdf = PdfDocument()
    val paint = Paint().apply {
        textSize = 10f
        isAntiAlias = true
    }

    val qrSize = 100
    val margin = 30
    val spacing = 20
    val columns = 4
    val pageWidth = 595
    val pageHeight = 842

    var pageNum = 1
    var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
    var canvas = page.canvas

    var x = margin
    var y = margin
    var col = 0

    productos.forEach { producto ->
        val clave = if (producto.clave.isBlank()) UUID.randomUUID().toString().take(8) else producto.clave
        val contenidoQR = clave
        val bitmap = generarQR(contenidoQR)?.asAndroidBitmap()

        if (bitmap != null) {
            val rect = android.graphics.Rect(x, y, x + qrSize, y + qrSize)
            canvas.drawBitmap(bitmap, null, rect, null)

            val nombre = producto.nombre.take(20)
            val nombreX = x + (qrSize - paint.measureText(nombre)) / 2
            val claveX = x + (qrSize - paint.measureText(clave)) / 2

            canvas.drawText(nombre, nombreX, (y + qrSize + 14).toFloat(), paint)
            canvas.drawText(clave, claveX, (y + qrSize + 28).toFloat(), paint)

            col++
            if (col == columns) {
                col = 0
                x = margin
                y += qrSize + 50
            } else {
                x += qrSize + spacing
            }

            if (y + qrSize + 50 > pageHeight - margin) {
                pdf.finishPage(page)
                pageNum++
                page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
                canvas = page.canvas
                x = margin
                y = margin
                col = 0
            }
        }
    }

    pdf.finishPage(page)

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "QR_Productos.pdf")
    return try {
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        pdf.close()
        null
    }
}
