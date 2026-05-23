package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.data.model.Application
import com.example.data.model.Payment
import com.example.data.model.Property
import com.example.data.model.User
import com.example.ui.theme.LivingOrangeSecondary
import com.example.ui.theme.LivingTealPrimary
import com.example.ui.viewmodel.LivingViewModel

// ==========================================
// 1. LANDLORD DASHBOARD VIEW
// ==========================================
@Composable
fun LandlordDashboardView(
    viewModel: LivingViewModel,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToChat: (Int) -> Unit
) {
    val properties by viewModel.allProperties.collectAsState()
    val applications by viewModel.landlordApplications.collectAsState()
    val userSession by viewModel.currentUser.collectAsState()

    val landlordProperties = properties.filter { it.landlordId == (userSession?.id ?: 0) }
    val totalIncome = landlordProperties.sumOf { if (it.isRented) it.rent else 0.0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("landlord_scroll_view"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming overview
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(LivingTealPrimary.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PORTFOLIO DASHBOARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Richard Vance",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Status",
                                tint = LivingTealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=120&q=80",
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Analytics Cards grid
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(text = "Overview Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Active Listings",
                        value = "${landlordProperties.size}",
                        icon = Icons.Default.MapsHomeWork,
                        color = LivingTealPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Monthly Income",
                        value = "$${totalIncome.toInt()}",
                        icon = Icons.Default.Paid,
                        color = LivingOrangeSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Vacant Spaces",
                        value = "${landlordProperties.count { !it.isRented }}",
                        icon = Icons.Default.Bed,
                        color = Color(0xFFF16E24),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Active Tenants",
                        value = "${landlordProperties.count { it.isRented }}",
                        icon = Icons.Default.People,
                        color = LivingTealPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Beautiful Interactive Micro Charts block
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(text = "Revenue Historical Trends ($)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MetricLineChart(
                            points = listOf(500f, 1200f, 1800f, 2400f, 2100f, totalIncome.toFloat()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Dec", style = MaterialTheme.typography.labelSmall)
                            Text(text = "Jan", style = MaterialTheme.typography.labelSmall)
                            Text(text = "Feb", style = MaterialTheme.typography.labelSmall)
                            Text(text = "Mar", style = MaterialTheme.typography.labelSmall)
                            Text(text = "Apr", style = MaterialTheme.typography.labelSmall)
                            Text(text = "May (Now)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LivingTealPrimary)
                        }
                    }
                }
            }
        }

        // Add Property CTA bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Your Apartment Listings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onNavigateToAddProperty,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
                    modifier = Modifier.testTag("add_property_fab_trigger")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Space")
                }
            }
        }

        // Landlord listings vertical roster block
        if (landlordProperties.isEmpty()) {
            item {
                EmptyStatePlaceholder(
                    title = "You have no properties listed",
                    subtitle = "List your real estate spaces easily and attract Verified tenants globally.",
                    icon = Icons.Default.AddHomeWork
                )
            }
        } else {
            items(landlordProperties) { prop ->
                LandlordPropertyListItem(
                    property = prop,
                    onToggleRented = { viewModel.toggleProductRented(prop) }
                )
            }
        }

        // Tenant Applicants Node
        item {
            Text(
                text = "Pending Tenant Applications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        if (applications.none { it.status == "PENDING" }) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(text = "No pending applicant actions currently.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            items(applications.filter { it.status == "PENDING" }) { app ->
                ApplicantCardItem(
                    application = app,
                    onApprove = { viewModel.updateApplicationStatus(app.id, app, "APPROVED") },
                    onReject = { viewModel.updateApplicationStatus(app.id, app, "REJECTED") },
                    onContact = { onNavigateToChat(app.tenantId) }
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, tonalElevation = 3.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }

            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LandlordPropertyListItem(
    property: Property,
    onToggleRented: () -> Unit
) {
    val firstImg = property.imageUrls.split(",").firstOrNull() ?: ""

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = firstImg,
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = property.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "$${property.rent.toInt()}/mo", style = MaterialTheme.typography.bodySmall, color = LivingTealPrimary)
                    // Status Badge
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (property.isApproved) LivingTealPrimary else Color.Gray, CircleShape)
                        )
                        Text(
                            text = if (property.isApproved) "Active Match Feed" else "Pending Admin Approval",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (property.isApproved) LivingTealPrimary else Color.Gray
                        )
                    }
                }
            }

            Button(
                onClick = onToggleRented,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (property.isRented) Color.Gray else LivingOrangeSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (property.isRented) "Mark Available" else "Mark Rented",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ApplicantCardItem(
    application: com.example.data.model.Application,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onContact: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)) {
                        AsyncImage(model = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=80", contentDescription = "Tenant")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Stuart Don", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(text = "Verifiably approved on May 2026", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                IconButton(onClick = onContact, colors = IconButtonDefaults.iconButtonColors(containerColor = LivingTealPrimary.copy(alpha = 0.1f))) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat", tint = LivingTealPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = "\"${application.coverLetter}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "📅 Visit: ${application.visitDate}", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                }
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "📂 Docs: stuart_identity_card_verified.pdf", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reject", fontSize = 12.sp)
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                ) {
                    Text("Approve Tenant", fontSize = 12.sp)
                }
            }
        }
    }
}


// ==========================================
// 2. ADMIN DASHBOARD VIEW
// ==========================================
@Composable
fun AdminDashboardView(
    viewModel: LivingViewModel
) {
    val properties by viewModel.allProperties.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val payments by viewModel.allPaymentsInSystem.collectAsState()

    val pendingProperties = properties.filter { !it.isApproved }
    val landlords = allUsers.filter { it.role == "LANDLORD" }
    val tenants = allUsers.filter { it.role == "TENANT" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_scroll_view"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming overview
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(LivingTealPrimary.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "ADMINISTRATIVE CONTROL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Management Tower",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // Broad Stats Row
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(text = "Global Platform Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminStatCard(label = "Total Landlords", value = "${landlords.size}", icon = Icons.Default.ManageAccounts, modifier = Modifier.weight(1f))
                    AdminStatCard(label = "Total Tenants", value = "${tenants.size}", icon = Icons.Default.PeopleAlt, modifier = Modifier.weight(1f))
                    AdminStatCard(label = "Listed Houses", value = "${properties.size}", icon = Icons.Default.HomeWork, modifier = Modifier.weight(1f))
                }
            }
        }

        // Moderation Queue Section
        item {
            Text(
                text = "Moderation Queue (Listings Approval Required)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        if (pendingProperties.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(text = "No properties pending approval. Platform catalog looks immaculate!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            items(pendingProperties) { p ->
                AdminModerateCardItem(
                    property = p,
                    onApprove = { viewModel.adminReviewProperty(p, true) },
                    onReject = { viewModel.adminReviewProperty(p, false) }
                )
            }
        }

        // User Verification verification table
        item {
            Text(
                text = "Landlord Verification Approvals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        val unverifiedLandlords = landlords.filter { it.verificationState == "PENDING" }
        if (unverifiedLandlords.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(text = "All active landlords are vetted and verified.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            items(unverifiedLandlords) { l ->
                AdminVerifyLandlordRow(
                    landlord = l,
                    onVerify = { viewModel.adminVerifyUser(l) }
                )
            }
        }

        // Transaction receipts audit
        item {
            Text(
                text = "Financial Payments Ledgers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        if (payments.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(text = "No payments ledger lines saved.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            items(payments) { p ->
                AdminPaymentLedgerRow(payment = p)
            }
        }
    }
}

@Composable
fun AdminStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, tonalElevation = 3.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = LivingTealPrimary, modifier = Modifier.size(20.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AdminModerateCardItem(
    property: Property,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val img = property.imageUrls.split(",").firstOrNull() ?: ""

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = property.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = "Address: ${property.address}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(text = "$${property.rent.toInt()}", style = MaterialTheme.typography.titleMedium, color = LivingTealPrimary, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(model = img, contentDescription = null, contentScale = ContentScale.Crop)
                }

                Text(
                    text = property.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reject Space", fontSize = 12.sp)
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                ) {
                    Text("Approve Listing", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AdminVerifyLandlordRow(
    landlord: User,
    onVerify: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape)) {
                    AsyncImage(model = landlord.avatarUrl, contentDescription = "LandlordAvatar")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = landlord.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = "Company: ${landlord.companyName.ifEmpty { "Independent" }}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            Button(
                onClick = onVerify,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
            ) {
                Text("Verify Vetting Badge", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun AdminPaymentLedgerRow(
    payment: Payment
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = payment.paymentType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = "Date: ${payment.date} via ${payment.paymentMethod}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$${payment.amount.toInt()}", style = MaterialTheme.typography.bodyMedium, color = LivingTealPrimary, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(LivingTealPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(text = "COMPLETED", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = LivingTealPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Direct media model for simulated uploads ---
data class DirectMediaItem(
    val name: String,
    val sizeMB: Double,
    val format: String,
    val isVideo: Boolean,
    val url: String = ""
)

// --- Form segment: Add Property Listing ---
@Composable
fun AddPropertyListingFormScreen(
    viewModel: LivingViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var rentPrice by remember { mutableStateOf("") }
    var depPrice by remember { mutableStateOf("") }
    var addressStr by remember { mutableStateOf("") }

    var hType by remember { mutableStateOf("Apartment") }
    var bedsVal by remember { mutableStateOf(1) }
    var bathsVal by remember { mutableStateOf(1) }

    var isFurnished by remember { mutableStateOf(false) }
    var hasPetPolicy by remember { mutableStateOf(false) }
    var hasWifiVal by remember { mutableStateOf(false) }
    var hasParkingVal by remember { mutableStateOf(false) }
    var hasSecureSec by remember { mutableStateOf(false) }

    // Uganda Kampala specific premium fields
    var waterSource by remember { mutableStateOf("NWSC Running Water Tap") }
    var electricityMeter by remember { mutableStateOf("Umeme Yaka Pre-paid Meter") }
    var roadAccess by remember { mutableStateOf("Paved Tarmac Access Road") }
    var securityFence by remember { mutableStateOf(true) }
    var paymentInstallments by remember { mutableStateOf("3 Months Upfront") }

    // Simulated multi-format media upload list
    var mediaList by remember {
        mutableStateOf(
            listOf(
                DirectMediaItem("lounge_view_hq.jpg", 4.2, "JPEG", false, "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80"),
                DirectMediaItem("balcony_cityview.png", 8.5, "PNG", false, "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80"),
                DirectMediaItem("house_walkthrough_1080p.mp4", 124.0, "MP4", true, "https://assets.mixkit.co/videos/preview/mixkit-luxury-resort-or-hotel-room-41983-large.mp4")
            )
        )
    }

    // Interactive uploader interface state
    var newMediaName by remember { mutableStateOf("") }
    var newMediaSizeMB by remember { mutableStateOf("15") }
    var newMediaFormat by remember { mutableStateOf("MP4") }
    var newMediaIsVideo by remember { mutableStateOf(true) }

    val fileValidationErrors = mediaList.filter { it.sizeMB >= 1024.0 }
    val isAnyOversizedFile = fileValidationErrors.isNotEmpty()
    val pictureCount = mediaList.count { !it.isVideo }
    val isUnderThreePictures = pictureCount < 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Publish Ugandan Property", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Listing Headline Title (e.g. Elegant 2-Bed in Muyenga)") },
            placeholder = { Text("E.g. Muyenga Hillside Apartments...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_property_input_title"),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Sanctuary Narrative Description") },
            placeholder = { Text("Describe the spacing, security, and surrounding mood...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = rentPrice,
                onValueChange = { rentPrice = it },
                label = { Text("Rent (UGX / month)") },
                placeholder = { Text("e.g. 1,200,000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = depPrice,
                onValueChange = { depPrice = it },
                label = { Text("Required Security Deposit") },
                placeholder = { Text("e.g. 600,000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        OutlinedTextField(
            value = addressStr,
            onValueChange = { addressStr = it },
            label = { Text("Apartment Physical Address / Plot No.") },
            placeholder = { Text("E.g. Plot 42, Muyenga Tank Hill Road, Kampala") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // -------------------------------------------------------------
        // KAMPALA CUSTOM HOME ATTRIBUTES & SYSTEMS (WHAT HAVE I LEFT?)
        // -------------------------------------------------------------
        Text(
            text = "Essential Domestic Utility Systems (Kampala)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LivingTealPrimary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Water Supply Selection
            Column {
                Text(text = "Water Supply System Type:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NWSC Running Water Tap", "Private Borehole Pump", "Gated Compound Storage Tanks", "NWSC Connected with Backup Tanks").forEach { valItem ->
                        val isSel = waterSource == valItem
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) LivingTealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSel) LivingTealPrimary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .clickable { waterSource = valItem }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(text = valItem, style = MaterialTheme.typography.bodySmall, color = if (isSel) LivingTealPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Power System Type
            Column {
                Text(text = "Electricity Metering System:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Umeme Yaka Pre-paid Meter", "Post-paid Umeme Grid Meter", "Umeme Grid + Automated Solar Backup").forEach { valItem ->
                        val isSel = electricityMeter == valItem
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) LivingTealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSel) LivingTealPrimary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .clickable { electricityMeter = valItem }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(text = valItem, style = MaterialTheme.typography.bodySmall, color = if (isSel) LivingTealPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Road Access
            Column {
                Text(text = "Road Connection & Access Quality:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Paved Tarmac Access Road", "Well-maintained Murram Road", "Gravel Private Lane", "Easy Main Bypass Proximity").forEach { valItem ->
                        val isSel = roadAccess == valItem
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) LivingTealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSel) LivingTealPrimary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .clickable { roadAccess = valItem }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(text = valItem, style = MaterialTheme.typography.bodySmall, color = if (isSel) LivingTealPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Standard Payment Installments
            Column {
                Text(text = "Default Payment Cycle / Billing Plan:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1 Month Upfront", "3 Months Upfront", "6 Months Upfront (Uganda Standard)", "Flexible Term Installments").forEach { valItem ->
                        val isSel = paymentInstallments == valItem
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) LivingTealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSel) LivingTealPrimary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .clickable { paymentInstallments = valItem }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(text = valItem, style = MaterialTheme.typography.bodySmall, color = if (isSel) LivingTealPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Compound Gate Check
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { securityFence = !securityFence }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Security Brick Wall & Gated Compound", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(text = "Includes perimeter security wire mesh & secure gate gatekeeper setup", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Switch(
                    checked = securityFence,
                    onCheckedChange = { securityFence = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = LivingTealPrimary)
                )
            }
        }

        // Structure selectors
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Dropdown-like House Type
            Column(modifier = Modifier.weight(1.5f)) {
                Text(text = "Structure Category", style = MaterialTheme.typography.labelMedium)
                listOf("Apartment", "House", "Penthouse", "Studio").forEach { type ->
                    Row(
                        modifier = Modifier
                            .clickable { hType = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = hType == type, onClick = { hType = type })
                        Text(text = type, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Bedrooms", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (bedsVal > 1) bedsVal-- }) { Icon(imageVector = Icons.Default.Remove, contentDescription = null) }
                    Text(text = "$bedsVal", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { bedsVal++ }) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
                }

                Text(text = "Bathrooms", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (bathsVal > 1) bathsVal-- }) { Icon(imageVector = Icons.Default.Remove, contentDescription = null) }
                    Text(text = "$bathsVal", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { bathsVal++ }) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
                }
            }
        }

        // Amenities toggling grid
        Column {
            Text(text = "Conveniences highlights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row {
                Checkbox(checked = isFurnished, onCheckedChange = { isFurnished = it })
                Text(text = "Furnished Room Fittings", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                Checkbox(checked = hasPetPolicy, onCheckedChange = { hasPetPolicy = it })
                Text(text = "Pet Friendly Spaces Allowed", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                Checkbox(checked = hasWifiVal, onCheckedChange = { hasWifiVal = it })
                Text(text = "Superfast WiFi Connected", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                Checkbox(checked = hasParkingVal, onCheckedChange = { hasParkingVal = it })
                Text(text = "Dedicated Reserved Parking", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        // -------------------------------------------------------------
        // PHOTOS AND VIDEOS UPLOADER (MAX 1GB ENFORCEMENT)
        // -------------------------------------------------------------
        Text(
            text = "Smart Media Upload Gateway (Photos & Videos)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LivingTealPrimary,
            modifier = Modifier.padding(top = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = if (isAnyOversizedFile) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add photos or videos of any format below. The system restricts any high-definition files over 1GB to optimize client network performance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Live uploaded roster
            Text(
                text = "Active Media Playlist Queue (${mediaList.size} files):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            mediaList.forEach { mediaItem ->
                val isTooBig = mediaItem.sizeMB >= 1024.0
                val accent = if (isTooBig) MaterialTheme.colorScheme.error else LivingTealPrimary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = (if (isTooBig) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            color = if (isTooBig) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (mediaItem.isVideo) Icons.Default.Movie else Icons.Default.Photo,
                            contentDescription = "Format",
                            tint = accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = mediaItem.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .background(accent.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(text = mediaItem.format, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = accent)
                                }
                            }
                            val sizeText = if (mediaItem.sizeMB >= 1024.0) {
                                String.format("%.2f GB (OVER 1GB EXCEEDED)", mediaItem.sizeMB / 1024.0)
                            } else {
                                String.format("%.1f MB", mediaItem.sizeMB)
                            }
                            Text(
                                text = "File Size: $sizeText",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTooBig) MaterialTheme.colorScheme.error else Color.Gray,
                                fontWeight = if (isTooBig) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    IconButton(onClick = {
                        mediaList = mediaList.filter { it.name != mediaItem.name }
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (isAnyOversizedFile) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Text(text = "Opload Blocked: Content exceeds 1GB limit", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Text(
                            text = "To maintain rapid data streaming for tenants on lower bandwidth networks, all videos and photos must remain under 1GB (1024 MB). Please delete oversized media files to publish.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (isUnderThreePictures) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("less_than_three_photos_error_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Text(text = "Mandatory Picture Minimum Limit: At least 3 pictures required", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Text(
                            text = "To guarantee property authenticity, listings must contain at least three (3) pictures captured or uploaded from the device. Current picture count: $pictureCount of 3.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Media Quick-Add Presets Drawer
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Text(text = "Simulated Device Capture & Upload Controls:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val randomized = (10..99).random()
                        val item = DirectMediaItem("device_camera_captured_$randomized.jpg", 3.8, "JPG", false, "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80")
                        mediaList = mediaList + item
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("device_capture_btn")
                ) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Capture Camera Photo (.JPG)", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val randomized = (10..99).random()
                        val item = DirectMediaItem("device_gallery_selection_$randomized.png", 2.5, "PNG", false, "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80")
                        mediaList = mediaList + item
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("device_gallery_btn")
                ) {
                    Icon(imageVector = Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Select Gallery Image (.PNG)", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val randomized = (10..99).random()
                        val item = DirectMediaItem("kitchen_panning_$randomized.mp4", 450.0, "MP4", true, "https://assets.mixkit.co/videos/preview/mixkit-luxury-resort-or-hotel-room-41983-large.mp4")
                        mediaList = mediaList + item
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add 450MB Video (.MP4)", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val randomized = (10..99).random()
                        val item = DirectMediaItem("uncompressed_drone_tour_$randomized.mkv", 1120.0, "MKV", true, "https://assets.mixkit.co/videos/preview/mixkit-luxury-resort-or-hotel-room-41983-large.mp4")
                        mediaList = mediaList + item
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add 1.1GB Video (.MKV)", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Custom manual selector input
            OutlinedTextField(
                value = newMediaName,
                onValueChange = { newMediaName = it },
                label = { Text("Custom File Mock Name") },
                placeholder = { Text("e.g. bathroom_closeup.png, tour.avi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        if (newMediaName.isNotEmpty()) {
                            val isVid = newMediaName.endsWith(".mp4") || newMediaName.endsWith(".mkv") || newMediaName.endsWith(".avi") || newMediaName.endsWith(".mov") || newMediaIsVideo
                            val size = newMediaSizeMB.toDoubleOrNull() ?: 15.0
                            val ext = newMediaName.substringAfterLast('.').uppercase().ifEmpty { newMediaFormat }
                            mediaList = mediaList + DirectMediaItem(
                                name = newMediaName,
                                sizeMB = size,
                                format = ext,
                                isVideo = isVid,
                                url = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80"
                            )
                            newMediaName = ""
                        }
                    }) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Add File")
                    }
                }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newMediaSizeMB,
                    onValueChange = { newMediaSizeMB = it },
                    label = { Text("Mock Size (MB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = newMediaFormat,
                    onValueChange = { newMediaFormat = it },
                    label = { Text("Format Extension") },
                    placeholder = { Text("MP4 / PNG / MKV") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Is file a video clip?", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Checkbox(checked = newMediaIsVideo, onCheckedChange = { newMediaIsVideo = it })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (title.isNotEmpty() && rentPrice.isNotEmpty() && !isAnyOversizedFile && !isUnderThreePictures) {
                    val imagesStr = mediaList.filter { !it.isVideo }.map { it.url.ifEmpty { "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80" } }.joinToString(",")
                    val videosStr = mediaList.filter { it.isVideo }.map { it.url.ifEmpty { "https://assets.mixkit.co/videos/preview/mixkit-luxury-resort-or-hotel-room-41983-large.mp4" } }.joinToString(",")
                    val videoSizes = mediaList.filter { it.isVideo }.map { "${it.sizeMB} MB" }.joinToString(",")

                    viewModel.landlordAddProperty(
                        title = title,
                        description = desc.ifEmpty { "A charming listed Living rental." },
                        rent = rentPrice.toDoubleOrNull() ?: 1200000.0,
                        deposit = depPrice.toDoubleOrNull() ?: 600000.0,
                        imageStrList = imagesStr.ifEmpty { "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80" },
                        address = addressStr.ifEmpty { "Muyenga Plot 12, Kampala" },
                        houseType = hType,
                        beds = bedsVal,
                        baths = bathsVal,
                        furnished = isFurnished,
                        parking = hasParkingVal,
                        pet = hasPetPolicy,
                        wifi = hasWifiVal,
                        security = hasSecureSec,
                        videoUrls = videosStr,
                        videoSizesStr = videoSizes,
                        waterSource = waterSource,
                        electricityMeter = electricityMeter,
                        roadAccess = roadAccess,
                        securityFence = securityFence,
                        paymentInstallments = paymentInstallments,
                        onSuccess = onSuccess
                    )
                }
            },
            enabled = title.isNotEmpty() && rentPrice.isNotEmpty() && !isAnyOversizedFile && !isUnderThreePictures,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("add_property_submit_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
        ) {
            val buttonText = if (isUnderThreePictures) "At least 3 pictures required ($pictureCount/3)" else "Publish real estate listing"
            Text(buttonText, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 6. ADMIN USER SECURITY & ROLES PANEL
// ==========================================
@Composable
fun AdminUsersManagementView(
    viewModel: LivingViewModel,
    onLogout: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val session by viewModel.currentUser.collectAsState()
    
    var subTab by remember { mutableStateOf("ROLES") } // ROLES, MY_PROFILE
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<String?>(null) } // null = All, "TENANT", "LANDLORD", "ADMIN"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Navigation tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (subTab == "ROLES") LivingTealPrimary else Color.Transparent)
                    .clickable { subTab = "ROLES" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = if (subTab == "ROLES") Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "User Security & Roles",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (subTab == "ROLES") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (subTab == "MY_PROFILE") LivingTealPrimary else Color.Transparent)
                    .clickable { subTab = "MY_PROFILE" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (subTab == "MY_PROFILE") Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "My Profile Settings",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (subTab == "MY_PROFILE") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (subTab == "MY_PROFILE") {
            // Render original profile screen
            UserProfileSettingsView(viewModel = viewModel, onLogout = onLogout)
        } else {
            // Filters and Search section for Directory
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "System User Credentials Control",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LivingTealPrimary
                )
                Text(
                    text = "Audit registered platforms users, change security levels, or impersonate specific user accounts to act as their roles temporarily.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, email, phone...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Quick role filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    listOf(null, "TENANT", "LANDLORD", "ADMIN").forEach { role ->
                        val isSelected = selectedRoleFilter == role
                        val label = role ?: "All Users"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LivingTealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(1.dp, if (isSelected) LivingTealPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedRoleFilter = role }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) LivingTealPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Users List
            val filteredUsers = allUsers.filter { user ->
                val matchesSearch = searchQuery.isEmpty() ||
                        user.name.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true) ||
                        user.phone.contains(searchQuery, ignoreCase = true)
                val matchesRole = selectedRoleFilter == null || user.role == selectedRoleFilter
                matchesSearch && matchesRole
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredUsers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No registered users match search criteria.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(filteredUsers) { user ->
                        AdminUserCardItem(
                            user = user,
                            currentUser = session,
                            onUpdateRole = { newRole ->
                                viewModel.updateUserRole(user.id, newRole)
                            },
                            onImpersonate = {
                                viewModel.impersonateUser(user)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserCardItem(
    user: User,
    currentUser: User?,
    onUpdateRole: (String) -> Unit,
    onImpersonate: () -> Unit
) {
    var showRolePickerDialog by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle initials avatar
                val initials = user.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = when (user.role) {
                                "ADMIN" -> Color(0xFFE53935).copy(alpha = 0.15f)
                                "LANDLORD" -> LivingTealPrimary.copy(alpha = 0.15f)
                                else -> LivingOrangeSecondary.copy(alpha = 0.15f)
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = when (user.role) {
                                "ADMIN" -> Color(0xFFE53935)
                                "LANDLORD" -> LivingTealPrimary
                                else -> LivingOrangeSecondary
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.ifEmpty { "?" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (user.role) {
                            "ADMIN" -> Color(0xFFE53935)
                            "LANDLORD" -> LivingTealPrimary
                            else -> LivingOrangeSecondary
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Credentials info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.phone.isNotEmpty()) {
                        Text(
                            text = "Tel: ${user.phone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Current Active Role Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (user.role) {
                                "ADMIN" -> Color(0xFFE53935).copy(alpha = 0.10f)
                                "LANDLORD" -> LivingTealPrimary.copy(alpha = 0.10f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .border(
                            1.dp,
                            when (user.role) {
                                "ADMIN" -> Color(0xFFE53935)
                                "LANDLORD" -> LivingTealPrimary
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = user.role,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (user.role) {
                            "ADMIN" -> Color(0xFFE53935)
                            "LANDLORD" -> LivingTealPrimary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Row of administrative actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to change role
                OutlinedButton(
                    onClick = { showRolePickerDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Change Role", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // "Do Role" / Impersonate accounts button
                if (user.id != currentUser?.id) {
                    Button(
                        onClick = onImpersonate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LivingTealPrimary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("act_as_button_${user.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Act As User (Do Role)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "(This is you)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }
        }
    }

    if (showRolePickerDialog) {
        AlertDialog(
            onDismissRequest = { showRolePickerDialog = false },
            title = { Text("Change Account Role", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select the new security and access permission level for ${user.name}:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf("TENANT", "LANDLORD", "ADMIN").forEach { roleName ->
                        val isCurrent = user.role == roleName
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) LivingTealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, if (isCurrent) LivingTealPrimary else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdateRole(roleName)
                                    showRolePickerDialog = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when(roleName) {
                                            "ADMIN" -> Icons.Default.AdminPanelSettings
                                            "LANDLORD" -> Icons.Default.HomeWork
                                            else -> Icons.Default.PeopleAlt
                                        },
                                        contentDescription = null,
                                        tint = if (isCurrent) LivingTealPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(roleName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isCurrent) LivingTealPrimary else MaterialTheme.colorScheme.onSurface)
                                }
                                if (isCurrent) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = LivingTealPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRolePickerDialog = false }) {
                    Text("Cancel", color = LivingTealPrimary)
                }
            }
        )
    }
}
