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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Application
import com.example.data.model.Message
import com.example.data.model.Payment
import com.example.data.model.Property
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

    val session by viewModel.currentUser.collectAsState()

    var activePropertyIdDetails by remember { mutableStateOf<Int?>(null) }
    var activeChatPartnerId by remember { mutableStateOf<Int?>(null) }

    // Handle initial splash screen countdown
    LaunchedEffect(Unit) {
        delay(2600)
        appStage = "ONBOARDING"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (appStage == "MAIN" && session != null) {
                // Role-based bottom navigation
                when (session?.role) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
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

                // ==========================================
                // LOGIN SCREEN STAGE
                // ==========================================
                "LOGIN" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
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

                // ==========================================
                // REGISTER SCREEN STAGE
                // ==========================================
                "REGISTER" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
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

                        TextButton(onClick = { appStage = "LOGIN" }) {
                            Text("Back to Sign In", color = LivingTealPrimary)
                        }
                    }
                }

                // ==========================================
                // MAIN APP ROUTER & WORKFLOW CONTROLLER
                // ==========================================
                "MAIN" -> {
                    if (session != null) {

                    // Dynamic Sub-screens wrapper based on role
                    when (session?.role) {
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
                                    UserProfileSettingsView(viewModel = viewModel, onLogout = { appStage = "LOGIN" })
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

    // Synchronize chat loading
    LaunchedEffect(partnerId) {
        viewModel.activeChatPartnerId.value = partnerId
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Vetted Profile Parameters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        GlassCard(tonalElevation = 6.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape)) {
                    AsyncImage(model = session?.avatarUrl ?: "https://unsplash.com", contentDescription = "Avatar")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = session?.name ?: "Stuart Don", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = session?.email ?: "tenant@living.com", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(LivingTealPrimary, CircleShape))
                        Text(text = "Vouched member: ${session?.role ?: "TENANT"}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Ratings feedback if present
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "My Community Rating:")
            RatingStars(rating = session?.rating ?: 5.0f, starSize = 18.dp)
        }

        Divider()

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.logout {
                    onLogout()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_btn_trigger")
        ) {
            Text("Secure Sign Out", fontWeight = FontWeight.Bold)
        }
    }
}
