import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize() .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Forgot Password", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Enter your email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                message = "❗ Email cannot be empty"
                                return@Button
                            }

                            isLoading = true
                            firestore.collection("users")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (!documents.isEmpty) {
                                        auth.sendPasswordResetEmail(email)
                                            .addOnSuccessListener {
                                                message = "✅ Reset link sent. Check your inbox."
                                                navController.navigate("login_screen") {
                                                    popUpTo("forgot_password") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener {
                                                message = "❌ Failed to send reset email"
                                            }
                                    } else {
                                        message = "❌ Email not found"
                                    }
                                    isLoading = false
                                }
                                .addOnFailureListener { e ->
                                    message = "❌ Error checking email: ${e.localizedMessage}"
                                    isLoading = false
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(if (isLoading) "Sending..." else "Send Reset Email")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Remember your password? Login",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { navController.navigate("login_screen") }
                    )

                    if (message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = message,
                            color = if (message.startsWith("✅")) Color(0xFF4CAF50) else Color.Red
                        )
                    }
                }
            }
        }
    }
}
