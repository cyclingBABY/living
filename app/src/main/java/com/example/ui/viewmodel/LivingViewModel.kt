package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.LivingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.OptIn

@OptIn(ExperimentalCoroutinesApi::class)
class LivingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LivingRepository(application)

    // Current Session
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // All Users for Admin
    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Reels
    val allReels: StateFlow<List<Reel>> = repository.getAllReelsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Properties
    val allProperties: StateFlow<List<Property>> = repository.getAllPropertiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Approved Properties for Home Page and Search
    val approvedProperties: StateFlow<List<Property>> = repository.getAllApprovedPropertiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedHouseType = MutableStateFlow<String?>(null)
    val selectedMinPrice = MutableStateFlow<Double?>(null)
    val selectedMaxPrice = MutableStateFlow<Double?>(null)
    val selectedBedrooms = MutableStateFlow<Int?>(null)
    val selectedBathrooms = MutableStateFlow<Int?>(null)
    val filterWifi = MutableStateFlow(false)
    val filterPetFriendly = MutableStateFlow(false)
    val filterFurnished = MutableStateFlow(false)
    val filterParking = MutableStateFlow(false)
    val filterSecurity = MutableStateFlow(false)

    // Filtered Properties Flow
    val filteredProperties: StateFlow<List<Property>> = combine(
        approvedProperties,
        searchQuery,
        selectedHouseType,
        selectedMinPrice,
        selectedMaxPrice,
        selectedBedrooms,
        selectedBathrooms,
        filterWifi,
        filterPetFriendly,
        filterFurnished,
        filterParking,
        filterSecurity
    ) { params ->
        val list = params[0] as List<Property>
        val query = params[1] as String
        val type = params[2] as String?
        val minP = params[3] as Double?
        val maxP = params[4] as Double?
        val beds = params[5] as Int?
        val baths = params[6] as Int?
        val wifi = params[7] as Boolean
        val pet = params[8] as Boolean
        val furn = params[9] as Boolean
        val park = params[10] as Boolean
        val sec = params[11] as Boolean

        list.filter { p ->
            val matchesQuery = query.isEmpty() || p.title.contains(query, ignoreCase = true) || p.address.contains(query, ignoreCase = true)
            val matchesType = type == null || p.houseType == type
            val matchesMinPrice = minP == null || p.rent >= minP
            val matchesMaxPrice = maxP == null || p.rent <= maxP
            val matchesBeds = beds == null || p.bedrooms >= beds
            val matchesBaths = baths == null || p.bathrooms >= baths
            val matchesWifi = !wifi || p.wifi
            val matchesPet = !pet || p.petFriendly
            val matchesFurn = !furn || p.furnished
            val matchesPark = !park || p.parking
            val matchesSec = !sec || p.security

            matchesQuery && matchesType && matchesMinPrice && matchesMaxPrice && matchesBeds && matchesBaths && matchesWifi && matchesPet && matchesFurn && matchesPark && matchesSec
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Saved Properties representing Favorite Checkboxes
    val savedPropertyIds: StateFlow<Set<Int>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptySet())
        else repository.getSavedByTenant(user.id).map { list -> list.map { it.propertyId }.toSet() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Saved properties full list
    val savedProperties: StateFlow<List<Property>> = combine(approvedProperties, savedPropertyIds) { props, savedIds ->
        props.filter { it.id in savedIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tenant Applications
    val tenantApplications: StateFlow<List<com.example.data.model.Application>> = _currentUser.flatMapLatest { user ->
        if (user == null || user.role != "TENANT") flowOf(emptyList())
        else repository.getApplicationsByTenant(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Landlord Applications
    val landlordApplications: StateFlow<List<com.example.data.model.Application>> = _currentUser.flatMapLatest { user ->
        if (user == null || user.role != "LANDLORD") flowOf(emptyList())
        else repository.getApplicationsByLandlord(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // System level Apps for Admin
    val allApplications: StateFlow<List<com.example.data.model.Application>> = _currentUser.flatMapLatest { user ->
        if (user == null || user.role != "ADMIN") flowOf(emptyList())
        else repository.getAllApplicationsFlow()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Messaging State
    val activeChatPartnerId = MutableStateFlow<Int?>(null)
    val activeConversation: StateFlow<List<Message>> = _currentUser.flatMapLatest { user ->
        activeChatPartnerId.flatMapLatest { partnerId ->
            if (user == null || partnerId == null) flowOf(emptyList())
            else repository.getConversationFlow(user.id, partnerId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Conversations List (Inbox) - Fetch unique messages grouped by contact
    val inboxMessages: StateFlow<List<Message>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getMessagesForUser(user.id).map { list ->
            // Filter latest per conversational contact
            val seenContacts = mutableSetOf<Int>()
            val uniqueList = mutableListOf<Message>()
            for (msg in list) {
                val contactId = if (msg.senderId == user.id) msg.receiverId else msg.senderId
                if (!seenContacts.contains(contactId)) {
                    seenContacts.add(contactId)
                    uniqueList.add(msg)
                }
            }
            uniqueList
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payments Hist
    val tenantPayments: StateFlow<List<Payment>> = _currentUser.flatMapLatest { user ->
        if (user == null || user.role != "TENANT") flowOf(emptyList())
        else repository.getPaymentsByTenant(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPaymentsInSystem: StateFlow<List<Payment>> = repository.getAllPaymentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live AI Recommendations
    private val _aiRecommendationText = MutableStateFlow("")
    val aiRecommendationText: StateFlow<String> = _aiRecommendationText.asStateFlow()

    private val _aiRecommendationLoading = MutableStateFlow(false)
    val aiRecommendationLoading: StateFlow<Boolean> = _aiRecommendationLoading.asStateFlow()

    // In-App Notifications State
    private val _notifications = MutableStateFlow<List<String>>(
        listOf(
            "Welcome to Living! Feel free to search for modern dynamic spaces.",
            "Living AI Assistant is ready! Type keywords in chat to match houses."
        )
    )
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    // Local persistence helper for remembering logins
    private val prefs = application.getSharedPreferences("living_auth_prefs", android.content.Context.MODE_PRIVATE)

    fun saveRememberedUser(email: String) {
        prefs.edit().putString("remembered_email", email).apply()
    }

    fun clearRememberedUser() {
        prefs.edit().remove("remembered_email").apply()
    }

    fun getRememberedEmail(): String? {
        return prefs.getString("remembered_email", null)
    }

    fun tryPersistentLogin(onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            try {
                val email = getRememberedEmail()
                if (!email.isNullOrEmpty()) {
                    val user = repository.getUserByEmail(email)
                    if (user != null) {
                        _currentUser.value = user
                        addNotification("Welcome back! Auto-logged in as ${user.name}.")
                        onSuccess()
                    } else {
                        onFailure()
                    }
                } else {
                    onFailure()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure()
            }
        }
    }

    init {
        // Run database initializer at launch
        viewModelScope.launch {
            try {
                repository.seedInitialData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addNotification(text: String) {
        try {
            val current = _notifications.value.toMutableList()
            current.add(0, text)
            _notifications.value = current
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Google Sign-In Operation
    fun loginWithGoogle(email: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val normalizedEmail = email.trim().lowercase()
                var user = repository.getUserByEmail(normalizedEmail)
                
                if (user == null) {
                    // Register a premium Google account dynamically
                    val isStuart = normalizedEmail == "stuartdonsms@gmail.com"
                    val googleUser = User(
                        email = normalizedEmail,
                        name = name,
                        phone = "N/A",
                        role = if (isStuart) "ADMIN" else "TENANT",
                        avatarUrl = if (isStuart) {
                            "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=120&q=80"
                        } else {
                            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80"
                        },
                        verificationState = "VERIFIED",
                        rating = 5.0f,
                        companyName = if (isStuart) "Living Corporate Admin" else ""
                    )
                    val newId = repository.insertUser(googleUser).toInt()
                    user = googleUser.copy(id = newId)
                } else if (normalizedEmail == "stuartdonsms@gmail.com" && user.role != "ADMIN") {
                    val updatedUser = user.copy(
                        role = "ADMIN",
                        verificationState = "VERIFIED",
                        companyName = "Living Corporate Admin"
                    )
                    repository.updateUser(updatedUser)
                    user = updatedUser
                }
                
                _currentUser.value = user
                saveRememberedUser(normalizedEmail)
                addNotification("Google continues seamlessly! Welcome back ${user.name}!")
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Authentication Operations
    fun login(email: String, authPass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val normalizedEmail = email.trim().lowercase()
                
                // Unified Admin login cases
                if (normalizedEmail == "stuartdonsms@gmail.com" || normalizedEmail == "admin@living.com") {
                    if (normalizedEmail == "stuartdonsms@gmail.com" && authPass != "code5_12345") {
                        onFailure("Incorrect password for admin access.")
                        return@launch
                    }
                    
                    var user = repository.getUserByEmail(normalizedEmail)
                    if (user == null) {
                        val adminUser = User(
                            email = normalizedEmail,
                            name = if (normalizedEmail == "admin@living.com") "Sarah Sterling" else "Stuart Don (Admin)",
                            phone = "1-646-555-4421",
                            role = "ADMIN",
                            avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=120&q=80",
                            verificationState = "VERIFIED",
                            rating = 5.0f,
                            companyName = "Living Corporate Admin"
                        )
                        val newId = repository.insertUser(adminUser).toInt()
                        user = adminUser.copy(id = newId)
                    } else if (user.role != "ADMIN") {
                        val updatedUser = user.copy(
                            role = "ADMIN",
                            verificationState = "VERIFIED",
                            companyName = "Living Corporate Admin",
                            name = if (normalizedEmail == "admin@living.com") "Sarah Sterling" else "Stuart Don (Admin)"
                        )
                        repository.updateUser(updatedUser)
                        user = updatedUser
                    }
                    _currentUser.value = user
                    saveRememberedUser(normalizedEmail)
                    addNotification("Welcome back, Administrator ${user.name}!")
                    onSuccess()
                    return@launch
                }

                // Standard login behavior
                val user = repository.getUserByEmail(normalizedEmail)
                if (user != null) {
                    _currentUser.value = user
                    saveRememberedUser(normalizedEmail)
                    addNotification("Welcome back, ${user.name}! Successful login.")
                    onSuccess()
                } else {
                    onFailure("No account registered with $email. Please register.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun register(
        email: String,
        name: String,
        phone: String,
        role: String,
        companyName: String = "",
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val normalizedEmail = email.trim().lowercase()
                val existing = repository.getUserByEmail(normalizedEmail)
                if (existing != null) {
                    onFailure("Account with this email already exists.")
                    return@launch
                }

                // Create beautiful profile picture avatar based on role
                val targetRole = if (normalizedEmail == "stuartdonsms@gmail.com") "ADMIN" else role
                val isVerified = if (targetRole == "ADMIN") "VERIFIED" else "PENDING"
                val randomNum = (11..99).random()
                val avatarUrl = if (targetRole == "TENANT") {
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80"
                } else {
                    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=120&q=80"
                }

                val newUser = User(
                    email = normalizedEmail,
                    name = name,
                    phone = phone,
                    role = targetRole,
                    avatarUrl = avatarUrl,
                    verificationState = isVerified,
                    companyName = companyName,
                    rating = 5.0f
                )

                val newId = repository.insertUser(newUser).toInt()
                val savedUser = newUser.copy(id = newId)
                _currentUser.value = savedUser
                saveRememberedUser(normalizedEmail)
                addNotification("Account created! Welcome ${savedUser.name} to the community.")
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure("Registration failed: ${e.message}")
            }
        }
    }

    fun continueAsGuestUser(onSuccess: () -> Unit) {
        try {
            val guestUser = User(
                id = -1,
                email = "guest@living.com",
                name = "Guest User",
                phone = "N/A",
                role = "TENANT",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80",
                verificationState = "REJECTED",
                rating = 5.0f
            )
            _currentUser.value = guestUser
            addNotification("Browsing Living catalog as Guest.")
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logout(onSuccess: () -> Unit) {
        try {
            _currentUser.value = null
            clearRememberedUser()
            addNotification("Logged out successfully.")
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Update Profile details
    fun updateUserProfile(
        name: String,
        phone: String,
        companyName: String,
        avatarUrl: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val updated = user.copy(
                    name = name,
                    phone = phone,
                    companyName = companyName,
                    avatarUrl = avatarUrl
                )
                repository.updateUser(updated)
                _currentUser.value = updated
                addNotification("Status: User profiles successfully updated.")
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                addNotification("Error: Profile update failed.")
            }
        }
    }

    // Delete User Account
    fun deleteUserAccount(onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                repository.deleteUser(user)
                _currentUser.value = null
                clearRememberedUser()
                addNotification("Account successfully deleted. See you again!")
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                addNotification("Error: Could not delete user account.")
            }
        }
    }

    // Subscription actions
    fun purchaseSubscription(months: Int, onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val currentExpiry = if (user.subscriptionExpiry > System.currentTimeMillis()) user.subscriptionExpiry else System.currentTimeMillis()
                val addedDuration = months * 30L * 24 * 60 * 60 * 1000
                val updated = user.copy(
                    subscriptionExpiry = currentExpiry + addedDuration
                )
                repository.updateUser(updated)
                _currentUser.value = updated
                addNotification("Subscription renewed successfully for $months month(s)!")
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Privacy Settings and Persisted Options
    private val privacyPrefs = application.getSharedPreferences("living_privacy_prefs", android.content.Context.MODE_PRIVATE)
    
    val isRatingHidden = MutableStateFlow(privacyPrefs.getBoolean("is_rating_hidden", false))
    val isDmsDisabled = MutableStateFlow(privacyPrefs.getBoolean("is_dms_disabled", false))
    val isPhoneHidden = MutableStateFlow(privacyPrefs.getBoolean("is_phone_hidden", false))
    val isLocationEnabled = MutableStateFlow(privacyPrefs.getBoolean("is_location_enabled", true))

    fun toggleRatingHidden(hidden: Boolean) {
        isRatingHidden.value = hidden
        privacyPrefs.edit().putBoolean("is_rating_hidden", hidden).apply()
        addNotification("Privacy: Community ratings is now ${if (hidden) "hidden from public view" else "visible to public"}.")
    }

    fun toggleDmsDisabled(disabled: Boolean) {
        isDmsDisabled.value = disabled
        privacyPrefs.edit().putBoolean("is_dms_disabled", disabled).apply()
        addNotification("Privacy: Direct messaging ${if (disabled) "disabled for others" else "enabled"}.")
    }

    fun togglePhoneHidden(hidden: Boolean) {
        isPhoneHidden.value = hidden
        privacyPrefs.edit().putBoolean("is_phone_hidden", hidden).apply()
        addNotification("Privacy: Listing contact number is ${if (hidden) "hidden" else "public"}.")
    }

    fun toggleLocationEnabled(enabled: Boolean) {
        isLocationEnabled.value = enabled
        privacyPrefs.edit().putBoolean("is_location_enabled", enabled).apply()
        addNotification("Privacy: Location matches are ${if (enabled) "active" else "inactive"}.")
    }

    // Property Saved State Action
    fun toggleSaveProperty(propertyId: Int) {
        val user = _currentUser.value ?: return
        if (user.id == -1) {
            addNotification("Guest users cannot save favorite properties!")
            return
        }
        viewModelScope.launch {
            repository.toggleSave(user.id, propertyId)
            val saved = repository.isSaved(user.id, propertyId)
            if (saved) {
                addNotification("Property bookmark saved.")
            } else {
                addNotification("Property bookmark removed.")
            }
        }
    }

    // Create Application
    fun applyForProperty(
        propertyId: Int,
        landlordId: Int,
        coverLetter: String,
        visitDate: String,
        visitTime: String,
        uploadedDocs: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        if (user.id == -1) {
            addNotification("Guests cannot submit rental applications.")
            return
        }
        viewModelScope.launch {
            val app = Application(
                propertyId = propertyId,
                tenantId = user.id,
                landlordId = landlordId,
                status = "PENDING",
                coverLetter = coverLetter,
                visitDate = visitDate,
                visitTime = visitTime,
                uploadedDocs = uploadedDocs
            )
            repository.insertApplication(app)
            addNotification("Active Application submitted to landlord.")
            onSuccess()
        }
    }

    // Landlord / Admin Update Application status
    fun updateApplicationStatus(applicationId: Int, app: com.example.data.model.Application, newStatus: String) {
        viewModelScope.launch {
            val updated = app.copy(status = newStatus)
            repository.updateApplication(updated)
            addNotification("Application registration updated to '$newStatus'.")
        }
    }

    // Send Message / Chat
    fun sendMessage(receiverId: Int, messageText: String, attachmentUrl: String = "") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newMsg = Message(
                senderId = user.id,
                receiverId = receiverId,
                messageText = messageText,
                attachmentUrl = attachmentUrl
            )
            repository.insertMessage(newMsg)

            // Dynamic System auto-response simulation if sending to Landlords
            if (receiverId != user.id) {
                val partner = repository.getUserByIdSync(receiverId)
                if (partner != null) {
                    addNotification("Message sent to ${partner.name}")
                }
            }
        }
    }

    // Make Payment Integration
    fun performRentalPayment(
        propertyId: Int,
        amount: Double,
        payType: String,
        payMethod: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val qrcodeStr = "LIVING-QR-" + UUID.randomUUID().toString().take(8).uppercase()
            val newBill = Payment(
                propertyId = propertyId,
                tenantId = user.id,
                amount = amount,
                paymentType = payType,
                paymentMethod = payMethod,
                date = "2026-05-22",
                qrcode = qrcodeStr,
                status = "Success"
            )
            repository.insertPayment(newBill)
            addNotification("Payment of \$$amount processed via $payMethod! Receipt generated.")
            onSuccess()
        }
    }

    // Submit Reviews & ratings
    fun submitPropertyReview(targetId: Int, reviewerName: String, rating: Int, comment: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val rev = Review(
                reviewerId = user.id,
                reviewerName = reviewerName,
                targetId = targetId,
                targetType = "PROPERTY",
                rating = rating,
                comment = comment
            )
            repository.insertReview(rev)

            // Update property averages dynamically
            val property = repository.getPropertyByIdSync(targetId)
            if (property != null) {
                val newCount = property.ratingsCount + 1
                val newAvg = ((property.averageRating * property.ratingsCount) + rating) / newCount
                val updatedProp = property.copy(ratingsCount = newCount, averageRating = newAvg)
                repository.updateProperty(updatedProp)
            }
            addNotification("Review and rating recorded successfully!")
        }
    }

    fun submitUserReview(targetUserId: Int, rating: Int, comment: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val rev = Review(
                reviewerId = user.id,
                reviewerName = user.name,
                targetId = targetUserId,
                targetType = "USER",
                rating = rating,
                comment = comment
            )
            repository.insertReview(rev)
            
            // Optionally update user average rating dynamically
            val targetUser = repository.getUserByIdSync(targetUserId)
            if (targetUser != null) {
                val newRating = if (targetUser.rating == 0f || targetUser.rating == 5.0f /* seed default fallback */) rating.toFloat() else (targetUser.rating + rating.toFloat()) / 2f
                val updatedTarget = targetUser.copy(rating = newRating)
                repository.updateUser(updatedTarget)
            }
            addNotification("Member vouched successfully!")
        }
    }

    // Landlord: Add Property Listing
    fun landlordAddProperty(
        title: String,
        description: String,
        rent: Double,
        deposit: Double,
        imageStrList: String,
        address: String,
        houseType: String,
        beds: Int,
        baths: Int,
        furnished: Boolean,
        parking: Boolean,
        pet: Boolean,
        wifi: Boolean,
        security: Boolean,
        videoUrls: String = "",
        videoSizesStr: String = "",
        waterSource: String = "NWSC Running Water",
        electricityMeter: String = "Umeme Yaka Pre-paid Meter",
        roadAccess: String = "Paved Tarmac Access Road",
        securityFence: Boolean = true,
        paymentInstallments: String = "3 Months Upfront",
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newProp = Property(
                title = title,
                description = description,
                rent = rent,
                deposit = deposit,
                imageUrls = imageStrList.ifEmpty { "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80" },
                address = address,
                lat = 37.7749,
                lng = -122.4194,
                houseType = houseType,
                bedrooms = beds,
                bathrooms = baths,
                furnished = furnished,
                parking = parking,
                petFriendly = pet,
                wifi = wifi,
                security = security,
                landlordId = user.id,
                isApproved = false, // Pending approval by Admin
                videoUrls = videoUrls,
                videoSizesStr = videoSizesStr,
                waterSource = waterSource,
                electricityMeter = electricityMeter,
                roadAccess = roadAccess,
                securityFence = securityFence,
                paymentInstallments = paymentInstallments
            )
            repository.insertProperty(newProp)
            addNotification("Property listed! Pending administrator approval queue.")
            onSuccess()
        }
    }

    // Landlord: Mark property status as rented/available
    fun toggleProductRented(property: Property) {
        viewModelScope.launch {
            val updated = property.copy(isRented = !property.isRented)
            repository.updateProperty(updated)
            addNotification("Listing status updated to: " + if(updated.isRented) "Rented" else "Available")
        }
    }

    // Admin: Verify Users (Landlords)
    fun adminVerifyUser(user: User) {
        viewModelScope.launch {
            val updated = user.copy(verificationState = "VERIFIED")
            repository.updateUser(updated)
            addNotification("Landlord ${user.name} verified successfully.")
        }
    }

    // Admin: Approve listings in moderation queue
    fun adminReviewProperty(property: Property, isApproved: Boolean) {
        viewModelScope.launch {
            if (isApproved) {
                val updated = property.copy(isApproved = true)
                repository.updateProperty(updated)
                addNotification("Listing '${property.title}' approved for Living feed!")
            } else {
                repository.deleteProperty(property)
                addNotification("Listing rejected and removed from system.")
            }
        }
    }

    // AI smart search assistant trigger
    fun generateAIRecommendations(userAskMsg: String) {
        _aiRecommendationText.value = ""
        _aiRecommendationLoading.value = true
        viewModelScope.launch {
            val candidates = approvedProperties.value
            val ans = repository.askGeminiForRecommendations(userAskMsg, candidates)
            _aiRecommendationText.value = ans
            _aiRecommendationLoading.value = false
        }
    }

    // Clear filters helper
    fun clearSearchFilters() {
        selectedHouseType.value = null
        selectedMinPrice.value = null
        selectedMaxPrice.value = null
        selectedBedrooms.value = null
        selectedBathrooms.value = null
        filterWifi.value = false
        filterPetFriendly.value = false
        filterFurnished.value = false
        filterParking.value = false
        filterSecurity.value = false
    }

    // Process UGX subscription payment
    fun paySubscription(
        paymentMethod: String,
        amount: Double,
        phoneNumber: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val qrcodeStr = "LIVING-SUB-QR-" + UUID.randomUUID().toString().take(8).uppercase()
            val newBill = Payment(
                propertyId = 0,
                tenantId = user.id,
                amount = amount,
                paymentType = "Account Subscription to keep account active",
                paymentMethod = "$paymentMethod ($phoneNumber)",
                date = "2026-05-23",
                qrcode = qrcodeStr,
                status = "Success"
            )
            repository.insertPayment(newBill)
            
            val currentExpiry = if (user.subscriptionExpiry > System.currentTimeMillis()) {
                user.subscriptionExpiry
            } else {
                System.currentTimeMillis()
            }
            val oneMonth = 30L * 24 * 60 * 60 * 1000L
            val updatedUser = user.copy(subscriptionExpiry = currentExpiry + oneMonth)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            
            addNotification("Subscription of ${amount.toInt()} UGX processed successfully via $paymentMethod!")
            onSuccess()
        }
    }

    // Impersonate / Role switcher capabilities
    private val _impersonatorAdminUser = MutableStateFlow<User?>(null)
    val impersonatorAdminUser: StateFlow<User?> = _impersonatorAdminUser.asStateFlow()

    fun impersonateUser(targetUser: User) {
        val current = _currentUser.value
        // If we aren't already impersonating, remember the actual admin
        if (_impersonatorAdminUser.value == null && current?.role == "ADMIN") {
            _impersonatorAdminUser.value = current
        }
        _currentUser.value = targetUser
        addNotification("Masquerading as ${targetUser.name} (${targetUser.role})")
    }

    fun stopImpersonating() {
        val admin = _impersonatorAdminUser.value
        if (admin != null) {
            _currentUser.value = admin
            _impersonatorAdminUser.value = null
            addNotification("Returned to Administrator Cockpit")
        }
    }

    fun updateUserRole(userId: Int, newRole: String) {
        viewModelScope.launch {
            allUsers.value.find { it.id == userId }?.let { user ->
                val updated = user.copy(role = newRole)
                repository.updateUser(updated)
                addNotification("User ${user.name} role changed to $newRole")
                
                // If the user we updated is the logged-in session, refresh it
                if (_currentUser.value?.id == userId) {
                    _currentUser.value = updated
                }
            }
        }
    }

    // --- Reels Actions ---
    fun postReel(
        mediaUrl: String,
        mediaType: String,
        externalPlatform: String,
        caption: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value
        viewModelScope.launch {
            val authorId = user?.id ?: -1
            val authorName = user?.name ?: "Guest User"
            val authorAvatar = user?.avatarUrl ?: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=120&q=80"
            val authorRole = user?.role ?: "TENANT"
            
            val reel = Reel(
                userId = authorId,
                userName = authorName,
                userAvatarUrl = authorAvatar,
                userRole = authorRole,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                externalPlatform = externalPlatform,
                caption = caption
            )
            repository.insertReel(reel)
            addNotification("Aesthetic Reel successfully posted!")
            onSuccess()
        }
    }

    fun toggleLikeReel(reel: Reel) {
        viewModelScope.launch {
            val updated = reel.copy(
                isLiked = !reel.isLiked,
                likesCount = if (reel.isLiked) reel.likesCount - 1 else reel.likesCount + 1
            )
            repository.updateReel(updated)
        }
    }

    fun deleteReel(reel: Reel) {
        viewModelScope.launch {
            repository.deleteReel(reel)
            addNotification("Your Reel has been deleted.")
        }
    }
}
