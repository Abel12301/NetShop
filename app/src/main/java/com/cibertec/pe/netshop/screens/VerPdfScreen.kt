package com.cibertec.pe.netshop.screens

import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.utils.PdfGenerator
import com.cibertec.pe.netshop.viewmodel.VentaViewModel
import kotlinx.coroutines.runBlocking
import java.io.File

@Composable
fun VerPdfScreen(navController: NavHostController, ventaId: Int) {
    val context = LocalContext.current
    val ventaViewModel = VentaViewModel(context.applicationContext as android.app.Application)

    LaunchedEffect(ventaId) {
        val venta = runBlocking { ventaViewModel.obtenerVentaPorIdSuspend(ventaId) }
        val detalles = runBlocking { ventaViewModel.obtenerDetallesParaPdf(ventaId) } // ✅ CORREGIDO

        if (venta != null && detalles.isNotEmpty()) {
            val file: File = PdfGenerator.generateTicket(context, venta, detalles)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val mime = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(file.extension) ?: "application/pdf"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // ✅ NECESARIO para no crashear
            }

            context.startActivity(intent)
            navController.popBackStack()
        }
    }
}
