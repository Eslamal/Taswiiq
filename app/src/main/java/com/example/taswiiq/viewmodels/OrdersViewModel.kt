package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.OrderModel
import com.example.taswiiq.data.TaswiiqRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(val orders: List<OrderModel>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

class OrdersViewModel : ViewModel() {

    private val repository = TaswiiqRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchUserOrders()
    }

    private fun fetchUserOrders() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = OrdersUiState.Error("User not logged in.")
            return
        }

        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading
            // First, get the user's profile to know their category
            repository.getUserProfile(userId).onSuccess { user ->
                if (user != null) {
                    // Based on the category, call the correct function
                    val result = if (user.category == "مورد (تاجر جملة)") {
                        repository.getOrdersForSupplier(userId)
                    } else {
                        repository.getOrdersForBuyer(userId)
                    }

                    result.onSuccess { orders ->
                        _uiState.value = OrdersUiState.Success(orders)
                    }.onFailure {
                        _uiState.value = OrdersUiState.Error(it.localizedMessage ?: "Failed to load orders.")
                    }

                } else {
                    _uiState.value = OrdersUiState.Error("Could not find user profile.")
                }
            }.onFailure {
                _uiState.value = OrdersUiState.Error(it.localizedMessage ?: "Failed to load user profile.")
            }
        }
    }
}