package com.example.taswiiq.data // تأكد من أن الحزمة صحيحة

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class NotificationModel(
    var senderId: String? = null,
    var senderName: String? = null,
    var senderProfileImageUrl: String? = null,
    var receiverId: String? = null,
    var type: String? = null, // e.g., "new_order", "order_accepted", "new_message"
    var content: String? = null, // e.g., "Order #12345 has been accepted"
    var referenceId: String? = null, // e.g., orderId or chatId
    var isRead: Boolean = false,
    var timestamp: Timestamp? = Timestamp.now(),

    @get:Exclude @set:Exclude // To prevent this from being saved to Firestore
    var documentId: String? = null
)