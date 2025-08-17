package com.example.taswiiq.viewmodels

import com.example.taswiiq.data.ProductModel
import com.example.taswiiq.data.ReviewModel
import com.example.taswiiq.data.UserModel

// This is the single, central definition for this UI state.
sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    data class Success(
        val user: UserModel,
        val products: List<ProductModel>,
        val reviews: List<ReviewModel>, // <-- MODIFIED HERE
        val isConnected: Boolean
    ) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
}