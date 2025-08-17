// In AddProductScreen.kt

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.taswiiq.viewmodels.AddProductState
import com.example.taswiiq.viewmodels.AddProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    supplierName: String,
    addProductViewModel: AddProductViewModel = viewModel()
) {
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var minimumOrderQuantity by remember { mutableStateOf("1") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // --- MODIFICATION 1: State now holds a list of PriceTiers ---
    var priceTiers by remember { mutableStateOf(listOf(PriceTier(minQuantity = 1, pricePerUnit = 0.0))) }

    val context = LocalContext.current
    val addProductState by addProductViewModel.addProductState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris -> selectedImages = uris }
    )

    LaunchedEffect(addProductState) {
        when (val state = addProductState) {
            is AddProductState.Success -> {
                Toast.makeText(context, "Product added successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is AddProductState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add New Product") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
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

            // --- MODIFICATION 2: UI for adding/editing price tiers dynamically ---
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


            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Select Images (${selectedImages.size})")
            }

            Button(
                onClick = {
                    // --- MODIFICATION 3: Passing the list of tiers to the ViewModel ---
                    val tiersToSubmit = priceTiers.filter { it.minQuantity > 0 && it.pricePerUnit > 0 }
                    addProductViewModel.createProduct(
                        productName = productName,
                        description = description,
                        category = "Default Category", // Placeholder
                        minimumOrderQuantity = minimumOrderQuantity.toIntOrNull() ?: 1,
                        priceTiers = tiersToSubmit,
                        imageUris = selectedImages,
                        supplierName = supplierName
                    )
                },
                enabled = addProductState !is AddProductState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (addProductState is AddProductState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Product")
                }
            }
        }
    }
}