package com.cibertec.pe.netshop.screens

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.viewmodel.VentaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfirmacion(navController: NavHostController, ventaId: Int?) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current.applicationContext as Application
    val ventaViewModel = remember { VentaViewModel(context) }
    val scope = rememberCoroutineScope()
    var pdfUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(ventaId) {
        ventaId?.let { id ->
            ventaViewModel.generarPdfVenta(context, id) { file ->
                file?.let {
                    pdfUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        it
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Venta Exitosa", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary
                )
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = colorScheme.tertiary,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¡Venta registrada con éxito!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Gracias por su compra.",
                fontSize = 16.sp,
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(30.dp))

            BotonAccion(
                texto = "Ver comprobante en PDF",
                icono = Icons.Default.PictureAsPdf,
                color = colorScheme.secondary,
                habilitado = pdfUri != null
            ) {
                pdfUri?.let { uri ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BotonAccion(
                texto = "Enviar por WhatsApp",
                icono = Icons.Default.Share,
                color = Color(0xFF25D366),
                habilitado = pdfUri != null
            ) {
                pdfUri?.let { uri ->
                    val pm = context.packageManager
                    val whatsappInstalled = try {
                        pm.getPackageInfo("com.whatsapp", 0)
                        true
                    } catch (_: Exception) {
                        false
                    }

                    if (whatsappInstalled) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, "Gracias por tu compra. Adjunto tu comprobante.")
                            setPackage("com.whatsapp")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(shareIntent)
                    } else {
                        Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BotonAccion(
                texto = "Compartir por otras apps",
                icono = Icons.Default.Share,
                color = colorScheme.tertiaryContainer,
                habilitado = pdfUri != null
            ) {
                pdfUri?.let { uri ->
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, "Comprobante de venta adjunto.")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(
                        Intent.createChooser(shareIntent, "Compartir comprobante PDF")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Volver al inicio", color = colorScheme.primary)
            }
        }
    }
}

@Composable
fun BotonAccion(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    habilitado: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(55.dp)
    ) {
        Icon(icono, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto, color = Color.White, fontSize = 16.sp)
    }
}
