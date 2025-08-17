package com.example.taswiiq.data

import com.google.firebase.Timestamp

data class ReviewModel(
    val reviewId: String = "",
    val orderId: String = "",           // To link the review to a specific order
    val targetUserId: String = "",      // ID of the user being reviewed (the supplier)
    val reviewerId: String = "",        // ID of the user who wrote the review (the buyer)
    val reviewerName: String = "",      // Name of the buyer
    val reviewerImageUrl: String? = null, // Profile image of the buyer
    val rating: Float = 0.0f,           // The star rating, e.g., 4.5
    val comment: String = "",           // The review text
    val timestamp: Timestamp = Timestamp.now()
)