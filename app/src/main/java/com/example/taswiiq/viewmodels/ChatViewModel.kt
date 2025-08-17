package com.example.taswiiq.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taswiiq.data.Message
import com.example.taswiiq.data.MessageType
import com.example.taswiiq.data.TaswiiqRepository
import com.example.taswiiq.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = TaswiiqRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _receiverProfile = MutableStateFlow<UserModel?>(null)
    val receiverProfile = _receiverProfile.asStateFlow()

    fun setupChat(receiverId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Fetch receiver's profile
        viewModelScope.launch {
            repository.getUserProfile(receiverId).onSuccess {
                _receiverProfile.value = it
            }
        }

        // --- EDITED HERE: Removed the illegal backslash ---
        val chatId = if (currentUserId < receiverId) "${currentUserId}_${receiverId}" else "${receiverId}_${currentUserId}"

        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _messages.value = it.toObjects(Message::class.java)
                }
            }
    }

    fun sendMessage(text: String, receiverId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // --- EDITED HERE: Removed the illegal backslash ---
        val chatId = if (currentUserId < receiverId) "${currentUserId}_${receiverId}" else "${receiverId}_${currentUserId}"

        val message = Message(senderId = currentUserId, content = text)

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                senderId = currentUserId,
                receiverId = receiverId,
                message = message
            )
        }
    }

    fun sendImageMessage(imageUri: Uri, receiverId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = if (currentUserId < receiverId) "${currentUserId}_${receiverId}" else "${receiverId}_${currentUserId}"

        viewModelScope.launch {
            // 1. Upload the image and get its URL
            val storagePath = "chat_images/$chatId/${System.currentTimeMillis()}_${imageUri.lastPathSegment}"
            repository.uploadFile(imageUri, storagePath).onSuccess { imageUrl ->
                // 2. Create a message object of type IMAGE with the URL as content
                val message = Message(
                    senderId = currentUserId,
                    type = MessageType.IMAGE.name,
                    content = imageUrl
                )
                // 3. Send the message object
                repository.sendMessage(
                    chatId = chatId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    message = message
                )
            }.onFailure {
                // Optionally handle upload failure, e.g., show an error
            }
        }
    }
}