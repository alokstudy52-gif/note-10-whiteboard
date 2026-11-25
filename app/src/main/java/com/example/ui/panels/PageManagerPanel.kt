package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PageFlipEffect
import com.example.model.PageManagerViewMode
import com.example.ui.dock.StudioChip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.DockBorder
import com.example.ui.theme.DockCharcoal
import com.example.ui.theme.DockOnSurface
import com.example.ui.theme.DockOnSurfaceMuted
import com.example.ui.theme.DockSurface
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NoteLeafGreen
import com.example.viewmodel.Note10UiState
import com.example.viewmodel.Note10ViewModel
import kotlin.math.abs

/**
 * Note3 Section 3.1.16 — Page Manager with List View, Crescent Mode, Page Ball Mode, Grouping & Flip Effects
 */
@Composable
fun PageManagerPanel(
    state: Note10UiState,
    viewModel: Note10ViewModel,
    modifier: Modifier = Modifier
) {
    var showGroupDialog by remember { mutableStateOf(false) }
    var showFlipEffects by remember { mutableStateOf(false) }
    var groupNameInput by remember { mutableStateOf("Unit 1") }

    Surface(
        shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp),
        color = DockCharcoal.copy(alpha = 0.96f),
        shadowElevation = 14.dp,
        modifier = modifier
            .width(268.dp)
            .fillMaxHeight(0.76f)
            .border(1.dp, DockBorder, RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
            .testTag("page_manager_panel")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header + View Mode Switchers (List / Crescent / Page Ball / Group / Flip Effect)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pages (${state.pages.size})", color = DockOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = viewModel::closePopovers, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = DockOnSurfaceMuted, modifier = Modifier.size(18.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModeIconBtn(
                    icon = Icons.Default.FormatListBulleted,
                    cd = "List mode",
                    active = state.pageManagerViewMode == PageManagerViewMode.LIST && !showFlipEffects
                ) {
                    showFlipEffects = false
                    viewModel.setPageManagerViewMode(PageManagerViewMode.LIST)
                }
                ModeIconBtn(
                    icon = Icons.Default.NightsStay,
                    cd = "Crescent mode",
                    active = state.pageManagerViewMode == PageManagerViewMode.CRESCENT && !showFlipEffects
                ) {
                    showFlipEffects = false
                    viewModel.setPageManagerViewMode(PageManagerViewMode.CRESCENT)
                }
                ModeIconBtn(
                    icon = Icons.Default.BubbleChart,
                    cd = "Page ball mode",
                    active = state.pageManagerViewMode == PageManagerViewMode.PAGE_BALL && !showFlipEffects
                ) {
                    showFlipEffects = false
                    viewModel.setPageManagerViewMode(PageManagerViewMode.PAGE_BALL)
                }
                ModeIconBtn(
                    icon = Icons.Default.CreateNewFolder,
                    cd = "Assign group",
                    active = false
                ) { showGroupDialog = true }
                ModeIconBtn(
                    icon = Icons.Default.AutoAwesome,
                    cd = "Flip animation",
                    active = showFlipEffects
                ) { showFlipEffects = !showFlipEffects }
            }

            HorizontalDivider(color = DockBorder.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 8.dp))

            if (showFlipEffects) {
                Text("Page Flip Effect", color = NoteLeafGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PageFlipEffect.entries.forEach { eff ->
                        StudioChip(
                            label = eff.label,
                            selected = state.currentPage.flipEffect == eff,
                            onClick = { viewModel.setPageFlipEffect(eff) }
                        )
                    }
                }
            } else {
                when (state.pageManagerViewMode) {
                    PageManagerViewMode.LIST -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(state.pages) { idx, page ->
                                val isCurrent = idx == state.currentPageIndex
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isCurrent) NoteLeafGreen.copy(alpha = 0.16f) else DockSurface)
                                        .border(
                                            width = if (isCurrent) 2.dp else 1.dp,
                                            color = if (isCurrent) NoteLeafGreen else DockBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.goToPage(idx) }
                                        .padding(8.dp)
                                ) {
                                    // Mini Board Thumbnail Preview
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(74.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(page.backgroundColorArgb)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${page.subjectMode.shortLabel} · ${page.items.size} items",
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 11.sp,
                                            color = Color.Black.copy(alpha = 0.72f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.75f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${idx + 1}. ${page.title}",
                                                color = DockOnSurface,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = page.groupName,
                                                color = DockOnSurfaceMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    viewModel.goToPage(idx)
                                                    viewModel.duplicateCurrentPage()
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, "Duplicate", tint = DockOnSurface, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { viewModel.deletePage(idx) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, "Delete page", tint = AccentCoral, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PageManagerViewMode.CRESCENT -> {
                        // Note3 Fig 3-1-16-1-1 Curved Crescent Browse Mode!
                        Text("Crescent Arc Browse", color = AccentAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(state.pages) { idx, page ->
                                val dist = abs(idx - state.currentPageIndex)
                                val curveOffset = (dist * 16).coerceAtMost(48)
                                val isCurrent = idx == state.currentPageIndex
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(page.backgroundColorArgb),
                                    modifier = Modifier
                                        .offset(x = curveOffset.dp)
                                        .width(180.dp)
                                        .height(if (isCurrent) 84.dp else 64.dp)
                                        .border(
                                            if (isCurrent) 3.dp else 1.dp,
                                            if (isCurrent) AccentAmber else Color.White.copy(alpha = 0.6f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.goToPage(idx) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Page ${idx + 1} (${page.subjectMode.shortLabel})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.8f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PageManagerViewMode.PAGE_BALL -> {
                        // Note3 Fig 3-1-16-2-1 Grouped Page Balls Mode!
                        val grouped = state.pages.withIndex().groupBy { it.value.groupName }
                        Text("Page Ball Clusters", color = NoteLeafGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            grouped.forEach { (groupName, indexedPages) ->
                                item(key = groupName) {
                                    Surface(
                                        shape = RoundedCornerShape(26.dp),
                                        color = DockSurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, NoteLeafGreen.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(groupName, color = NoteLeafGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                indexedPages.forEach { (idx, pg) ->
                                                    val active = idx == state.currentPageIndex
                                                    Box(
                                                        modifier = Modifier
                                                            .size(46.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(pg.backgroundColorArgb))
                                                            .border(
                                                                if (active) 3.dp else 1.5.dp,
                                                                if (active) AccentAmber else Color.White,
                                                                CircleShape
                                                            )
                                                            .clickable { viewModel.goToPage(idx) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            "${idx + 1}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color.Black,
                                                            modifier = Modifier
                                                                .clip(CircleShape)
                                                                .background(Color.White.copy(alpha = 0.78f))
                                                                .padding(horizontal = 5.dp, vertical = 1.dp)
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

            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = NoteLeafGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.addNewPage() }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFF0E2107), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add New Page", color = Color(0xFF0E2107), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    if (showGroupDialog) {
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("Assign Page Group") },
            text = {
                OutlinedTextField(
                    value = groupNameInput,
                    onValueChange = { groupNameInput = it },
                    label = { Text("Group Name (e.g. Chapter 1)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setPageGroup(state.currentPageIndex, groupNameInput)
                    showGroupDialog = false
                }) { Text("Assign") }
            },
            dismissButton = {
                TextButton(onClick = { showGroupDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ModeIconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cd: String,
    active: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) NoteLeafGreen.copy(alpha = 0.24f) else Color.Transparent)
    ) {
        Icon(icon, contentDescription = cd, tint = if (active) NoteLeafGreen else DockOnSurface, modifier = Modifier.size(19.dp))
    }
}
