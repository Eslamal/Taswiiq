package com.example.taswiiq.view


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.taswiiq.R
import com.example.taswiiq.data.NotificationModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import java.util.*

// --- دالة مساعدة لتنسيق الوقت (بدون تغيير) ---
private fun formatNotificationTimestamp(timestamp: Timestamp?, yesterdayString: String, nowString: String, minutesAgoString: String, hoursAgoString: String, daysAgoString: String, weeksAgoString: String, monthsAgoString: String, yearsAgoString: String): String {
    if (timestamp == null) return ""
    val now = Calendar.getInstance(); val time = Calendar.getInstance(); time.time = timestamp.toDate()
    val diffMillis = now.timeInMillis - time.timeInMillis; val seconds = diffMillis / 1000; val minutes = seconds / 60; val hours = minutes / 60; val days = hours / 24; val weeks = days / 7; val months = (days / 30.44).toLong(); val years = days / 365
    return when {
        seconds < 60 -> nowString; minutes < 60 -> String.format(minutesAgoString, minutes); hours < 24 -> String.format(hoursAgoString, hours); days == 1L -> yesterdayString
        days < 7 -> String.format(daysAgoString, days); weeks < 4 -> String.format(weeksAgoString, weeks); months < 12 -> String.format(monthsAgoString, months); else -> String.format(yearsAgoString, years)
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    var notifications by remember { mutableStateOf<List<NotificationModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val pullRefreshState = rememberPullRefreshState(isRefreshing, { isRefreshing = true })

    val scope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // --- String Resources ---
    // All string resources are fetched here in the correct composable context.
    val notificationsTitle = stringResource(id = R.string.notifications_title)
    val notificationsEmptyState = stringResource(id = R.string.notifications_empty_state)
    val yesterdayText = stringResource(id = R.string.yesterday)
    val nowText = stringResource(id = R.string.date_now)
    val minutesAgoText = stringResource(id = R.string.date_minutes_ago)
    val hoursAgoText = stringResource(id = R.string.date_hours_ago)
    val daysAgoText = stringResource(id = R.string.date_days_ago)
    val weeksAgoText = stringResource(id = R.string.date_weeks_ago)
    val monthsAgoText = stringResource(id = R.string.date_months_ago)
    val yearsAgoText = stringResource(id = R.string.date_years_ago)
    val contentDescriptionSenderImage = stringResource(id = R.string.content_description_sender_image)
    val contentDescriptionNotificationIcon = stringResource(id = R.string.content_description_notification_icon)
    val notificationNewMessage = stringResource(id = R.string.notification_new_message)
    val notificationNewOrder = stringResource(id = R.string.notification_new_order)
    val notificationOrderAccepted = stringResource(id = R.string.notification_order_accepted)
    val notificationOrderShipped = stringResource(id = R.string.notification_order_shipped)
    // --- THIS IS THE VARIABLE FOR THE FIX ---
    val notificationsUserNotLoggedIn = stringResource(id = R.string.notifications_user_not_logged_in)


    DisposableEffect(currentUserId, isRefreshing) {
        if (currentUserId == null) {
            // --- FIXED: Use the variable instead of the composable function ---
            error = notificationsUserNotLoggedIn
            isLoading = false
            isRefreshing = false
            return@DisposableEffect onDispose {}
        }
        if (!isRefreshing) { isLoading = true }

        val query = FirebaseFirestore.getInstance()
            .collection("users").document(currentUserId)
            .collection("notifications").orderBy("timestamp", Query.Direction.DESCENDING).limit(50)

        val listenerRegistration = query.addSnapshotListener { snapshot, e ->
            isLoading = false; isRefreshing = false
            if (e != null) { error = "Error: ${e.localizedMessage}"; return@addSnapshotListener }

            if (snapshot != null) {
                notifications = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<NotificationModel>()?.apply { documentId = doc.id }
                }
            }
        }
        onDispose { listenerRegistration.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(notificationsTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .pullRefresh(pullRefreshState)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(text = error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp))
                notifications.isEmpty() -> Text(text = notificationsEmptyState, modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp))
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(notifications, key = { it.documentId ?: UUID.randomUUID().toString() }) { notification ->
                            NotificationItem(
                                notification = notification,
                                yesterdayText = yesterdayText,
                                nowText = nowText,
                                minutesAgoText = minutesAgoText,
                                hoursAgoText = hoursAgoText,
                                daysAgoText = daysAgoText,
                                weeksAgoText = weeksAgoText,
                                monthsAgoText = monthsAgoText,
                                yearsAgoString = yearsAgoText,
                                contentDescriptionSenderImage = contentDescriptionSenderImage,
                                contentDescriptionNotificationIcon = contentDescriptionNotificationIcon,
                                notificationNewMessage = notificationNewMessage,
                                notificationNewOrder = notificationNewOrder,
                                notificationOrderAccepted = notificationOrderAccepted,
                                notificationOrderShipped = notificationOrderShipped
                            ) {
                                scope.launch {
                                    if (currentUserId != null && notification.documentId != null && !notification.isRead) {
                                        FirebaseFirestore.getInstance()
                                            .collection("users").document(currentUserId)
                                            .collection("notifications").document(notification.documentId!!)
                                            .update("isRead", true)
                                    }
                                    when (notification.type) {
                                        "new_message" -> navController.navigate("chat_screen/${notification.senderId}")
                                        "new_order", "order_accepted", "order_shipped" -> notification.referenceId?.let {
                                            navController.navigate("order_detail/${it}")
                                        }
                                        else -> Log.w("NotificationsScreen", "Unknown notification type or missing referenceId for navigation: ${notification.type}")
                                    }

                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
            PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationModel,
    yesterdayText: String,
    nowText: String,
    minutesAgoText: String,
    hoursAgoText: String,
    daysAgoText: String,
    weeksAgoText: String,
    monthsAgoText: String,
    yearsAgoString: String,
    contentDescriptionSenderImage: String,
    contentDescriptionNotificationIcon: String,
    notificationNewMessage: String,
    notificationNewOrder: String,
    notificationOrderAccepted: String,
    notificationOrderShipped: String,
    onClick: () -> Unit
) {
    val backgroundColor = if (notification.isRead) Color.Transparent else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    val rawText = when(notification.type) {
        "new_message" -> String.format(notificationNewMessage, notification.senderName ?: "")
        "new_order" -> String.format(notificationNewOrder, notification.senderName ?: "")
        "order_accepted" -> String.format(notificationOrderAccepted, notification.senderName ?: "")
        "order_shipped" -> String.format(notificationOrderShipped, notification.senderName ?: "")
        else -> notification.content ?: ""
    }
    val icon = when(notification.type) {
        "new_message" -> Icons.Default.Chat
        "new_order" -> Icons.Default.ShoppingCart
        "order_accepted" -> Icons.Default.CheckCircle
        "order_shipped" -> Icons.Default.LocalShipping
        else -> Icons.Default.Notifications
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp)) {
            if (!notification.senderProfileImageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = notification.senderProfileImageUrl),
                    contentDescription = contentDescriptionSenderImage,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescriptionNotificationIcon,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    val name = notification.senderName ?: "Someone"
                    val restOfMessage = rawText.replace(name, "").trim()
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(name)
                    }
                    append(" ")
                    append(restOfMessage)
                },
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatNotificationTimestamp(
                    notification.timestamp,
                    yesterdayText, nowText, minutesAgoText, hoursAgoText, daysAgoText,
                    weeksAgoText, monthsAgoText, yearsAgoString
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}