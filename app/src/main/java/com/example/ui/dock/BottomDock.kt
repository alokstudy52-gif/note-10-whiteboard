package com.example.ui.dock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveTool
import com.example.model.OpenPopover
import com.example.model.SubjectMode
import com.example.model.WorkMode
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.DockBorder
import com.example.ui.theme.DockCharcoal
import com.example.ui.theme.DockOnSurface
import com.example.ui.theme.DockOnSurfaceMuted
import com.example.ui.theme.DockSurface
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NoteLeafGreen
import com.example.viewmodel.Note10UiState
import com.example.viewmodel.Note10ViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopBoardHud(
    state: Note10UiState,
    onModeChipClick: () -> Unit,
    onSubjectBadgeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var clockText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            clockText = fmt.format(Date())
            delay(10_000L)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DockCharcoal.copy(alpha = 0.84f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onSubjectBadgeClick)
                    .testTag("top_subject_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(NoteLeafGreen)
                    )
                    Text(
                        text = "Note 10 · ${state.currentPage.subjectMode.title}",
                        color = DockOnSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (state.workMode != WorkMode.PRESENTING) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AccentAmber.copy(alpha = 0.92f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onModeChipClick)
                ) {
                    Text(
                        text = state.workMode.label,
                        color = Color(0xFF1F1300),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Iconic Note3 Top-Right Live Digital Clock (Fig 3-1-1)
        if (state.settings.showClock && clockText.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DockCharcoal.copy(alpha = 0.55f)
            ) {
                Text(
                    text = clockText,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun BottomDock(
    state: Note10UiState,
    viewModel: Note10ViewModel,
    modifier: Modifier = Modifier
) {
    // In Focus Clean Mode, show a tiny restore pill on the bottom-left
    if (state.workMode == WorkMode.FOCUS) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Surface(
                shape = CircleShape,
                color = DockCharcoal.copy(alpha = 0.88f),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { viewModel.setWorkMode(WorkMode.PRESENTING) }
                    .testTag("restore_toolbar_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Show toolbar", tint = NoteLeafGreen, modifier = Modifier.size(18.dp))
                    Text("Toolbar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    val scrollState = rememberScrollState()
    val showLabels = state.settings.showToolbarLabels
    val isMathOrPhysics = state.currentPage.subjectMode == SubjectMode.MATH || state.currentPage.subjectMode == SubjectMode.PHYSICS

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Unified ergonomic floating studio dock with Note3's 3 logical groups
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DockCharcoal.copy(alpha = 0.95f),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DockBorder.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // --- Group 1: Menu, Treasure Chest, Resource Library ---
                DockIconBtn(
                    icon = Icons.Default.Menu,
                    label = "Menu",
                    showLabel = showLabels,
                    selected = state.openPopover == OpenPopover.MAIN_MENU,
                    tag = "dock_menu_btn",
                    onClick = { viewModel.togglePopover(OpenPopover.MAIN_MENU) }
                )
                DockIconBtn(
                    icon = Icons.Default.Construction,
                    label = "Tools",
                    showLabel = showLabels,
                    selected = state.openPopover == OpenPopover.TREASURE_CHEST,
                    accentColor = AccentAmber,
                    tag = "dock_treasure_btn",
                    onClick = { viewModel.togglePopover(OpenPopover.TREASURE_CHEST) }
                )
                DockIconBtn(
                    icon = Icons.Default.FolderSpecial,
                    label = "Bank",
                    showLabel = showLabels,
                    selected = state.openPopover == OpenPopover.RESOURCE_LIBRARY,
                    tag = "dock_resource_btn",
                    onClick = { viewModel.togglePopover(OpenPopover.RESOURCE_LIBRARY) }
                )

                DockDivider()

                // --- Group 2: Core Whiteboard Tools ---
                DockIconBtn(
                    icon = Icons.Default.NearMe,
                    label = "Select",
                    showLabel = showLabels,
                    selected = state.activeTool == ActiveTool.SELECT,
                    tag = "dock_select_btn",
                    onClick = { viewModel.selectTool(ActiveTool.SELECT) }
                )
                DockIconBtn(
                    icon = Icons.Default.Create,
                    label = state.penType.label,
                    showLabel = showLabels,
                    selected = state.activeTool == ActiveTool.PEN,
                    dotColor = Color(state.penColorArgb),
                    tag = "dock_pen_btn",
                    onClick = { viewModel.selectTool(ActiveTool.PEN) }
                )
                DockIconBtn(
                    icon = Icons.Default.CleaningServices,
                    label = "Eraser",
                    showLabel = showLabels,
                    selected = state.activeTool == ActiveTool.ERASER,
                    tag = "dock_eraser_btn",
                    onClick = { viewModel.selectTool(ActiveTool.ERASER) }
                )
                DockIconBtn(
                    icon = if (state.shape2DType.name == "SMART_RECOGNIZER") Icons.Default.AutoFixHigh else Icons.Default.Category,
                    label = "Shape",
                    showLabel = showLabels,
                    selected = state.activeTool == ActiveTool.SHAPE_2D,
                    tag = "dock_shape2d_btn",
                    onClick = { viewModel.selectTool(ActiveTool.SHAPE_2D) }
                )
                DockIconBtn(
                    icon = Icons.Default.ViewInAr,
                    label = "3D",
                    showLabel = showLabels,
                    selected = state.activeTool == ActiveTool.SHAPE_3D,
                    accentColor = if (isMathOrPhysics) Color(0xFF38BDF8) else NoteLeafGreen,
                    tag = "dock_shape3d_btn",
                    onClick = { viewModel.selectTool(ActiveTool.SHAPE_3D) }
                )
                DockIconBtn(
                    icon = Icons.Default.TextFields,
                    label = "Text",
                    showLabel = showLabels,
                    selected = state.activeTool == ActiveTool.TEXT || state.openPopover == OpenPopover.TEXT_EDITOR,
                    tag = "dock_text_btn",
                    onClick = { viewModel.selectTool(ActiveTool.TEXT) }
                )
                DockIconBtn(
                    icon = Icons.Default.OpenWith,
                    label = "Roam",
                    showLabel = showLabels,
                    selected = state.activeTool == ActiveTool.ROAM,
                    tag = "dock_roam_btn",
                    onClick = { viewModel.selectTool(ActiveTool.ROAM) }
                )
                DockIconBtn(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    label = "Undo",
                    showLabel = showLabels,
                    selected = false,
                    enabled = state.canUndo,
                    tag = "dock_undo_btn",
                    onClick = { viewModel.undo() }
                )
                DockIconBtn(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    label = "Redo",
                    showLabel = showLabels,
                    selected = false,
                    enabled = state.canRedo,
                    tag = "dock_redo_btn",
                    onClick = { viewModel.redo() }
                )

                DockDivider()

                // --- Group 3: Page Navigation & Page Manager ---
                DockIconBtn(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    label = "Prev",
                    showLabel = showLabels,
                    selected = false,
                    enabled = state.currentPageIndex > 0,
                    tag = "dock_prev_page_btn",
                    onClick = { viewModel.previousPage() }
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (state.openPopover == OpenPopover.PAGE_MANAGER) NoteLeafGreen else DockSurface,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.togglePopover(OpenPopover.PAGE_MANAGER) }
                        .testTag("dock_page_manager_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = "Pages",
                            tint = if (state.openPopover == OpenPopover.PAGE_MANAGER) Color(0xFF0E2107) else DockOnSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${state.currentPageIndex + 1}/${state.pages.size}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.openPopover == OpenPopover.PAGE_MANAGER) Color(0xFF0E2107) else DockOnSurface
                        )
                    }
                }
                DockIconBtn(
                    icon = if (state.currentPageIndex == state.pages.lastIndex) Icons.Default.Add else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    label = if (state.currentPageIndex == state.pages.lastIndex) "Add" else "Next",
                    showLabel = showLabels,
                    selected = false,
                    accentColor = NoteLeafGreen,
                    tag = "dock_next_add_page_btn",
                    onClick = { viewModel.nextOrAddPage() }
                )
            }
        }
    }
}

@Composable
private fun DockIconBtn(
    icon: ImageVector,
    label: String,
    showLabel: Boolean,
    selected: Boolean,
    enabled: Boolean = true,
    accentColor: Color = NoteLeafGreen,
    dotColor: Color? = null,
    tag: String,
    onClick: () -> Unit
) {
    val bg = if (selected) accentColor.copy(alpha = 0.22f) else Color.Transparent
    val fg = when {
        !enabled -> DockOnSurfaceMuted.copy(alpha = 0.35f)
        selected -> accentColor
        else -> DockOnSurface
    }

    Column(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(22.dp))
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }
        AnimatedVisibility(visible = showLabel) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = fg,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DockDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(width = 1.dp, height = 28.dp)
            .background(DockBorder)
    )
}
