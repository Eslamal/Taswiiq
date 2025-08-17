package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.ProductModel
import com.example.taswiiq.data.TaswiiqRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// حالة الواجهة: تحميل، نجاح مع قائمة المنتجات، أو خطأ
sealed class ManageProductsUiState {
    object Loading : ManageProductsUiState()
    data class Success(val products: List<ProductModel>) : ManageProductsUiState()
    data class Error(val message: String) : ManageProductsUiState()
}

class ManageProductsViewModel : ViewModel() {

    private val repository = TaswiiqRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<ManageProductsUiState>(ManageProductsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        // I've renamed this function for clarity
        loadProductsForCurrentUser()
    }

    // Renamed for clarity
    private fun loadProductsForCurrentUser() {
        val supplierId = auth.currentUser?.uid
        if (supplierId == null) {
            _uiState.value = ManageProductsUiState.Error("Supplier not logged in.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ManageProductsUiState.Loading
            repository.getProductsForSupplier(supplierId).onSuccess { products ->
                _uiState.value = ManageProductsUiState.Success(products)
            }.onFailure {
                _uiState.value = ManageProductsUiState.Error(it.localizedMessage ?: "Failed to load products.")
            }
        }
    }

    /**
     * --- NEW FUNCTION ADDED ---
     * Deletes a product and then refreshes the product list.
     */
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId).onSuccess {
                // After successful deletion, refresh the list of products.
                // A better user experience might be to remove the item locally from the list
                // for an instant update, but this is simpler and effective.
                loadProductsForCurrentUser()
            }.onFailure {
                // Optionally, you can create a new state to show a deletion error message
                _uiState.value = ManageProductsUiState.Error(it.localizedMessage ?: "Failed to delete product.")
            }
        }
    }
}