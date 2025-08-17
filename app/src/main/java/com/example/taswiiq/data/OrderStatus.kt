package com.example.taswiiq.data

enum class OrderStatus {
    PENDING,    // الطلب معلق في انتظار موافقة المورد
    ACCEPTED,   // وافق المورد على الطلب وجاري التجهيز
    SHIPPED,    // تم شحن الطلب
    COMPLETED,  // تم استلام الطلب بنجاح
    CANCELLED   // تم إلغاء الطلب
}