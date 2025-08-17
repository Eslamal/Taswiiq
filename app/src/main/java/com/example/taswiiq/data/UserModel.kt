// In UserModel.kt
package com.example.taswiiq.data

data class UserModel(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val companyName: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val mainProducts: List<String> = emptyList(),
    val phone: String? = null,
    val category: String = "",
    val commercialRecord: String? = null,
    val fcmToken: String? = null // <-- أضف هذا السطر
)