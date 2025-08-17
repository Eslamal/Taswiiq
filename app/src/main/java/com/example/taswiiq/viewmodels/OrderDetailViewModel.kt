package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.OrderModel
import com.example.taswiiq.data.OrderStatus
import com.example.taswiiq.data.ReviewModel
import com.example.taswiiq.data.TaswiiqRepository
import com.example.taswiiq.data.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// This sealed class represents the state of the OrderDetailScreen UI
sealed class OrderDetailUiState {
    object Loading : OrderDetailUiState()
    data class Success(val order: OrderModel) : OrderDetailUiState()
    data class Error(val message: String) : OrderDetailUiState()
}

class OrderDetailViewModel : ViewModel() {

    private val repository = TaswiiqRepository()

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun fetchOrderDetails(orderId: String) {
        // This is a placeholder for a function you might need later.
        // For now, we will pass the OrderModel object directly to the screen.
    }

    fun updateStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            // Here you can set a loading state for the button if you want
            repository.updateOrderStatus(orderId, newStatus.name).onSuccess {
                // The UI will handle navigation, no need for action here
            }.onFailure { exception ->
                // Handle the error, maybe show a toast
            }
        }
    }

    // --- NEW FUNCTION ADDED FOR SUBMITTING REVIEWS ---
    fun submitReview(order: OrderModel, reviewer: UserModel, rating: Float, comment: String) {
        viewModelScope.launch {
            val review = ReviewModel(
                orderId = order.orderId,
                targetUserId = order.supplierId,
                reviewerId = reviewer.uid,
                reviewerName = reviewer.companyName.ifBlank { "${reviewer.firstName} ${reviewer.lastName}" },
                reviewerImageUrl = reviewer.profileImageUrl,
                rating = rating,
                comment = comment
            )
            repository.submitReview(review).onSuccess {
                // Review submitted successfully.
                // You could update a state here to show a "Thank you" message to the user.
            }.onFailure {
                // Handle the error, for example, by showing a toast message.
            }
        }
    }
}