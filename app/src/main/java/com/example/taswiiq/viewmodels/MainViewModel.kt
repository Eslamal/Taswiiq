package com.example.taswiiq.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import com.example.taswiiq.data.ProductCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    private val _categories = MutableStateFlow<List<ProductCategory>>(emptyList())
    val categories = _categories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        _categories.value = listOf(
            ProductCategory("مواد غذائية", "Groceries", Icons.Default.Fastfood),
            ProductCategory("ملابس", "Apparel", Icons.Default.Checkroom),
            ProductCategory("إلكترونيات", "Electronics", Icons.Default.ElectricalServices),
            ProductCategory("أدوات منزلية", "Home Goods", Icons.Default.HomeWork),
            ProductCategory("مستحضرات تجميل", "Cosmetics", Icons.Default.Face),
            ProductCategory("ألعاب أطفال", "Toys", Icons.Default.Toys),
            ProductCategory("أدوات مكتبية", "Stationery", Icons.Default.Edit),
            ProductCategory("أخرى", "Other", Icons.Default.Category)
        )
    }
}