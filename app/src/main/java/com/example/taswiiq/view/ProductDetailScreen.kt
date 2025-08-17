import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.taswiiq.data.PriceTier
import com.example.taswiiq.data.ProductModel
import com.example.taswiiq.viewmodels.CartViewModel
import com.example.taswiiq.viewmodels.ProductDetailUiState
import com.example.taswiiq.viewmodels.ProductDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    productDetailViewModel: ProductDetailViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel() // Ensure this is the shared instance
) {
    LaunchedEffect(key1 = productId) {
        productDetailViewModel.fetchProductDetails(productId)
    }

    val uiState by productDetailViewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Product Details") }) }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ProductDetailUiState.Loading -> CircularProgressIndicator()
                is ProductDetailUiState.Success -> ProductDetailsContent(
                    product = state.product,
                    cartViewModel = cartViewModel
                )
                is ProductDetailUiState.Error -> Text(text = state.message)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductDetailsContent(product: ProductModel, cartViewModel: CartViewModel) {
    val pagerState = rememberPagerState(pageCount = { product.imageUrls.size })
    var quantity by remember { mutableStateOf(product.minimumOrderQuantity.toString()) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (product.imageUrls.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(300.dp)
            ) { page ->
                AsyncImage(
                    model = product.imageUrls[page],
                    contentDescription = "Product Image ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = product.productName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "Offered by: ${product.supplierName}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Minimum Order: ${product.minimumOrderQuantity} pieces", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- MODIFICATION 1: Display Price Tiers Table ---
            if (product.priceTiers.isNotEmpty()) {
                Text("Wholesale Prices:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                PriceTiersTable(tiers = product.priceTiers)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = product.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp)
                )
                Button(
                    onClick = {
                        val qty = quantity.toIntOrNull()
                        if (qty == null || qty <= 0) {
                            Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                        } else if (qty < product.minimumOrderQuantity) {
                            Toast.makeText(context, "Minimum order quantity is ${product.minimumOrderQuantity}", Toast.LENGTH_LONG).show()
                        } else {
                            cartViewModel.addToCart(product, qty)
                            Toast.makeText(context, "${product.productName} added to order", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("Add to Order")
                }
            }
        }
    }
}

// --- NEW COMPOSABLE ADDED ---
@Composable
fun PriceTiersTable(tiers: List<PriceTier>) {
    val sortedTiers = tiers.sortedBy { it.minQuantity }
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column {
            // Header Row
            Row(
                Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).padding(8.dp)
            ) {
                Text("Quantity", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Price per Unit", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Divider()
            // Data Rows
            sortedTiers.forEach { tier ->
                Row(Modifier.padding(8.dp)) {
                    Text("From ${tier.minQuantity} pieces", modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("${tier.pricePerUnit} EGP", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
                }
                Divider()
            }
        }
    }
}