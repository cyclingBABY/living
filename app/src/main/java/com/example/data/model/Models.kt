package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val name: String,
    val phone: String,
    val role: String, // "TENANT", "LANDLORD", "ADMIN"
    val avatarUrl: String,
    val verificationState: String, // "REJECTED", "PENDING", "VERIFIED"
    val rating: Float = 5.0f,
    val companyName: String = "",
    val joinedDate: String = "May 2026",
    val joinedTimestamp: Long = System.currentTimeMillis(),
    val subscriptionExpiry: Long = 0L
) {
    fun hasActiveAccess(): Boolean {
        if (role == "ADMIN") return true
        if (id == -1 || email == "guest@living.com") return true // Guest viewing is free
        
        val now = System.currentTimeMillis()
        val trialDuration = 7 * 24 * 60 * 60 * 1000L // 1 week
        val trialExpired = now > (joinedTimestamp + trialDuration)
        
        if (!trialExpired) return true
        return subscriptionExpiry > now
    }

    fun getSubscriptionStatusText(): String {
        if (role == "ADMIN") return "Administrator Active"
        if (id == -1 || email == "guest@living.com") return "Guest Account (Viewing Free)"
        
        val now = System.currentTimeMillis()
        val trialDuration = 7 * 24 * 60 * 60 * 1000L
        val trialEndTime = joinedTimestamp + trialDuration
        
        return if (now < trialEndTime) {
            val daysLeft = ((trialEndTime - now) / (24 * 60 * 60 * 1000L)) + 1
            "Free Trial: $daysLeft days left"
        } else if (subscriptionExpiry > now) {
            val daysLeft = ((subscriptionExpiry - now) / (24 * 60 * 60 * 1000L)) + 1
            "Active subscription: $daysLeft days left"
        } else {
            "Trial and Subscription Expired"
        }
    }
}

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val rent: Double,
    val deposit: Double,
    val imageUrls: String, // Comma-separated list of image URLs/descriptions
    val address: String,
    val lat: Double,
    val lng: Double,
    val houseType: String, // "Apartment", "House", "Penthouse", "Studio"
    val bedrooms: Int,
    val bathrooms: Int,
    val furnished: Boolean,
    val parking: Boolean,
    val petFriendly: Boolean,
    val wifi: Boolean,
    val security: Boolean,
    val landlordId: Int,
    val isApproved: Boolean = false, // Must be approved by Admin
    val isPromoted: Boolean = false,
    val isRented: Boolean = false,
    val nearbySchools: String = "Greenwood High, Westside Academy",
    val nearbyHospitals: String = "City General, St. Jude Medical",
    val ratingsCount: Int = 0,
    val averageRating: Float = 0.0f,
    val videoUrls: String = "",
    val videoSizesStr: String = "",
    val waterSource: String = "NWSC Running Water",
    val electricityMeter: String = "Umeme Yaka Pre-paid Meter",
    val roadAccess: String = "Paved Tarmac Access Road",
    val securityFence: Boolean = true,
    val paymentInstallments: String = "3 Months Upfront"
)

@Entity(tableName = "applications")
data class Application(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propertyId: Int,
    val tenantId: Int,
    val landlordId: Int,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val coverLetter: String,
    val visitDate: String = "",
    val visitTime: String = "",
    val uploadedDocs: String = "", // Comma-separated documents
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: Int,
    val receiverId: Int,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentUrl: String = "",
    val isRead: Boolean = false
)

@Entity(tableName = "saved_properties")
data class SavedProperty(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenantId: Int,
    val propertyId: Int
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propertyId: Int,
    val tenantId: Int,
    val amount: Double,
    val paymentType: String, // "Deposit", "Rent", "Premium Placement"
    val paymentMethod: String, // "Mobile Money", "Visa/Mastercard", "PayPal", "Stripe"
    val date: String,
    val qrcode: String = "", // Holds base-64 or text representation of QR Code
    val status: String = "Success" // "Success", "Failed", "Pending"
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reviewerId: Int,
    val reviewerName: String,
    val targetId: Int, // User or Property ID
    val targetType: String, // "USER" or "PROPERTY"
    val rating: Int, // 1 to 5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
