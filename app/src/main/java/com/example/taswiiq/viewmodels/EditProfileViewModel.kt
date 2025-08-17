package com.example.taswiiq.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.TaswiiqRepository
import com.example.taswiiq.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// State to represent the UI of the Edit Profile screen
sealed class EditProfileUiState {
    object Loading : EditProfileUiState()
    data class Success(val user: UserModel) : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

// State for the save operation
sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Saved : SaveState()
    data class Error(val message: String) : SaveState()
}


class EditProfileViewModel : ViewModel() {
    private val repository = TaswiiqRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    init {
        loadCurrentUser()
    }

    // Fetches the current user's data to pre-fill the form
    fun loadCurrentUser() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = EditProfileUiState.Error("User not logged in.")
            return
        }
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            repository.getUserProfile(userId).onSuccess { user ->
                if (user != null) {
                    _uiState.value = EditProfileUiState.Success(user)
                } else {
                    _uiState.value = EditProfileUiState.Error("User profile not found.")
                }
            }.onFailure {
                _uiState.value = EditProfileUiState.Error(it.localizedMessage ?: "Failed to load data.")
            }
        }
    }

    // Saves the updated profile data
    // We can reuse the existing saveUserProfile function in the repository
    fun saveProfile(
        firstName: String,
        lastName: String,
        companyName: String,
        phone: String,
        mainProducts: String,
        newImageUri: Uri? // Uri for a new image, if selected
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _saveState.value = SaveState.Error("User not authenticated.")
            return
        }

        // Basic validation
        if (firstName.isBlank() || lastName.isBlank() || companyName.isBlank()) {
            _saveState.value = SaveState.Error("Names and Company Name cannot be empty.")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            // We need the user's existing category and email to pass to the repo function
            val existingUser = (uiState.value as? EditProfileUiState.Success)?.user
            if (existingUser == null) {
                _saveState.value = SaveState.Error("Could not retrieve existing user data.")
                return@launch
            }

            repository.saveUserProfile(
                userId = currentUser.uid,
                email = currentUser.email!!,
                firstName = firstName,
                lastName = lastName,
                companyName = companyName,
                phone = phone.ifEmpty { null },
                category = existingUser.category, // Category is not editable
                commercialRecord = existingUser.commercialRecord, // Commercial record is not editable
                mainProducts = mainProducts.split(",").map { it.trim() },
                imageUri = newImageUri
            ).onSuccess {
                _saveState.value = SaveState.Saved
            }.onFailure {
                _saveState.value = SaveState.Error(it.localizedMessage ?: "Failed to save profile.")
            }
        }
    }
}