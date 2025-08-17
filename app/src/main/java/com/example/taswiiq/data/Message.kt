package com.example.taswiiq.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val type: String = MessageType.TEXT.name, // The type of the message (TEXT, IMAGE, etc.)
    val content: String = "", // For TEXT messages, this holds the text. For others, the download URL.
    val timestamp: Timestamp = Timestamp.now(),
    val read: Boolean = false
)