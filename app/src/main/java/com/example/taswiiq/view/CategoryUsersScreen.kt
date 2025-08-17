import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.taswiiq.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CategoryUsersScreen(navController: NavController, category: String) {
    var searchQuery by remember { mutableStateOf("") }
    var filteredUsers by remember { mutableStateOf(emptyList<UserModel>()) }

    LaunchedEffect(category) {
        fetchUsersByCategory(category) { users ->
            filteredUsers = users
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Users in $category") })
        },
        content = { padding ->
            Column(modifier = Modifier
                .padding(padding)
                .padding(16.dp)) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search users...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(filteredUsers.filter {
                        searchQuery.isBlank() ||
                                it.firstName.contains(searchQuery, ignoreCase = true) ||
                                it.lastName.contains(searchQuery, ignoreCase = true)
                    }) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    navController.navigate("userProfile/${user.uid}")
                                },
                            elevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    // Use companyName if available, otherwise first and last name
                                    val displayName = if (user.companyName.isNotBlank()) {
                                        user.companyName
                                    } else {
                                        "${user.firstName} ${user.lastName}"
                                    }
                                    Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }

                                if (!user.profileImageUrl.isNullOrEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(user.profileImageUrl),
                                        contentDescription = "Profile Image",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Default Icon",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    )
}

fun fetchUsersByCategory(category: String, onResult: (List<UserModel>) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    firestore.collection("users")
        .whereEqualTo("category", category)
        .get()
        .addOnSuccessListener { result ->
            val users = result.documents.mapNotNull { doc ->
                val uid = doc.id
                if (uid == currentUid) return@mapNotNull null

                // --- EDITED HERE ---
                // Replaced the old fromDocument() with the modern .toObject() method
                doc.toObject(UserModel::class.java)
            }
            onResult(users)
        }
        .addOnFailureListener {
            Log.e("Firestore", "Failed to fetch users: ${it.message}")
        }
}