package com.cibertec.pe.netshop

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cibertec.pe.netshop.navigation.AppNavigation
import com.cibertec.pe.netshop.navigation.NavDrawerItem
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch

val qrScanned = mutableStateOf<String?>(null)

class MainActivity : ComponentActivity() {
    companion object {
        const val QR_REQUEST_CODE = IntentIntegrator.REQUEST_CODE
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route

            val selectedItem by remember(currentRoute) {
                derivedStateOf {
                    NavDrawerItem.items.find { it.route == currentRoute } ?: NavDrawerItem.Inventario
                }
            }

            val screenWidth = LocalConfiguration.current.screenWidthDp.dp

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color(0xFFD9B96A),
                        drawerTonalElevation = 6.dp,
                        modifier = Modifier.width(screenWidth / 2)
                    ) {
                        Text(
                            text = "Mi Negocio Plus",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                        NavDrawerItem.items.forEach { item ->
                            NavigationDrawerItem(
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(item.title) },
                                selected = item.route == selectedItem.route,
                                onClick = {
                                    if (navController.currentBackStackEntry?.destination?.route != item.route) {
                                        navController.navigate(item.route) {
                                            launchSingleTop = true
                                            restoreState = true
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                        }
                                    }
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        if (NavDrawerItem.items.any { it.route == currentRoute }) {
                            TopAppBar(
                                title = {
                                    if (currentRoute == NavDrawerItem.Inventario.route) {
                                        Column {
                                            Text("Productos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            Text(selectedItem.title, fontSize = 14.sp, color = Color.LightGray)
                                        }
                                    } else {
                                        Text(selectedItem.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                                    }
                                },
                                actions = {
                                    if (currentRoute == NavDrawerItem.Inventario.route) {
                                        IconButton(onClick = {
                                            val integrator = IntentIntegrator(this@MainActivity)
                                            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                                            integrator.setPrompt("Escanea el código QR o de barras")
                                            integrator.setBeepEnabled(true)
                                            integrator.setOrientationLocked(false)
                                            integrator.setCaptureActivity(ScannerActivity::class.java)
                                            integrator.initiateScan()
                                        }) {
                                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear", tint = Color.White)
                                        }

                                        IconButton(onClick = { }) {
                                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                                        }
                                        IconButton(onClick = { }) {
                                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar", tint = Color.White)
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color(0xFF1D2951),
                                    titleContentColor = Color.White,
                                    navigationIconContentColor = Color.White,
                                    actionIconContentColor = Color.White
                                )
                            )
                        }
                    },
                    containerColor = Color(0xFFE5E5E5)
                ) { paddingValues ->
                    Surface(modifier = Modifier.padding(paddingValues)) {
                        AppNavigation(navController)
                    }
                }
            }
        }
    }

    val qrScanned = mutableStateOf<String?>(null)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            qrScanned.value = result.contents // guarda el resultado
        }
    }
}