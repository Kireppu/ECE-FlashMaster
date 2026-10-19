package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowingAtmosphereBackground
import com.example.ui.components.bounceClick
import com.example.ui.screens.CalcuVaultScreen
import com.example.ui.screens.FormulaVaultScreen
import com.example.ui.screens.ImportHistoryScreen
import com.example.ui.screens.ImportScreen
import com.example.ui.screens.ReviewerScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OrangePrimary
import com.example.ui.viewmodel.EceViewModel

enum class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    SUBJECTS("subjects", "Subjects", Icons.AutoMirrored.Filled.MenuBook, "nav_subjects"),
    REVIEWER("reviewer", "Reviewer", Icons.Default.Style, "nav_reviewer"),
    FORMULAS("formulas", "Formulas", Icons.Default.Functions, "nav_formulas"),
    CALCU("calcu", "Calcu Tech", Icons.Default.Calculate, "nav_calcu"),
    IMPORT("import", "Import", Icons.Default.Download, "nav_import")
}

class MainActivity : ComponentActivity() {

    private val viewModel: EceViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                var currentDestination by remember { mutableStateOf(AppDestination.SUBJECTS) }
                var showImportHistory by remember { mutableStateOf(false) }

                BackHandler(enabled = showImportHistory || currentDestination != AppDestination.SUBJECTS) {
                    if (showImportHistory) {
                        showImportHistory = false
                    } else {
                        currentDestination = AppDestination.SUBJECTS
                    }
                }

                GlowingAtmosphereBackground {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent, // Let glowing atmosphere shine through
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricBolt,
                                            contentDescription = null,
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ECE FlashMaster",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 19.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Offline verified badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0x3310B981))
                                                .border(1.dp, Color(0x6610B981), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.OfflinePin,
                                                    contentDescription = null,
                                                    tint = EmeraldTertiary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "OFFLINE",
                                                    color = EmeraldTertiary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                },
                                actions = {
                                    if (currentDestination == AppDestination.IMPORT && !showImportHistory) {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x33FB923C))
                                                .clickable { showImportHistory = true }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .testTag("topbar_history_button")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.History,
                                                    contentDescription = "History",
                                                    tint = OrangePrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "History",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OrangePrimary
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = Color(0xCC0B0B0E)
                                ),
                                modifier = Modifier.drawBehind {
                                    drawLine(
                                        color = Color(0x24FDBA74),
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = Color(0xD9111116),
                                tonalElevation = 4.dp,
                                modifier = Modifier.drawBehind {
                                    drawLine(
                                        color = Color(0x28FDBA74),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                            ) {
                                AppDestination.values().forEach { destination ->
                                    val selected = currentDestination == destination
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { currentDestination = destination },
                                        icon = {
                                            Icon(
                                                imageVector = destination.icon,
                                                contentDescription = destination.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = destination.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = OrangePrimary,
                                            indicatorColor = Color(0x33FB923C),
                                            unselectedIconColor = Color(0xFF71717A),
                                            unselectedTextColor = Color(0xFF71717A)
                                        ),
                                        modifier = Modifier
                                            .bounceClick(scaleDown = 0.92f)
                                            .testTag(destination.testTag)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (showImportHistory) {
                                ImportHistoryScreen(
                                    viewModel = viewModel,
                                    snackbarHostState = snackbarHostState,
                                    onNavigateBack = { showImportHistory = false }
                                )
                            } else {
                                AnimatedContent(
                                    targetState = currentDestination,
                                    transitionSpec = {
                                        val forward = targetState.ordinal > initialState.ordinal
                                        val slideDir = if (forward) 1 else -1
                                        (slideInHorizontally(
                                            initialOffsetX = { fullWidth -> slideDir * fullWidth / 4 },
                                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                                        ) + fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f))
                                            .togetherWith(
                                                slideOutHorizontally(
                                                    targetOffsetX = { fullWidth -> -slideDir * fullWidth / 4 },
                                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                                                ) + fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.98f)
                                            )
                                    },
                                    label = "screen_transition"
                                ) { destination ->
                                    when (destination) {
                                        AppDestination.SUBJECTS -> SubjectsScreen(
                                            viewModel = viewModel,
                                            onNavigateToReviewer = {
                                                currentDestination = AppDestination.REVIEWER
                                            }
                                        )
                                        AppDestination.REVIEWER -> ReviewerScreen(
                                            viewModel = viewModel,
                                            onNavigateToSubjects = {
                                                currentDestination = AppDestination.SUBJECTS
                                            }
                                        )
                                        AppDestination.FORMULAS -> FormulaVaultScreen(viewModel = viewModel)
                                        AppDestination.CALCU -> CalcuVaultScreen(viewModel = viewModel)
                                        AppDestination.IMPORT -> ImportScreen(
                                            viewModel = viewModel,
                                            snackbarHostState = snackbarHostState,
                                            onNavigateToHistory = { showImportHistory = true }
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
