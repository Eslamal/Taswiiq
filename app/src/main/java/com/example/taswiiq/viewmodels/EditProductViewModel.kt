package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.PriceTier
import com.example.taswiiq.data.ProductModel
import com.example.taswiiq.data.TaswiiqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI State for fetching the product to edit
sealed class EditProductUiState {
    object Loading : EditProductUiState()
    data class Success(val product: ProductModel) : EditProductUiState()
    data class Error(val message: String) : EditProductUiState()
}

// UI State for the save operation
sealed class UpdateProductState {
    object Idle : UpdateProductState()
    object Saving : UpdateProductState()
    object Saved : UpdateProductState()
    data class Error(val message: String) : UpdateProductState()
}

class EditProductViewModel : ViewModel() {
    private val repository = TaswiiqRepository()

    private val _uiState = MutableStateFlow<EditProductUiState>(EditProductUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateProductState>(UpdateProductState.Idle)
    val updateState = _updateState.asStateFlow()

    // Fetches the product details to pre-fill the edit form
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = EditProductUiState.Loading
            repository.getProductDetails(productId).onSuccess { product ->
                if (product != null) {
                    _uiState.value = EditProductUiState.Success(product)
                } else {
                    _uiState.value = EditProductUiState.Error("Product not found.")
                }
            }.onFailure {
                _uiState.value = EditProductUiState.Error(it.localizedMessage ?: "Failed to load product.")
            }
        }
    }

    // Saves the updated product data
    fun saveChanges(
        productName: String,
        description: String,
        minimumOrderQuantity: Int,
        priceTiers: List<PriceTier>
    ) {
        val currentState = _uiState.value
        if (currentState !is EditProductUiState.Success) {
            _updateState.value = UpdateProductState.Error("Cannot save, original product data not loaded.")
            return
        }

        // Create the updated product model, keeping non-editable fields
        val updatedProduct = currentState.product.copy(
            productName = productName,
            description = description,
            minimumOrderQuantity = minimumOrderQuantity,
            priceTiers = priceTiers
        )

        viewModelScope.launch {
            _updateState.value = UpdateProductState.Saving
            repository.updateProduct(updatedProduct).onSuccess {
                _updateState.value = UpdateProductState.Saved
            }.onFailure {
                _updateState.value = UpdateProductState.Error(it.localizedMessage ?: "Failed to save changes.")
            }
        }
    }
}