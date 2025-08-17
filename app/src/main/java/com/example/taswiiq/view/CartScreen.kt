import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.taswiiq.data.OrderItem
import com.example.taswiiq.viewmodels.AppViewModel
import com.example.taswiiq.viewmodels.CartViewModel
import com.example.taswiiq.viewmodels.CheckoutState
import com.example.taswiiq.viewmodels.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    checkoutViewModel: CheckoutViewModel = viewModel(),
    appViewModel: AppViewModel // To get current user info
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val checkoutState by checkoutViewModel.checkoutState.collectAsState()
    val currentUser by appViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(checkoutState) {
        when(val state = checkoutState) {
            is CheckoutState.Success -> {
                Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                cartViewModel.clearCart()
                navController.popBackStack()
            }
            is CheckoutState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Order") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                } }
            )
        },
        bottomBar = {
            if (cartState.items.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Total: ${cartState.totalPrice} EGP",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                currentUser?.let { buyer ->
                                    checkoutViewModel.placeOrder(cartState, buyer)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = checkoutState !is CheckoutState.Loading
                        ) {
                            if (checkoutState is CheckoutState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Place Order Now")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartState.items) { item ->
                    CartItemRow(item = item)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: OrderItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.productName,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.productName, fontWeight = FontWeight.Bold)
            Text(text = "Quantity: ${item.quantity}")
        }
        Text(text = "${item.price * item.quantity} EGP", fontWeight = FontWeight.SemiBold)
    }
}