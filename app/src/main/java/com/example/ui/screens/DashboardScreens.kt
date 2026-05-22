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
    var imageListUrls by remember { mutableStateOf("") }

    var hType by remember { mutableStateOf("Apartment") }
    var bedsVal by remember { mutableStateOf(1) }
    var bathsVal by remember { mutableStateOf(1) }

    var isFurnished by remember { mutableStateOf(false) }
    var hasPetPolicy by remember { mutableStateOf(false) }
    var hasWifiVal by remember { mutableStateOf(false) }
    var hasParkingVal by remember { mutableStateOf(false) }
    var hasSecureSec by remember { mutableStateOf(false) }

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
            Text(text = "Upload Visual Real Estate", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Listing Headline Title") },
            placeholder = { Text("E.g. Penthouse Oasis, Sea Crest...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_property_input_title"),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Sanctuary Narrative Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = rentPrice,
                onValueChange = { rentPrice = it },
                label = { Text("Rent Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = depPrice,
                onValueChange = { depPrice = it },
                label = { Text("Deposit Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        OutlinedTextField(
            value = addressStr,
            onValueChange = { addressStr = it },
            label = { Text("Apartment Coordinate Address") },
            placeholder = { Text("E.g. 10 Pine Avenue, San Jose") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = imageListUrls,
            onValueChange = { imageListUrls = it },
            label = { Text("Visual Gallery links (Comma-separated)") },
            placeholder = { Text("Paste unsplash URLs links...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (title.isNotEmpty() && rentPrice.isNotEmpty()) {
                    viewModel.landlordAddProperty(
                        title = title,
                        description = desc.ifEmpty { "A charming listed Living rental." },
                        rent = rentPrice.toDoubleOrNull() ?: 1200.0,
                        deposit = depPrice.toDoubleOrNull() ?: 1200.0,
                        imageStrList = imageListUrls,
                        address = addressStr.ifEmpty { "San Jose, CA" },
                        houseType = hType,
                        beds = bedsVal,
                        baths = bathsVal,
                        furnished = isFurnished,
                        parking = hasParkingVal,
                        pet = hasPetPolicy,
                        wifi = hasWifiVal,
                        security = hasSecureSec,
                        onSuccess = onSuccess
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("add_property_submit_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
        ) {
            Text("Publish real estate listing", fontWeight = FontWeight.Bold)
        }
    }
}
