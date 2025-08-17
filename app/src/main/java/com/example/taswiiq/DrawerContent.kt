package com.example.taswiiq

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.HelpOutline // Changed for consistency
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.taswiiq.data.UserModel

/**
 * --- MODIFIED HERE ---
 * Instead of taking a NavController, the Drawer now takes specific lambda functions
 * for each action. This makes it more flexible and solves the crash.
 */
@Composable
fun DrawerContent(
    user: UserModel?,
    onMyAccountClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onSupportClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    closeDrawer: () -> Unit
) {

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // Section 1: User Profile Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                .clickable {
                    onMyAccountClicked() // Use the specific function
                    closeDrawer()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val displayName = user?.companyName?.ifBlank { "${user.firstName} ${user.lastName}" } ?: "Username"
            val profileImageUrl = user?.profileImageUrl

            if (!profileImageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUrl),
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.AccountCircle, contentDescription = "Default Icon", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Section 2: Navigation Items
        Column(modifier = Modifier.weight(1f)) {
            DrawerMenuItem(
                icon = Icons.Default.AccountCircle,
                label = stringResource(id = R.string.drawer_my_account),
                onClick = {
                    onMyAccountClicked() // Use the specific function
                    closeDrawer()
                }
            )
            DrawerMenuItem(
                icon = Icons.Default.Settings,
                label = stringResource(id = R.string.drawer_settings),
                onClick = {
                    onSettingsClicked() // Use the specific function
                    closeDrawer()
                }
            )
            DrawerMenuItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline, // Changed icon
                label = stringResource(id = R.string.settings_support), // Changed string resource for consistency
                onClick = {
                    onSupportClicked() // Use the specific function
                    closeDrawer()
                }
            )
        }

        // Section 3: Logout
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        DrawerMenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp, // Changed icon
            label = stringResource(id = R.string.drawer_logout),
            onClick = {
                onLogoutClicked()
                closeDrawer()
            }
        )
    }
}

@Composable
private fun DrawerMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}