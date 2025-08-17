package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.ProductModel
import com.example.taswiiq.data.ReviewModel
import com.example.taswiiq.data.TaswiiqRepository
import com.example.taswiiq.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class MyAccountViewModel : ViewModel() {
    private val repository = TaswiiqRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    fun loadCurrentUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = UserProfileUiState.Error("User not logged in.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading

            // --- MODIFIED HERE: Fetch user, products, and reviews in parallel ---
            val userResult = async { repository.getUserProfile(userId) }
            val productsResult = async { repository.getProductsForSupplier(userId) }
            // Although we may not display reviews on "My Account" screen,
            // the UI state requires it, so we fetch it.
            val reviewsResult = async { repository.getReviewsForUser(userId) }

            val user = userResult.await().getOrNull()

            if (user != null) {
                val products = productsResult.await().getOrDefault(emptyList())
                val reviews = reviewsResult.await().getOrDefault(emptyList())

                // Pass the new 'reviews' list to the Success state constructor
                _uiState.value = UserProfileUiState.Success(
                    user = user,
                    products = products,
                    reviews = reviews,
                    isConnected = false // 'isConnected' is not relevant for the user's own account screen
                )

            } else {
                _uiState.value = UserProfileUiState.Error("User profile not found.")
            }
        }
    }
}