package com.example.ui.dock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PeriodicTableData
import com.example.model.BohrAtomItem
import com.example.model.ChemApparatusItem
import com.example.model.ChemApparatusKind
import com.example.model.EraserType
import com.example.model.OpenPopover
import com.example.model.PenType
import com.example.model.PhysicsCircuitItem
import com.example.model.PhysicsInstrumentKind
import com.example.model.Shape2DItem
import com.example.model.Shape2DType
import com.example.model.Shape3DType
import com.example.model.StampKind
import com.example.model.TextItem
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.DockBorder
import com.example.ui.theme.DockCharcoal
import com.example.ui.theme.DockOnSurface
import com.example.ui.theme.DockOnSurfaceMuted
import com.example.ui.theme.DockSurface
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NoteLeafGreen
import com.example.ui.theme.TeachingPalette
import com.example.viewmodel.Note10UiState
import com.example.viewmodel.Note10ViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToolPopoverHost(
    state: Note10UiState,
    viewModel: Note10ViewModel,
    modifier: Modifier = Modifier
) {
    when (state.openPopover) {
        OpenPopover.PEN -> PopoverCard(title = "Pen & Brush · ${state.penType.label}", onClose = viewModel::closePopovers, modifier = modifier) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PenType.entries.forEach { pen ->
                    StudioChip(
                        label = pen.label,
                        selected = state.penType == pen,
                        onClick = { viewModel.setPenConfig(penType = pen) }
                    )
                }
            }
            if (state.penType == PenType.STAMP) {
                Spacer(Modifier.height(8.dp))
                Text("Stamp Symbol", color = DockOnSurfaceMuted, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StampKind.entries.forEach { sk ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (state.stampKind == sk) NoteLeafGreen else DockSurface,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setPenConfig(stampKind = sk) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(sk.glyph, fontSize = 18.sp, color = if (state.stampKind == sk) Color.Black else Color.White)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Size", color = DockOnSurfaceMuted, fontSize = 12.sp)
                Slider(
                    value = state.penStrokeWidth,
                    onValueChange = { viewModel.setPenConfig(width = it) },
                    valueRange = 2f..28f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = NoteLeafGreen, activeTrackColor = NoteLeafGreen)
                )
                Box(
                    modifier = Modifier
                        .size((state.penStrokeWidth.coerceIn(6f, 26f)).dp)
                        .clip(CircleShape)
                        .background(Color(state.penColorArgb))
                        .border(1.dp, Color.White, CircleShape)
                )
            }
            Spacer(Modifier.height(6.dp))
            ColorPaletteRow(
                selectedColorArgb = state.penColorArgb,
                onSelect = { viewModel.setPenConfig(colorArgb = it) }
            )
        }

        OpenPopover.ERASER -> PopoverCard(title = "Eraser", onClose = viewModel::closePopovers, modifier = modifier) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EraserType.entries.forEach { et ->
                    StudioChip(
                        label = et.label,
                        selected = state.eraserType == et,
                        onClick = { viewModel.setEraserType(et) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            // Note3 Fig 3-1-8 Slide-to-Clear-Page safety slider!
            var clearSlider by remember { mutableFloatStateOf(0f) }
            Text("Slide all the way right to clear page", color = DockOnSurfaceMuted, fontSize = 11.sp)
            Slider(
                value = clearSlider,
                onValueChange = {
                    clearSlider = it
                    if (it >= 0.96f) {
                        clearSlider = 0f
                        viewModel.clearCurrentPage()
                    }
                },
                onValueChangeFinished = { if (clearSlider < 0.96f) clearSlider = 0f },
                colors = SliderDefaults.colors(thumbColor = AccentCoral, activeTrackColor = AccentCoral)
            )
        }

        OpenPopover.SHAPE_2D -> PopoverCard(title = "2D Shapes & Smart Recognizer", onClose = viewModel::closePopovers, modifier = modifier) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Shape2DType.entries.forEach { st ->
                    StudioChip(
                        label = st.label,
                        selected = state.shape2DType == st,
                        onClick = { viewModel.setShape2DConfig(shapeType = st) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Stroke", color = DockOnSurfaceMuted, fontSize = 11.sp)
                Slider(
                    value = state.shapeStrokeWidth,
                    onValueChange = { viewModel.setShape2DConfig(strokeWidth = it) },
                    valueRange = 2f..18f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = NoteLeafGreen, activeTrackColor = NoteLeafGreen)
                )
            }
            ColorPaletteRow(
                selectedColorArgb = state.shapeStrokeColorArgb,
                onSelect = { viewModel.setShape2DConfig(strokeColorArgb = it) }
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Fill:", color = DockOnSurfaceMuted, fontSize = 11.sp)
                StudioChip(
                    label = "None",
                    selected = state.shapeFillColorArgb == null,
                    onClick = { viewModel.setShape2DConfig(clearFill = true) }
                )
                listOf(0xFFEF4444.toInt(), 0xFFFACC15.toInt(), 0xFF22C55E.toInt(), 0xFF2563EB.toInt(), 0xFF7C3AED.toInt()).forEach { argb ->
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(argb))
                            .border(
                                if (state.shapeFillColorArgb == argb) 2.5.dp else 1.dp,
                                if (state.shapeFillColorArgb == argb) Color.White else DockBorder,
                                CircleShape
                            )
                            .clickable { viewModel.setShape2DConfig(fillColorArgb = argb) }
                    )
                }
            }
        }

        OpenPopover.SHAPE_3D -> PopoverCard(title = "3D Geometric Solids", onClose = viewModel::closePopovers, modifier = modifier) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Shape3DType.entries.forEach { s3 ->
                    StudioChip(
                        label = s3.label,
                        selected = state.shape3DType == s3,
                        onClick = { viewModel.setShape3DConfig(shapeType = s3) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            ColorPaletteRow(
                selectedColorArgb = state.shapeStrokeColorArgb,
                onSelect = { viewModel.setShape3DConfig(strokeColorArgb = it) }
            )
        }

        OpenPopover.ROAM -> PopoverCard(title = "Roaming Navigation Map", onClose = viewModel::closePopovers, modifier = modifier) {
            // Note3 Fig 3-1-10 Navigation chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(state.currentPage.backgroundColorArgb).copy(alpha = 0.85f))
                    .border(1.dp, DockBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(state.zoomScale * 100).toInt()}% · ${state.currentPage.items.size} objects",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.78f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Slider(
                    value = state.zoomScale,
                    onValueChange = { viewModel.updatePanZoom(zoom = it) },
                    valueRange = 0.5f..3.0f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = NoteLeafGreen, activeTrackColor = NoteLeafGreen)
                )
                Button(
                    onClick = { viewModel.resetPanZoom() },
                    colors = ButtonDefaults.buttonColors(containerColor = NoteLeafGreen, contentColor = Color.Black)
                ) {
                    Text("100%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        else -> Unit
    }
}

@Composable
fun SelectedObjectActionBar(
    state: Note10UiState,
    viewModel: Note10ViewModel,
    onSpeakText: (String) -> Unit,
    onOpenHyperlink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val item = state.selectedItem ?: return

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = DockCharcoal.copy(alpha = 0.94f),
        shadowElevation = 8.dp,
        modifier = modifier
            .padding(horizontal = 12.dp)
            .border(1.dp, AccentAmber.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            // Context-specific scientific/geometric controls row (Note3 Fig 3-1-6-3, 3-3-4-2, 3-3-3-2)
            when (item) {
                is Shape2DItem -> {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.shapeType == Shape2DType.CIRCLE || item.shapeType == Shape2DType.ELLIPSE) {
                            StudioChip("Center", item.showCenter) { viewModel.toggleSelectedPlanarFeature("center") }
                            StudioChip("Radius", item.showRadius) { viewModel.toggleSelectedPlanarFeature("radius") }
                        }
                        if (item.shapeType == Shape2DType.RECTANGLE) {
                            StudioChip("Inscribed ○", item.showIncircle) { viewModel.toggleSelectedPlanarFeature("incircle") }
                            StudioChip("Circumcircle", item.showCircumcircle) { viewModel.toggleSelectedPlanarFeature("circumcircle") }
                        }
                        if (item.shapeType == Shape2DType.ISOSCELES_TRIANGLE || item.shapeType == Shape2DType.RIGHT_TRIANGLE) {
                            StudioChip("Incircle", item.showIncircle) { viewModel.toggleSelectedPlanarFeature("incircle") }
                            StudioChip("Circumcircle", item.showCircumcircle) { viewModel.toggleSelectedPlanarFeature("circumcircle") }
                        }
                        // Quick Fill Colors
                        listOf(null, 0xFFEF4444.toInt(), 0xFF22C55E.toInt(), 0xFF2563EB.toInt(), 0xFFFACC15.toInt()).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(c?.let { Color(it) } ?: Color.Transparent)
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .clickable { viewModel.setSelectedItemFillColor(c) }
                            )
                        }
                    }
                }
                is BohrAtomItem -> {
                    val elem = PeriodicTableData.getByNumber(item.atomicNumber)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("${elem.number} ${elem.symbol} · ${elem.name}", color = NoteLeafGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.stepSelectedBohrAtom(-1) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, "Previous atom", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { viewModel.stepSelectedBohrAtom(1) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, "Next atom", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        StudioChip("Periodic Table", false) {
                            viewModel.updateOverlays { it.copy(periodicTableOpen = true) }
                        }
                    }
                }
                is ChemApparatusItem -> {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(item.kind.label, color = NoteLeafGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        if (item.kind.canPour) {
                            Text("Lvl ${(item.liquidLevel * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
                            Slider(
                                value = item.liquidLevel,
                                onValueChange = { viewModel.updateSelectedChemApparatus(liquidLevel = it) },
                                modifier = Modifier.width(86.dp),
                                colors = SliderDefaults.colors(thumbColor = Color(item.liquidColorArgb), activeTrackColor = Color(item.liquidColorArgb))
                            )
                            Text("Tilt ${item.rotationDeg.toInt()}°", color = AccentAmber, fontSize = 11.sp)
                            Slider(
                                value = item.rotationDeg,
                                onValueChange = { viewModel.updateSelectedChemApparatus(rotationDeg = it) },
                                valueRange = -90f..90f,
                                modifier = Modifier.width(86.dp),
                                colors = SliderDefaults.colors(thumbColor = AccentAmber, activeTrackColor = AccentAmber)
                            )
                            if (item.kind.hasPlug) {
                                StudioChip(if (item.isPlugged) "Plugged" else "Plug", item.isPlugged) {
                                    viewModel.updateSelectedChemApparatus(togglePlug = true)
                                }
                            }
                            listOf(0xFF22D3EE.toInt(), 0xFF4ADE80.toInt(), 0xFFF43F5E.toInt(), 0xFFA855F7.toInt(), 0xFFFACC15.toInt()).forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(col))
                                        .border(1.dp, Color.White, CircleShape)
                                        .clickable { viewModel.setSelectedItemColor(col) }
                                )
                            }
                        } else if (item.kind == ChemApparatusKind.ALCOHOL_LAMP) {
                            StudioChip(if (item.isLit) "Extinguish Flame" else "Light Lamp", item.isLit) {
                                viewModel.updateSelectedChemApparatus(toggleFlame = true)
                            }
                        }
                    }
                }
                is PhysicsCircuitItem -> {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(item.kind.label, color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        when (item.kind) {
                            PhysicsInstrumentKind.SWITCH -> StudioChip(
                                label = if (item.isSwitchClosed) "Switch Closed (ON)" else "Switch Open (OFF)",
                                selected = item.isSwitchClosed,
                                onClick = { viewModel.updateSelectedPhysicsInstrument(toggleSwitch = true) }
                            )
                            PhysicsInstrumentKind.RHEOSTAT -> {
                                Text("${item.resistanceOhm.toInt()} Ω", color = Color.White, fontSize = 12.sp)
                                Slider(
                                    value = item.resistanceOhm,
                                    onValueChange = { viewModel.updateSelectedPhysicsInstrument(resistanceOhm = it) },
                                    valueRange = 1f..30f,
                                    modifier = Modifier.width(120.dp)
                                )
                            }
                            PhysicsInstrumentKind.BATTERY -> {
                                Text("${item.voltageVolts.toInt()} V", color = Color.White, fontSize = 12.sp)
                                Slider(
                                    value = item.voltageVolts,
                                    onValueChange = { viewModel.updateSelectedPhysicsInstrument(voltageVolts = it) },
                                    valueRange = 3f..24f,
                                    modifier = Modifier.width(110.dp)
                                )
                            }
                            else -> Unit
                        }
                    }
                }
                else -> Unit
            }

            // Standard Note3 Object Operations Row (Move/Scale/Rotate/Color/Clone/Lock/Layer/Read/Link/Save/Delete)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (item is TextItem) {
                    SmallActionIcon(Icons.Default.Edit, "Edit Text") { viewModel.openTextEditor(item, item.isBanner) }
                    SmallActionIcon(Icons.AutoMirrored.Filled.VolumeUp, "Read Aloud", tint = NoteLeafGreen) { onSpeakText(item.text) }
                }
                SmallActionIcon(Icons.Default.ZoomIn, "Scale Up") { viewModel.scaleSelectedItem(1.16f) }
                SmallActionIcon(Icons.Default.ZoomOut, "Scale Down") { viewModel.scaleSelectedItem(0.86f) }
                SmallActionIcon(Icons.Default.RotateRight, "Rotate 15°") { viewModel.rotateSelectedItem(15f) }
                SmallActionIcon(Icons.Default.ContentCopy, "Clone") { viewModel.cloneSelectedItem() }
                SmallActionIcon(Icons.Default.FlipToFront, "Bring Front") { viewModel.bringSelectedToFront() }
                SmallActionIcon(Icons.Default.FlipToBack, "Send Back") { viewModel.sendSelectedToBack() }
                SmallActionIcon(
                    if (item.locked) Icons.Default.Lock else Icons.Default.LockOpen,
                    if (item.locked) "Unlock" else "Lock",
                    tint = if (item.locked) AccentAmber else Color.White
                ) { viewModel.toggleLockSelectedItem() }
                SmallActionIcon(Icons.Default.BookmarkAdd, "Save to Resource Library") {
                    viewModel.saveSelectedToResourceLibrary("Saved ${item.javaClass.simpleName.removeSuffix("Item")}")
                }
                if (!item.hyperlink.isNullOrBlank()) {
                    SmallActionIcon(Icons.Default.Link, "Open Link", tint = Color(0xFF38BDF8)) {
                        onOpenHyperlink(item.hyperlink!!)
                    }
                } else {
                    SmallActionIcon(Icons.Default.Link, "Attach Link") {
                        viewModel.togglePopover(OpenPopover.HYPERLINK_EDITOR)
                    }
                }
                SmallActionIcon(Icons.Default.Delete, "Delete", tint = AccentCoral) { viewModel.deleteSelectedItem() }
                SmallActionIcon(Icons.Default.Close, "Deselect", tint = DockOnSurfaceMuted) { viewModel.selectItemById(null) }
            }
        }
    }
}

@Composable
private fun SmallActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cd: String,
    tint: Color = DockOnSurface,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(38.dp)) {
        Icon(icon, contentDescription = cd, tint = tint, modifier = Modifier.size(19.dp))
    }
}

@Composable
fun PopoverCard(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DockCharcoal.copy(alpha = 0.96f),
        shadowElevation = 12.dp,
        modifier = modifier
            .widthIn(max = 380.dp)
            .padding(horizontal = 12.dp)
            .border(1.dp, DockBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = DockOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = DockOnSurfaceMuted, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun ColorPaletteRow(
    selectedColorArgb: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeachingPalette.forEach { color ->
            val argb = color.toArgb()
            val selected = argb == selectedColorArgb
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (selected) 3.dp else 1.2.dp,
                        color = if (selected) AccentAmber else DockBorder,
                        shape = CircleShape
                    )
                    .clickable { onSelect(argb) }
            )
        }
    }
}

@Composable
fun StudioChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = DockSurface,
            labelColor = DockOnSurface,
            selectedContainerColor = NoteLeafGreen,
            selectedLabelColor = Color(0xFF0E2107)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = DockBorder,
            selectedBorderColor = NoteLeafGreen
        )
    )
}
