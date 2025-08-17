package com.example.taswiiq.viewmodels

import androidx.lifecycle.ViewModel
import com.example.taswiiq.data.OrderItem
import com.example.taswiiq.data.PriceTier
import com.example.taswiiq.data.ProductModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartState(
    val items: List<OrderItem> = emptyList(),
    val supplierId: String? = null,
    val totalPrice: Double = 0.0
)

class CartViewModel : ViewModel() {

    private val _cartState = MutableStateFlow(CartState())
    val cartState = _cartState.asStateFlow()

    /**
     * --- HELPER FUNCTION ADDED ---
     * Finds the correct price per unit for a given quantity from the price tiers.
     */
    private fun findPriceForQuantity(tiers: List<PriceTier>, quantity: Int): Double {
        // Sort tiers by quantity in descending order to find the best match first
        val sortedTiers = tiers.sortedByDescending { it.minQuantity }
        // Find the first tier where the required quantity is met
        val applicableTier = sortedTiers.firstOrNull { quantity >= it.minQuantity }
        // Return the price of that tier, or 0.0 if no tier is applicable (should not happen with valid data)
        return applicableTier?.pricePerUnit ?: 0.0
    }

    fun addToCart(product: ProductModel, quantity: Int) {
        if (_cartState.value.supplierId != null && _cartState.value.supplierId != product.supplierId) {
            clearCart()
        }

        // --- MODIFIED HERE: Calculate price before creating the OrderItem ---
        val pricePerUnit = findPriceForQuantity(product.priceTiers, quantity)
        if (pricePerUnit <= 0) {
            // Handle error, e.g., product has no valid pricing.
            return
        }

        _cartState.update { currentState ->
            val newItems = currentState.items.toMutableList()
            val existingItem = newItems.find { it.productId == product.productId }

            if (existingItem != null) {
                val newQuantity = existingItem.quantity + quantity
                // Recalculate price for the new total quantity
                val newPricePerUnit = findPriceForQuantity(product.priceTiers, newQuantity)
                val updatedItem = existingItem.copy(
                    quantity = newQuantity,
                    price = newPricePerUnit // Update the price based on the new quantity
                )
                val itemIndex = newItems.indexOf(existingItem)
                newItems[itemIndex] = updatedItem
            } else {
                newItems.add(
                    OrderItem(
                        productId = product.productId,
                        productName = product.productName,
                        price = pricePerUnit, // Use the calculated price
                        quantity = quantity,
                        imageUrl = product.imageUrls.firstOrNull()
                    )
                )
            }

            val newTotalPrice = newItems.sumOf { it.price * it.quantity }
            currentState.copy(
                items = newItems,
                supplierId = product.supplierId,
                totalPrice = newTotalPrice
            )
        }
    }

    fun clearCart() {
        _cartState.value = CartState()
    }
}