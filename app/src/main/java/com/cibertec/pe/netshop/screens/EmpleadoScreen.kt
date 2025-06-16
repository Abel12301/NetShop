package com.cibertec.pe.netshop.screens

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.data.entity.Empleado
import com.cibertec.pe.netshop.viewmodel.EmpleadoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadoScreen(
    navController: NavHostController,
    viewModel: EmpleadoViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    var mostrarDialogo by remember { mutableStateOf(false) }
    val empleados by viewModel.empleados.collectAsState()
    var empleadoEditando by remember { mutableStateOf<Empleado?>(null) }

    Scaffold(
        bottomBar = { BottomNavBarEmpleado() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    empleadoEditando = null
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
            FilterRowEmpleado()
            empleados.forEach { empleado ->
                EmpleadoCard(empleado = empleado, onClick = {
                    empleadoEditando = empleado
                    mostrarDialogo = true
                })
            }
        }

        if (mostrarDialogo) {
            DialogAgregarEditarEmpleado(
                empleado = empleadoEditando,
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, puesto, salario, telefono, correo, direccion, infoAdicional ->
                    if (empleadoEditando == null) {
                        viewModel.insertar(
                            Empleado(
                                nombre = nombre,
                                puesto = puesto,
                                salario = salario,
                                telefono = telefono,
                                correo = correo,
                                direccion = direccion,
                                infoAdicional = infoAdicional
                            )
                        )
                    } else {
                        viewModel.actualizar(
                            empleadoEditando!!.copy(
                                nombre = nombre,
                                puesto = puesto,
                                salario = salario,
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
                    empleadoEditando?.let { viewModel.eliminar(it) }
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun FilterRowEmpleado() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterChipEmpleado("Activos")
        FilterChipEmpleado("Inactivos")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipEmpleado(text: String) {
    val colorScheme = MaterialTheme.colorScheme
    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = if (text == "Activos") Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = colorScheme.primary
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = colorScheme.secondaryContainer,
            labelColor = colorScheme.onSecondaryContainer
        )
    )
}

@Composable
fun EmpleadoCard(empleado: Empleado, onClick: () -> Unit) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("📞 ${empleado.telefono}", fontSize = 12.sp, color = colorScheme.outline)
                Text("✉️ ${empleado.correo}", fontSize = 12.sp, color = colorScheme.outline)
            }
            Spacer(Modifier.height(6.dp))
            Text("👤 ${empleado.nombre}", fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Text("Puesto: ${empleado.puesto}", color = colorScheme.onSurfaceVariant)
            Text("Salario: ${empleado.salario}", color = colorScheme.onSurfaceVariant)
            Text("📍 Dirección: ${empleado.direccion}", color = colorScheme.onSurfaceVariant)
            if (empleado.infoAdicional.isNotEmpty()) {
                Text("ℹ️ ${empleado.infoAdicional}", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarEditarEmpleado(
    empleado: Empleado?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String) -> Unit,
    onEliminar: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    var nombre by remember { mutableStateOf(empleado?.nombre ?: "") }
    var puesto by remember { mutableStateOf(empleado?.puesto ?: "") }
    var salario by remember { mutableStateOf(empleado?.salario ?: "") }
    var telefono by remember { mutableStateOf(empleado?.telefono ?: "") }
    var correo by remember { mutableStateOf(empleado?.correo ?: "") }
    var direccion by remember { mutableStateOf(empleado?.direccion ?: "") }
    var infoAdicional by remember { mutableStateOf(empleado?.infoAdicional ?: "") }

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
                    if (empleado == null) "Agregar Empleado" else "Editar Empleado",
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { launcher.launch(null) }) {
                    Icon(Icons.Default.Contacts, contentDescription = "Contactos", tint = colorScheme.primary)
                }
            }
        },
        text = {
            Box(modifier = Modifier.heightIn(max = 500.dp)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    CampoConIconoYContador(
                        valor = nombre,
                        onValorCambio = {
                            nombre = it
                            errorNombre = it.isBlank()
                        },
                        etiqueta = "Nombre*",
                        icono = Icons.Default.Person,
                        longitudMax = 30,
                        isError = errorNombre
                    )

                    CampoConIconoYContador(
                        valor = puesto,
                        onValorCambio = { puesto = it },
                        etiqueta = "Puesto",
                        icono = Icons.Default.Work,
                        longitudMax = 30
                    )

                    OutlinedTextField(
                        value = salario,
                        onValueChange = { salario = it },
                        label = { Text("Salario") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    CampoConIconoYContador(
                        valor = telefono,
                        onValorCambio = { telefono = it },
                        etiqueta = "Teléfono",
                        icono = Icons.Default.Phone,
                        longitudMax = 15
                    )

                    CampoConIconoYContador(
                        valor = correo,
                        onValorCambio = {
                            correo = it
                            errorCorreo = correo.isNotBlank() && !esCorreoValido(it)
                        },
                        etiqueta = "Correo",
                        icono = Icons.Default.Email,
                        longitudMax = 40,
                        isError = errorCorreo
                    )

                    CampoConIconoYContador(
                        valor = direccion,
                        onValorCambio = { direccion = it },
                        etiqueta = "Dirección",
                        icono = Icons.Default.LocationOn,
                        longitudMax = 50
                    )

                    CampoConIconoYContador(
                        valor = infoAdicional,
                        onValorCambio = { infoAdicional = it },
                        etiqueta = "Información adicional",
                        icono = Icons.Default.Info,
                        longitudMax = 100,
                        maxLineas = 3
                    )
                }
            }
        },
        confirmButton = {
            IconButton(onClick = {
                errorNombre = nombre.isBlank()
                errorCorreo = correo.isNotBlank() && !esCorreoValido(correo)

                if (!errorNombre && !errorCorreo) {
                    onConfirm(nombre, puesto, salario, telefono, correo, direccion, infoAdicional)
                }
            }) {
                Icon(Icons.Default.Check, contentDescription = "Guardar", tint = colorScheme.primary)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (empleado != null) {
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

@Composable
fun CampoConIconoYContador(
    valor: String,
    onValorCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    longitudMax: Int,
    maxLineas: Int = 1,
    isError: Boolean = false
) {
    Column {
        OutlinedTextField(
            value = valor,
            onValueChange = { if (it.length <= longitudMax) onValorCambio(it) },
            label = { Text(etiqueta) },
            singleLine = maxLineas == 1,
            maxLines = maxLineas,
            leadingIcon = { Icon(icono, contentDescription = null) },
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("${valor.length}/$longitudMax", style = MaterialTheme.typography.labelSmall)
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBarEmpleado() {
    val colorScheme = MaterialTheme.colorScheme
    NavigationBar(containerColor = colorScheme.primary) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Work, contentDescription = null, tint = colorScheme.onPrimary) },
            label = { Text("Empleados", color = colorScheme.onPrimary) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = colorScheme.onPrimary) },
            label = { Text("Turnos", color = colorScheme.onPrimary) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Badge, contentDescription = null, tint = colorScheme.onPrimary) },
            label = { Text("Cargos", color = colorScheme.onPrimary) }
        )
    }
}
