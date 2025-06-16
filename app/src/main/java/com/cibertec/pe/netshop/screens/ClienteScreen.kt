package com.cibertec.pe.netshop.screens

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.data.entity.Cliente
import com.cibertec.pe.netshop.viewmodel.ClienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(
    navController: NavHostController,
    viewModel: ClienteViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    var mostrarDialogo by remember { mutableStateOf(false) }
    var clienteEditando by remember { mutableStateOf<Cliente?>(null) }
    val clientes by viewModel.clientes.collectAsState()

    Scaffold(
        bottomBar = { BottomNavBarCliente() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clienteEditando = null
                    mostrarDialogo = true
                },
                containerColor = colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cliente", tint = colorScheme.onPrimary)
            }
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            clientes.forEach { cliente ->
                ClienteCard(cliente = cliente, onClick = {
                    clienteEditando = cliente
                    mostrarDialogo = true
                })
            }
        }

        if (mostrarDialogo) {
            DialogAgregarEditarCliente(
                cliente = clienteEditando,
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, alias, telefono, correo, direccion, infoAdicional ->
                    if (clienteEditando == null) {
                        viewModel.insertar(
                            Cliente(0, nombre, alias, telefono, correo, direccion, infoAdicional)
                        )
                    } else {
                        viewModel.actualizar(
                            clienteEditando!!.copy(
                                nombre = nombre,
                                alias = alias,
                                telefono = telefono,
                                correo = correo,
                                direccion = direccion,
                                infoAdicional = infoAdicional
                            )
                        )
                    }
                    mostrarDialogo = false
                },
                onEliminar = {
                    clienteEditando?.let { viewModel.eliminar(it) }
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun ClienteCard(cliente: Cliente, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("👤 ${cliente.nombre}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Text("Alias: ${cliente.alias}", color = colorScheme.onSurfaceVariant)
            Text("📞 ${cliente.telefono}", color = colorScheme.onSurfaceVariant)
            Text("✉️ ${cliente.correo}", color = colorScheme.onSurfaceVariant)
            Text("📍 Dirección: ${cliente.direccion}", color = colorScheme.onSurfaceVariant)
            if (cliente.infoAdicional.isNotEmpty()) {
                Text("ℹ️ ${cliente.infoAdicional}", fontSize = 12.sp, color = colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarEditarCliente(
    cliente: Cliente?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String) -> Unit,
    onEliminar: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    var nombre by remember { mutableStateOf(cliente?.nombre ?: "") }
    var alias by remember { mutableStateOf(cliente?.alias ?: "") }
    var telefono by remember { mutableStateOf(cliente?.telefono ?: "") }
    var correo by remember { mutableStateOf(cliente?.correo ?: "") }
    var direccion by remember { mutableStateOf(cliente?.direccion ?: "") }
    var infoAdicional by remember { mutableStateOf(cliente?.infoAdicional ?: "") }

    var errorNombre by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        uri?.let {
            val cursor = contentResolver.query(it, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nombreIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                    val nombreContacto = if (nombreIndex >= 0) it.getString(nombreIndex) else ""
                    val contactoId = if (idIndex >= 0) it.getString(idIndex) else ""

                    val phones = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactoId),
                        null
                    )

                    var telefonoContacto = ""
                    phones?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            val phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (phoneIndex >= 0) telefonoContacto = phoneCursor.getString(phoneIndex)
                        }
                    }

                    var correoContacto = ""
                    val emails = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                        arrayOf(contactoId),
                        null
                    )

                    emails?.use { emailCursor ->
                        if (emailCursor.moveToFirst()) {
                            val emailIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                            if (emailIndex >= 0) correoContacto = emailCursor.getString(emailIndex)
                        }
                    }

                    nombre = nombreContacto
                    telefono = telefonoContacto
                    correo = correoContacto
                }
            }
        }
    }

    fun esCorreoValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (cliente == null) "Agregar Cliente" else "Editar Cliente",
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { launcher.launch(null) }) {
                    Icon(Icons.Default.Contacts, contentDescription = "Abrir contactos", tint = colorScheme.primary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorNombre = it.isBlank()
                    },
                    label = { Text("Nombre*") },
                    isError = errorNombre,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Alias") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        errorCorreo = correo.isNotBlank() && !esCorreoValido(it)
                    },
                    label = { Text("Correo") },
                    isError = errorCorreo,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = infoAdicional,
                    onValueChange = { infoAdicional = it },
                    label = { Text("Información adicional") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (cliente != null) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = colorScheme.error)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = colorScheme.outline)
                }
                IconButton(onClick = {
                    errorNombre = nombre.isBlank()
                    errorCorreo = correo.isNotBlank() && !esCorreoValido(correo)

                    if (!errorNombre && !errorCorreo) {
                        onConfirm(nombre, alias, telefono, correo, direccion, infoAdicional)
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar", tint = colorScheme.primary)
                }
            }
        },
        containerColor = colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBarCliente() {
    val colorScheme = MaterialTheme.colorScheme

    NavigationBar(containerColor = colorScheme.primary) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = { Text("Clientes", color = colorScheme.onPrimary) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { Text("Pedidos", color = colorScheme.onPrimary) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            label = { Text("Favoritos", color = colorScheme.onPrimary) }
        )
    }
}
