import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.taswiiq.data.UserModel
import com.example.taswiiq.viewmodels.ChatsUiState
import com.example.taswiiq.viewmodels.ChatsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatsScreen(
    navController: NavController,
    chatsViewModel: ChatsViewModel = viewModel()
) {
    val uiState by chatsViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ChatsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ChatsUiState.Success -> {
            if (state.chatPartners.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No chats yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.chatPartners, key = { it.first.uid }) { (user, chatDetails) ->
                        ChatListItem(
                            user = user,
                            lastMessage = chatDetails["lastMessage"] as? String ?: "",
                            lastMessageTimestamp = chatDetails["lastMessageTimestamp"] as? Date ?: Date(),
                            unreadCount = chatDetails["unreadCount"] as? Long ?: 0L,
                            onClick = {
                                navController.navigate("chat_screen/${user.uid}")
                            }
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }
        is ChatsUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message)
            }
        }
    }
}

@Composable
fun ChatListItem(
    user: UserModel,
    lastMessage: String,
    lastMessageTimestamp: Date,
    unreadCount: Long,
    onClick: () -> Unit
) {
    val isUnread = unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.profileImageUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier.size(60.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            val displayName = user.companyName.ifBlank { "${user.firstName} ${user.lastName}" }
            Text(
                text = displayName,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatChatTimestamp(lastMessageTimestamp),
                style = MaterialTheme.typography.bodySmall,
                color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper function to format the timestamp for the chat list
private fun formatChatTimestamp(date: Date): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { time = date }

    return when {
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(date) // e.g., 10:30 AM
        }
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - 1 == then.get(Calendar.DAY_OF_YEAR) -> {
            "Yesterday"
        }
        else -> {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date) // e.g., 25/12/24
        }
    }
}