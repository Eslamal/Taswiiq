import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taswiiq.data.PriceTier
import com.example.taswiiq.data.ProductModel
import com.example.taswiiq.viewmodels.EditProductUiState
import com.example.taswiiq.viewmodels.EditProductViewModel
import com.example.taswiiq.viewmodels.UpdateProductState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    navController: NavController,
    editProductViewModel: EditProductViewModel = viewModel()
) {
    // Load the product details when the screen is first composed
    LaunchedEffect(key1 = productId) {
        editProductViewModel.loadProduct(productId)
    }

    val uiState by editProductViewModel.uiState.collectAsState()
    val updateState by editProductViewModel.updateState.collectAsState()
    val context = LocalContext.current

    // Listen to the save state to show toast and navigate back
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateProductState.Saved -> {
                Toast.makeText(context, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is UpdateProductState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is EditProductUiState.Loading -> CircularProgressIndicator()
                is EditProductUiState.Success -> {
                    EditProductContent(
                        product = state.product,
                        updateState = updateState,
                        onSaveChanges = { productName, description, minQty, tiers ->
                            editProductViewModel.saveChanges(productName, description, minQty, tiers)
                        }
                    )
                }
                is EditProductUiState.Error -> Text(text = state.message)
            }
        }
    }
}

@Composable
fun EditProductContent(
    product: ProductModel,
    updateState: UpdateProductState,
    onSaveChanges: (String, String, Int, List<PriceTier>) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var minimumOrderQuantity by remember { mutableStateOf("") }
    var priceTiers by remember { mutableStateOf<List<PriceTier>>(emptyList()) }

    // Pre-fill the form fields when the product data is loaded
    LaunchedEffect(product) {
        productName = product.productName
        description = product.description
        minimumOrderQuantity = product.minimumOrderQuantity.toString()
        priceTiers = product.priceTiers
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 5)
        OutlinedTextField(
            value = minimumOrderQuantity,
            onValueChange = { minimumOrderQuantity = it },
            label = { Text("Minimum Order Quantity") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Divider()
        Text("Price Tiers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        priceTiers.forEachIndexed { index, tier ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (tier.minQuantity == 0) "" else tier.minQuantity.toString(),
                    onValueChange = {
                        val newTiers = priceTiers.toMutableList()
                        newTiers[index] = tier.copy(minQuantity = it.toIntOrNull() ?: 0)
                        priceTiers = newTiers
                    },
                    label = { Text("Min Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = if (tier.pricePerUnit == 0.0) "" else tier.pricePerUnit.toString(),
                    onValueChange = {
                        val newTiers = priceTiers.toMutableList()
                        newTiers[index] = tier.copy(pricePerUnit = it.toDoubleOrNull() ?: 0.0)
                        priceTiers = newTiers
                    },
                    label = { Text("Price/Unit") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (priceTiers.size > 1) {
                    IconButton(onClick = {
                        priceTiers = priceTiers.toMutableList().also { it.removeAt(index) }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Tier")
                    }
                }
            }
        }

        TextButton(onClick = {
            priceTiers = priceTiers + PriceTier() // Add a new empty tier
        }) {
            Text("+ Add Price Tier")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val tiersToSubmit = priceTiers.filter { it.minQuantity > 0 && it.pricePerUnit > 0 }
                onSaveChanges(
                    productName,
                    description,
                    minimumOrderQuantity.toIntOrNull() ?: 1,
                    tiersToSubmit
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = updateState !is UpdateProductState.Saving
        ) {
            if (updateState is UpdateProductState.Saving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Save Changes")
            }
        }
    }
}