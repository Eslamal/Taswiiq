package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.TaswiiqRepository
import com.example.taswiiq.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    private val repository = TaswiiqRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                repository.getUserProfile(userId).onSuccess { user ->
                    _currentUser.value = user
                }
            }
        }
    }
}