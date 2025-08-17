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
import androidx.compose.material.Checkbox
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
import androidx.compose.ui.res.stringResource
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
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(sharedPreferences.getBoolean("remember", false)) }
    val errorr=stringResource(id = R.string.login_failed)
    val nulll= stringResource(id = R.string.login_null)
    val emailpass_null=stringResource(id = R.string.emailpass_null)
    val signIn= stringResource(id = R.string.signIn)

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
                            sharedPreferences.edit().putBoolean("remember", true).apply()
                            checkUserProfileAndNavigate(context, navController)
                        } else {
                            message ="$errorr${task.exception?.localizedMessage}"
                        }
                    }
            } else {
                message = nulll
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
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
                    Text(text=stringResource(id = R.string.login), fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(text=stringResource(id = R.string.email)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(text=stringResource(id = R.string.pass)) },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(imageVector = icon, contentDescription = "Toggle Password Visibility")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically
                            , modifier = Modifier.padding(top = 20.dp)) {
                            Checkbox(checked = rememberMe, onCheckedChange = {
                                rememberMe = it
                                sharedPreferences.edit().putBoolean("remember", it).apply()
                            })
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text=stringResource(id = R.string.remember))
                        }
                        Text(
                            text =stringResource(id = R.string.forget),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                navController.navigate("forgot_password")
                            }
                        )
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                message = emailpass_null
                                return@Button
                            }
                            isLoading = true
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        if (rememberMe) {
                                            sharedPreferences.edit().putBoolean("remember", true).apply()
                                        }
                                        checkUserProfileAndNavigate(context, navController)
                                    } else {
                                        message = "❌ ${task.exception?.localizedMessage}"
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(text=stringResource(id = R.string.login))
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = stringResource(id = R.string.to_register),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { navController.navigate("register_screen") }
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(text=stringResource(id = R.string.or_login), fontSize = 16.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                oneTapClient
                                    .beginSignIn(signInRequest)
                                    .addOnSuccessListener { result ->
                                        googleSignInLauncher.launch(
                                            IntentSenderRequest
                                                .Builder(result.pendingIntent.intentSender)
                                                .build()
                                        )
                                    }
                                    .addOnFailureListener {
                                        message = "$signIn${it.localizedMessage}"
                                    }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Sign In",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
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

fun checkUserProfileAndNavigate(context: Context, navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    FirebaseFirestore.getInstance().collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                navController.navigate("main") {
                    popUpTo("login_screen") { inclusive = true }
                }
            } else {
                navController.navigate("profile") {
                    popUpTo("login_screen") { inclusive = true }
                }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error checking profile", Toast.LENGTH_SHORT).show()
        }
}
