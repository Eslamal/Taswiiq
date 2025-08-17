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


class UserProfileViewModel : ViewModel() {

    private val repository = TaswiiqRepository()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadUserProfile(profileUserId: String) {
        if (currentUserId == null) {
            _uiState.value = UserProfileUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading

            // --- MODIFIED HERE: Fetch user, products, connection, and reviews in parallel ---
            val userResult = async { repository.getUserProfile(profileUserId) }
            val productsResult = async { repository.getProductsForSupplier(profileUserId) }
            val connectionResult = async { repository.checkConnection(currentUserId, profileUserId) }
            val reviewsResult = async { repository.getReviewsForUser(profileUserId) }

            // Await all results
            val user = userResult.await().getOrNull()

            if (user != null) {
                // Get other results, providing a default empty list if they fail
                val products = productsResult.await().getOrDefault(emptyList())
                val isConnected = connectionResult.await().getOrDefault(false)
                val reviews = reviewsResult.await().getOrDefault(emptyList())

                // Update the UI state with all the fetched data
                // REMINDER: Make sure UserProfileUiState.Success accepts the 'reviews' parameter
                _uiState.value = UserProfileUiState.Success(
                    user = user,
                    products = products,
                    reviews = reviews,
                    isConnected = isConnected
                )
            } else {
                _uiState.value = UserProfileUiState.Error("User not found")
            }
        }
    }

    fun connect(profileUserId: String) {
        if (currentUserId == null) return
        viewModelScope.launch {
            repository.connectWithUser(currentUserId, profileUserId).onSuccess {
                loadUserProfile(profileUserId) // Reload profile to update the button state
            }
        }
    }
}