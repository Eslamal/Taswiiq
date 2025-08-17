@file:OptIn(ExperimentalMaterial3Api::class)

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.taswiiq.viewmodels.ProfileViewModel
import com.example.taswiiq.viewmodels.SaveProfileState

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var commercialRecord by remember { mutableStateOf("") }
    var mainProducts by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // --- EDITED HERE ---
    val categories = listOf("مورد (تاجر جملة)", "صاحب محل (تاجر قطاعي)")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    val saveState by profileViewModel.saveState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveProfileState.Success -> {
                Toast.makeText(context, "تم حفظ الملف الشخصي بنجاح!", Toast.LENGTH_SHORT).show()
                navController.navigate("main") {
                    popUpTo("profile") { inclusive = true }
                    launchSingleTop = true
                }
            }
            is SaveProfileState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).clickable { launcher.launch("image/*") }
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = "Add Photo", modifier = Modifier.size(70.dp))
            }
        }
        Text("أضف شعار الشركة أو صورة شخصية")

        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("الاسم الأول*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("اسم العائلة*") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("اسم الشركة*") }, modifier = Modifier.fillMaxWidth())

        // --- EDITED HERE ---
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف (اختياري)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = commercialRecord, onValueChange = { commercialRecord = it }, label = { Text("رقم السجل التجاري (اختياري)") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = mainProducts, onValueChange = { mainProducts = it }, label = { Text("المنتجات الأساسية (بينها فاصلة)*") }, modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("حدد نوع حسابك*") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                profileViewModel.saveProfile(
                    firstName = firstName,
                    lastName = lastName,
                    companyName = companyName,
                    phone = phone,
                    category = selectedCategory,
                    commercialRecord = commercialRecord,
                    mainProducts = mainProducts,
                    imageUri = imageUri
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = saveState !is SaveProfileState.Loading
        ) {
            Text(if (saveState is SaveProfileState.Loading) "جاري الحفظ..." else "حفظ وإنشاء الحساب", fontSize = 16.sp)
        }
    }
}