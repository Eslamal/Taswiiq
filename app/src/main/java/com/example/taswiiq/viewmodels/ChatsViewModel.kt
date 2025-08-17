package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.TaswiiqRepository
import com.example.taswiiq.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// حالة الواجهة: إما تحميل، أو نجاح مع قائمة المحادثات، أو خطأ
sealed class ChatsUiState {
    object Loading : ChatsUiState()
    data class Success(val chatPartners: List<Pair<UserModel, Map<String, Any>>>) : ChatsUiState()
    data class Error(val message: String) : ChatsUiState()
}

class ChatsViewModel : ViewModel() {

    private val repository = TaswiiqRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<ChatsUiState>(ChatsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadUserChats()
    }

    private fun loadUserChats() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = ChatsUiState.Error("User not logged in.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ChatsUiState.Loading
            repository.getChatPartners(currentUserId).onSuccess { chatList ->
                _uiState.value = ChatsUiState.Success(chatList)
            }.onFailure { exception ->
                _uiState.value = ChatsUiState.Error(exception.localizedMessage ?: "Failed to load chats.")
            }
        }
    }
}