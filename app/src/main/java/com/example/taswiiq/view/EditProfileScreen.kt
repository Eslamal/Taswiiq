import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.taswiiq.data.UserModel
import com.example.taswiiq.viewmodels.EditProfileUiState
import com.example.taswiiq.viewmodels.EditProfileViewModel
import com.example.taswiiq.viewmodels.SaveState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    editProfileViewModel: EditProfileViewModel = viewModel()
) {
    val uiState by editProfileViewModel.uiState.collectAsState()
    val saveState by editProfileViewModel.saveState.collectAsState()
    val context = LocalContext.current

    // --- ADDED: State and Scope for the Snackbar ---
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // --- ADDED: Effect to show Snackbar on error ---
    LaunchedEffect(uiState, saveState) {
        if (uiState is EditProfileUiState.Error) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = (uiState as EditProfileUiState.Error).message)
            }
        }
        if (saveState is SaveState.Error) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = (saveState as SaveState.Error).message)
            }
        }
    }

    // Effect for successful save (unchanged)
    LaunchedEffect(saveState) {
        if (saveState is SaveState.Saved) {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    Scaffold(
        // --- ADDED: snackbarHost parameter ---
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
            // --- MODIFIED: Removed the Text for the Error case ---
            when (val state = uiState) {
                is EditProfileUiState.Loading -> CircularProgressIndicator()
                is EditProfileUiState.Success -> {
                    EditProfileContent(
                        user = state.user,
                        saveState = saveState,
                        onSave = { firstName, lastName, companyName, phone, mainProducts, newImageUri ->
                            editProfileViewModel.saveProfile(firstName, lastName, companyName, phone, mainProducts, newImageUri)
                        }
                    )
                }
                is EditProfileUiState.Error -> {
                    // Body is empty on error, Snackbar will show the message
                }
            }
        }
    }
}

@Composable
fun EditProfileContent(
    user: UserModel,
    saveState: SaveState,
    onSave: (String, String, String, String, String, Uri?) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var mainProducts by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(user) {
        firstName = user.firstName
        lastName = user.lastName
        companyName = user.companyName
        phone = user.phone ?: ""
        mainProducts = user.mainProducts.joinToString(", ")
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable { launcher.launch("image/*") }
        ) {
            AsyncImage(
                model = imageUri ?: user.profileImageUrl,
                contentDescription = "Profile Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(image = Icons.Default.AccountCircle)
            )
        }
        Text("Click on image to change")

        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Company Name*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number (Optional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = mainProducts, onValueChange = { mainProducts = it }, label = { Text("Main Products (comma-separated)") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                onSave(firstName, lastName, companyName, phone, mainProducts, imageUri)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = saveState !is SaveState.Saving
        ) {
            if (saveState is SaveState.Saving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Save Changes", fontSize = 16.sp)
            }
        }
    }
}