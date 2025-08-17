package com.example.taswiiq.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.taswiiq.R

// Data class to hold FAQ information using String Resource IDs
data class FaqData(
    val categoryResId: Int,
    val questions: List<Pair<Int, Int>>
)

// The main Composable for the Support Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavController) {
    val context = LocalContext.current
    val faqs = getFaqs() // Fetching our FAQ data

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_support)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.support_back_button)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                ScreenHeader()
            }

            // FAQ Section
            item {
                SectionTitle(
                    text = stringResource(id = R.string.support_faq_section_header),
                    icon = Icons.Default.HelpOutline
                )
            }

            items(faqs) { faqCategory ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = faqCategory.categoryResId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                faqCategory.questions.forEach { (questionResId, answerResId) ->
                    FaqItem(
                        question = stringResource(id = questionResId),
                        answer = stringResource(id = answerResId)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Spacer
            item {
                Divider(modifier = Modifier.padding(vertical = 24.dp))
            }

            // Contact Us Section
            item {
                SectionTitle(
                    text = stringResource(id = R.string.support_contact_section_header),
                    icon = Icons.Default.ContactSupport
                )
                Spacer(modifier = Modifier.height(16.dp))
                ContactCard(context = context)
            }

            // Spacer
            item {
                Divider(modifier = Modifier.padding(vertical = 24.dp))
            }

            // Developer Info Section
            item {
                DeveloperInfoSection()
            }
        }
    }
}

@Composable
fun ScreenHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = "Support Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.support_main_header),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.support_sub_header),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ContactCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.support_contact_card_text),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val emailSubject = context.getString(R.string.support_email_subject)
                    val emailBody = getDeviceInfoBody(context)
                    // TODO: Replace with your actual support email
                    val supportEmail = "support@your-app-name.com"
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$supportEmail")
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        putExtra(Intent.EXTRA_TEXT, emailBody)
                    }
                    try {
                        context.startActivity(emailIntent)
                    } catch (e: Exception) {
                        // Handle case where no email app is available
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.support_contact_button))
            }
        }
    }
}

@Composable
fun DeveloperInfoSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DeveloperMode,
            contentDescription = "Developer",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(
                id = R.string.support_developer_info,
                stringResource(id = R.string.developer_name)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getDeviceInfoBody(context: Context): String {
    return try {
        val appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        val infoHeader = context.getString(R.string.support_email_info_header)
        """
        
        $infoHeader
        ----------------------------------
        App Version: $appVersion
        Device: ${Build.MANUFACTURER} ${Build.MODEL}
        OS Version: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
        """.trimIndent()
    } catch (e: Exception) {
        "Could not retrieve device info."
    }
}

fun getFaqs(): List<FaqData> {
    return listOf(
        FaqData(
            categoryResId = R.string.support_faq_category_account,
            questions = listOf(
                R.string.support_q_edit_profile to R.string.support_a_edit_profile,
                R.string.support_q_forgot_password to R.string.support_a_forgot_password
            )
        ),
        FaqData(
            categoryResId = R.string.support_faq_category_orders,
            questions = listOf(
                R.string.support_q_track_order to R.string.support_a_track_order,
                R.string.support_q_leave_review to R.string.support_a_leave_review
            )
        ),
        FaqData(
            categoryResId = R.string.support_faq_category_general,
            questions = listOf(
                R.string.support_q_what_is_taswiiq to R.string.support_a_what_is_taswiiq,
                R.string.support_q_is_it_free to R.string.support_a_is_it_free
            )
        )
    )
}