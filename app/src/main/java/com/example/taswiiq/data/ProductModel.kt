package com.example.taswiiq.data

import com.google.firebase.Timestamp

data class ProductModel(
    val productId: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val productName: String = "",
    val description: String = "",
    // val price: Double = 0.0, // <-- THIS FIELD IS REMOVED
    val imageUrls: List<String> = emptyList(),
    val category: String = "",
    val minimumOrderQuantity: Int = 1,

    // --- NEW FIELD ADDED HERE ---
    val priceTiers: List<PriceTier> = emptyList(),

    val createdAt: Timestamp = Timestamp.now()
)