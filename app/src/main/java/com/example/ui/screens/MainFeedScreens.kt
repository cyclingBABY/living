package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.map
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Property
import com.example.data.model.Review
import com.example.ui.theme.LivingOrangeSecondary
import com.example.ui.theme.LivingTealPrimary
import com.example.ui.theme.LivingTealLight
import com.example.ui.viewmodel.LivingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantHomeScreen(
    viewModel: LivingViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onOpenSearchFilters: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val properties by viewModel.filteredProperties.collectAsState()
    val savedIds by viewModel.savedPropertyIds.collectAsState()
    val focusManager = LocalFocusManager.current

    val promotedProperties = properties.filter { it.isPromoted }
    val regularProperties = properties.filter { !it.isPromoted }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Apartment", "House", "Penthouse", "Studio")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tenant_home_scroll"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Beautiful Header Welcome Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
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
                            text = "FIND YOUR SANCTUARY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Living",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(LivingTealLight)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(LivingTealPrimary, LivingOrangeSecondary)
                                    )
                                )
                        )
                    }
                }
            }
        }

        // 2. Search & Categories Filter bar
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.searchQuery.value = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field_input"),
                    placeholder = { Text("Search by city, neighborhood, or building...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = onOpenSearchFilters,
                            modifier = Modifier.testTag("filter_slider_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Advanced Filters",
                                tint = LivingTealPrimary
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable categories tag rows
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        val bg = if (isSelected) LivingTealPrimary else MaterialTheme.colorScheme.surface
                        val tc = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        val borderMod = if (isSelected) Modifier else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))

                        Box(
                            modifier = Modifier
                                .then(borderMod)
                                .clip(RoundedCornerShape(20.dp))
                                .background(bg)
                                .clickable {
                                    selectedCategory = cat
                                    viewModel.selectedHouseType.value = if (cat == "All") null else cat
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = tc
                            )
                        }
                    }
                }
            }
        }

        // 3. Promoted House Carousels
        if (promotedProperties.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Featured Bazaars",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Featured",
                                tint = LivingOrangeSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Premium Match",
                                style = MaterialTheme.typography.labelSmall,
                                color = LivingOrangeSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(promotedProperties) { prop ->
                            PromotedPropertyCard(
                                property = prop,
                                isSaved = prop.id in savedIds,
                                onSaveToggle = { viewModel.toggleSaveProperty(prop.id) },
                                onClick = { onNavigateToDetails(prop.id) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Regular Recommendation grid
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Recently Added & Recommended",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (regularProperties.isEmpty()) {
            item {
                EmptyStatePlaceholder(
                    title = "No apartments match your search",
                    subtitle = "Try resetting filters or expanding budget scope to find options.",
                    icon = Icons.Outlined.SearchOff
                )
            }
        } else {
            items(regularProperties) { prop ->
                RegularPropertyRowItem(
                    property = prop,
                    isSaved = prop.id in savedIds,
                    onSaveToggle = { viewModel.toggleSaveProperty(prop.id) },
                    onClick = { onNavigateToDetails(prop.id) }
                )
            }
        }
    }
}

// --- Card Item for Promoted Homes ---
@Composable
fun PromotedPropertyCard(
    property: Property,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit
) {
    val firstImg = property.imageUrls.split(",").firstOrNull() ?: ""

    GlassCard(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick)
            .testTag("promoted_card_${property.id}"),
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            AsyncImage(
                model = firstImg,
                contentDescription = property.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LivingOrangeSecondary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "FEATURED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onSaveToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .testTag("bookmark_${property.id}")
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Save favorite",
                    tint = if (isSaved) LivingOrangeSecondary else Color.White
                )
            }

            // Rent tag positioned elegantly
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "$${property.rent.toInt()}/mo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = property.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = LivingTealPrimary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = property.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "🛏️ ${property.bedrooms} Beds",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "🛁 ${property.bathrooms} Baths",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                if (property.ratingsCount > 0) {
                    RatingStars(rating = property.averageRating, starSize = 12.dp)
                }
            }
        }
    }
}

// --- Layout Row for standard recommendations ---
@Composable
fun RegularPropertyRowItem(
    property: Property,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit
) {
    val firstImg = property.imageUrls.split(",").firstOrNull() ?: ""

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
            .testTag("property_row_${property.id}"),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = firstImg,
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (property.isRented) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RENTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = property.houseType,
                            style = MaterialTheme.typography.labelSmall,
                            color = LivingTealPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "$${property.rent.toInt()}/mo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = LivingTealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = property.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = property.address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "🛏️ ${property.bedrooms} Bed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "🛁 ${property.bathrooms} Bath",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save favorite",
                        tint = if (isSaved) LivingOrangeSecondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onSaveToggle() }
                    )
                }
            }
        }
    }
}

// --- Sliding Search and Filters Screen Drawer ---
@Composable
fun AdvancedFiltersSection(
    viewModel: LivingViewModel,
    onDismiss: () -> Unit
) {
    val selectedType by viewModel.selectedHouseType.collectAsState()
    val minPrice by viewModel.selectedMinPrice.collectAsState()
    val maxPrice by viewModel.selectedMaxPrice.collectAsState()
    val bedrooms by viewModel.selectedBedrooms.collectAsState()
    val bathrooms by viewModel.selectedBathrooms.collectAsState()

    val wifi by viewModel.filterWifi.collectAsState()
    val pet by viewModel.filterPetFriendly.collectAsState()
    val furn by viewModel.filterFurnished.collectAsState()
    val park by viewModel.filterParking.collectAsState()

    val houseTypes = listOf("Apartment", "House", "Penthouse", "Studio")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Advanced Search Matcher",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        // 1. House Type selector
        Column {
            Text(
                text = "Property Structure",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                houseTypes.forEach { type ->
                    val isSelected = selectedType == type
                    Box(
                        modifier = Modifier
                            .border(
                                1.dp,
                                if (isSelected) LivingTealPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) LivingTealPrimary.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { viewModel.selectedHouseType.value = if (isSelected) null else type }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) LivingTealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // 2. Pricing Range Row fields
        Column {
            Text(
                text = "Monthly Budget Limits ($)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = minPrice?.toString() ?: "",
                    onValueChange = {
                        viewModel.selectedMinPrice.value = it.toDoubleOrNull()
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Min Rent") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                OutlinedTextField(
                    value = maxPrice?.toString() ?: "",
                    onValueChange = {
                        viewModel.selectedMaxPrice.value = it.toDoubleOrNull()
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Max Rent") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        }

        // 3. Bed/Bath rooms count selectors
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bedrooms Count",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 2, 3, 4).forEach { count ->
                        val isSelected = bedrooms == count
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) LivingTealPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                .clickable { viewModel.selectedBedrooms.value = if (isSelected) null else count }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count+",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bathrooms Count",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 2, 3).forEach { count ->
                        val isSelected = bathrooms == count
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) LivingTealPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                .clickable { viewModel.selectedBathrooms.value = if (isSelected) null else count }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count+",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Amenities Toggles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Key Conveniences",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            AmenityToggleRow(label = "Fitted WiFi Enabled", checked = wifi, onCheckedChange = { viewModel.filterWifi.value = it })
            AmenityToggleRow(label = "Pet Acceptance Policy", checked = pet, onCheckedChange = { viewModel.filterPetFriendly.value = it })
            AmenityToggleRow(label = "Interior Furnishings Installed", checked = furn, onCheckedChange = { viewModel.filterFurnished.value = it })
            AmenityToggleRow(label = "Parking Space Reserved", checked = park, onCheckedChange = { viewModel.filterParking.value = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions: Clear All and Save matches
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.clearSearchFilters() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Reset Filters")
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
            ) {
                Text("Apply Match")
            }
        }
    }
}

@Composable
fun AmenityToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = LivingTealPrimary)
        )
    }
}

// --- Property Details Overlay View ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailsAndApplicationView(
    propertyId: Int,
    viewModel: LivingViewModel,
    onBack: () -> Unit,
    onChatRequested: (Int) -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val propertyFlowFlow: kotlinx.coroutines.flow.Flow<com.example.data.model.Property?> = remember(propertyId) {
        viewModel.allProperties.map { list -> list.find { it.id == propertyId } }
    }
    val property by propertyFlowFlow.collectAsState(initial = null)
    val savedIds by viewModel.savedPropertyIds.collectAsState()
    val userSession by viewModel.currentUser.collectAsState()

    var showApplicationSheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    var coverLetter by remember { mutableStateOf("") }
    var preferDate by remember { mutableStateOf("2026-06-01") }
    var preferTime by remember { mutableStateOf("11:00 AM") }

    var userReviewText by remember { mutableStateOf("") }
    var userRatingValue by remember { mutableStateOf(5) }

    if (property == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LivingTealPrimary)
        }
        return
    }

    val p = property!!
    val imageList = p.imageUrls.split(",")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Sliding Header Images Stack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = imageList.firstOrNull() ?: "",
                    contentDescription = p.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )

                // Overlapping Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clickable { viewModel.toggleSaveProperty(p.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (p.id in savedIds) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Bookmark",
                                tint = if (p.id in savedIds) LivingOrangeSecondary else Color.White
                            )
                        }
                    }
                }
            }

            // 2. Info Core section
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = p.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = LivingTealPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = p.address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${p.rent.toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = LivingTealPrimary
                        )
                        Text(
                            text = "monthly + $${p.deposit.toInt()} dep",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Core Configuration stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PropDetailIconItem(icon = Icons.Default.Bed, label = "${p.bedrooms} Beds")
                    PropDetailIconItem(icon = Icons.Default.Bathtub, label = "${p.bathrooms} Baths")
                    PropDetailIconItem(icon = Icons.Default.Roofing, label = p.houseType)
                    PropDetailIconItem(
                        icon = if (p.furnished) Icons.Default.Chair else Icons.Default.ChairAlt,
                        label = if (p.furnished) "Furnished" else "Unfurnished"
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // 3. Description text & Gallery rows
                Column {
                    Text(text = "The Sanctuary Vibe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = p.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 22.sp
                    )
                }

                // Interactive gallery row
                Text(text = "Visual Gallery Collection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(imageList) { img ->
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(model = img, contentDescription = "Gallery item", contentScale = ContentScale.Crop)
                        }
                    }
                }

                // 4. Landlord segment with badges
                GlassCard(tonalElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=120&q=80",
                                    contentDescription = "Landlord"
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Richard Vance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified Profile", tint = LivingTealPrimary, modifier = Modifier.size(14.dp))
                                }
                                Text(text = "Apex Residential Co.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        IconButton(
                            onClick = { onChatRequested(p.landlordId) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = LivingTealPrimary.copy(alpha = 0.1f))
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat with Landlord", tint = LivingTealPrimary)
                        }
                    }
                }

                // 5. Stylized Interactive Map Segment
                Column {
                    Text(text = "Sanctuary Landmark Coordinates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Render a stunning styled modern map simulation
                            drawRect(color = Color(0xFFE5F1E9)) // Soft Sage green
                            // Draw hospital, transport lines grid, roads
                            drawCircle(color = Color.White, radius = 55.dp.toPx(), center = center)
                            drawLine(color = Color(0xFFAFD1BC), start = Offset(0f, size.height/2f), end = Offset(size.width, size.height/2f), strokeWidth = 8f)
                            drawLine(color = Color(0xFFAFD1BC), start = Offset(size.width/3f, 0f), end = Offset(size.width/3f, size.height), strokeWidth = 6f)

                            // Highlight pin of property
                            drawCircle(color = LivingOrangeSecondary, radius = 10.dp.toPx(), center = center)
                            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = center)
                        }

                        // Coordinates info tag overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(text = "Nearby schools: Greenwood Academy", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Hospitals: Mercy Family Medical Center", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // 6. Review segment (Read/Write reviews)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Ratings & Tenant Feedbacks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (p.ratingsCount == 0) {
                        Text(
                            text = "No reviews yet. Be the first to rate your experience!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        RatingStars(rating = p.averageRating, starSize = 18.dp)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = "Stuart Don", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Text(text = "May 2026", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Absolutely spectacular penthouse space! Excellent double glass insulation which keeps it warm even during winter nights.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Leave a review component for tenants only
                    if (userSession != null && userSession?.role == "TENANT" && userSession?.id != -1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Express your thoughts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { userRatingValue = star }) {
                                    Icon(
                                        imageVector = if (star <= userRatingValue) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Rating $star",
                                        tint = LivingOrangeSecondary
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = userReviewText,
                            onValueChange = { userReviewText = it },
                            placeholder = { Text("Write your honest review of properties...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                if (userReviewText.isNotEmpty()) {
                                    viewModel.submitPropertyReview(p.id, userSession?.name ?: "Stuart", userRatingValue, userReviewText)
                                    userReviewText = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                        ) {
                            Text("Post Review")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 7. Large primary CTA action button
                if (userSession?.role == "TENANT") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPaymentSheet = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, LivingTealPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = LivingTealPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fast Pay")
                        }

                        Button(
                            onClick = { showApplicationSheet = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("apply_house_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply to Rent")
                        }
                    }
                } else if (userSession == null || userSession?.id == -1) {
                    // Tell guests to log in
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Register account to Apply & Secure")
                    }
                }
            }
        }

        // --- Bottom Sheet: Apply to Rent ---
        if (showApplicationSheet) {
            AlertDialog(
                onDismissRequest = { showApplicationSheet = false },
                title = { Text("Secure Rental Lease Application") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Add a lease application to Richard Vance. This covers your dynamic evaluation.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        OutlinedTextField(
                            value = coverLetter,
                            onValueChange = { coverLetter = it },
                            placeholder = { Text("Write personal details, career info, pay notes, move-in periods...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = preferDate,
                                onValueChange = { preferDate = it },
                                label = { Text("Visit Date") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = preferTime,
                                onValueChange = { preferTime = it },
                                label = { Text("Visit Time") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.applyForProperty(p.id, p.landlordId, coverLetter, preferDate, preferTime, "stuart_identity_card_verified.pdf") {
                                showApplicationSheet = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                    ) {
                        Text("Apply Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApplicationSheet = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // --- Bottom Sheet: Make Payments ---
        if (showPaymentSheet) {
            var selectedMethod by remember { mutableStateOf("Visa/Mastercard") }
            var cardNum by remember { mutableStateOf("") }
            val amountDue = p.rent + p.deposit

            AlertDialog(
                onDismissRequest = { showPaymentSheet = false },
                title = { Text("Secure Living Gateway Pay") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Due Rent & Security Deposit amount: $$amountDue",
                            fontWeight = FontWeight.Bold,
                            color = LivingTealPrimary
                        )

                        Text(text = "Choose Payment Option:")
                        // Option Radio buttons
                        listOf("Visa/Mastercard", "Mobile Money", "PayPal", "Stripe").forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMethod = m }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedMethod == m, onClick = { selectedMethod = m })
                                Text(text = m, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        OutlinedTextField(
                            value = cardNum,
                            onValueChange = { cardNum = it },
                            placeholder = { Text("Enter Card Number or Billing Phone Line") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.performRentalPayment(p.id, amountDue, "First Month Rent + Deposit", selectedMethod) {
                                onPaymentSuccess()
                            }
                            showPaymentSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                    ) {
                        Text("Pay Securely")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPaymentSheet = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun PropDetailIconItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LivingTealPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}
