import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.taswiiq.data.ProductModel
import com.example.taswiiq.data.ReviewModel
import com.example.taswiiq.data.UserModel
import com.example.taswiiq.viewmodels.UserProfileUiState
import com.example.taswiiq.viewmodels.UserProfileViewModel

@Composable
fun UserProfileScreen(
    navController: NavController,
    userId: String,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    LaunchedEffect(key1 = userId) {
        userProfileViewModel.loadUserProfile(userId)
    }

    val uiState by userProfileViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is UserProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UserProfileUiState.Success -> {
            UserProfileContent(
                user = state.user,
                products = state.products,
                reviews = state.reviews,
                isConnected = state.isConnected,
                onConnectClick = { userProfileViewModel.connect(userId) },
                onMessageClick = { navController.navigate("chat_screen/$userId") },
                navController = navController
            )
        }
        is UserProfileUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message)
            }
        }
    }
}
@Composable
fun UserProfileContent(
    user: UserModel,
    products: List<ProductModel>,
    reviews: List<ReviewModel>,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    onMessageClick: () -> Unit,
    navController: NavController
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("About Supplier", "Products (${products.size})", "Reviews (${reviews.size})")

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = user.profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                error = rememberVectorPainter(image = Icons.Filled.AccountCircle)
            )

            val displayName = user.companyName.ifBlank { "${user.firstName} ${user.lastName}" }
            Text(text = displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }

        when (tabIndex) {
            0 -> AboutSupplierTab(user = user, isConnected = isConnected, onConnectClick = onConnectClick, onMessageClick = onMessageClick)
            1 -> ProductsListTab(products = products, navController = navController)
            2 -> ReviewsListTab(reviews = reviews)
        }
    }
}

@Composable
fun AboutSupplierTab(user: UserModel, isConnected: Boolean, onConnectClick: () -> Unit, onMessageClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Email: ${user.email}")
        Text("Phone: ${user.phone ?: "Not Available"}")
        Text("Commercial Record: ${user.commercialRecord ?: "Not Available"}")

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onConnectClick, enabled = !isConnected) {
                Text(if (isConnected) "Connected" else "Connect")
            }
            Button(onClick = onMessageClick) {
                Text("Message")
            }
        }
    }
}

@Composable
fun ProductsListTab(products: List<ProductModel>, navController: NavController) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("This supplier has not added any products yet.")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { product ->
            // --- This now correctly calls the simple ProductCard defined below ---
            ProductCard(product = product, onClick = {
                navController.navigate("product_detail/${product.productId}")
            })
        }
    }
}

@Composable
fun ReviewsListTab(reviews: List<ReviewModel>) {
    if (reviews.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No reviews yet for this supplier.")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(reviews) { review ->
            ReviewCard(review = review)
        }
    }
}

@Composable
fun ReviewCard(review: ReviewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = review.reviewerImageUrl,
                    contentDescription = "Reviewer",
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(image = Icons.Filled.AccountCircle)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(review.reviewerName, fontWeight = FontWeight.Bold)
                    Row {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (star <= review.rating) Color(0xFFFFC107) else Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(review.comment, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * --- NEW, SIMPLE ProductCard ADDED HERE ---
 * This version is for display purposes only and doesn't have edit/delete buttons.
 * It's used here in the UserProfileScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCard(product: ProductModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrls.firstOrNull(),
                contentDescription = product.productName,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val basePrice = product.priceTiers.minByOrNull { it.minQuantity }?.pricePerUnit
                if (basePrice != null) {
                    Text(text = "Starts at: $basePrice EGP", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}