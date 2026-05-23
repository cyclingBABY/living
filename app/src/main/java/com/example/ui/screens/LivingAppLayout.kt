package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.gestures.detectDragGestures
import kotlin.math.roundToInt
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalConfiguration
import com.example.data.model.Application
import com.example.data.model.Message
import com.example.data.model.Payment
import com.example.data.model.Property
import com.example.data.model.User
import com.example.ui.theme.LivingOrangeSecondary
import com.example.ui.theme.LivingTealPrimary
import com.example.ui.theme.LivingTealLight
import com.example.ui.theme.DarkBg
import com.example.ui.viewmodel.LivingViewModel
import kotlinx.coroutines.delay

@Composable
fun LivingAppLayout(viewModel: LivingViewModel) {
    var appStage by remember { mutableStateOf("SPLASH") } // SPLASH, ONBOARDING, LOGIN, REGISTER, MAIN
    var onboardingIndex by remember { mutableStateOf(0) }

    // Onboarding slides definition
    val onboardingSlides = listOf(
        Triple("Find homes easily", "Browse premium, highly insulated modern apartments, houses, and penthouses matching your comfort criteria.", Icons.Outlined.Home),
        Triple("List properties swiftly", "Submit listings for your real estate assets, manage bookings, and communicate with vetted candidates.", Icons.Outlined.AddHomeWork),
        Triple("Connect landlords & tenants", "Interact on our hyper-secure platform backed by smart AI recommendations and instant digital receipts.", Icons.Outlined.Handshake)
    )

    // Form inputs states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var registrationRole by remember { mutableStateOf("TENANT") } // TENANT, LANDLORD
    var companyName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Dynamic Navigation states inside Main
    var activeTenantTab by remember { mutableStateOf("HOME") } // HOME, SEARCH, SAVE, APPS, NOTIFY, MESSAGES, PROFILE
    var activeLandlordTab by remember { mutableStateOf("DASHBOARD") } // DASHBOARD, ADD, APPLICANTS, MESSAGES, PROFILE, PROMOTE
    var activeAdminTab by remember { mutableStateOf("DASHBOARD") } // DASHBOARD, USERS, PAYMENTS
    var adminDashboardViewOverride by remember { mutableStateOf<String?>(null) }
    var showGoogleChooser by remember { mutableStateOf(false) }
    var isCockpitCollapsed by remember { mutableStateOf(false) }
    var cockpitOffset by remember { mutableStateOf(Offset.Zero) }

    val session by viewModel.currentUser.collectAsState()
    val renderRole = if (session?.role == "ADMIN") adminDashboardViewOverride ?: "ADMIN" else session?.role

    var activePropertyIdDetails by remember { mutableStateOf<Int?>(null) }
    var activeChatPartnerId by remember { mutableStateOf<Int?>(null) }

    // Handle initial splash screen countdown with automatic session restoration
    LaunchedEffect(Unit) {
        delay(2600)
        viewModel.tryPersistentLogin(
            onSuccess = {
                appStage = "MAIN"
            },
            onFailure = {
                appStage = "ONBOARDING"
            }
        )
    }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val impersonatorAdminUser by viewModel.impersonatorAdminUser.collectAsState()
            if (impersonatorAdminUser != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Masquerade Mode: Active Session as ${session?.name} (${session?.role})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Button(
                            onClick = { viewModel.stopImpersonating() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Exit Masquerade", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (appStage == "MAIN" && session != null && session!!.hasActiveAccess() && !isWideScreen) {
                // Role-based bottom navigation
                when (renderRole) {
                    "TENANT" -> {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = LivingTealPrimary,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            NavigationBarItem(
                                selected = activeTenantTab == "HOME",
                                onClick = { activeTenantTab = "HOME"; activePropertyIdDetails = null },
                                icon = { Icon(imageVector = Icons.Default.Explore, contentDescription = "Home") },
                                label = { Text("Explore", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = activeTenantTab == "SEARCH",
                                onClick = { activeTenantTab = "SEARCH" },
                                icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Guide") },
                                label = { Text("AI Match", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = activeTenantTab == "SAVE",
                                onClick = { activeTenantTab = "SAVE" },
                                icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorites") },
                                label = { Text("Favorites", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = activeTenantTab == "APPS",
                                onClick = { activeTenantTab = "APPS" },
                                icon = { Icon(imageVector = Icons.Default.Description, contentDescription = "Applications") },
                                label = { Text("My Space", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = activeTenantTab == "INBOX",
                                onClick = { activeTenantTab = "INBOX" },
                                icon = { Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Inbox") },
                                label = { Text("Inbox", fontSize = 10.sp) }
                            )
                        }
                    }
                    "LANDLORD" -> {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = LivingTealPrimary,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            NavigationBarItem(
                                selected = activeLandlordTab == "DASHBOARD",
                                onClick = { activeLandlordTab = "DASHBOARD"; activePropertyIdDetails = null },
                                icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Overview", fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = activeLandlordTab == "INBOX",
                                onClick = { activeLandlordTab = "INBOX" },
                                icon = { Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Inbox") },
                                label = { Text("Inbox", fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = activeLandlordTab == "PROMOTE",
                                onClick = { activeLandlordTab = "PROMOTE" },
                                icon = { Icon(imageVector = Icons.Default.Stars, contentDescription = "Promote") },
                                label = { Text("Advers", fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = activeLandlordTab == "PROFILE",
                                onClick = { activeLandlordTab = "PROFILE" },
                                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile") },
                                label = { Text("Profile", fontSize = 11.sp) }
                            )
                        }
                    }
                    "ADMIN" -> {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = LivingTealPrimary,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            NavigationBarItem(
                                selected = activeAdminTab == "DASHBOARD",
                                onClick = { activeAdminTab = "DASHBOARD" },
                                icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Panel") },
                                label = { Text("Board", fontSize = 12.sp) }
                            )
                            NavigationBarItem(
                                selected = activeAdminTab == "SUPPORT",
                                onClick = { activeAdminTab = "SUPPORT" },
                                icon = { Icon(imageVector = Icons.Default.SupportAgent, contentDescription = "Support") },
                                label = { Text("Concierge", fontSize = 12.sp) }
                            )
                            NavigationBarItem(
                                selected = activeAdminTab == "USERS",
                                onClick = { activeAdminTab = "USERS" },
                                icon = { Icon(imageVector = Icons.Default.SupervisorAccount, contentDescription = "Users") },
                                label = { Text("Users", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (appStage == "MAIN" && session != null && session!!.hasActiveAccess() && isWideScreen) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = LivingTealPrimary,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            AppLogoCanvas(animationTrigger = true, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Living",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LivingTealPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight(),
                    windowInsets = WindowInsets.systemBars
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    when (renderRole) {
                        "TENANT" -> {
                            NavigationRailItem(
                                selected = activeTenantTab == "HOME",
                                onClick = { activeTenantTab = "HOME"; activePropertyIdDetails = null },
                                icon = { Icon(imageVector = Icons.Default.Explore, contentDescription = "Home") },
                                label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NavigationRailItem(
                                selected = activeTenantTab == "SEARCH",
                                onClick = { activeTenantTab = "SEARCH" },
                                icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Match") },
                                label = { Text("AI Match", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NavigationRailItem(
                                selected = activeTenantTab == "SAVE",
                                onClick = { activeTenantTab = "SAVE" },
                                icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorites") },
                                label = { Text("Favorites", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NavigationRailItem(
                                selected = activeTenantTab == "APPS",
                                onClick = { activeTenantTab = "APPS" },
                                icon = { Icon(imageVector = Icons.Default.Description, contentDescription = "Applications") },
                                label = { Text("My Space", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NavigationRailItem(
                                selected = activeTenantTab == "INBOX",
                                onClick = { activeTenantTab = "INBOX" },
                                icon = { Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Inbox") },
                                label = { Text("Inbox", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                        "LANDLORD" -> {
                            NavigationRailItem(
                                selected = activeLandlordTab == "DASHBOARD",
                                onClick = { activeLandlordTab = "DASHBOARD"; activePropertyIdDetails = null },
                                icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Overview", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NavigationRailItem(
                                selected = activeLandlordTab == "INBOX",
                                onClick = { activeLandlordTab = "INBOX" },
                                icon = { Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Inbox") },
                                label = { Text("Inbox", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NavigationRailItem(
                                selected = activeLandlordTab == "PROMOTE",
                                onClick = { activeLandlordTab = "PROMOTE" },
                                icon = { Icon(imageVector = Icons.Default.Stars, contentDescription = "Promote") },
                                label = { Text("Advers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NavigationRailItem(
                                selected = activeLandlordTab == "PROFILE",
                                onClick = { activeLandlordTab = "PROFILE" },
                                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile") },
                                label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                        "ADMIN" -> {
                            NavigationRailItem(
                                selected = activeAdminTab == "DASHBOARD",
                                onClick = { activeAdminTab = "DASHBOARD" },
                                icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Panel") },
                                label = { Text("Board", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NavigationRailItem(
                                selected = activeAdminTab == "SUPPORT",
                                onClick = { activeAdminTab = "SUPPORT" },
                                icon = { Icon(imageVector = Icons.Default.SupportAgent, contentDescription = "Support") },
                                label = { Text("Concierge", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NavigationRailItem(
                                selected = activeAdminTab == "USERS",
                                onClick = { activeAdminTab = "USERS" },
                                icon = { Icon(imageVector = Icons.Default.SupervisorAccount, contentDescription = "Users") },
                                label = { Text("Users", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (appStage) {
                // ==========================================
                // SPLASH STAGE
                // ==========================================
                "SPLASH" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(DarkBg, Color(0xFF1D1F24))
                                )
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AppLogoCanvas(animationTrigger = true)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Living",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = LivingTealPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Find and secure your sanctuary.",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                // ==========================================
                // ONBOARDING CAROUSEL STAGE
                // ==========================================
                "ONBOARDING" -> {
                    val currentSlide = onboardingSlides[onboardingIndex]
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = if (isWideScreen) {
                                Modifier
                                    .widthIn(max = 520.dp)
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(32.dp)
                            } else {
                                Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                            },
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { appStage = "LOGIN" }) {
                                Text("Skip", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Sliding Card content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(LivingTealPrimary.copy(alpha = 0.08f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = currentSlide.third,
                                    contentDescription = null,
                                    tint = LivingTealPrimary,
                                    modifier = Modifier.size(56.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = currentSlide.first,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentSlide.second,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                lineHeight = 20.sp
                            )
                        }

                        // Dot Indicator & Navigation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dots
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                onboardingSlides.forEachIndexed { i, _ ->
                                    Box(
                                        modifier = Modifier
                                            .size(width = if (onboardingIndex == i) 20.dp else 8.dp, height = 8.dp)
                                            .clip(CircleShape)
                                            .background(if (onboardingIndex == i) LivingTealPrimary else Color.Gray.copy(alpha = 0.3f))
                                    )
                                }
                            }

                            // Next Trigger
                            Button(
                                onClick = {
                                    if (onboardingIndex < onboardingSlides.size - 1) {
                                        onboardingIndex++
                                    } else {
                                        appStage = "LOGIN"
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                            ) {
                                Text(if (onboardingIndex == onboardingSlides.size - 1) "Get Started" else "Next")
                            }
                        }
                    }
                    }
                }

                // ==========================================
                // LOGIN SCREEN STAGE
                // ==========================================
                "LOGIN" -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = if (isWideScreen) {
                                Modifier
                                    .widthIn(max = 520.dp)
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(32.dp)
                                    .verticalScroll(rememberScrollState())
                            } else {
                                Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState())
                            },
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        AppLogoCanvas(animationTrigger = true)

                        Text(
                            text = "Log in to Living",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Access your bespoke rental dashboard securely.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )

                        if (errorMessage.isNotEmpty()) {
                            Text(text = errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_login_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Secret Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_login_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                errorMessage = ""
                                if (email.isNotEmpty()) {
                                    viewModel.login(
                                        email = email.trim(),
                                        authPass = password,
                                        onSuccess = { appStage = "MAIN" },
                                        onFailure = { errorMessage = it }
                                    )
                                } else {
                                    errorMessage = "Please enter an email address."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_login_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                        ) {
                            Text("Confirm Sign In", fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "— OR —",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )

                        // Google Sign In button
                        OutlinedButton(
                            onClick = { showGoogleChooser = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_login_btn"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.DarkGray
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    val w = size.width
                                    drawArc(color = Color(0xFFEA4335), startAngle = 180f, sweepAngle = 90f, useCenter = true)
                                    drawArc(color = Color(0xFFFBBC05), startAngle = 90f, sweepAngle = 90f, useCenter = true)
                                    drawArc(color = Color(0xFF34A853), startAngle = 0f, sweepAngle = 90f, useCenter = true)
                                    drawArc(color = Color(0xFF4285F4), startAngle = 270f, sweepAngle = 90f, useCenter = true)
                                    drawCircle(color = Color.White, radius = w * 0.35f)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF202124),
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Don't have an account? ")
                            Text(
                                text = "Register here",
                                color = LivingTealPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { appStage = "REGISTER" }
                            )
                        }

                        Divider()

                        // Guest Bypass link
                        TextButton(
                            onClick = {
                                viewModel.continueAsGuestUser {
                                    appStage = "MAIN"
                                    activeTenantTab = "HOME"
                                }
                            },
                            modifier = Modifier.testTag("continue_guest_btn")
                        ) {
                            Text("Continue as Guest (Read Only)", color = LivingTealPrimary, fontWeight = FontWeight.Bold)
                        }

                        // Help shortcuts
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LivingTealPrimary.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Try demo accounts instant login:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text(text = "- Tenant: tenant@living.com", style = MaterialTheme.typography.labelSmall)
                                Text(text = "- Landlord: landlord@living.com", style = MaterialTheme.typography.labelSmall)
                                Text(text = "- Administrator: admin@living.com", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    }
                }

                // ==========================================
                // REGISTER SCREEN STAGE
                // ==========================================
                "REGISTER" -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = if (isWideScreen) {
                                Modifier
                                    .widthIn(max = 520.dp)
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(32.dp)
                                    .verticalScroll(rememberScrollState())
                            } else {
                                Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState())
                            },
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                        Text(
                            text = "Create Living Account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { registrationRole = "TENANT" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = registrationRole == "TENANT", onClick = { registrationRole = "TENANT" })
                                Text("Tenant looking for homes", style = MaterialTheme.typography.bodySmall)
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { registrationRole = "LANDLORD" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = registrationRole == "LANDLORD", onClick = { registrationRole = "LANDLORD" })
                                Text("Landlord looking to list", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Legal Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email ID") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone coordinates") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (registrationRole == "LANDLORD") {
                            OutlinedTextField(
                                value = companyName,
                                onValueChange = { companyName = it },
                                label = { Text("Estates Company Name (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (email.isNotEmpty() && name.isNotEmpty()) {
                                    viewModel.register(
                                        email = email.trim(),
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        role = registrationRole,
                                        companyName = companyName,
                                        onSuccess = { appStage = "MAIN" },
                                        onFailure = { errorMessage = it }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                        ) {
                            Text("Complete Registration", fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "— OR —",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )

                        // Google Sign In button for Register Stage
                        OutlinedButton(
                            onClick = { showGoogleChooser = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_register_btn"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.DarkGray
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    val w = size.width
                                    drawArc(color = Color(0xFFEA4335), startAngle = 180f, sweepAngle = 90f, useCenter = true)
                                    drawArc(color = Color(0xFFFBBC05), startAngle = 90f, sweepAngle = 90f, useCenter = true)
                                    drawArc(color = Color(0xFF34A853), startAngle = 0f, sweepAngle = 90f, useCenter = true)
                                    drawArc(color = Color(0xFF4285F4), startAngle = 270f, sweepAngle = 90f, useCenter = true)
                                    drawCircle(color = Color.White, radius = w * 0.35f)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF202124),
                                    fontSize = 15.sp
                                )
                            }
                        }

                        TextButton(onClick = { appStage = "LOGIN" }) {
                            Text("Back to Sign In", color = LivingTealPrimary)
                        }
                    }
                    }
                }

                // ==========================================
                // MAIN APP ROUTER & WORKFLOW CONTROLLER
                // ==========================================
                "MAIN" -> {
                    if (session != null) {
                        if (!session!!.hasActiveAccess()) {
                            SubscriptionBarrierView(
                                user = session!!,
                                viewModel = viewModel,
                                onSignOut = { appStage = "LOGIN" }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .widthIn(max = 1200.dp)
                                        .fillMaxWidth()
                                ) {
                                    // Dynamic Sub-screens wrapper based on role
                                    when (renderRole) {
                        "TENANT" -> {
                            if (activePropertyIdDetails != null) {
                                PropertyDetailsAndApplicationView(
                                    propertyId = activePropertyIdDetails!!,
                                    viewModel = viewModel,
                                    onBack = { activePropertyIdDetails = null },
                                    onChatRequested = { partnerId ->
                                        activeChatPartnerId = partnerId
                                        activeTenantTab = "MESSENGER_CHAT"
                                    },
                                    onPaymentSuccess = {
                                        activePropertyIdDetails = null
                                        activeTenantTab = "APPS"
                                    }
                                )
                            } else if (activeTenantTab == "MESSENGER_CHAT" && activeChatPartnerId != null) {
                                MessengerChatRoomOverlay(
                                    partnerId = activeChatPartnerId!!,
                                    viewModel = viewModel,
                                    onBack = {
                                        activeTenantTab = "INBOX"
                                        activeChatPartnerId = null
                                    }
                                )
                            } else {
                                when (activeTenantTab) {
                                    "HOME" -> {
                                        TenantHomeScreen(
                                            viewModel = viewModel,
                                            onNavigateToDetails = { activePropertyIdDetails = it },
                                            onOpenSearchFilters = { activeTenantTab = "SEARCH_FILTERS" }
                                        )
                                    }
                                    "SEARCH_FILTERS" -> {
                                        AdvancedFiltersSection(
                                            viewModel = viewModel,
                                            onDismiss = { activeTenantTab = "HOME" }
                                        )
                                    }
                                    "SEARCH" -> {
                                        PersonalAIConsultantCenter(viewModel = viewModel, onNavigateDetails = { activePropertyIdDetails = it })
                                    }
                                    "SAVE" -> {
                                        FavoritesListView(viewModel = viewModel, onNavigateDetails = { activePropertyIdDetails = it })
                                    }
                                    "APPS" -> {
                                        MySpacesApplicationsView(viewModel = viewModel, onSignOut = { appStage = "LOGIN" })
                                    }
                                    "INBOX" -> {
                                        InboxChatConversationsList(
                                            viewModel = viewModel,
                                            onChatRoom = { partnerId ->
                                                activeChatPartnerId = partnerId
                                                activeTenantTab = "MESSENGER_CHAT"
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        "LANDLORD" -> {
                            if (activeLandlordTab == "MESSENGER_CHAT" && activeChatPartnerId != null) {
                                MessengerChatRoomOverlay(
                                    partnerId = activeChatPartnerId!!,
                                    viewModel = viewModel,
                                    onBack = {
                                        activeLandlordTab = "DASHBOARD"
                                        activeChatPartnerId = null
                                    }
                                )
                            } else {
                                when (activeLandlordTab) {
                                    "DASHBOARD" -> {
                                        LandlordDashboardView(
                                            viewModel = viewModel,
                                            onNavigateToAddProperty = { activeLandlordTab = "ADD_PAGE" },
                                            onNavigateToChat = { partnerId ->
                                                activeChatPartnerId = partnerId
                                                activeLandlordTab = "MESSENGER_CHAT"
                                            }
                                        )
                                    }
                                    "ADD_PAGE" -> {
                                        AddPropertyListingFormScreen(
                                            viewModel = viewModel,
                                            onSuccess = { activeLandlordTab = "DASHBOARD" },
                                            onBack = { activeLandlordTab = "DASHBOARD" }
                                        )
                                    }
                                    "INBOX" -> {
                                        InboxChatConversationsList(
                                            viewModel = viewModel,
                                            onChatRoom = { partnerId ->
                                                activeChatPartnerId = partnerId
                                                activeLandlordTab = "MESSENGER_CHAT"
                                            }
                                        )
                                    }
                                    "PROMOTE" -> {
                                        PromoteListingSubscriptionScreen(viewModel = viewModel, onDone = { activeLandlordTab = "DASHBOARD" })
                                    }
                                    "PROFILE" -> {
                                        UserProfileSettingsView(viewModel = viewModel, onLogout = { appStage = "LOGIN" })
                                    }
                                }
                            }
                        }

                        "ADMIN" -> {
                            when (activeAdminTab) {
                                "DASHBOARD" -> {
                                    AdminDashboardView(viewModel = viewModel)
                                }
                                "SUPPORT" -> {
                                    PersonalAIConsultantCenter(viewModel = viewModel, onNavigateDetails = {})
                                }
                                "USERS" -> {
                                    AdminUsersManagementView(viewModel = viewModel, onLogout = { appStage = "LOGIN" })
                                }
                            }
                        }
                    }
                    }

                    if (session?.role == "ADMIN") {
                        val alignment = if (isWideScreen) Alignment.BottomEnd else Alignment.BottomCenter
                        val bottomPadding = if (isWideScreen) 24.dp else 85.dp
                        val endPadding = if (isWideScreen) 32.dp else 0.dp

                        Box(
                            modifier = Modifier
                                .align(alignment)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(bottom = bottomPadding, end = endPadding)
                                .offset { IntOffset(cockpitOffset.x.roundToInt(), cockpitOffset.y.roundToInt()) }
                                .testTag("admin_floating_cockpit")
                        ) {
                            if (isCockpitCollapsed) {
                                // Sleek space-saving circular bubble, fully draggable & click-re-expandable
                                Card(
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = LivingTealPrimary,
                                        contentColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                    modifier = Modifier
                                        .size(56.dp)
                                        .pointerInput(Unit) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                cockpitOffset = Offset(
                                                    cockpitOffset.x + dragAmount.x,
                                                    cockpitOffset.y + dragAmount.y
                                                )
                                            }
                                        }
                                        .clickable { isCockpitCollapsed = false }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = "Expand Admin Cockpit",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        // A tiny white ring indicator to hint interactivity
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(top = 4.dp, end = 4.dp)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                }
                            } else {
                                // Upgraded toolbar with explicit Drag Handle to move, dashboard tabs, and minimize control
                                Card(
                                    shape = RoundedCornerShape(32.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                                    modifier = Modifier
                                        .border(
                                            width = 1.5.dp,
                                            color = LivingTealPrimary.copy(alpha = 0.45f),
                                            shape = RoundedCornerShape(32.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Sleek Drag Handle on Left to reposition the toolbar smoothly
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "Drag to reposition",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier
                                                .padding(start = 6.dp)
                                                .size(22.dp)
                                                .pointerInput(Unit) {
                                                    detectDragGestures { change, dragAmount ->
                                                        change.consume()
                                                        cockpitOffset = Offset(
                                                            cockpitOffset.x + dragAmount.x,
                                                            cockpitOffset.y + dragAmount.y
                                                        )
                                                    }
                                                }
                                        )

                                        // Admin Tab Selector
                                        val isAdminSel = adminDashboardViewOverride == null
                                        FilledIconButton(
                                            onClick = {
                                                adminDashboardViewOverride = null
                                                activePropertyIdDetails = null
                                                activeChatPartnerId = null
                                            },
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = if (isAdminSel) LivingTealPrimary else Color.Transparent,
                                                contentColor = if (isAdminSel) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                            ),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AdminPanelSettings,
                                                contentDescription = "Admin Cockpit",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Tenant Tab Selector
                                        val isTenantSel = adminDashboardViewOverride == "TENANT"
                                        FilledIconButton(
                                            onClick = {
                                                adminDashboardViewOverride = "TENANT"
                                                activePropertyIdDetails = null
                                                activeChatPartnerId = null
                                            },
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = if (isTenantSel) LivingTealPrimary else Color.Transparent,
                                                contentColor = if (isTenantSel) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                            ),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Explore,
                                                contentDescription = "Tenant Environment",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Landlord Tab Selector
                                        val isLandlordSel = adminDashboardViewOverride == "LANDLORD"
                                        FilledIconButton(
                                            onClick = {
                                                adminDashboardViewOverride = "LANDLORD"
                                                activePropertyIdDetails = null
                                                activeChatPartnerId = null
                                            },
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = if (isLandlordSel) LivingTealPrimary else Color.Transparent,
                                                contentColor = if (isLandlordSel) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                            ),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HomeWork,
                                                contentDescription = "Landlord Environment",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(2.dp))

                                        // Minimize button to toggle back to compact state
                                        IconButton(
                                            onClick = { isCockpitCollapsed = true },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Collapse Cockpit",
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                    }
                }
                }
            }
            }
        }
    }

    if (showGoogleChooser) {
        AlertDialog(
            onDismissRequest = { showGoogleChooser = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .padding(20.dp)
                .widthIn(max = 440.dp)
                .clip(RoundedCornerShape(28.dp)),
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleChooser = false }) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val w = size.width
                            drawArc(color = Color(0xFFEA4335), startAngle = 180f, sweepAngle = 90f, useCenter = true)
                            drawArc(color = Color(0xFFFBBC05), startAngle = 90f, sweepAngle = 90f, useCenter = true)
                            drawArc(color = Color(0xFF34A853), startAngle = 0f, sweepAngle = 90f, useCenter = true)
                            drawArc(color = Color(0xFF4285F4), startAngle = 270f, sweepAngle = 90f, useCenter = true)
                            drawCircle(color = Color.White, radius = w * 0.35f)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF202124)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Sign in to Living",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF202124)
                    )
                    Text(
                        text = "Choose an account to continue securely",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val googleAccounts = listOf(
                        Triple("stuartdonsms@gmail.com", "Stuart Don (Admin)", "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=120&q=80"),
                        Triple("leila.nassali@gmail.com", "Leila Nassali (Tenant)", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80"),
                        Triple("mukasa.lands@gmail.com", "Mukasa Properties (Landlord)", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=120&q=80")
                    )

                    googleAccounts.forEach { (gEmail, gName, gAvatar) ->
                        Card(
                            onClick = {
                                showGoogleChooser = false
                                viewModel.loginWithGoogle(gEmail, gName) {
                                    appStage = "MAIN"
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4)),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("google_account_${gEmail.split("@")[0]}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = gAvatar,
                                    contentDescription = gName,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = gName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF202124)
                                    )
                                    Text(
                                        text = gEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            showGoogleChooser = false
                            viewModel.loginWithGoogle("stuartdonsms@gmail.com", "Stuart Don (Admin)") {
                                appStage = "MAIN"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Text("Use another account", color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        )
    }
}

// ==========================================
// 3. SECURE MESSENGER OVERLAY CHAT
// ==========================================
@Composable
fun MessengerChatRoomOverlay(
    partnerId: Int,
    viewModel: LivingViewModel,
    onBack: () -> Unit
) {
    val activeConversation by viewModel.activeConversation.collectAsState()
    val userSession by viewModel.currentUser.collectAsState()

    var textInput by remember { mutableStateOf("") }

    // Synchronize chat loading with safe lifecycle cleanup
    DisposableEffect(partnerId) {
        viewModel.activeChatPartnerId.value = partnerId
        onDispose {
            viewModel.activeChatPartnerId.value = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(LivingTealPrimary.copy(alpha = 0.05f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(36.dp).clip(CircleShape)) {
                AsyncImage(model = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=120&q=80", contentDescription = "Avatar")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = "Richard Vance/Stuart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Online | Vetted Member", style = MaterialTheme.typography.labelSmall, color = LivingTealPrimary)
            }
        }

        // Message lists
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(activeConversation) { msg ->
                val isMe = msg.senderId == (userSession?.id ?: 0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 16.dp
                                )
                            )
                            .background(if (isMe) LivingTealPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.messageText,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Read marker indicators
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "19:28",
                                    fontSize = 8.sp,
                                    color = if (isMe) Color.White.copy(alpha = 0.6f) else Color.Gray
                                )
                                if (isMe) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Read",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input keyboard bar with files attachment clips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Attachment", tint = LivingTealPrimary)
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Tape a secured message...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_box"),
                shape = RoundedCornerShape(24.dp)
            )

            IconButton(
                onClick = {
                    if (textInput.isNotEmpty()) {
                        viewModel.sendMessage(partnerId, textInput)
                        textInput = ""
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = LivingTealPrimary),
                modifier = Modifier.testTag("chat_send_btn")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// ==========================================
// 4. FAVORITES LIST SCREEN
// ==========================================
@Composable
fun FavoritesListView(
    viewModel: LivingViewModel,
    onNavigateDetails: (Int) -> Unit
) {
    val favorites by viewModel.savedProperties.collectAsState()
    val savedIds by viewModel.savedPropertyIds.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(text = "My Bookmarked Sanctuaries", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Easily track status, rates, and schedule visits on your saved properties.", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            EmptyStatePlaceholder(
                title = "No bookmarks saved yet",
                subtitle = "Click the heart button on featured apartments or studios to track them on dynamic feed.",
                icon = Icons.Default.FavoriteBorder
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favorites) { prop ->
                    RegularPropertyRowItem(
                        property = prop,
                        isSaved = prop.id in savedIds,
                        onSaveToggle = { viewModel.toggleSaveProperty(prop.id) },
                        onClick = { onNavigateDetails(prop.id) }
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. PERSONAL AI CONSULTANT CENTER
// ==========================================
@Composable
fun PersonalAIConsultantCenter(
    viewModel: LivingViewModel,
    onNavigateDetails: (Int) -> Unit
) {
    var aiQuery by remember { mutableStateOf("") }
    val response by viewModel.aiRecommendationText.collectAsState()
    val loading by viewModel.aiRecommendationLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "AI Living Smart Consult", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Living Intelligent recommendations. Specify beds, prices, and locations, or type questions to our automated concierge.", style = MaterialTheme.typography.bodySmall)

        OutlinedTextField(
            value = aiQuery,
            onValueChange = { aiQuery = it },
            placeholder = { Text("E.g. find a luxury penthouse in Manhattan with parking...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_support_query"),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                IconButton(onClick = {
                    if (aiQuery.isNotEmpty()) {
                        viewModel.generateAIRecommendations(aiQuery)
                    }
                }) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Consult AI", tint = LivingTealPrimary)
                }
            }
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LivingTealPrimary)
            }
        } else if (response.isNotEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_response_box"),
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = LivingOrangeSecondary)
                        Text(text = "Living AI Assistant", fontWeight = FontWeight.Bold, color = LivingTealPrimary)
                    }
                    Text(text = response, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Pre-defined quick consult chips
        Text(text = "Popular Quick Ask:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Luxury Penthouse NY", "Cheap Studio", "Suburban Family House").forEach { path ->
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable {
                            aiQuery = "Find $path"
                            viewModel.generateAIRecommendations("Find $path")
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(text = path, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// ==========================================
// 6. INBOX CHAT CONVERSATIONS LIST (INBOX)
// ==========================================
@Composable
fun InboxChatConversationsList(
    viewModel: LivingViewModel,
    onChatRoom: (Int) -> Unit
) {
    val inbox by viewModel.inboxMessages.collectAsState()
    val session by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Messages Inbox", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Secure workspace dialogs with vetted landlords or tenants.", style = MaterialTheme.typography.bodySmall)

        if (inbox.isEmpty()) {
            EmptyStatePlaceholder(
                title = "No chats started yet",
                subtitle = "Open standard property specifications list, find landlord profile and click chat to start secure chats.",
                icon = Icons.Default.ChatBubbleOutline
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(inbox) { msg ->
                    val counterpartId = if (msg.senderId == (session?.id ?: 0)) msg.receiverId else msg.senderId

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChatRoom(counterpartId) },
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape)) {
                                AsyncImage(model = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=120&q=80", contentDescription = "Avatar")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Vance (Landlord) / Stuart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = msg.messageText,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            // Icon indicators
                            if (!msg.isRead && msg.receiverId == (session?.id ?: 0)) {
                                Box(modifier = Modifier.size(8.dp).background(LivingOrangeSecondary, CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. MY SPACES & LEASES / APPLICATION VIEW
// ==========================================
@Composable
fun MySpacesApplicationsView(
    viewModel: LivingViewModel,
    onSignOut: () -> Unit
) {
    val apps by viewModel.tenantApplications.collectAsState()
    val payments by viewModel.tenantPayments.collectAsState()

    var activeReceiptToShow by remember { mutableStateOf<Payment?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "My Sanctuary Board", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = "Secure application checkouts, leases status, and historic receipts.", style = MaterialTheme.typography.bodySmall)
                }

                Button(onClick = onSignOut, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Exit", fontSize = 11.sp)
                }
            }
        }

        item {
            Text(text = "My Leases Applications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (apps.isEmpty()) {
            item {
                Text(text = "No applications filed. Browse houses to apply.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        } else {
            items(apps) { app ->
                GlassCard(tonalElevation = 3.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Lumina Penthouse/Home", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            // State color indicator
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (app.status) {
                                            "APPROVED" -> LivingTealPrimary.copy(alpha = 0.15f)
                                            "REJECTED" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                            else -> LivingOrangeSecondary.copy(alpha = 0.15f)
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = app.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (app.status) {
                                        "APPROVED" -> LivingTealPrimary
                                        "REJECTED" -> MaterialTheme.colorScheme.error
                                        else -> LivingOrangeSecondary
                                    }
                                )
                            }
                        }

                        Text(text = "Cover narrative: ${app.coverLetter}", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(text = "📅 Visit schedules prefer date: ${app.visitDate} @ ${app.visitTime}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Payments History segments
        item {
            Text(text = "Historic Digital Invoices Receipts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (payments.isEmpty()) {
            item {
                Text(text = "No billing transactions found.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        } else {
            items(payments) { bill ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeReceiptToShow = bill },
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = bill.paymentType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = "Cleared via ${bill.paymentMethod} on ${bill.date}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "$${bill.amount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LivingTealPrimary)
                            Text(text = "Click to QR receipt", style = MaterialTheme.typography.labelSmall, color = LivingOrangeSecondary, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }

    // --- Receipt Dialog overlay with QR Canvas code ---
    if (activeReceiptToShow != null) {
        val r = activeReceiptToShow!!
        AlertDialog(
            onDismissRequest = { activeReceiptToShow = null },
            title = { Text("Secured Rent Transaction Ledger") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = r.paymentType, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "Processed via Living Payments Gateway system", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    Divider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Amount Paid:")
                        Text(text = "$${r.amount.toInt()}", fontWeight = FontWeight.Bold, color = LivingTealPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Method:")
                        Text(text = r.paymentMethod, fontWeight = FontWeight.Bold)
                    }

                    // Canvas Render QR Receipt!
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(1.dp, Color.LightGray)
                            .padding(12.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Render modular QR structure
                            val w = size.width
                            val h = size.height
                            // Corner secure alignment anchors
                            drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(30f, 30f), topLeft = Offset(0f, 0f))
                            drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(30f, 30f), topLeft = Offset(w - 30f, 0f))
                            drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(30f, 30f), topLeft = Offset(0f, h - 30f))

                            // Dummy QR line tracks
                            drawLine(color = Color.Black, start = Offset(w / 2f, 20f), end = Offset(w / 2f, h - 20f), strokeWidth = 10f)
                            drawLine(color = Color.Black, start = Offset(20f, h / 3f), end = Offset(w - 20f, h / 3f), strokeWidth = 8f)
                            drawLine(color = Color.Black, start = Offset(20f, 2f * h / 3f), end = Offset(w - 20f, 2f * h / 3f), strokeWidth = 12f)
                        }
                    }

                    Text(text = r.qrcode, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { activeReceiptToShow = null }) {
                    Text("Download & Save")
                }
            }
        )
    }
}

// ==========================================
// 8. LANDLORD PROMOTION SUBSCRIPTION
// ==========================================
@Composable
fun PromoteListingSubscriptionScreen(
    viewModel: LivingViewModel,
    onDone: () -> Unit
) {
    var promoPlan by remember { mutableStateOf("Basic") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Promote Listings To Bazaars Feed", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Add custom premium badges, features, and promote real estates automatically.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(16.dp))

        // Multi Tier packages
        PromotionTierCard(
            title = "Pro Featured Tier Booster",
            price = "$49/mo",
            description = "Get placed on Featured carousel immediately. Up to 8x visitor clicks matched.",
            selected = promoPlan == "Pro",
            onClick = { promoPlan = "Pro" }
        )

        PromotionTierCard(
            title = "Elite Infinite Tier Booster",
            price = "$99/mo",
            description = "Unlimited Featured placements + Living AI smart search match priorities on top.",
            selected = promoPlan == "Elite",
            onClick = { promoPlan = "Elite" }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Subscribe Promotional Matches", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PromotionTierCard(
    title: String,
    price: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        borderColor = if (selected) LivingTealPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        backgroundColor = if (selected) LivingTealPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = price, fontWeight = FontWeight.Bold, color = LivingTealPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

// ==========================================
// 9. PROFILE & SETTINGS DETAILS
// ==========================================
@Composable
fun UserProfileSettingsView(
    viewModel: LivingViewModel,
    onLogout: () -> Unit
) {
    val session by viewModel.currentUser.collectAsState()
    val isRatingHidden by viewModel.isRatingHidden.collectAsState()
    val isDmsDisabled by viewModel.isDmsDisabled.collectAsState()
    val isPhoneHidden by viewModel.isPhoneHidden.collectAsState()
    val isLocationEnabled by viewModel.isLocationEnabled.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var editedCompanyName by remember { mutableStateOf("") }
    var editedAvatarUrl by remember { mutableStateOf("") }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }

    val avatarPresets = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80" to "Modern Pink",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=80" to "Classic Professional",
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80" to "Minimal Soft",
        "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=120&q=80" to "Warm Artsy",
        "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=120&q=80" to "Vintage Dark"
    )

    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            editedName = session?.name ?: ""
            editedPhone = session?.phone ?: ""
            editedCompanyName = session?.companyName ?: ""
            editedAvatarUrl = session?.avatarUrl ?: ""
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Account Permanently?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Are you absolutely sure value you want to close your 'Living' account? All your registered digital catalogs, applications, and messaging trails will be deleted permanently. This is irreversible.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteUserAccount {
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Permanently Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Keep Account")
                }
            }
        )
    }

    if (showSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showSubscriptionDialog = false },
            title = {
                Text(
                    text = "Extend Living Access Package",
                    fontWeight = FontWeight.Bold,
                    color = LivingTealPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose your desired premium membership duration. Payments will be simulated using Uganda MTNs/Airtel secured API checkout gateway.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Button(
                        onClick = {
                            viewModel.purchaseSubscription(1) {
                                showSubscriptionDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("1 Month Renewal (3,000 UGX)")
                    }

                    Button(
                        onClick = {
                            viewModel.purchaseSubscription(6) {
                                showSubscriptionDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("6 Months Package (15,000 UGX - Save 3K)")
                    }

                    Button(
                        onClick = {
                            viewModel.purchaseSubscription(12) {
                                showSubscriptionDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LivingOrangeSecondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("12 Months Super-saver (28,000 UGX)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSubscriptionDialog = false }) {
                    Text("Dismiss Gateway")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "My Profile & App Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 1. Profile Core Details Card
        GlassCard(tonalElevation = 6.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isEditMode) {
                    Text(
                        text = "Edit Vetted Parameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LivingTealPrimary
                    )

                    // Avatar Preview with edit helper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.dp, LivingTealPrimary, CircleShape)
                        ) {
                            AsyncImage(
                                model = editedAvatarUrl.ifEmpty { "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=120&q=80" },
                                contentDescription = "Active avatar preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Profile Avatar URL", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = editedAvatarUrl,
                                onValueChange = { editedAvatarUrl = it },
                                placeholder = { Text("https://example.com/photo.jpg") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("avatar_url_input")
                            )
                        }
                    }

                    // Preset Avatars Picker
                    Text(
                        text = "Or choose from luxury presets:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        avatarPresets.forEach { (url, label) ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (editedAvatarUrl == url) 2.5.dp else 1.dp,
                                        color = if (editedAvatarUrl == url) LivingTealPrimary else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { editedAvatarUrl = url }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = label,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Basic Inputs
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "Full Identity Name", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name")
                        )

                        Text(text = "Contact Phone Number", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = editedPhone,
                            onValueChange = { editedPhone = it },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone")
                        )

                        if (session?.role == "LANDLORD" || session?.role == "ADMIN") {
                            Text(text = "Real Estate / Corporate Company Group Name", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = editedCompanyName,
                                onValueChange = { editedCompanyName = it },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_company")
                            )
                        }
                    }

                    // Save / Cancel Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isEditMode = false },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                viewModel.updateUserProfile(
                                    name = editedName.trim(),
                                    phone = editedPhone.trim(),
                                    companyName = editedCompanyName.trim(),
                                    avatarUrl = editedAvatarUrl.trim()
                                ) {
                                    isEditMode = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("save_profile_details")
                        ) {
                            Text("Save Profile", fontWeight = FontWeight.Bold)
                        }
                    }

                } else {
                    // Standard Profile Read Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(72.dp).clip(CircleShape).border(1.5.dp, LivingTealPrimary, CircleShape)) {
                            AsyncImage(
                                model = session?.avatarUrl ?: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=120&q=80",
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = session?.name ?: "Guest User", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = session?.email ?: "guest@living.com", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(LivingTealPrimary, CircleShape))
                                Text(
                                    text = "Member Level: ${session?.role ?: "TENANT"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (session?.phone != null && session?.phone != "N/A" && session?.phone!!.isNotEmpty()) {
                        Text(
                            text = "Phone Contact: ${session?.phone}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (session?.companyName != null && session?.companyName!!.isNotEmpty()) {
                        Text(
                            text = "Organization: ${session?.companyName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Rating Info
                    if (!isRatingHidden) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Community Rating:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            RatingStars(rating = session?.rating ?: 5.0f, starSize = 18.dp)
                        }
                    }

                    Button(
                        onClick = { isEditMode = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_trigger")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit profile", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile Parameters", fontSize = 14.sp)
                    }
                }
            }
        }

        // 2. Access Protection and Subscription status
        if (session != null && session?.email != "guest@living.com" && session?.id != -1) {
            val statusColor = if (session!!.hasActiveAccess()) LivingTealPrimary else MaterialTheme.colorScheme.error
            GlassCard(tonalElevation = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "System Access and Premium Billing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Membership Access:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = session!!.getSubscriptionStatusText(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Text(
                        text = "Standard subscription keeps your high-fidelity catalogs live, active, and indexed. Support local operations in Uganda via standard mobile checks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Button(
                        onClick = { showSubscriptionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("extend_subscription_trigger")
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = "Extend membership", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Renew / Boost Membership Pack")
                    }
                }
            }
        }

        // 3. Robust Privacy Settings Controls
        GlassCard(tonalElevation = 4.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Aesthetic Privacy Configurations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Hide Rating Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Hide Rating on listings", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Prevent other agents from seeing your rating statistics.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = isRatingHidden,
                        onCheckedChange = { viewModel.toggleRatingHidden(it) },
                        modifier = Modifier.testTag("privacy_rating_switch")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Disable MSG Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Restrict Direct Messaging", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Only let active verified tenants/landlords inbox your profile.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = isDmsDisabled,
                        onCheckedChange = { viewModel.toggleDmsDisabled(it) },
                        modifier = Modifier.testTag("privacy_dms_switch")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Hide Phone Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Hide Contact Phone Number", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Mask your telephone numbers until you accept visitation request.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = isPhoneHidden,
                        onCheckedChange = { viewModel.togglePhoneHidden(it) },
                        modifier = Modifier.testTag("privacy_phone_switch")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Geo Location personalized recommendations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Location Personalization", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Enable smart nearby educational/medical facility highlighting.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = isLocationEnabled,
                        onCheckedChange = { viewModel.toggleLocationEnabled(it) },
                        modifier = Modifier.testTag("privacy_location_switch")
                    )
                }
            }
        }

        // 4. Secure Sign Out & Danger Zone Account Deletion Card
        GlassCard(
            tonalElevation = 1.dp,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Danger Zone Configurations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Deleting your account is permanent. It clears all security data records, application histories, and communication records instantly. Guest credentials do not require manual deletion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                if (session?.email != "guest@living.com" && session?.id != -1) {
                    Button(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("delete_account_btn")
                    ) {
                        Text("Delete Account Permanently", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.logout {
                    onLogout()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_btn_trigger")
        ) {
            Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout icon", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Secure Sign Out", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 4. UGANDA BIOMETRIC SECURED MOBILE MONEY CHECKOUT GATEWAY SCREEN
// ==========================================
@Composable
fun SubscriptionBarrierView(
    user: User,
    viewModel: LivingViewModel,
    onSignOut: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf(user.phone) }
    var selectedNetwork by remember { mutableStateOf("MTN Mobile Money") }
    var isLoading by remember { mutableStateOf(false) }
    var paymentStatusText by remember { mutableStateOf("") }
    
    val requiredPrice = if (user.role == "LANDLORD") 35000 else 3000
    val formattedPrice = "%,d UGX".format(requiredPrice)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Logo Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(LivingTealPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Wallet",
                    tint = LivingTealPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "Account Renewal Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Your one-week free trial has completed. To keep your ${user.role.lowercase()} account active, clear the monthly system maintenance premium fee specified below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subscription Price:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$formattedPrice / month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LivingTealPrimary
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (!isLoading) {
                Text(
                    text = "Select Mobile Money Network:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("MTN Mobile Money", "Airtel Money").forEach { network ->
                        val isSelected = selectedNetwork == network
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) LivingTealPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) LivingTealPrimary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedNetwork = network }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = network,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) LivingTealPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Mobile Money Phone Number") },
                    placeholder = { Text("e.g. 0782XXXXXX") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.Gray)
                    }
                )

                Button(
                    onClick = {
                        if (phoneNumber.isNotEmpty()) {
                            isLoading = true
                            paymentStatusText = "Initializing $selectedNetwork secured prompt..."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LivingTealPrimary)
                ) {
                    Text(text = "Initialize Payment of $formattedPrice", fontWeight = FontWeight.Bold)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = LivingTealPrimary, strokeWidth = 3.dp)
                    Text(
                        text = paymentStatusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                // Simulate payment prompt confirmation after brief delays
                LaunchedEffect(Unit) {
                    delay(1500)
                    paymentStatusText = "Secured connection verified. Awaiting PIN authorization on device..."
                    delay(2500)
                    paymentStatusText = "Processing receipt validation..."
                    delay(1000)
                    viewModel.paySubscription(
                        paymentMethod = selectedNetwork,
                        amount = requiredPrice.toDouble(),
                        phoneNumber = phoneNumber,
                        onSuccess = {
                            isLoading = false
                            paymentStatusText = ""
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    viewModel.logout {
                        onSignOut()
                    }
                }
            ) {
                Text(text = "Log out or Switch Account", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
