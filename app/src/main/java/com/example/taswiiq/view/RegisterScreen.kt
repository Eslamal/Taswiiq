import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.taswiiq.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val oneTapClient = remember { Identity.getSignInClient(context) }
    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("467959471475-ru8bvpdruc0rq22apgnqeh5q7hr5keg9.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).setAutoSelectEnabled(false)
            .build()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navController.navigate("profile")
                        } else {
                            message = "❌ Google Sign-In failed: ${task.exception?.localizedMessage}"
                        }
                    }
            } else {
                message = "❌ Google ID token is null"
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize() .background(
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
                    Text("Register", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(imageVector = icon, contentDescription = "Toggle Password Visibility")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(imageVector = icon, contentDescription = "Toggle Confirm Password Visibility")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                                    message = "❗ All fields are required"
                                }
                                password != confirmPassword -> {
                                    message = "❗ Passwords do not match"
                                }
                                else -> {
                                    auth.createUserWithEmailAndPassword(email.trim(), password)
                                        .addOnCompleteListener { task ->
                                            message = if (task.isSuccessful) {
                                                navController.navigate("profile")
                                                "✅ Registered Successfully"
                                            } else {
                                                "❌ ${task.exception?.localizedMessage}"
                                            }
                                        }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Register")
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Already have an account? Login",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { navController.navigate("login_screen") }
                    )

                    Spacer(Modifier.height(24.dp))
                    Text("Or register with", fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                oneTapClient.beginSignIn(signInRequest)
                                    .addOnSuccessListener { result ->
                                        googleSignInLauncher.launch(
                                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                                        )
                                    }
                                    .addOnFailureListener {
                                        message = "❌ Google Sign-In: ${it.localizedMessage}"
                                    }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Sign In",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    if (message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
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
