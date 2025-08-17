// In AddProductViewModel.kt

package com.example.taswiiq.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.PriceTier
import com.example.taswiiq.data.TaswiiqRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// حالة واجهة إضافة المنتج (No changes here)
sealed class AddProductState {
    object Idle : AddProductState()
    object Loading : AddProductState()
    object Success : AddProductState()
    data class Error(val message: String) : AddProductState()
}

class AddProductViewModel : ViewModel() {
    private val repository = TaswiiqRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _addProductState = MutableStateFlow<AddProductState>(AddProductState.Idle)
    val addProductState = _addProductState.asStateFlow()

    /**
     * --- MODIFIED FUNCTION ---
     * Now accepts a list of PriceTiers and performs validation.
     */
    fun createProduct(
        productName: String,
        description: String,
        category: String,
        minimumOrderQuantity: Int,
        priceTiers: List<PriceTier>, // MODIFIED HERE
        imageUris: List<Uri>,
        supplierName: String
    ) {
        val supplierId = auth.currentUser?.uid
        if (supplierId == null) {
            _addProductState.value = AddProductState.Error("Supplier not logged in")
            return
        }
        // Basic Validations
        if (productName.isBlank() || description.isBlank() || imageUris.isEmpty()) {
            _addProductState.value = AddProductState.Error("Please fill product name, description, and select at least one image.")
            return
        }
        if (minimumOrderQuantity < 1) {
            _addProductState.value = AddProductState.Error("Minimum quantity must be 1 or higher.")
            return
        }
        // Price Tiers Validation
        if (priceTiers.isEmpty() || priceTiers.any { it.minQuantity <= 0 || it.pricePerUnit <= 0 }) {
            _addProductState.value = AddProductState.Error("Please define at least one valid price tier with quantity and price greater than 0.")
            return
        }


        viewModelScope.launch {
            _addProductState.value = AddProductState.Loading
            repository.addProduct(
                supplierId = supplierId,
                supplierName = supplierName,
                productName = productName,
                description = description,
                category = category,
                minimumOrderQuantity = minimumOrderQuantity,
                priceTiers = priceTiers, // MODIFIED HERE
                imageUris = imageUris
            ).onSuccess {
                _addProductState.value = AddProductState.Success
            }.onFailure {
                _addProductState.value = AddProductState.Error(it.localizedMessage ?: "Unknown error")
            }
        }
    }
}