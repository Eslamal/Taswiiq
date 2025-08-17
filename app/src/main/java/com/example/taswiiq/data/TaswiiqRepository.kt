// في ملف: TaswiiqRepository.kt
package com.example.taswiiq.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldPath

class TaswiiqRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun saveUserProfile(
        userId: String,
        email: String,
        firstName: String,
        lastName: String,
        companyName: String,
        phone: String?,
        category: String,
        commercialRecord: String?,
        mainProducts: List<String>,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            val profileData = mutableMapOf<String, Any?>(
                "uid" to userId,
                "email" to email,
                "firstName" to firstName,
                "lastName" to lastName,
                "companyName" to companyName,
                "phone" to phone,
                "category" to category,
                "commercialRecord" to commercialRecord,
                "mainProducts" to mainProducts,
                "profileImageUrl" to null
            )

            if (imageUri != null) {
                val imageUrl = storage.reference.child("profile_images/$userId.jpg")
                    .putFile(imageUri).await()
                    .storage.downloadUrl.await().toString()
                profileData["profileImageUrl"] = imageUrl
            }

            db.collection("users").document(userId).set(profileData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersByCategory(category: String): Result<List<UserModel>> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("category", category)
                .get()
                .await()

            val users = snapshot.toObjects(UserModel::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<UserModel?> {
        return try {
            val document = db.collection("users").document(userId).get().await()
            val user = document.toObject(UserModel::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update("fcmToken", token)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkConnection(currentUserId: String, profileUserId: String): Result<Boolean> {
        return try {
            val document = db.collection("connections").document(currentUserId).get().await()
            val connections = document.get("connected") as? List<*> ?: emptyList<String>()
            Result.success(profileUserId in connections)
        } catch (e: Exception) {
            Result.success(false)
        }
    }

    suspend fun connectWithUser(currentUserId: String, profileUserId: String): Result<Unit> {
        return try {
            val docRef = db.collection("connections").document(currentUserId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val existingConnections = (snapshot.get("connected") as? List<String>)?.toMutableList() ?: mutableListOf()
                if (!existingConnections.contains(profileUserId)) {
                    existingConnections.add(profileUserId)
                }
                transaction.set(docRef, mapOf("connected" to existingConnections))
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addProduct(
        supplierId: String,
        supplierName: String,
        productName: String,
        description: String,
        category: String,
        minimumOrderQuantity: Int,
        priceTiers: List<PriceTier>,
        imageUris: List<Uri>
    ): Result<Unit> {
        return try {
            val newProductRef = db.collection("products").document()
            val productId = newProductRef.id

            val imageUrls = mutableListOf<String>()
            imageUris.forEachIndexed { index, uri ->
                val imageUrl = storage.reference.child("product_images/$productId/image_$index.jpg")
                    .putFile(uri).await()
                    .storage.downloadUrl.await().toString()
                imageUrls.add(imageUrl)
            }

            val product = ProductModel(
                productId = productId,
                supplierId = supplierId,
                supplierName = supplierName,
                productName = productName,
                description = description,
                category = category,
                imageUrls = imageUrls,
                minimumOrderQuantity = minimumOrderQuantity,
                priceTiers = priceTiers,
                createdAt = com.google.firebase.Timestamp.now()
            )

            newProductRef.set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsForSupplier(supplierId: String): Result<List<ProductModel>> {
        return try {
            val snapshot = db.collection("products")
                .whereEqualTo("supplierId", supplierId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val products = snapshot.toObjects(ProductModel::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductDetails(productId: String): Result<ProductModel?> {
        return try {
            val document = db.collection("products").document(productId).get().await()
            val product = document.toObject(ProductModel::class.java)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatPartners(userId: String): Result<List<Pair<UserModel, Map<String, Any>>>> {
        return try {
            val chatsSnapshot = db.collection("chats")
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get().await()

            if (chatsSnapshot.isEmpty) return Result.success(emptyList())

            val visibleChats = chatsSnapshot.documents.filter { doc ->
                val deletedForList = doc.get("deletedFor") as? List<String> ?: emptyList()
                !deletedForList.contains(userId)
            }

            if (visibleChats.isEmpty()) return Result.success(emptyList())

            val otherParticipantIds = visibleChats.mapNotNull { chatDoc ->
                (chatDoc.get("participants") as? List<*>)?.firstOrNull { it != userId } as? String
            }

            if (otherParticipantIds.isEmpty()) return Result.success(emptyList())

            val usersSnapshot = db.collection("users").whereIn(FieldPath.documentId(), otherParticipantIds).get().await()
            val userModels = usersSnapshot.documents.mapNotNull { it.toObject(UserModel::class.java) }
                .associateBy { it.uid }

            val chatPartners = visibleChats.mapNotNull { chatDoc ->
                val lastMessage = chatDoc.getString("lastMessage") ?: ""
                val lastMessageTimestamp = chatDoc.getTimestamp("lastMessageTimestamp")?.toDate()
                val unreadCounts = chatDoc.get("unreadCounts") as? Map<String, Long>
                val unreadCount = unreadCounts?.get(userId) ?: 0L
                val otherId = (chatDoc.get("participants") as? List<*>)?.firstOrNull { it != userId } as? String

                userModels[otherId]?.let { user ->
                    val details = mapOf(
                        "lastMessage" to lastMessage,
                        "lastMessageTimestamp" to (lastMessageTimestamp ?: java.util.Date(0)),
                        "unreadCount" to unreadCount
                    )
                    Pair(user, details)
                }
            }
            Result.success(chatPartners)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        message: Message
    ): Result<Unit> {
        return try {
            val chatRef = db.collection("chats").document(chatId)
            val messageText = when (message.type) {
                "TEXT" -> message.content
                "IMAGE" -> "📷 Image"
                "AUDIO" -> "🎤 Audio Record"
                else -> "File Attachment"
            }

            chatRef.collection("messages").add(message).await()

            val chatUpdateData = mapOf(
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to messageText,
                "lastMessageTimestamp" to message.timestamp,
                "lastMessageSenderId" to senderId,
                "unreadCounts.$receiverId" to FieldValue.increment(1),
                "deletedFor" to FieldValue.arrayRemove(receiverId)
            )

            chatRef.set(chatUpdateData, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(fileUri: Uri, storagePath: String): Result<String> {
        return try {
            val fileRef = storage.reference.child(storagePath)
            val downloadUrl = fileRef.putFile(fileUri).await()
                .storage.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun placeOrder(order: OrderModel): Result<Unit> {
        return try {
            val newOrderRef = db.collection("orders").document()
            val finalOrder = order.copy(orderId = newOrderRef.id)
            newOrderRef.set(finalOrder).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrdersForBuyer(buyerId: String): Result<List<OrderModel>> {
        return try {
            val snapshot = db.collection("orders")
                .whereEqualTo("buyerId", buyerId)
                .orderBy("orderTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val orders = snapshot.toObjects(OrderModel::class.java)
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrdersForSupplier(supplierId: String): Result<List<OrderModel>> {
        return try {
            val snapshot = db.collection("orders")
                .whereEqualTo("supplierId", supplierId)
                .orderBy("orderTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val orders = snapshot.toObjects(OrderModel::class.java)
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            val updateData = mutableMapOf<String, Any>(
                "status" to newStatus
            )
            when (OrderStatus.valueOf(newStatus)) {
                OrderStatus.ACCEPTED -> updateData["acceptedTimestamp"] = com.google.firebase.Timestamp.now()
                OrderStatus.SHIPPED -> updateData["shippedTimestamp"] = com.google.firebase.Timestamp.now()
                OrderStatus.COMPLETED -> updateData["completedTimestamp"] = com.google.firebase.Timestamp.now()
                else -> {}
            }

            db.collection("orders").document(orderId).update(updateData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitReview(review: ReviewModel): Result<Unit> {
        return try {
            val newReviewRef = db.collection("reviews").document()
            val finalReview = review.copy(reviewId = newReviewRef.id)
            newReviewRef.set(finalReview).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewsForUser(userId: String): Result<List<ReviewModel>> {
        return try {
            val snapshot = db.collection("reviews")
                .whereEqualTo("targetUserId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val reviews = snapshot.toObjects(ReviewModel::class.java)
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NEW FUNCTIONS ADDED FOR EDIT/DELETE PRODUCT ---

    /**
     * Updates an existing product in Firestore.
     * We pass the entire ProductModel object to overwrite the existing one.
     */
    suspend fun updateProduct(product: ProductModel): Result<Unit> {
        return try {
            db.collection("products").document(product.productId)
                .set(product) // .set() will overwrite the document with the new data
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a product from Firestore.
     * Note: This doesn't delete the images from Storage to keep it simple.
     */
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            // TODO: Implement deletion of images from Firebase Storage later.
            // This requires listing all files under "product_images/$productId/" and deleting them.
            db.collection("products").document(productId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}