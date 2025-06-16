package com.cibertec.pe.netshop.screens

import android.app.Activity
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.data.entity.Proveedor
import com.cibertec.pe.netshop.viewmodel.ProveedorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedorScreen(
    navController: NavHostController,
    viewModel: ProveedorViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    var mostrarDialogo by remember { mutableStateOf(false) }
    var proveedorEditando by remember { mutableStateOf<Proveedor?>(null) }
    val proveedores by viewModel.proveedores.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    proveedorEditando = null
                    mostrarDialogo = true
                },
                containerColor = colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = colorScheme.onPrimary)
            }
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                "Proveedores",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )

            proveedores.forEach { proveedor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            proveedorEditando = proveedor
                            mostrarDialogo = true
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🧾 ${proveedor.nombre}", fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                        Text("Empresa: ${proveedor.empresa}", color = colorScheme.onSurfaceVariant)
                        Text("📞 ${proveedor.telefono} - ✉️ ${proveedor.correo}", color = colorScheme.onSurfaceVariant)
                        Text("📍 ${proveedor.direccion}", color = colorScheme.onSurfaceVariant)
                        if (proveedor.infoAdicional.isNotEmpty()) {
                            Text("ℹ️ ${proveedor.infoAdicional}", fontSize = 12.sp, color = colorScheme.outline)
                        }
                    }
                }
            }
        }

        if (mostrarDialogo) {
            DialogAgregarEditarProveedor(
                proveedor = proveedorEditando,
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, empresa, telefono, correo, direccion, info ->
                    if (proveedorEditando == null) {
                        viewModel.insertar(
                            Proveedor(
                                nombre = nombre,
                                empresa = empresa,
                                telefono = telefono,
                                correo = correo,
                                direccion = direccion,
                                infoAdicional = info
                            )
                        )
                    } else {
                        viewModel.actualizar(
                            proveedorEditando!!.copy(
                                nombre = nombre,
                                empresa = empresa,
                                telefono = telefono,
                                correo = correo,
                                direccion = direccion,
                                infoAdicional = info
                            )
                        )
                    }
                    mostrarDialogo = false
                },
                onEliminar = {
                    proveedorEditando?.let { viewModel.eliminar(it) }
                    mostrarDialogo = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarEditarProveedor(
    proveedor: Proveedor?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String) -> Unit,
    onEliminar: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var nombre by remember { mutableStateOf(proveedor?.nombre ?: "") }
    var empresa by remember { mutableStateOf(proveedor?.empresa ?: "") }
    var telefono by remember { mutableStateOf(proveedor?.telefono ?: "") }
    var correo by remember { mutableStateOf(proveedor?.correo ?: "") }
    var direccion by remember { mutableStateOf(proveedor?.direccion ?: "") }
    var infoAdicional by remember { mutableStateOf(proveedor?.infoAdicional ?: "") }

    var errorNombre by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    var errorTelefono by remember { mutableStateOf(false) }

    fun esCorreoValido(email: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun esTelefonoValido(phone: String): Boolean {
        val limpio = phone.replace(" ", "").replace("-", "")
        return limpio.matches(Regex("^\\+?[0-9]{9,15}$"))
    }

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri ->
            uri?.let {
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameFromContact = it.getString(nameIndex)
                        val contactId = it.getString(idIndex)

                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            null
                        )
                        var telefonoDesdeContacto = ""
                        phoneCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                val phoneIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                telefonoDesdeContacto = pc.getString(phoneIndex)
                            }
                        }

                        val emailCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            null
                        )
                        var correoDesdeContacto = ""
                        emailCursor?.use { ec ->
                            if (ec.moveToFirst()) {
                                val emailIndex = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                                correoDesdeContacto = ec.getString(emailIndex)
                            }
                        }

                        nombre = nameFromContact
                        telefono = telefonoDesdeContacto
                        correo = correoDesdeContacto
                    }
                }
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (proveedor == null) "Agregar Proveedor" else "Editar Proveedor",
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                IconButton(onClick = { launcher.launch(null) }) {
                    Icon(Icons.Default.Contacts, contentDescription = "Abrir contactos", tint = colorScheme.primary)
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 420.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            if (it.length <= 50) nombre = it
                            if (errorNombre && it.isNotBlank()) errorNombre = false
                        },
                        label = { Text("Nombre") },
                        isError = errorNombre,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        supportingText = {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(
                                    if (errorNombre) "El nombre es obligatorio" else "Obligatorio",
                                    color = if (errorNombre) colorScheme.error else colorScheme.outline,
                                    fontSize = 12.sp
                                )
                                Text("${nombre.length}/50", fontSize = 12.sp, color = colorScheme.outline)
                            }
                        }
                    )

                    // Empresa
                    OutlinedTextField(
                        value = empresa,
                        onValueChange = { if (it.length <= 50) empresa = it },
                        label = { Text("Empresa") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        supportingText = {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Opcional", fontSize = 12.sp, color = colorScheme.outline)
                                Text("${empresa.length}/50", fontSize = 12.sp, color = colorScheme.outline)
                            }
                        }
                    )

                    // Teléfono
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = {
                            if (it.length <= 20) telefono = it
                            if (errorTelefono && esTelefonoValido(it)) errorTelefono = false
                        },
                        label = { Text("Teléfono") },
                        isError = errorTelefono,
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        supportingText = {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(
                                    if (errorTelefono) "Ej: +51 987 987 098" else "Opcional",
                                    color = if (errorTelefono) colorScheme.error else colorScheme.outline,
                                    fontSize = 12.sp
                                )
                                Text("${telefono.length}/20", fontSize = 12.sp, color = colorScheme.outline)
                            }
                        }
                    )

                    // Correo
                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            val nuevoCorreo = it.trim()
                            if (nuevoCorreo.length <= 50) correo = nuevoCorreo
                            if (errorCorreo && esCorreoValido(nuevoCorreo)) errorCorreo = false
                        },
                        label = { Text("Correo") },
                        isError = errorCorreo,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        supportingText = {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(
                                    if (errorCorreo) "Correo inválido" else "Opcional",
                                    color = if (errorCorreo) colorScheme.error else colorScheme.outline,
                                    fontSize = 12.sp
                                )
                                Text("${correo.length}/50", fontSize = 12.sp, color = colorScheme.outline)
                            }
                        }
                    )

                    // Dirección
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { if (it.length <= 100) direccion = it },
                        label = { Text("Dirección") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        supportingText = {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Opcional", fontSize = 12.sp, color = colorScheme.outline)
                                Text("${direccion.length}/100", fontSize = 12.sp, color = colorScheme.outline)
                            }
                        }
                    )

                    // Información adicional
                    OutlinedTextField(
                        value = infoAdicional,
                        onValueChange = { if (it.length <= 200) infoAdicional = it },
                        label = { Text("Información adicional") },
                        maxLines = 3,
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        supportingText = {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Opcional", fontSize = 12.sp, color = colorScheme.outline)
                                Text("${infoAdicional.length}/200", fontSize = 12.sp, color = colorScheme.outline)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            IconButton(onClick = {
                var esValido = true
                if (nombre.isBlank()) {
                    errorNombre = true
                    esValido = false
                }
                if (correo.isNotBlank() && !esCorreoValido(correo)) {
                    errorCorreo = true
                    esValido = false
                }
                if (telefono.isNotBlank() && !esTelefonoValido(telefono)) {
                    errorTelefono = true
                    esValido = false
                }
                if (esValido) {
                    onConfirm(nombre, empresa, telefono, correo, direccion, infoAdicional)
                }
            }) {
                Icon(Icons.Default.Save, contentDescription = "Guardar", tint = colorScheme.primary)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (proveedor != null) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = colorScheme.error)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = colorScheme.outline)
                }
            }
        },
        containerColor = colorScheme.surface
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogEditarProveedor(
    proveedor: Proveedor,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String) -> Unit,
    onEliminar: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var nombre by remember { mutableStateOf(proveedor.nombre) }
    var empresa by remember { mutableStateOf(proveedor.empresa) }
    var telefono by remember { mutableStateOf(proveedor.telefono) }
    var correo by remember { mutableStateOf(proveedor.correo) }
    var direccion by remember { mutableStateOf(proveedor.direccion) }
    var infoAdicional by remember { mutableStateOf(proveedor.infoAdicional) }

    var errorNombre by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    var errorTelefono by remember { mutableStateOf(false) }

    fun esCorreoValido(email: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun esTelefonoValido(phone: String): Boolean {
        val limpio = phone.replace(" ", "").replace("-", "")
        return limpio.matches(Regex("^\\+?[0-9]{9,15}$"))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar proveedor",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        if (it.length <= 50) nombre = it
                        if (errorNombre && it.isNotBlank()) errorNombre = false
                    },
                    label = { Text("Nombre completo") },
                    isError = errorNombre,
                    supportingText = {
                        if (errorNombre) Text("El nombre es obligatorio", color = colorScheme.error)
                    }
                )
                OutlinedTextField(
                    value = empresa,
                    onValueChange = { if (it.length <= 50) empresa = it },
                    label = { Text("Empresa (opcional)") }
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        if (it.length <= 20) telefono = it
                        if (errorTelefono && esTelefonoValido(it)) errorTelefono = false
                    },
                    label = { Text("Teléfono") },
                    isError = errorTelefono,
                    supportingText = {
                        if (errorTelefono) Text("Número inválido", color = colorScheme.error)
                    }
                )
                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        if (it.length <= 50) correo = it.trim()
                        if (errorCorreo && esCorreoValido(it)) errorCorreo = false
                    },
                    label = { Text("Correo electrónico") },
                    isError = errorCorreo,
                    supportingText = {
                        if (errorCorreo) Text("Correo inválido", color = colorScheme.error)
                    }
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { if (it.length <= 100) direccion = it },
                    label = { Text("Dirección (opcional)") }
                )
                OutlinedTextField(
                    value = infoAdicional,
                    onValueChange = { if (it.length <= 200) infoAdicional = it },
                    label = { Text("Información adicional") },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                var esValido = true
                if (nombre.isBlank()) {
                    errorNombre = true
                    esValido = false
                }
                if (correo.isNotBlank() && !esCorreoValido(correo)) {
                    errorCorreo = true
                    esValido = false
                }
                if (telefono.isNotBlank() && !esTelefonoValido(telefono)) {
                    errorTelefono = true
                    esValido = false
                }
                if (esValido) {
                    onConfirm(nombre, empresa, telefono, correo, direccion, infoAdicional)
                }
            }) {
                Text("GUARDAR")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEliminar) {
                    Text("ELIMINAR", color = colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("CANCELAR")
                }
            }
        },
        containerColor = colorScheme.background
    )
}
