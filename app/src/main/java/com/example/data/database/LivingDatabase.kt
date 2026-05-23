package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY isPromoted DESC, id DESC")
    fun getAllPropertiesFlow(): Flow<List<Property>>

    @Query("SELECT * FROM properties")
    suspend fun getAllProperties(): List<Property>

    @Query("SELECT * FROM properties WHERE isApproved = 1 ORDER BY isPromoted DESC, id DESC")
    fun getAllApprovedPropertiesFlow(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE landlordId = :landlordId")
    fun getPropertiesByLandlord(landlordId: Int): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyById(id: Int): Flow<Property?>

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyByIdSync(id: Int): Property?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property): Long

    @Update
    suspend fun updateProperty(property: Property)

    @Delete
    suspend fun deleteProperty(property: Property)
}

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY id DESC")
    fun getAllApplicationsFlow(): Flow<List<Application>>

    @Query("SELECT * FROM applications WHERE tenantId = :tenantId ORDER BY id DESC")
    fun getApplicationsByTenant(tenantId: Int): Flow<List<Application>>

    @Query("SELECT * FROM applications WHERE landlordId = :landlordId ORDER BY id DESC")
    fun getApplicationsByLandlord(landlordId: Int): Flow<List<Application>>

    @Query("SELECT * FROM applications WHERE propertyId = :propertyId")
    fun getApplicationsByProperty(propertyId: Int): Flow<List<Application>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(app: Application): Long

    @Update
    suspend fun updateApplication(app: Application)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :u1 AND receiverId = :u2) OR (senderId = :u2 AND receiverId = :u1) ORDER BY timestamp ASC")
    fun getConversationFlow(u1: Int, u2: Int): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getMessagesForUser(userId: Int): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long
}

@Dao
interface SavedPropertyDao {
    @Query("SELECT * FROM saved_properties WHERE tenantId = :tenantId")
    fun getSavedByTenant(tenantId: Int): Flow<List<SavedProperty>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_properties WHERE tenantId = :tenantId AND propertyId = :propertyId)")
    suspend fun isSaved(tenantId: Int, propertyId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaved(saved: SavedProperty)

    @Query("DELETE FROM saved_properties WHERE tenantId = :tenantId AND propertyId = :propertyId")
    suspend fun deleteSaved(tenantId: Int, propertyId: Int)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY id DESC")
    fun getPaymentsByTenant(tenantId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPaymentsFlow(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE targetId = :targetId AND targetType = :targetType ORDER BY timestamp DESC")
    fun getReviews(targetId: Int, targetType: String): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long
}

@Database(
    entities = [
        User::class,
        Property::class,
        Application::class,
        Message::class,
        SavedProperty::class,
        Payment::class,
        Review::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LivingDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun propertyDao(): PropertyDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun messageDao(): MessageDao
    abstract fun savedPropertyDao(): SavedPropertyDao
    abstract fun paymentDao(): PaymentDao
    abstract fun reviewDao(): ReviewDao
}
