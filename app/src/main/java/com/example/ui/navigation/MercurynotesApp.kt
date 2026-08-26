package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LockScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.theme.MercuryBlue
import com.example.ui.theme.MercuryPink
import com.example.ui.theme.MercuryPrimaryGradient
import com.example.ui.theme.MercuryTheme
import com.example.ui.theme.MercuryViolet
import com.example.ui.theme.MercurynotesTheme
import com.example.ui.viewmodel.NotesViewModel

enum class BottomNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    NOTES("Notes", Icons.Default.Description, Icons.Outlined.Description, "nav_notes"),
    FOLDERS("Folders", Icons.Default.Folder, Icons.Outlined.Folder, "nav_folders"),
    SEARCH("Search", Icons.Default.Search, Icons.Outlined.Search, "nav_search"),
    SETTINGS("Settings", Icons.Default.Settings, Icons.Outlined.Settings, "nav_settings")
}

sealed interface ScreenDestination {
    data class Main(val tab: BottomNavTab = BottomNavTab.NOTES) : ScreenDestination
    data class Editor(val noteId: Long? = null) : ScreenDestination
    data object Trash : ScreenDestination
}

@Composable
fun MercurynotesApp(
    viewModel: NotesViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val pastelTheme by viewModel.pastelTheme.collectAsStateWithLifecycle()
    val liquidGlassEnabled by viewModel.liquidGlassEnabled.collectAsStateWithLifecycle()
    val compactMode by viewModel.compactMode.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val reduceTransparency by viewModel.reduceTransparency.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<ScreenDestination>(ScreenDestination.Main()) }

    MercurynotesTheme(
        themeMode = themeMode,
        pastelTheme = pastelTheme,
        liquidGlassEnabled = liquidGlassEnabled,
        compactMode = compactMode,
        fontSize = fontSize,
        reduceTransparency = reduceTransparency
    ) {
        val glass = MercuryTheme.glass

        if (isAppLocked) {
            LockScreen(
                onUnlockSuccess = { viewModel.unlockApp("1234") },
                onVerifyPin = { viewModel.unlockApp(it) }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(glass.canvasBackground)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState is ScreenDestination.Editor) {
                            (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300)))
                        } else if (initialState is ScreenDestination.Editor) {
                            (slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)))
                        } else {
                            fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                        }
                    },
                    label = "fluid_screen_transition"
                ) { screen ->
                    when (screen) {
                        is ScreenDestination.Main -> {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = Color.Transparent,
                                bottomBar = {
                                    FrostedBottomNavigation(
                                        currentTab = screen.tab,
                                        onTabSelected = { tab ->
                                            currentScreen = ScreenDestination.Main(tab)
                                        }
                                    )
                                }
                            ) { innerPadding ->
                                val screenModifier = Modifier.padding(innerPadding)
                                when (screen.tab) {
                                    BottomNavTab.NOTES -> {
                                        HomeScreen(
                                            viewModel = viewModel,
                                            onNoteClick = { id -> currentScreen = ScreenDestination.Editor(id) },
                                            onNewNoteClick = { currentScreen = ScreenDestination.Editor(null) },
                                            onSearchClick = { currentScreen = ScreenDestination.Main(BottomNavTab.SEARCH) },
                                            modifier = screenModifier
                                        )
                                    }
                                    BottomNavTab.FOLDERS -> {
                                        FoldersScreen(
                                            viewModel = viewModel,
                                            onFolderSelected = { folderId ->
                                                viewModel.setSelectedFolder(folderId)
                                                currentScreen = ScreenDestination.Main(BottomNavTab.NOTES)
                                            },
                                            onNewNoteInFolder = { folderId ->
                                                viewModel.setSelectedFolder(folderId)
                                                currentScreen = ScreenDestination.Editor(null)
                                            },
                                            modifier = screenModifier
                                        )
                                    }
                                    BottomNavTab.SEARCH -> {
                                        SearchScreen(
                                            viewModel = viewModel,
                                            onNoteClick = { id -> currentScreen = ScreenDestination.Editor(id) },
                                            modifier = screenModifier
                                        )
                                    }
                                    BottomNavTab.SETTINGS -> {
                                        SettingsScreen(
                                            viewModel = viewModel,
                                            onOpenTrash = { currentScreen = ScreenDestination.Trash },
                                            modifier = screenModifier
                                        )
                                    }
                                }
                            }
                        }
                        is ScreenDestination.Editor -> {
                            EditorScreen(
                                noteId = screen.noteId,
                                viewModel = viewModel,
                                onBack = { currentScreen = ScreenDestination.Main(BottomNavTab.NOTES) }
                            )
                        }
                        is ScreenDestination.Trash -> {
                            TrashScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = ScreenDestination.Main(BottomNavTab.SETTINGS) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrostedBottomNavigation(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = MercuryTheme.glass
    val isCompact = MercuryTheme.isCompact

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                horizontal = if (isCompact) 14.dp else 24.dp,
                vertical = if (isCompact) 6.dp else 10.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isCompact) 56.dp else 64.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(
                    BorderStroke(1.dp, glass.bottomNavBorder),
                    RoundedCornerShape(32.dp)
                ),
            color = glass.bottomNavBackground,
            shape = RoundedCornerShape(32.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab

                    Box(
                        modifier = Modifier
                            .testTag(tab.testTag)
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(
                                        if (glass.isDark) Color(0x338B5CF6) else Color(0x223B82F6)
                                    )
                                } else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(tab) }
                            .padding(
                                horizontal = if (isCompact) 10.dp else 14.dp,
                                vertical = if (isCompact) 6.dp else 8.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) MercuryViolet else glass.textSecondary,
                                modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                            )

                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    color = if (glass.isDark) Color.White else MercuryViolet,
                                    fontSize = if (isCompact) 12.sp else 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
