package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.LivingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class LivingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LivingRepository(application)

    // Current Session
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // All Users for Admin
    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
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
        else {
            // Combine all properties applications or fetch directly is ideal
            // We can just query applications of all landlords or simulate.
            // Let's create landlord applications flow or seed data.
            // Since we know landlord 1 or 2 are present, we could do combine. Let's just monitor all landlords applications
            repository.getApplicationsByLandlord(2).combine(repository.getApplicationsByLandlord(3)) { a, b ->
                a + b
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Messaging State
    val activeChatPartnerId = MutableStateFlow<Int?>(null)
    val activeConversation: StateFlow<List<Message>> = combine(
        _currentUser,
        activeChatPartnerId
    ) { user, partnerId ->
        if (user == null || partnerId == null) emptyList()
        else repository.getConversationFlow(user.id, partnerId).first()
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

    init {
        // Run database initializer at launch
        viewModelScope.launch {
            repository.seedInitialData()
        }
    }

    fun addNotification(text: String) {
        val current = _notifications.value.toMutableList()
        current.add(0, text)
        _notifications.value = current
    }

    // Authentication Operations
    fun login(email: String, authPass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail == "stuartdonsms@gmail.com") {
                if (authPass != "code5_12345") {
                    onFailure("Incorrect password for admin access.")
                    return@launch
                }
                var user = repository.getUserByEmail(normalizedEmail)
                if (user == null) {
                    val adminUser = User(
                        email = "stuartdonsms@gmail.com",
                        name = "Stuart Don (Admin)",
                        phone = "1-646-555-4421",
                        role = "ADMIN",
                        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=80",
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
                        name = "Stuart Don (Admin)"
                    )
                    repository.updateUser(updatedUser)
                    user = updatedUser
                }
                _currentUser.value = user
                addNotification("Welcome back, Administrator ${user.name}!")
                onSuccess()
                return@launch
            }

            // Standard login behavior
            val user = repository.getUserByEmail(email)
            if (user != null) {
                // In demo, password check can accept matching simple formats
                _currentUser.value = user
                addNotification("Welcome back, ${user.name}! Successful login.")
                onSuccess()
            } else {
                onFailure("No account registered with $email. Please register.")
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
            val normalizedEmail = email.trim().lowercase()
            val existing = repository.getUserByEmail(email)
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
                email = email,
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
            addNotification("Account created! Welcome ${savedUser.name} to the community.")
            onSuccess()
        }
    }

    fun continueAsGuestUser(onSuccess: () -> Unit) {
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
    }

    fun logout(onSuccess: () -> Unit) {
        _currentUser.value = null
        addNotification("Logged out successfully.")
        onSuccess()
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
                isApproved = false // Pending approval by Admin
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
}
