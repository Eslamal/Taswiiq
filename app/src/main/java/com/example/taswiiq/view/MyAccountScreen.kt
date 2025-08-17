import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.taswiiq.data.UserModel
import com.example.taswiiq.viewmodels.MyAccountViewModel
import com.example.taswiiq.viewmodels.UserProfileUiState

@Composable
fun MyAccountScreen(
    navController: NavController,
    myAccountViewModel: MyAccountViewModel = viewModel()
) {
    val uiState by myAccountViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is UserProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UserProfileUiState.Success -> {
            AccountDashboard(user = state.user, navController = navController)
        }
        is UserProfileUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message)
            }
        }
    }
}

@Composable
fun AccountDashboard(user: UserModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Info Header
        val displayName = user.companyName.ifBlank { "${user.firstName} ${user.lastName}" }
        Image(
            painter = rememberAsyncImagePainter(model = user.profileImageUrl),
            contentDescription = "Profile Picture",
            modifier = Modifier.size(100.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
        Text(text = "النوع: ${user.category}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))
        Divider()

        // Action Buttons
        Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardButton(
                text = "تعديل الملف الشخصي",
                icon = Icons.Default.Edit,
                onClick = {
                    navController.navigate("edit_profile") // <-- قم بتغيير هذا السطر
                }
            )

            // Supplier-specific buttons
            if (user.category == "مورد (تاجر جملة)") {
                DashboardButton(
                    text = "إضافة منتج جديد",
                    icon = Icons.Default.AddBusiness,
                    onClick = {
                        val supplierName = user.companyName.ifBlank { "${user.firstName} ${user.lastName}" }
                        navController.navigate("add_product/$supplierName")
                    }
                )
                DashboardButton(
                    text = "إدارة منتجاتي",
                    icon = Icons.Default.Inventory,
                    onClick = {
                        // --- EDITED HERE ---
                        // Navigate to the ManageProductsScreen
                        navController.navigate("manage_products")
                    }
                )
                DashboardButton(
                    text = "الإعدادات",
                    icon = Icons.Default.Settings,
                    onClick = {
                        navController.navigate("settings") // <-- قم بتغيير هذا السطر
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(imageVector = icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}