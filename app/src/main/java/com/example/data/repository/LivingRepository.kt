package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.BuildConfig
import com.example.data.database.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class LivingRepository(private val context: Context) {

    val db: LivingDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            LivingDatabase::class.java,
            "living_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val userDao = db.userDao()
    private val propertyDao = db.propertyDao()
    private val applicationDao = db.applicationDao()
    private val messageDao = db.messageDao()
    private val savedPropertyDao = db.savedPropertyDao()
    private val paymentDao = db.paymentDao()
    private val reviewDao = db.reviewDao()

    // --- Users ---
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    fun getUserById(id: Int): Flow<User?> = userDao.getUserById(id)
    suspend fun getUserByIdSync(id: Int): User? = userDao.getUserByIdSync(id)
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    // --- Properties ---
    fun getAllPropertiesFlow(): Flow<List<Property>> = propertyDao.getAllPropertiesFlow()
    fun getAllApprovedPropertiesFlow(): Flow<List<Property>> = propertyDao.getAllApprovedPropertiesFlow()
    fun getPropertiesByLandlord(landlordId: Int): Flow<List<Property>> = propertyDao.getPropertiesByLandlord(landlordId)
    fun getPropertyById(id: Int): Flow<Property?> = propertyDao.getPropertyById(id)
    suspend fun getPropertyByIdSync(id: Int): Property? = propertyDao.getPropertyByIdSync(id)
    suspend fun insertProperty(property: Property): Long = propertyDao.insertProperty(property)
    suspend fun updateProperty(property: Property) = propertyDao.updateProperty(property)
    suspend fun deleteProperty(property: Property) = propertyDao.deleteProperty(property)

    // --- Applications ---
    fun getApplicationsByTenant(tenantId: Int): Flow<List<Application>> = applicationDao.getApplicationsByTenant(tenantId)
    fun getApplicationsByLandlord(landlordId: Int): Flow<List<Application>> = applicationDao.getApplicationsByLandlord(landlordId)
    fun getApplicationsByProperty(propertyId: Int): Flow<List<Application>> = applicationDao.getApplicationsByProperty(propertyId)
    suspend fun insertApplication(app: Application): Long = applicationDao.insertApplication(app)
    suspend fun updateApplication(app: Application) = applicationDao.updateApplication(app)

    // --- Messages ---
    fun getConversationFlow(u1: Int, u2: Int): Flow<List<Message>> = messageDao.getConversationFlow(u1, u2)
    fun getMessagesForUser(userId: Int): Flow<List<Message>> = messageDao.getMessagesForUser(userId)
    suspend fun insertMessage(message: Message): Long = messageDao.insertMessage(message)

    // --- Saved Properties ---
    fun getSavedByTenant(tenantId: Int): Flow<List<SavedProperty>> = savedPropertyDao.getSavedByTenant(tenantId)
    suspend fun isSaved(tenantId: Int, propertyId: Int): Boolean = savedPropertyDao.isSaved(tenantId, propertyId)
    suspend fun toggleSave(tenantId: Int, propertyId: Int) {
        if (savedPropertyDao.isSaved(tenantId, propertyId)) {
            savedPropertyDao.deleteSaved(tenantId, propertyId)
        } else {
            savedPropertyDao.insertSaved(SavedProperty(tenantId = tenantId, propertyId = propertyId))
        }
    }

    // --- Payments ---
    fun getPaymentsByTenant(tenantId: Int): Flow<List<Payment>> = paymentDao.getPaymentsByTenant(tenantId)
    fun getAllPaymentsFlow(): Flow<List<Payment>> = paymentDao.getAllPaymentsFlow()
    suspend fun insertPayment(payment: Payment): Long = paymentDao.insertPayment(payment)

    // --- Reviews ---
    fun getReviews(targetId: Int, targetType: String): Flow<List<Review>> = reviewDao.getReviews(targetId, targetType)
    suspend fun insertReview(review: Review): Long = reviewDao.insertReview(review)

    // --- Ask AI (Gemini Assistant API Integration with Safe Design Fallback) ---
    suspend fun askGeminiForRecommendations(promptInput: String, properties: List<Property>): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Local high-fidelity matcher fallback
            return@withContext getLocalSmartPropertyRecommendation(promptInput, properties)
        }

        val urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val propertiesJson = properties.joinToString("\n") { p ->
                "- ID ${p.id}: ${p.title} in ${p.address}, Rent \$${p.rent}, ${p.bedrooms} beds, ${p.bathrooms} baths, ${p.houseType}. Highlights: Furnished=${p.furnished}, WiFi=${p.wifi}, Pets=${p.petFriendly}"
            }

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "You are Living AI, the premier property consultant assistant for 'Living'. Code with visual flair! Below are our listings:\n$propertiesJson\n\nThe user says: \"$promptInput\"\n\nRecommend the best matching properties clearly in 2-3 short, friendly sentences. Include property name and ID. Do not use asterisks or markdown headings. Keep it super polished!")
                            })
                        })
                    })
                })
            }

            val writer = connection.outputStream.bufferedWriter()
            writer.write(requestBody.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                val candidates = jsonResponse.getJSONArray("candidates")
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val text = parts.getJSONObject(0).getString("text")
                return@withContext text.trim()
            } else {
                return@withContext getLocalSmartPropertyRecommendation(promptInput, properties)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext getLocalSmartPropertyRecommendation(promptInput, properties)
        }
    }

    private fun getLocalSmartPropertyRecommendation(query: String, properties: List<Property>): String {
        val lowercaseQuery = query.lowercase()
        val filtered = properties.filter {
            lowercaseQuery.contains(it.houseType.lowercase()) ||
            lowercaseQuery.contains(it.address.lowercase()) ||
            (lowercaseQuery.contains("cheap") && it.rent < 1500) ||
            (lowercaseQuery.contains("luxury") && it.rent >= 2000) ||
            (lowercaseQuery.contains("furnished") && it.furnished) ||
            (lowercaseQuery.contains("pet") && it.petFriendly) ||
            (lowercaseQuery.contains("wifi") && it.wifi) ||
            (lowercaseQuery.contains("bedroom") && it.bedrooms >= 2) ||
            lowercaseQuery.contains(it.title.lowercase())
        }

        val bestMatches = filtered.take(2).ifEmpty { properties.take(2) }
        val recommendations = bestMatches.joinToString(" and ") { "**${it.title}** (\$${it.rent}/mo, ${it.bedrooms} beds in ${it.address})" }

        return "Living AI Recommendation: Based on your request, I highly recommend checking out $recommendations. They match your comfort criteria and offer a spectacular lifestyle!"
    }


    // --- Seeding of Initial Data ---
    suspend fun seedInitialData() {
        // Only seed if users table is empty
        val existingUsers = userDao.getAllUsers().first()
        if (existingUsers.isNotEmpty()) return

        // 1. Seed Roles
        val admin = User(
            email = "admin@living.com",
            name = "Sarah Sterling",
            phone = "1-202-555-0143",
            role = "ADMIN",
            avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=120&q=80",
            verificationState = "VERIFIED",
            rating = 5.0f,
            companyName = "Living Corporate HQ"
        )
        val landlord1 = User(
            email = "landlord@living.com",
            name = "Richard Vance",
            phone = "1-415-555-9118",
            role = "LANDLORD",
            avatarUrl = "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=120&q=80",
            verificationState = "VERIFIED",
            rating = 4.8f,
            companyName = "Apex Residential Ltd"
        )
        val landlord2 = User(
            email = "elizabeth@living.com",
            name = "Elizabeth Cho",
            phone = "1-312-555-8833",
            role = "LANDLORD",
            avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=120&q=80",
            verificationState = "PENDING",
            rating = 4.5f,
            companyName = "Cho Loft Collections"
        )
        val tenant = User(
            email = "tenant@living.com",
            name = "Stuart Don",
            phone = "1-646-555-4421",
            role = "TENANT",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=80",
            verificationState = "VERIFIED",
            rating = 4.9f
        )

        val adminId = userDao.insertUser(admin).toInt()
        val landlord1Id = userDao.insertUser(landlord1).toInt()
        val landlord2Id = userDao.insertUser(landlord2).toInt()
        val tenantId = userDao.insertUser(tenant).toInt()

        // 2. Seed Beautiful Properties
        val props = listOf(
            Property(
                title = "The Lumina Sky Penthouse",
                description = "Breathtaking floor-to-ceiling visual luxury penthouse offering 360-degree views of the skyline. Designed with premium custom glassmorphism, spacious layouts, a private pool deck, and state-of-the-art smart systems.",
                rent = 3500.0,
                deposit = 4000.0,
                imageUrls = "https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80",
                address = "777 Grand Avenue, Manhattan, NY",
                lat = 40.7128,
                lng = -74.0060,
                houseType = "Penthouse",
                bedrooms = 3,
                bathrooms = 4,
                furnished = true,
                parking = true,
                petFriendly = true,
                wifi = true,
                security = true,
                landlordId = landlord1Id,
                isApproved = true,
                isPromoted = true,
                nearbySchools = "Manhattan Charter High, Riverdale Country",
                nearbyHospitals = "NY Presbyterian, Mount Sinai",
                ratingsCount = 4,
                averageRating = 4.9f
            ),
            Property(
                title = "Mid-Century Modern Suburban",
                description = "Charming family home set in quiet suburbia. Modern open-plan kitchen, fireplace, and sliding doors leading to a spacious child-friendly landscaped garden with tall pines. Ready for immediate occupation.",
                rent = 2100.0,
                deposit = 2500.0,
                imageUrls = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80",
                address = "14 Redwood Lane, San Francisco, CA",
                lat = 37.7749,
                lng = -122.4194,
                houseType = "House",
                bedrooms = 4,
                bathrooms = 3,
                furnished = false,
                parking = true,
                petFriendly = true,
                wifi = true,
                security = true,
                landlordId = landlord1Id,
                isApproved = true,
                isPromoted = false,
                nearbySchools = "San Francisco Montessori, Lowell High",
                nearbyHospitals = "UCSF Health, Kaiser Permanente",
                ratingsCount = 2,
                averageRating = 4.5f
            ),
            Property(
                title = "Monochrome Luxury Studio",
                description = "Ultra sleek minimalist industrial studio with custom cast-iron fixtures, concrete floorings, dark slate detailing, and hyper-premium acoustic sound-proofing. Located in the bustling fashion and design district.",
                rent = 1450.0,
                deposit = 1500.0,
                imageUrls = "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=800&q=80",
                address = "402 Design Quarter, Chicago, IL",
                lat = 41.8781,
                lng = -87.6298,
                houseType = "Studio",
                bedrooms = 1,
                bathrooms = 1,
                furnished = true,
                parking = false,
                petFriendly = false,
                wifi = true,
                security = true,
                landlordId = landlord2Id,
                isApproved = true,
                isPromoted = true,
                nearbySchools = "Chicago Public Arts, British School IL",
                nearbyHospitals = "Northwestern Memorial, Mercy Hospital",
                ratingsCount = 1,
                averageRating = 5.0f
            ),
            Property(
                title = "Suntide Coastal Apartment",
                description = "Hear the seagulls and the crashing waves! Beautiful beachside light-filled apartment with a panoramic balcony, outdoor grill, and warm wooden accents. Unfurnished, letting you design your own ocean sanctuary.",
                rent = 1800.0,
                deposit = 1800.0,
                imageUrls = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80",
                address = "85 Marine Drive, Miami, FL",
                lat = 25.7617,
                lng = -80.1918,
                houseType = "Apartment",
                bedrooms = 2,
                bathrooms = 2,
                furnished = false,
                parking = true,
                petFriendly = true,
                wifi = false,
                security = true,
                landlordId = landlord2Id,
                isApproved = false, // Pending admin approval to demonstrate the Admin approval flow!
                isPromoted = false,
                nearbySchools = "Miami Beach Senior High, MAST Academy",
                nearbyHospitals = "Mount Sinai Medical, Mercy Miami",
                ratingsCount = 0,
                averageRating = 0.0f
            )
        )

        val insertedProperties = mutableListOf<Long>()
        for (p in props) {
            val pId = propertyDao.insertProperty(p)
            insertedProperties.add(pId)
        }

        // 3. Seed some initial applications to display in Landlord/Tenant/Admin views
        val app1 = Application(
            propertyId = insertedProperties[0].toInt(), // Penthouse
            tenantId = tenantId,
            landlordId = landlord1Id,
            status = "PENDING",
            coverLetter = "Hello Richard, I am Stuart and I love this sky penthouse. I am a tech consultant looking for a 1-year lease starting next week. I have uploaded my pay stubs and ID. Looking forward to your visit schedule!",
            visitDate = "2026-05-28",
            visitTime = "14:00",
            uploadedDocs = "stuart_identity.pdf, income_statements_q1.pdf"
        )
        val app2 = Application(
            propertyId = insertedProperties[1].toInt(), // Sub-urban
            tenantId = tenantId,
            landlordId = landlord1Id,
            status = "APPROVED",
            coverLetter = "Hi, I am looking to secure this beautiful suburban home for me and my golden retriever. He will love the garden!",
            visitDate = "2026-05-20",
            visitTime = "10:30",
            uploadedDocs = "stuart_id.pdf"
        )
        applicationDao.insertApplication(app1)
        applicationDao.insertApplication(app2)

        // 4. Seed Messages
        val msg1 = Message(
            senderId = tenantId,
            receiverId = landlord1Id,
            messageText = "Hello Richard! The Lumina Sky Penthouse looks absolute dream. Is the parking space included in the rent?",
            timestamp = System.currentTimeMillis() - 3600000 * 24 // 1 day ago
        )
        val msg2 = Message(
            senderId = landlord1Id,
            receiverId = tenantId,
            messageText = "Hello Stuart! Yes, indeed. You get two underground parking garage bays with full EV charging support included.",
            timestamp = System.currentTimeMillis() - 3600000 * 20, // 20 hrs ago
            isRead = true
        )
        val msg3 = Message(
            senderId = tenantId,
            receiverId = landlord1Id,
            messageText = "That's fantastic. I've submitted my application. Let me know when is a good day for visit.",
            timestamp = System.currentTimeMillis() - 3600000 * 5, // 5 hours ago
            isRead = true
        )
        messageDao.insertMessage(msg1)
        messageDao.insertMessage(msg2)
        messageDao.insertMessage(msg3)

        // 5. Seed Payments
        val pay1 = Payment(
            propertyId = insertedProperties[1].toInt(),
            tenantId = tenantId,
            amount = 2500.0,
            paymentType = "Deposit",
            paymentMethod = "Visa/Mastercard",
            date = "2026-05-18",
            qrcode = "LIVING-PAY-DEP-99120485"
        )
        val pay2 = Payment(
            propertyId = insertedProperties[1].toInt(),
            tenantId = tenantId,
            amount = 2100.0,
            paymentType = "Rent (June 2026)",
            paymentMethod = "Mobile Money",
            date = "2026-05-19",
            qrcode = "LIVING-PAY-RNT-48529241"
        )
        paymentDao.insertPayment(pay1)
        paymentDao.insertPayment(pay2)

        // 6. Seed Reviews
        val rev1 = Review(
            reviewerId = tenantId,
            reviewerName = "Stuart Don",
            targetId = insertedProperties[0].toInt(),
            targetType = "PROPERTY",
            rating = 5,
            comment = "An absolutely spectacular property. Floor-to-ceiling windows and stunning Material glassmorphism aesthetics in every corner!"
        )
        val rev2 = Review(
            reviewerId = landlord1Id,
            reviewerName = "Richard Vance",
            targetId = tenantId,
            targetType = "USER",
            rating = 5,
            comment = "Stuart has been an exemplary tenant, extremely tidy, on-time rent payments, and excellent communication. Highly recommended!"
        )
        reviewDao.insertReview(rev1)
        reviewDao.insertReview(rev2)
    }
}
