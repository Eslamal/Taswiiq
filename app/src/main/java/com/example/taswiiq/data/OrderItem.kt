package com.example.taswiiq.data

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String? = null // صورة المنتج للعرض في ملخص الطلب
)