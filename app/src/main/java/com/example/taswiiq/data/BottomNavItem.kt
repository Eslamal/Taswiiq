package com.example.taswiiq.data

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.taswiiq.R

// هذا الكلاس يعرف كل عنصر في الشريط السفلي
sealed class BottomNavItem(
    val route: String, // المسار الذي سننتقل إليه عند الضغط
    @StringRes val titleResId: Int, // النص الذي سيظهر تحت الأيقونة
    val icon: ImageVector // الأيقونة نفسها
) {
    object Home : BottomNavItem("home", R.string.bottom_nav_home, Icons.Default.Home)
    object Orders : BottomNavItem("orders", R.string.bottom_nav_orders, Icons.Default.ShoppingCart)
    object Messages : BottomNavItem("messages", R.string.bottom_nav_messages, Icons.Default.Email)
    object Account : BottomNavItem("account", R.string.bottom_nav_account, Icons.Default.AccountCircle)
}