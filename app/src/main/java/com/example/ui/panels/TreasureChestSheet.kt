package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Curtains
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveTool
import com.example.model.BackgroundStyle
import com.example.model.BohrAtomItem
import com.example.model.ChemApparatusItem
import com.example.model.ChemApparatusKind
import com.example.model.PhysicsCircuitItem
import com.example.model.PhysicsInstrumentKind
import com.example.model.SubjectMode
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.BackgroundSwatches
import com.example.ui.theme.DockBorder
import com.example.ui.theme.DockCharcoal
import com.example.ui.theme.DockOnSurface
import com.example.ui.theme.DockOnSurfaceMuted
import com.example.ui.theme.DockSurface
import com.example.ui.theme.NoteLeafGreen
import com.example.viewmodel.Note10UiState
import com.example.viewmodel.Note10ViewModel

/**
 * Note3 Section 3.1.3 — Tabbed Dark-Charcoal Treasure Chest Drawer
 * Tabs: Backgrounds | Subjects | Tools | Shortcuts
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TreasureChestPanel(
    state: Note10UiState,
    viewModel: Note10ViewModel,
    onPickBackgroundPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultTab = when (state.currentPage.subjectMode) {
        SubjectMode.STANDARD, SubjectMode.ENGLISH -> 2 // Tools
        else -> 1 // Subject Tools
    }
    var selectedTab by remember(state.currentPage.subjectMode) { mutableIntStateOf(defaultTab) }
    val tabs = listOf("Backgrounds", "Subjects", "Tools", "Tips")

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = DockCharcoal.copy(alpha = 0.97f),
        shadowElevation = 14.dp,
        modifier = modifier
            .widthIn(max = 410.dp)
            .heightIn(max = 460.dp)
            .padding(horizontal = 10.dp)
            .border(1.dp, DockBorder, RoundedCornerShape(22.dp))
            .testTag("treasure_chest_panel")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Treasure Chest", color = DockOnSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = viewModel::closePopovers, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = DockOnSurfaceMuted, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(6.dp))

            // 4-Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DockSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { idx, title ->
                    val active = idx == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) NoteLeafGreen else Color.Transparent)
                            .clickable { selectedTab = idx }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) Color(0xFF0E2107) else DockOnSurfaceMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedTab) {
                    0 -> BackgroundsTab(state, viewModel, onPickBackgroundPhoto)
                    1 -> SubjectToolsTab(viewModel)
                    2 -> SmallToolsTab(state, viewModel)
                    3 -> ShortcutsTipsTab()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackgroundsTab(
    state: Note10UiState,
    viewModel: Note10ViewModel,
    onPickBackgroundPhoto: () -> Unit
) {
    Text("Solid Colors", color = DockOnSurfaceMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BackgroundSwatches.forEach { color ->
            val argb = color.toArgb()
            val selected = state.currentPage.backgroundStyle == BackgroundStyle.SOLID && state.currentPage.backgroundColorArgb == argb
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color)
                    .border(if (selected) 2.5.dp else 1.dp, if (selected) AccentAmber else DockBorder, RoundedCornerShape(10.dp))
                    .clickable { viewModel.setPageBackground(style = BackgroundStyle.SOLID, colorArgb = argb) }
            )
        }
    }

    HorizontalDivider(color = DockBorder.copy(alpha = 0.5f))
    Text("Subject & Scenic Templates", color = DockOnSurfaceMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val templates = listOf(
            BackgroundStyle.GRID to Icons.Default.GridOn,
            BackgroundStyle.RULED to Icons.Default.Draw,
            BackgroundStyle.ENGLISH_FOUR_LINE to Icons.Default.Book,
            BackgroundStyle.MUSIC_STAVE to Icons.Default.AutoAwesome,
            BackgroundStyle.COORDINATE_AXES to Icons.Default.ShowChart,
            BackgroundStyle.CHESSBOARD to Icons.Default.Palette,
            BackgroundStyle.SCENIC_INK to Icons.Default.Image,
            BackgroundStyle.SCENIC_SKY to Icons.Default.Image
        )
        templates.forEach { (style, icon) ->
            ToolTile(
                icon = icon,
                label = style.label,
                active = state.currentPage.backgroundStyle == style,
                onClick = { viewModel.setPageBackground(style = style) }
            )
        }
        ToolTile(
            icon = Icons.Default.Image,
            label = "Pick Photo",
            active = state.currentPage.backgroundStyle == BackgroundStyle.CUSTOM_IMAGE,
            accent = AccentAmber,
            onClick = {
                viewModel.closePopovers()
                onPickBackgroundPhoto()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubjectToolsTab(viewModel: Note10ViewModel) {
    // --- MATH TOOLS (Note3 Section 3.3.2.2) ---
    Text("Mathematics Instruments", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolTile(Icons.Default.ShowChart, "Function Graph") {
            viewModel.updateOverlays { it.copy(functionEditorOpen = true) }
        }
        ToolTile(Icons.Default.Functions, "Formula") {
            viewModel.updateOverlays { it.copy(mathFormulaEditorOpen = true) }
        }
        ToolTile(Icons.Default.Architecture, "Compass") {
            viewModel.updateOverlays { it.copy(compassActive = !it.compassActive) }
        }
        ToolTile(Icons.Default.Calculate, "Calculator") {
            viewModel.updateOverlays { it.copy(calculatorActive = !it.calculatorActive) }
        }
        ToolTile(Icons.Default.Straighten, "Ruler") {
            viewModel.updateOverlays { it.copy(rulerActive = !it.rulerActive) }
        }
        ToolTile(Icons.Default.SquareFoot, "30° Triangle") {
            viewModel.updateOverlays { it.copy(setSquare30Active = !it.setSquare30Active) }
        }
        ToolTile(Icons.Default.SquareFoot, "45° Triangle") {
            viewModel.updateOverlays { it.copy(setSquare45Active = !it.setSquare45Active) }
        }
        ToolTile(Icons.Default.AutoAwesome, "Protractor") {
            viewModel.updateOverlays { it.copy(protractorActive = !it.protractorActive) }
        }
    }

    HorizontalDivider(color = DockBorder.copy(alpha = 0.5f))

    // --- PHYSICS TOOLS (Note3 Section 3.3.3.2) ---
    Text("Physics Lab (Electricity & Mechanics)", color = Color(0xFF818CF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolTile(Icons.Default.Bolt, "Demo Circuit", accent = AccentAmber) {
            viewModel.addBoardItem(PhysicsCircuitItem(x = 220f, y = 210f, kind = PhysicsInstrumentKind.BATTERY), false)
            viewModel.addBoardItem(PhysicsCircuitItem(x = 420f, y = 210f, kind = PhysicsInstrumentKind.SWITCH), false)
            viewModel.addBoardItem(PhysicsCircuitItem(x = 560f, y = 350f, kind = PhysicsInstrumentKind.BULB), false)
            viewModel.addBoardItem(PhysicsCircuitItem(x = 380f, y = 460f, kind = PhysicsInstrumentKind.RHEOSTAT), false)
            viewModel.addBoardItem(PhysicsCircuitItem(x = 180f, y = 380f, kind = PhysicsInstrumentKind.AMMETER), true)
            viewModel.closePopovers()
            viewModel.selectTool(ActiveTool.SELECT)
            viewModel.showToast("Tap Switch to open/close circuit, select Rheostat to change Ω")
        }
        PhysicsInstrumentKind.entries.forEach { kind ->
            ToolTile(
                icon = if (kind == PhysicsInstrumentKind.BULB) Icons.Default.Lightbulb else Icons.Default.Bolt,
                label = kind.label
            ) {
                viewModel.addBoardItem(PhysicsCircuitItem(x = 320f, y = 320f, kind = kind))
                viewModel.closePopovers()
                viewModel.selectTool(ActiveTool.SELECT)
            }
        }
        ToolTile(Icons.Default.ShowChart, "Force Vectors", accent = Color(0xFFFACC15)) {
            viewModel.selectTool(ActiveTool.FORCE_VECTOR)
            viewModel.showToast("Drag arrows from same origin to compose Resultant Force")
        }
    }

    HorizontalDivider(color = DockBorder.copy(alpha = 0.5f))

    // --- CHEMISTRY TOOLS (Note3 Section 3.3.4.2) ---
    Text("Chemistry Lab (Atoms, Table & Glassware)", color = Color(0xFF2DD4BF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolTile(Icons.Default.Science, "Bohr Atom") {
            viewModel.addBoardItem(BohrAtomItem(x = 320f, y = 300f, atomicNumber = 20))
            viewModel.closePopovers()
            viewModel.selectTool(ActiveTool.SELECT)
        }
        ToolTile(Icons.Default.GridOn, "Periodic Table") {
            viewModel.updateOverlays { it.copy(periodicTableOpen = true) }
        }
        ToolTile(Icons.Default.Functions, "Chem Formula") {
            viewModel.updateOverlays { it.copy(chemFormulaEditorOpen = true) }
        }
        ChemApparatusKind.entries.forEach { kind ->
            ToolTile(Icons.Default.Science, kind.label) {
                viewModel.addBoardItem(ChemApparatusItem(x = 300f, y = 320f, kind = kind))
                viewModel.closePopovers()
                viewModel.selectTool(ActiveTool.SELECT)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SmallToolsTab(state: Note10UiState, viewModel: Note10ViewModel) {
    val ov = state.overlays
    Text("Classroom Presentation Tools", color = DockOnSurfaceMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolTile(Icons.Default.Highlight, "Spotlight", active = ov.spotlightActive) {
            viewModel.updateOverlays { it.copy(spotlightActive = !it.spotlightActive) }
        }
        ToolTile(Icons.Default.Curtains, "Screen Wrap", active = ov.screenWrapActive) {
            viewModel.updateOverlays { it.copy(screenWrapActive = !it.screenWrapActive) }
        }
        ToolTile(Icons.Default.Search, "Magnifier", active = ov.magnifierActive) {
            viewModel.updateOverlays { it.copy(magnifierActive = !it.magnifierActive) }
        }
        ToolTile(Icons.Default.AccessTime, "Clock", active = ov.clockActive) {
            viewModel.updateOverlays { it.copy(clockActive = !it.clockActive) }
        }
        ToolTile(Icons.Default.CalendarMonth, "Calendar", active = ov.calendarActive) {
            viewModel.updateOverlays { it.copy(calendarActive = !it.calendarActive) }
        }
        ToolTile(Icons.Default.Timer, "Timer", active = ov.timerActive) {
            viewModel.updateOverlays { it.copy(timerActive = !it.timerActive) }
        }
        ToolTile(Icons.Default.Draw, "Mini Board", active = ov.writingBoardActive) {
            viewModel.updateOverlays { it.copy(writingBoardActive = !it.writingBoardActive) }
        }
        ToolTile(Icons.Default.AccountTree, "Mind Map", active = ov.mindMapActive) {
            viewModel.updateOverlays { it.copy(mindMapActive = !it.mindMapActive) }
        }
        ToolTile(Icons.Default.CameraAlt, "Screenshot", active = ov.screenshotSelectActive) {
            viewModel.updateOverlays { it.copy(screenshotSelectActive = !it.screenshotSelectActive) }
        }
        ToolTile(Icons.Default.Book, "Encyclopedia", active = ov.encyclopediaOpen) {
            viewModel.updateOverlays { it.copy(encyclopediaOpen = true) }
        }
    }
}

@Composable
private fun ShortcutsTipsTab() {
    val tips = listOf(
        "Tap any active tool icon twice (Pen, Eraser, Shape, 3D, Roam) to open its detailed settings popover.",
        "Smart Shape Recognizer: Draw a rough circle, triangle, box, arrow, or line and watch it snap cleanly!",
        "Chemistry Pouring: Place a Beaker below a Test Tube, select the Test Tube, and slide Tilt > 45° to pour liquid!",
        "Physics Circuit: Tap the Switch directly on the board to open/close the circuit and watch the Bulb & Ammeter react live!",
        "English Mode: Tap any 4-line row to type words, then tap the Speaker button to hear Read Aloud via free on-device TTS!",
        "Left Edge Handle: Tap the green arrow tab on the left edge to switch between Standard, Math, English, Physics, and Chemistry scenes."
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tips.forEach { tip ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DockSurface
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Keyboard, null, tint = NoteLeafGreen, modifier = Modifier.size(16.dp))
                    Text(tip, color = DockOnSurface, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun ToolTile(
    icon: ImageVector,
    label: String,
    active: Boolean = false,
    accent: Color = NoteLeafGreen,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (active) accent.copy(alpha = 0.24f) else DockSurface,
        modifier = Modifier
            .size(width = 86.dp, height = 74.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (active) accent else DockBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = if (active) accent else DockOnSurface, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = if (active) accent else DockOnSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp
            )
        }
    }
}
