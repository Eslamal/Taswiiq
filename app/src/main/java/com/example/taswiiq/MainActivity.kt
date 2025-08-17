package com.example.taswiiq

import OrderDetailScreen
import AddProductScreen
import CartScreen
import ChatScreen
import EditProductScreen
import EditProfileScreen
import ForgotPasswordScreen
import LoginScreen
import ManageProductsScreen
import ProductDetailScreen
import ProfileScreen
import RegisterScreen
import SettingsScreen
import SplashScreen
import SuppliersListScreen
import UserProfileScreen
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taswiiq.data.LocaleHelper
import com.example.taswiiq.ui.theme.TaswiiqTheme
import com.example.taswiiq.view.NotificationsScreen
import com.example.taswiiq.view.SupportScreen
import com.example.taswiiq.viewmodels.AppViewModel
import com.example.taswiiq.viewmodels.CartViewModel
import com.example.taswiiq.viewmodels.OrdersViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val PREFS_NAME = "app_prefs"
    private val KEY_LANGUAGE = "app_language"
    private val KEY_DARK_THEME = "dark_theme"

    override fun attachBaseContext(newBase: Context) {
        val sharedPref = newBase.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedLanguage = sharedPref.getString(KEY_LANGUAGE, Locale.getDefault().language) ?: Locale.getDefault().language
        val context = LocaleHelper.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val savedLanguage = getSavedLanguage() ?: Locale.getDefault().language
        super.onCreate(savedInstanceState)

        setContent {
            var currentLanguage by remember { mutableStateOf(savedLanguage) }
            var isDarkTheme by remember { mutableStateOf(getDarkThemePref()) }
            TaswiiqTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                // --- ADDED HERE: Handles navigation when app is opened from a notification ---
                HandleNotificationIntent(navController = navController, intent = intent)

                NavHost(navController, startDestination = "splash") {
                    composable("splash") { SplashScreen(navController) }
                    composable("main") { AppScreen(mainNavController = navController) }
                    composable("login_screen") { LoginScreen(navController) }
                    composable("register_screen") { RegisterScreen(navController) }
                    composable("profile"){ ProfileScreen(navController) }
                    composable("edit_profile") { EditProfileScreen(navController) }
                    composable("notifications") { NotificationsScreen(navController) }
                    composable("support") { SupportScreen(navController) }

                    composable("settings"){ SettingsScreen(
                        navController = navController,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { newValue ->
                            isDarkTheme = newValue
                            saveDarkThemePref(newValue)
                        },
                        currentLanguage = currentLanguage,
                        onLanguageChange = { newLang ->
                            currentLanguage = newLang
                            saveLanguage(newLang)
                            restartApp(this@MainActivity)
                        })
                    }
                    composable("suppliers_list/{categoryName}") { backStackEntry ->
                        val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                        SuppliersListScreen(categoryName = categoryName, navController = navController)
                    }
                    composable("cart_screen") {
                        val mainGraphEntry = remember(it) {
                            navController.getBackStackEntry("main")
                        }
                        val cartViewModel: CartViewModel = viewModel(mainGraphEntry)
                        val appViewModel: AppViewModel = viewModel(mainGraphEntry)
                        CartScreen(navController = navController, cartViewModel = cartViewModel, appViewModel = appViewModel)
                    }
                    composable("chat_screen/{receiverId}") { backStackEntry ->
                        val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
                        ChatScreen(navController = navController, receiverId = receiverId)
                    }
                    composable("add_product/{supplierName}") { backStackEntry ->
                        val supplierName = backStackEntry.arguments?.getString("supplierName") ?: ""
                        AddProductScreen(navController = navController, supplierName = supplierName)
                    }
                    composable("edit_product/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
                        EditProductScreen(productId = productId, navController = navController)
                    }
                    composable("manage_products") {
                        ManageProductsScreen(navController = navController)
                    }
                    composable("product_detail/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId") ?: ""
                        ProductDetailScreen(productId = productId, navController = navController)
                    }
                    composable("userProfile/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        UserProfileScreen(navController, userId)
                    }
                    composable("forgot_password") { ForgotPasswordScreen(navController) }

                    composable("order_detail/{orderId}") { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                        val mainGraphEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("main")
                        }
                        val appViewModel: AppViewModel = viewModel(mainGraphEntry)
                        val ordersViewModel: OrdersViewModel = viewModel(mainGraphEntry)

                        OrderDetailScreen(
                            navController = navController,
                            orderId = orderId,
                            appViewModel = appViewModel,
                            ordersViewModel = ordersViewModel
                        )
                    }
                }
            }
        }
    }

    private fun getSavedLanguage(): String? {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return sharedPref.getString(KEY_LANGUAGE, null)
    }

    private fun saveLanguage(langCode: String) {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPref.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    private fun getDarkThemePref(): Boolean {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return sharedPref.getBoolean(KEY_DARK_THEME, false)
    }

    private fun saveDarkThemePref(isDark: Boolean) {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPref.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        val config = Configuration(overrideConfiguration ?: Configuration())
        val langCode = getSavedLanguage() ?: Locale.getDefault().language
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            config.setLayoutDirection(locale)
        }
        super.applyOverrideConfiguration(config)
    }

    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
    }
}

/**
 * --- NEW HELPER COMPOSABLE ---
 * A helper composable to handle navigation from a notification intent.
 */
@Composable
private fun HandleNotificationIntent(navController: NavController, intent: Intent?) {
    LaunchedEffect(intent) {
        val screen = intent?.getStringExtra("screen")
        val referenceId = intent?.getStringExtra("referenceId")

        if (screen != null && referenceId != null) {
            val route = when (screen) {
                "orders" -> "order_detail/$referenceId"
                "messages" -> "chat_screen/$referenceId"
                // Add other cases as needed for different notification types
                else -> null
            }
            route?.let {
                navController.navigate(it)
                // Clear the extras from the intent so it doesn't trigger again on configuration change
                intent.removeExtra("screen")
                intent.removeExtra("referenceId")
            }
        }
    }
}