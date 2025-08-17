package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.TaswiiqRepository
import com.example.taswiiq.data.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// حالة الواجهة: إما تحميل، أو نجاح مع قائمة المستخدمين، أو خطأ
sealed class SuppliersUiState {
    object Loading : SuppliersUiState()
    data class Success(val users: List<UserModel>) : SuppliersUiState()
    data class Error(val message: String) : SuppliersUiState()
}

class SuppliersViewModel : ViewModel() {

    private val repository = TaswiiqRepository()

    private val _uiState = MutableStateFlow<SuppliersUiState>(SuppliersUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun fetchSuppliers(categoryName: String) {
        viewModelScope.launch {
            _uiState.value = SuppliersUiState.Loading
            val result = repository.getUsersByCategory(categoryName)
            result.onSuccess { users ->
                _uiState.value = SuppliersUiState.Success(users)
            }.onFailure { exception ->
                _uiState.value = SuppliersUiState.Error(exception.localizedMessage ?: "Error fetching suppliers")
            }
        }
    }
}