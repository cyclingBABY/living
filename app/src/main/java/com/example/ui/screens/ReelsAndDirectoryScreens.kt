package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.LivingTealPrimary
import com.example.ui.viewmodel.LivingViewModel
import kotlinx.coroutines.launch

@Composable
fun LivingReelsSection(
    viewModel: LivingViewModel,
    onNavigateToChat: (Int) -> Unit
) {
    val reels by viewModel.allReels.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var showPostDialog by remember { mutableStateOf(false) }
    var caption by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf("PHOTO") } // "PHOTO", "VIDEO", "LINK"
    var externalPlatform by remember { mutableStateOf("Direct") } // "YouTube", "TikTok", "Vimeo", "Direct"

    var selectedVideoLinkToPlay by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reels Header with Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Aesthetic Living Reels",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "High-impact visual stories & virtual video tours",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }

            Button(
                onClick = { showPostDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("post_reel_trigger")
            ) {
                Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Post Reel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (reels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Reels Posted Yet",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Be the first verified member to post a tour!",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reels) { reel ->
                    ReelItemCard(
                        reel = reel,
                        currentUserId = currentUser?.id ?: -1,
                        onLikeClick = { viewModel.toggleLikeReel(reel) },
                        onDeleteClick = { viewModel.deleteReel(reel) },
                        onPlayClick = { selectedVideoLinkToPlay = reel.mediaUrl },
                        onChatClick = { onNavigateToChat(reel.userId) }
                    )
                }
            }
        }
    }

    // Video Player overlay Dialog
    if (selectedVideoLinkToPlay != null) {
        AlertDialog(
            onDismissRequest = { selectedVideoLinkToPlay = null },
            title = {
                Text(
                    text = "Media Stream Player",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LivingTealPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Simulating active media render stream for:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "Playing",
                                tint = LivingTealPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedVideoLinkToPlay ?: "",
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Text(
                                text = "[ Streaming Live LTE-Optimized Link ]",
                                color = Color.Green,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Since this is a simulated sandbox environment, real YouTube/TikTok widgets are buffered internally. Live video stream simulation is initialized.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedVideoLinkToPlay = null },
                    colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                ) {
                    Text("Close Player")
                }
            }
        )
    }

    // Post Reel Dialog
    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = {
                Text(
                    text = "Post Social Living Reel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LivingTealPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Caption
                    Text("Reel Story Caption", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        placeholder = { Text("E.g., Beautiful backyard pool at my listing... 🌴☀️") },
                        modifier = Modifier.fillMaxWidth().testTag("add_reel_caption_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Media Type Selector
                    Text("Media Format", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("PHOTO", "VIDEO", "LINK").forEach { type ->
                            val isSel = mediaType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) LivingTealPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(1.dp, if (isSel) LivingTealPrimary else Color.Gray, RoundedCornerShape(8.dp))
                                    .clickable {
                                        mediaType = type
                                        if (type == "LINK") externalPlatform = "YouTube" else externalPlatform = "Direct"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSel) LivingTealPrimary else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Media URL Input
                    Text(
                        text = if (mediaType == "LINK") "Other Site Video Link (e.g. YouTube, TikTok, Vimeo)" else "Image or Direct Video URL",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = mediaUrl,
                        onValueChange = { mediaUrl = it },
                        placeholder = {
                            if (mediaType == "LINK") {
                                Text("https://www.youtube.com/watch?v=...")
                            } else {
                                Text("https://images.unsplash.com/photo-...")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("add_reel_media_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // External Platform selection if LINK
                    if (mediaType == "LINK") {
                        Text("Source Platform", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("YouTube", "TikTok", "Vimeo", "Instagram").forEach { platform ->
                                val isSel = externalPlatform == platform
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) LivingTealPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(1.dp, if (isSel) LivingTealPrimary else Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .clickable { externalPlatform = platform }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = platform,
                                        color = if (isSel) LivingTealPrimary else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Luxury presets helper
                    Text("Aesthetic Preset Media (For Quick Posting)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = LivingTealPrimary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80" to "Coastal Sunset",
                            "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80" to "Marble Penthouse",
                            "https://images.unsplash.com/photo-1502672016832-359000026622?auto=format&fit=crop&w=800&q=80" to "Compact Smart Loft",
                            "https://www.youtube.com/watch?v=ScMzIvxBSi4" to "YouTube Tiny Home Tour"
                        )
                        presets.forEach { (url, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .clickable {
                                        mediaUrl = url
                                        if (url.startsWith("http")) {
                                            if (url.contains("youtube") || url.contains("you.tu")) {
                                                mediaType = "LINK"
                                                externalPlatform = "YouTube"
                                            } else {
                                                mediaType = "PHOTO"
                                                externalPlatform = "Direct"
                                            }
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(label, color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (caption.trim().isEmpty()) return@Button
                        val finalUrl = mediaUrl.ifEmpty { "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80" }
                        viewModel.postReel(
                            mediaUrl = finalUrl.trim(),
                            mediaType = mediaType,
                            externalPlatform = externalPlatform,
                            caption = caption.trim()
                        ) {
                            showPostDialog = false
                            caption = ""
                            mediaUrl = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
                    modifier = Modifier.testTag("submit_reel_btn")
                ) {
                    Text("Add Active Reel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReelItemCard(
    reel: Reel,
    currentUserId: Int,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPlayClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Author row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).border(1.5.dp, LivingTealPrimary, CircleShape)) {
                        AsyncImage(
                            model = reel.userAvatarUrl,
                            contentDescription = reel.userName,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(reel.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LivingTealPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(reel.userRole, fontSize = 8.sp, color = LivingTealPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Aesthetic Creator",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Send message to creator
                    if (reel.userId != currentUserId && reel.userId != -1) {
                        IconButton(
                            onClick = onChatClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = "Inbox", tint = LivingTealPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Delete reel if mine or admin
                    if (reel.userId == currentUserId || currentUserId == 1) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Media Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (reel.mediaType == "PHOTO") {
                    AsyncImage(
                        model = reel.mediaUrl,
                        contentDescription = "Reel post banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (reel.mediaType == "LINK") {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80",
                        contentDescription = "Video placeholder image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().alpha(0.6f)
                    )
                    // Custom badge for third party sites
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Red.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = reel.externalPlatform.uppercase(),
                            fontSize = 8.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Play Button Overlay
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play external website video",
                            tint = LivingTealPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    // Simulated local direct video layout
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1502672016832-359000026622?auto=format&fit=crop&w=800&q=80",
                        contentDescription = "Video thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().alpha(0.6f)
                    )
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Play direct sandbox video file",
                            tint = LivingTealPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Caption details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = reel.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                // Interactions bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeLikeColor = if (reel.isLiked) Color.Red else Color.White
                    Row(
                        modifier = Modifier.clickable { onLikeClick() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like icon",
                            tint = activeLikeColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${reel.likesCount} Likes",
                            color = activeLikeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.ModeComment, contentDescription = "Comments", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        Text(
                            text = "${reel.commentsCount} Comments",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeopleDirectorySection(
    viewModel: LivingViewModel,
    onNavigateToChat: (Int) -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("All") } // "All", "TENANT", "LANDLORD", "ADMIN"

    var showReviewDialogForUser by remember { mutableStateOf<User?>(null) }
    var ratingChosen by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    val filteredList = allUsers.filter { user ->
        val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.phone.contains(searchQuery, ignoreCase = true) ||
                user.companyName.contains(searchQuery, ignoreCase = true)
        val matchesRole = selectedRoleFilter == "All" || user.role == selectedRoleFilter
        matchesSearch && matchesRole
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Header Label
        Column {
            Text(
                text = "Living Directories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Direct interactions & vetted landlord validation checks",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, phone, corporate company...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("people_directory_search_input"),
            shape = RoundedCornerShape(12.dp)
        )

        // Segment filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "TENANT", "LANDLORD", "ADMIN").forEach { filterType ->
                val isSelected = selectedRoleFilter == filterType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) LivingTealPrimary.copy(alpha = 0.25f) else Color.Transparent)
                        .border(1.dp, if (isSelected) LivingTealPrimary else Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { selectedRoleFilter = filterType }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filterType == "All") "All People" else filterType,
                        color = if (isSelected) LivingTealPrimary else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No members matched your search criteria.",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { user ->
                    DirectoryUserCard(
                        user = user,
                        isPublicRatingHidden = viewModel.isRatingHidden.collectAsState().value,
                        isPublicPhoneHidden = viewModel.isPhoneHidden.collectAsState().value,
                        onChatClick = { onNavigateToChat(user.id) },
                        onReviewClick = {
                            showReviewDialogForUser = user
                            ratingChosen = 5
                            reviewComment = ""
                        }
                    )
                }
            }
        }
    }

    // Direct User review modal
    if (showReviewDialogForUser != null) {
        val target = showReviewDialogForUser!!
        AlertDialog(
            onDismissRequest = { showReviewDialogForUser = null },
            title = {
                Text(
                    text = "Write Community Review for ${target.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LivingTealPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Vouch or leave feedback regarding licensing standard checks or visitation etiquette records.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    // Star builder selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rating Status:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row {
                            (1..5).forEach { starIndex ->
                                val selectedStream = starIndex <= ratingChosen
                                Icon(
                                    imageVector = if (selectedStream) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star",
                                    tint = if (selectedStream) Color(0xFFFFB300) else Color.Gray,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { ratingChosen = starIndex }
                                )
                            }
                        }
                    }

                    // Comments
                    Text("Etiquette Comments", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        placeholder = { Text("E.g., High-fidelity professional landlord, instant responses and clean contracts.") },
                        modifier = Modifier.fillMaxWidth().testTag("user_review_comment_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewComment.trim().isNotBlank()) {
                            viewModel.submitUserReview(target.id, ratingChosen, reviewComment.trim())
                        }
                        showReviewDialogForUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
                    modifier = Modifier.testTag("submit_user_review_btn")
                ) {
                    Text("Submit standard Vouch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialogForUser = null }) {
                    Text("Discard")
                }
            }
        )
    }
}

@Composable
fun DirectoryUserCard(
    user: User,
    isPublicRatingHidden: Boolean,
    isPublicPhoneHidden: Boolean,
    onChatClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile avatar
                Box(modifier = Modifier.size(54.dp).clip(CircleShape).border(1.5.dp, LivingTealPrimary, CircleShape)) {
                    AsyncImage(
                        model = user.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=120&q=80" },
                        contentDescription = user.name,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LivingTealPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = user.role,
                                fontSize = 8.sp,
                                color = LivingTealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }

            // Organization metadata if landlord
            if (user.role == "LANDLORD" && user.companyName.isNotEmpty()) {
                Text(
                    text = "🏢 Real Estate Group: ${user.companyName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LivingTealPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Rating display if not hidden
            if (!isPublicRatingHidden) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Vouched Score:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        (1..5).forEach { star ->
                            val activeVal = star <= user.rating
                            Icon(
                                imageVector = if (activeVal) Icons.Filled.Star else Icons.Filled.StarOutline,
                                contentDescription = null,
                                tint = if (activeVal) Color(0xFFFFB300) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", user.rating),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Public Phone details if not hidden
            if (!isPublicPhoneHidden && user.phone.isNotEmpty()) {
                Text(
                    text = "📞 Verified Line: ${user.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            } else if (isPublicPhoneHidden) {
                Text(
                    text = "📞 Phone hidden by privacy standard",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Quick Interactive Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReviewClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("vouch_member_trigger")
                ) {
                    Icon(imageVector = Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Vouch", fontSize = 11.sp)
                }

                Button(
                    onClick = onChatClick,
                    colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("chat_member_trigger")
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Direct DM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
