package com.example.taswiiq

import ChatsScreen
import MainScreen
import MyAccountScreen
import OrdersScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taswiiq.data.BottomNavItem
import com.example.taswiiq.viewmodels.AppViewModel
import com.example.taswiiq.viewmodels.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    mainNavController: NavController,
    appViewModel: AppViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val bottomNavController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val currentUser by appViewModel.currentUser.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Orders,
        BottomNavItem.Messages,
        BottomNavItem.Account
    )

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Taswiiq") },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch { scaffoldState.drawerState.open() }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartState.items.isNotEmpty()) {
                                Badge { Text("${cartState.items.sumOf { it.quantity }}") }
                            }
                        }
                    ) {
                        IconButton(onClick = { mainNavController.navigate("cart_screen") }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                        }
                    }
                    IconButton(onClick = {  mainNavController.navigate("notifications") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        // --- THIS BLOCK IS CORRECTED ---
        drawerContent = {
            DrawerContent(
                user = currentUser,
                closeDrawer = { coroutineScope.launch { scaffoldState.drawerState.close() } },
                onMyAccountClicked = { bottomNavController.navigate("account") },
                onSettingsClicked = { mainNavController.navigate("settings") },
                onSupportClicked = { mainNavController.navigate("support") },
                onLogoutClicked = {
                    FirebaseAuth.getInstance().signOut()
                    mainNavController.navigate("login_screen") {
                        // This is the correct way to clear the back stack
                        popUpTo(mainNavController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        // Ensure the login screen is a single top-level destination
                        launchSingleTop = true
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.titleResId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(bottomNavController, startDestination = BottomNavItem.Home.route, Modifier.padding(innerPadding)) {
            composable(BottomNavItem.Home.route) {
                MainScreen(navController = mainNavController)
            }
            composable(BottomNavItem.Orders.route) {
                OrdersScreen(navController = mainNavController)
            }
            composable(BottomNavItem.Messages.route) {
                ChatsScreen(navController = mainNavController)
            }
            composable(BottomNavItem.Account.route) {
                MyAccountScreen(navController = mainNavController)
            }
        }
    }
}