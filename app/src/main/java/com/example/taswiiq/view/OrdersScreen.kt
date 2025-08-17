import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taswiiq.data.OrderModel
import com.example.taswiiq.viewmodels.OrdersUiState
import com.example.taswiiq.viewmodels.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrdersScreen(
    navController: NavController,
    ordersViewModel: OrdersViewModel = viewModel()
) {
    val uiState by ordersViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is OrdersUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is OrdersUiState.Success -> {
                if (state.orders.isEmpty()) {
                    Text("You have no orders yet.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.orders) { order ->
                            // MODIFIED HERE: Passed the navigation logic to the card's onClick
                            OrderCard(order = order, onClick = {
                                navController.navigate("order_detail/${order.orderId}")
                            })
                        }
                    }
                }
            }
            is OrdersUiState.Error -> {
                Text(text = state.message, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(
    order: OrderModel,
    onClick: () -> Unit // MODIFIED HERE: Added onClick lambda parameter
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick // MODIFIED HERE: Used the passed onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order #${order.orderId.take(6)}...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Status: ${order.status}", style = MaterialTheme.typography.bodyMedium)
            Text("Total: ${order.totalPrice} EGP", style = MaterialTheme.typography.bodyMedium)
            Text("Date: ${dateFormatter.format(order.orderTimestamp.toDate())}", style = MaterialTheme.typography.bodySmall)
        }
    }
}