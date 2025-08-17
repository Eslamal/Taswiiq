import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.taswiiq.data.UserModel
import com.example.taswiiq.viewmodels.SuppliersUiState
import com.example.taswiiq.viewmodels.SuppliersViewModel

@Composable
fun SuppliersListScreen(
    categoryName: String,
    navController: NavController, // <-- EDITED: Added NavController
    suppliersViewModel: SuppliersViewModel = viewModel()
) {
    // This part remains the same
    LaunchedEffect(key1 = categoryName) {
        suppliersViewModel.fetchSuppliers(categoryName)
    }
    val uiState by suppliersViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is SuppliersUiState.Loading -> {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
        }
        is SuppliersUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.users) { user ->
                    // --- EDITED HERE ---
                    // The navigation logic is now passed to the SupplierCard
                    SupplierCard(user = user, onClick = {
                        navController.navigate("userProfile/${user.uid}")
                    })
                }
            }
        }
        is SuppliersUiState.Error -> {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = state.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierCard(
    user: UserModel,
    onClick: () -> Unit // <-- EDITED: Added onClick lambda
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick // <-- EDITED: Used the onClick lambda here
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profileImageUrl,
                contentDescription = "Supplier Image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = user.companyName.ifBlank { "${user.firstName} ${user.lastName}" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Supplier", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
            }
        }
    }
}