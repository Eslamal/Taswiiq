import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taswiiq.data.OrderModel
import com.example.taswiiq.data.OrderStatus
import com.example.taswiiq.viewmodels.AppViewModel
import com.example.taswiiq.viewmodels.OrderDetailViewModel
import com.example.taswiiq.viewmodels.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavController,
    orderId: String,
    appViewModel: AppViewModel,
    ordersViewModel: OrdersViewModel
) {
    val orderDetailViewModel: OrderDetailViewModel = viewModel()
    val currentUser by appViewModel.currentUser.collectAsState()
    val ordersState by ordersViewModel.uiState.collectAsState()

    var showReviewDialog by remember { mutableStateOf(false) }

    val order = (ordersState as? com.example.taswiiq.viewmodels.OrdersUiState.Success)
        ?.orders?.find { it.orderId == orderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Order not found or still loading...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Order Info (No changes here)
                item { Text("Order ID: #${order.orderId.take(8)}...", style = MaterialTheme.typography.titleLarge) }
                item { Text("Buyer: ${order.buyerName}", style = MaterialTheme.typography.bodyLarge) }
                item {
                    val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    Text("Date: ${dateFormatter.format(order.orderTimestamp.toDate())}", style = MaterialTheme.typography.bodySmall)
                }
                item {
                    Text(
                        "Status: ${order.status}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
                item { Text("Items in this order:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(order.items) { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.productName} (x${item.quantity})")
                        Text("${item.price * item.quantity} EGP")
                    }
                }
                item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
                item {
                    Text(
                        "Total Price: ${order.totalPrice} EGP",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }

                // Action Buttons Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    val isUserTheSupplier = currentUser?.uid == order.supplierId
                    val isUserTheBuyer = currentUser?.uid == order.buyerId

                    // Buttons for the Supplier (No changes here)
                    if (isUserTheSupplier) {
                        when (OrderStatus.valueOf(order.status)) {
                            OrderStatus.PENDING -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            orderDetailViewModel.updateStatus(order.orderId, OrderStatus.ACCEPTED)
                                            navController.popBackStack()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Accept Order") }

                                    Button(
                                        onClick = {
                                            orderDetailViewModel.updateStatus(order.orderId, OrderStatus.CANCELLED)
                                            navController.popBackStack()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Reject Order") }
                                }
                            }
                            OrderStatus.ACCEPTED -> {
                                Button(
                                    onClick = {
                                        orderDetailViewModel.updateStatus(order.orderId, OrderStatus.SHIPPED)
                                        navController.popBackStack()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Mark as Shipped") }
                            }
                            else -> { /* No actions for other statuses */ }
                        }
                    }

                    // --- NEW LOGIC ADDED HERE ---
                    // "Confirm Delivery" button for the Buyer
                    if (isUserTheBuyer && order.status == OrderStatus.SHIPPED.name) {
                        Button(
                            onClick = {
                                orderDetailViewModel.updateStatus(order.orderId, OrderStatus.COMPLETED)
                                // We don't pop back, so the user can see the status change and leave a review
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirm Delivery")
                        }
                    }

                    // "Leave a Review" button for the Buyer (No changes here)
                    if (isUserTheBuyer && order.status == OrderStatus.COMPLETED.name) {
                        Button(
                            onClick = { showReviewDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Leave a Review")
                        }
                    }
                }
            }
        }
    }

    if (showReviewDialog && order != null) {
        ReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                currentUser?.let { user ->
                    orderDetailViewModel.submitReview(order, user, rating, comment)
                }
                showReviewDialog = false
            }
        )
    }
}

// ReviewDialog composable remains the same
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Float, comment: String) -> Unit
) {
    var rating by remember { mutableFloatStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave a Review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How was your experience?")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star $star",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { rating = star.toFloat() },
                            tint = if (star <= rating) Color(0xFFFFC107) else Color.Gray
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your comment (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = rating > 0
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}