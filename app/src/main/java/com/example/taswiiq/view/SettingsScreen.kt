import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taswiiq.R
import com.example.taswiiq.viewmodels.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    appViewModel: AppViewModel = viewModel()
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(id = R.string.settings_logout_confirmation_title)) },
            text = { Text(stringResource(id = R.string.settings_logout_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        navController.navigate("login_screen") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                        showLogoutDialog = false
                    }
                ) { Text(stringResource(id = R.string.settings_logout_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(id = R.string.settings_logout_cancel)) }
            }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { newLang ->
                onLanguageChange(newLang)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(vertical = 8.dp)
        ) {
            item { SettingsSectionTitle(stringResource(id = R.string.settings_appearance)) }
            item {
                SwitchSettingItem(
                    title = stringResource(id = R.string.settings_dark_mode),
                    subtitle = stringResource(id = R.string.settings_dark_mode_subtitle),
                    icon = Icons.Default.Brightness4,
                    checked = isDarkTheme,
                    onCheckedChange = onToggleDarkTheme
                )
            }
            item {
                ClickableSettingItem(
                    title = stringResource(id = R.string.settings_language),
                    subtitle = stringResource(id = R.string.settings_language_subtitle),
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
            }

            item { SettingsSectionTitle(stringResource(id = R.string.settings_notifications)) }
            item {
                ClickableSettingItem(
                    title = stringResource(id = R.string.settings_notifications),
                    subtitle = stringResource(id = R.string.settings_notifications_subtitle),
                    icon = Icons.Default.Notifications,
                    onClick = { /* TODO: Navigate to system notification settings */ }
                )
            }

            item { SettingsSectionTitle(stringResource(id = R.string.settings_account)) }
            item {
                ClickableSettingItem(
                    title = stringResource(id = R.string.settings_edit_profile),
                    icon = Icons.Default.Person,
                    onClick = { navController.navigate("edit_profile") }
                )
            }
            item {
                ClickableSettingItem(
                    title = stringResource(id = R.string.settings_logout),
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    isDestructive = true,
                    onClick = { showLogoutDialog = true }
                )
            }

            item { SettingsSectionTitle(stringResource(id = R.string.settings_support)) }
            item {
                ClickableSettingItem(
                    title = stringResource(id = R.string.settings_help_support),
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    onClick = { navController.navigate("support") }
                )
            }
            item {
                ClickableSettingItem(
                    title = stringResource(id = R.string.settings_privacy_policy),
                    icon = Icons.Default.Shield,
                    onClick = {
                        val url = "https://www.your-privacy-policy-url.com" // <-- غيّر هذا الرابط
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handle case where no web browser is available
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ClickableSettingItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val titleColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = titleColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = titleColor, fontSize = 16.sp)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.language_dialog_title)) },
        text = {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLanguageSelected("en") }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentLanguage == "en", onClick = { onLanguageSelected("en") })
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.language_english))
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLanguageSelected("ar") }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentLanguage == "ar", onClick = { onLanguageSelected("ar") })
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.language_arabic))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}