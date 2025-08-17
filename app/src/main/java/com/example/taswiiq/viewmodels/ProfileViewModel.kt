// في ملف: ProfileViewModel.kt
package com.example.taswiiq.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.TaswiiqRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SaveProfileState {
    object Idle : SaveProfileState()
    object Loading : SaveProfileState()
    object Success : SaveProfileState()
    data class Error(val message: String) : SaveProfileState()
}

class ProfileViewModel : ViewModel() {

    private val repository = TaswiiqRepository()

    private val _saveState = MutableStateFlow<SaveProfileState>(SaveProfileState.Idle)
    val saveState = _saveState.asStateFlow()

    // In ProfileViewModel.kt

    fun saveProfile(
        firstName: String,
        lastName: String,
        companyName: String,
        phone: String, // UI will pass an empty string
        category: String,
        commercialRecord: String, // UI will pass an empty string
        mainProducts: String,
        imageUri: Uri?
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _saveState.value = SaveProfileState.Error("المستخدم غير مسجل.")
            return
        }

        // Updated validation: We only check for the essential fields now
        if (firstName.isBlank() || lastName.isBlank() || companyName.isBlank() || mainProducts.isBlank()) {
            _saveState.value = SaveProfileState.Error("يرجى ملء الحقول المطلوبة (الأسماء، اسم الشركة، والمنتجات).")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveProfileState.Loading
            val productsList = mainProducts.split(",").map { it.trim() }

            val result = repository.saveUserProfile(
                userId = currentUser.uid,
                email = currentUser.email!!,
                firstName = firstName,
                lastName = lastName,
                companyName = companyName,
                phone = phone.ifEmpty { null }, // Pass null if the string is empty
                category = category,
                commercialRecord = commercialRecord.ifEmpty { null }, // Pass null if the string is empty
                mainProducts = productsList,
                imageUri = imageUri
            )

            result.onSuccess {
                _saveState.value = SaveProfileState.Success
            }.onFailure { exception ->
                _saveState.value = SaveProfileState.Error(exception.localizedMessage ?: "حدث خطأ غير معروف")
            }
        }
    }
}