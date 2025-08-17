# Taswiiq - B2B Marketplace Android App

A full-stack Android B2B marketplace application designed to streamline communication and business transactions between wholesale suppliers and retailers. The app is built entirely with modern, Google-recommended technologies.

## ✨ Key Features

- ✅ **Full Authentication System:** User registration, login (Email & Google), and password reset.
- ✅ **Multi-Role Profiles:**
    - **Suppliers:** Can add, edit, and delete products, and manage their inventory.
    - **Retailers:** Can browse suppliers and products by category.
- ✅ **Tiered Pricing System:** Implements wholesale pricing that varies based on order quantity.
- ✅ **Order Management:** A complete lifecycle for creating new orders and tracking their status (Pending, Accepted, Shipped, Completed).
- ✅ **Real-time Chat:** Direct messaging between suppliers and retailers, powered by Cloud Firestore.
- ✅ **Push Notifications:** Instant notifications for new messages and order status updates using Firebase Cloud Messaging (FCM).
- ✅ **Review & Rating System:** Allows retailers to rate and review suppliers after an order is completed.
- ✅ **Multi-Language Support:** Full support for both English and Arabic.
- ✅ **Dark Mode Support.**

## 🛠️ Tech Stack & Architecture

- **Language:** 100% **Kotlin**.
- **UI:** **Jetpack Compose** for building a modern, declarative, and reactive user interface.
- **Architecture:** **MVVM (Model-View-ViewModel)** for a clean separation of concerns.
- **Asynchronous Programming:** **Coroutines** and **StateFlow** for managing background tasks and UI state efficiently.
- **Navigation:** **Jetpack Navigation Component** for handling all in-app navigation.
- **Backend Services:** **Firebase**
    - **Cloud Firestore:** Real-time NoSQL database for all application data.
    - **Firebase Authentication:** For handling user authentication and sessions.
    - **Firebase Storage:** For storing user-generated content like profile pictures and product images.
    - **Firebase Cloud Messaging (FCM):** For sending push notifications.
- **Image Loading:** **Coil** for efficient image loading and caching from URLs.
