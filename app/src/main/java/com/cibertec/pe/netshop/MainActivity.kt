package com.cibertec.pe.netshop

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cibertec.pe.netshop.navigation.AppNavigation
import com.cibertec.pe.netshop.navigation.NavDrawerItem
import com.cibertec.pe.netshop.ui.theme.NetShopTheme

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
            // Estado de tema
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            NetShopTheme(darkTheme = isDarkTheme) {
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
                            drawerContainerColor = Color(0xFFFAF8F8),
                            drawerTonalElevation = 6.dp,
                            modifier = Modifier.width(screenWidth * 0.7f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1D2951))
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Net Shop",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            NavDrawerItem.items.forEach { item ->
                                NavigationDrawerItem(
                                    icon = {
                                        Icon(
                                            item.icon,
                                            contentDescription = item.title,
                                            tint = if (item.route == selectedItem.route)
                                                Color.White else Color(0xFF050001)
                                        )
                                    },
                                    label = {
                                        Text(
                                            item.title,
                                            fontWeight = if (item.route == selectedItem.route)
                                                FontWeight.Bold else FontWeight.Normal,
                                            color = if (item.route == selectedItem.route)
                                                Color.White else Color(0xFF1D2951)
                                        )
                                    },
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
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = Color(0xFF4A70EE),
                                        unselectedContainerColor = Color.Transparent
                                    )
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
                                        Text(
                                            selectedItem.title,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch { drawerState.open() }
                                        }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                                        }
                                    },
                                    actions = {
                                        IconButton(onClick = {
                                            isDarkTheme = !isDarkTheme
                                        }) {
                                            Icon(
                                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                contentDescription = "Cambiar tema",
                                                tint = Color.White
                                            )
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
                        containerColor = MaterialTheme.colorScheme.background
                    ) { paddingValues ->
                        Surface(modifier = Modifier.padding(paddingValues)) {
                            AppNavigation(navController)
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            qrScanned.value = result.contents
        }
    }
}
