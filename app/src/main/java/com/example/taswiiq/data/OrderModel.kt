package com.example.taswiiq.data

import com.google.firebase.Timestamp

data class OrderModel(
    val orderId: String = "",
    val buyerId: String = "",       // ID التاجر (المشتري)
    val buyerName: String = "",
    val supplierId: String = "",    // ID المورد (البائع)
    val supplierName: String = "",

    val items: List<OrderItem> = emptyList(), // قائمة المنتجات المطلوبة
    val totalPrice: Double = 0.0,

    val status: String = OrderStatus.PENDING.name, // حالة الطلب الحالية

    val orderTimestamp: Timestamp = Timestamp.now(),
    val acceptedTimestamp: Timestamp? = null,
    val shippedTimestamp: Timestamp? = null,
    val completedTimestamp: Timestamp? = null
)