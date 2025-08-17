package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.ProductModel
import com.example.taswiiq.data.TaswiiqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductDetailUiState {
    object Loading : ProductDetailUiState()
    data class Success(val product: ProductModel) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}

class ProductDetailViewModel : ViewModel() {

    private val repository = TaswiiqRepository()

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun fetchProductDetails(productId: String) {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading
            repository.getProductDetails(productId).onSuccess { product ->
                if (product != null) {
                    _uiState.value = ProductDetailUiState.Success(product)
                } else {
                    _uiState.value = ProductDetailUiState.Error("Product not found.")
                }
            }.onFailure {
                _uiState.value = ProductDetailUiState.Error(it.localizedMessage ?: "Failed to load product details.")
            }
        }
    }
}