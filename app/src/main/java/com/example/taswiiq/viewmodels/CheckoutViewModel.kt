package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.OrderModel
import com.example.taswiiq.data.TaswiiqRepository
import com.example.taswiiq.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Loading : CheckoutState()
    object Success : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

class CheckoutViewModel : ViewModel() {

    private val repository = TaswiiqRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState = _checkoutState.asStateFlow()

    fun placeOrder(cartState: CartState, buyer: UserModel) {
        val supplierId = cartState.supplierId
        if (supplierId == null || cartState.items.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("Cart is empty or supplier not found.")
            return
        }

        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading
            // We need the supplier's name for the order model
            repository.getUserProfile(supplierId).onSuccess { supplier ->
                if (supplier != null) {
                    val order = OrderModel(
                        buyerId = buyer.uid,
                        buyerName = "${buyer.firstName} ${buyer.lastName}",
                        supplierId = supplierId,
                        supplierName = supplier.companyName.ifBlank { "${supplier.firstName} ${supplier.lastName}" },
                        items = cartState.items,
                        totalPrice = cartState.totalPrice
                    )
                    repository.placeOrder(order).onSuccess {
                        _checkoutState.value = CheckoutState.Success
                    }.onFailure {
                        _checkoutState.value = CheckoutState.Error(it.localizedMessage ?: "Failed to place order.")
                    }
                } else {
                    _checkoutState.value = CheckoutState.Error("Supplier details could not be found.")
                }
            }
        }
    }
}