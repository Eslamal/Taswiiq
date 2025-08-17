import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.taswiiq.data.ProductModel
import com.example.taswiiq.viewmodels.ManageProductsUiState
import com.example.taswiiq.viewmodels.ManageProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(
    navController: NavController,
    manageProductsViewModel: ManageProductsViewModel = viewModel()
) {
    val uiState by manageProductsViewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) } // Holds the ID of product to delete

    // Dialog to confirm deletion
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this product? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        manageProductsViewModel.deleteProduct(showDeleteDialog!!)
                        showDeleteDialog = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage My Products") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ManageProductsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ManageProductsUiState.Success -> {
                    if (state.products.isEmpty()) {
                        Text("You haven't added any products yet.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.products) { product ->
                                ProductCard(
                                    product = product,
                                    onEditClick = {
                                        navController.navigate("edit_product/${product.productId}")
                                    },
                                    onDeleteClick = {
                                        showDeleteDialog = product.productId
                                    }
                                )
                            }
                        }
                    }
                }
                is ManageProductsUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrls.firstOrNull(),
                contentDescription = product.productName,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val basePrice = product.priceTiers.minByOrNull { it.minQuantity }?.pricePerUnit
                if (basePrice != null) {
                    Text(text = "Starts at: $basePrice EGP", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(text = "No price defined", style = MaterialTheme.typography.bodyMedium)
                }
            }
            // --- Action Buttons ---
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Product")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}