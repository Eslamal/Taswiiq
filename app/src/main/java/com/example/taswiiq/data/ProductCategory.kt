package com.example.taswiiq.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.taswiiq.R // تأكد من استيراد R بشكل صحيح

// هذا الكلاس يمثل فئة منتج واحدة
data class ProductCategory(
    val nameAr: String,
    val nameEn: String,
    val icon: ImageVector
)