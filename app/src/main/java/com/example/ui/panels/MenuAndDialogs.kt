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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.NotebookEntity
import com.example.data.ResourceEntity
import com.example.model.OpenPopover
import com.example.model.Shape2DItem
import com.example.model.Shape2DType
import com.example.model.Shape3DItem
import com.example.model.Shape3DType
import com.example.model.TextItem
import com.example.model.WorkMode
import com.example.ui.dock.ColorPaletteRow
import com.example.ui.dock.StudioChip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.DockBorder
import com.example.ui.theme.DockCharcoal
import com.example.ui.theme.DockOnSurface
import com.example.ui.theme.DockOnSurfaceMuted
import com.example.ui.theme.DockSurface
import com.example.ui.theme.NoteLeafGreen
import com.example.viewmodel.Note10UiState
import com.example.viewmodel.Note10ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainMenuPopover(
    state: Note10UiState,
    viewModel: Note10ViewModel,
    onImportPhoto: () -> Unit,
    onImportN10: () -> Unit,
    onExportPng: () -> Unit,
    onExportPdf: () -> Unit,
    onExportN10: () -> Unit,
    onPrint: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var saveName by remember(state.notebookName) { mutableStateOf(state.notebookName) }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = DockCharcoal.copy(alpha = 0.97f),
        shadowElevation = 14.dp,
        modifier = modifier
            .widthIn(max = 320.dp)
            .heightIn(max = 470.dp)
            .padding(horizontal = 10.dp)
            .border(1.dp, DockBorder, RoundedCornerShape(22.dp))
            .testTag("main_menu_popover")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Note 10", color = NoteLeafGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(state.notebookName, color = DockOnSurfaceMuted, fontSize = 11.sp)
                }
                IconButton(onClick = viewModel::closePopovers, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = DockOnSurfaceMuted, modifier = Modifier.size(18.dp))
                }
            }

            HorizontalDivider(color = DockBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

            MenuSectionHeader("File")
            MenuRow(Icons.Default.NoteAdd, "New Notebook") { viewModel.createNewNotebook() }
            MenuRow(Icons.Default.FolderOpen, "Open Saved Notebooks") { viewModel.togglePopover(OpenPopover.NOTEBOOKS) }
            MenuRow(Icons.Default.Save, "Save / Save As...") { showSaveAsDialog = true }

            MenuSectionHeader("Import")
            MenuRow(Icons.Default.FolderOpen, "Import .n10 Notebook File") {
                viewModel.closePopovers()
                onImportN10()
            }
            MenuRow(Icons.Default.AddPhotoAlternate, "Insert Picture (Photo Picker)") {
                viewModel.closePopovers()
                onImportPhoto()
            }
            MenuRow(Icons.Default.Title, "Insert Formatted Text") { viewModel.openTextEditor(null, isBanner = false) }
            MenuRow(Icons.Default.Bookmark, "Insert Chapter Banner Material") { viewModel.openTextEditor(null, isBanner = true) }

            MenuSectionHeader("Export & Print (100% Free)")
            MenuRow(Icons.Default.FileDownload, "Export Current Page as PNG") {
                viewModel.closePopovers()
                onExportPng()
            }
            MenuRow(Icons.Default.PictureAsPdf, "Export All Pages as PDF") {
                viewModel.closePopovers()
                onExportPdf()
            }
            MenuRow(Icons.Default.Description, "Share .n10 Notebook File") {
                viewModel.closePopovers()
                onExportN10()
            }
            MenuRow(Icons.Default.Print, "Print Notebook") {
                viewModel.closePopovers()
                onPrint()
            }

            MenuSectionHeader("Display Mode")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                WorkMode.entries.forEach { wm ->
                    StudioChip(wm.label, state.workMode == wm) { viewModel.setWorkMode(wm) }
                }
            }

            HorizontalDivider(color = DockBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
            MenuRow(Icons.Default.Settings, "Settings") { viewModel.togglePopover(OpenPopover.SETTINGS) }
            MenuRow(Icons.AutoMirrored.Filled.Help, "User Guide & Shortcuts") { viewModel.togglePopover(OpenPopover.HELP_GUIDE) }
            MenuRow(Icons.Default.Info, "About Note 10") { viewModel.togglePopover(OpenPopover.ABOUT) }
        }
    }

    if (showSaveAsDialog) {
        AlertDialog(
            onDismissRequest = { showSaveAsDialog = false },
            title = { Text("Save Notebook") },
            text = {
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    label = { Text("Lesson / Notebook Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveNotebookAs(saveName)
                    showSaveAsDialog = false
                    viewModel.closePopovers()
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveAsDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MenuSectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        color = NoteLeafGreen,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = label, tint = DockOnSurface, modifier = Modifier.size(19.dp))
        Text(label, color = DockOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResourceLibraryPanel(
    resources: List<ResourceEntity>,
    viewModel: Note10ViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = DockCharcoal.copy(alpha = 0.97f),
        shadowElevation = 14.dp,
        modifier = modifier
            .widthIn(max = 370.dp)
            .heightIn(max = 440.dp)
            .padding(horizontal = 10.dp)
            .border(1.dp, DockBorder, RoundedCornerShape(22.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Resource Bank", color = DockOnSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = viewModel::closePopovers, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = DockOnSurfaceMuted, modifier = Modifier.size(18.dp))
                }
            }

            Text("System Teaching Templates", color = NoteLeafGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StudioChip("Chapter Banner", false) {
                    viewModel.addBoardItem(TextItem(x = 120f, y = 140f, text = "Chapter 1: Key Concepts", fontSizeSp = 28f, isBold = true, isBanner = true))
                    viewModel.closePopovers()
                }
                StudioChip("3D Cube", false) {
                    viewModel.addBoardItem(Shape3DItem(x = 220f, y = 240f, width = 180f, height = 180f, shape3DType = Shape3DType.CUBOID, strokeColorArgb = 0xFF111827.toInt()))
                    viewModel.closePopovers()
                }
                StudioChip("3D Cylinder", false) {
                    viewModel.addBoardItem(Shape3DItem(x = 220f, y = 240f, width = 150f, height = 210f, shape3DType = Shape3DType.CYLINDER, strokeColorArgb = 0xFF111827.toInt()))
                    viewModel.closePopovers()
                }
                StudioChip("Circle + Radius", false) {
                    viewModel.addBoardItem(Shape2DItem(x = 220f, y = 240f, width = 190f, height = 190f, shapeType = Shape2DType.CIRCLE, strokeColorArgb = 0xFF111827.toInt(), showCenter = true, showRadius = true))
                    viewModel.closePopovers()
                }
                StudioChip("Triangle + Incircle", false) {
                    viewModel.addBoardItem(Shape2DItem(x = 200f, y = 230f, width = 220f, height = 190f, shapeType = Shape2DType.ISOSCELES_TRIANGLE, strokeColorArgb = 0xFF111827.toInt(), showIncircle = true))
                    viewModel.closePopovers()
                }
                StudioChip("Sticky Note", false) {
                    viewModel.addBoardItem(TextItem(x = 180f, y = 220f, text = "Important Note:\nRemember this formula!", fontSizeSp = 22f, colorArgb = 0xFF1F2937.toInt(), bgColorArgb = 0xFFFEF08A.toInt()))
                    viewModel.closePopovers()
                }
            }

            HorizontalDivider(color = DockBorder.copy(alpha = 0.5f))
            Text("My Saved Resources (${resources.size})", color = AccentAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            if (resources.isEmpty()) {
                Text(
                    "Select any object on the canvas and tap the Bookmark+ icon to save reusable diagrams here.",
                    color = DockOnSurfaceMuted,
                    fontSize = 12.sp
                )
            } else {
                resources.forEach { res ->
                    Surface(shape = RoundedCornerShape(12.dp), color = DockSurface) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.insertFromResourceLibrary(res) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(res.title, color = DockOnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(res.category, color = DockOnSurfaceMuted, fontSize = 10.sp)
                            }
                            IconButton(onClick = { viewModel.deleteResource(res.id) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Delete, "Delete resource", tint = AccentCoral, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Note10ModalDialogsHost(
    state: Note10UiState,
    notebooks: List<NotebookEntity>,
    viewModel: Note10ViewModel,
    onSpeakText: (String) -> Unit
) {
    when (state.openPopover) {
        OpenPopover.TEXT_EDITOR -> {
            val existing = state.editingTextItem
            var text by remember(existing) { mutableStateOf(existing?.text ?: "") }
            var fontSize by remember(existing) { mutableFloatStateOf(existing?.fontSizeSp ?: state.settings.defaultFontSizeSp) }
            var isBold by remember(existing) { mutableStateOf(existing?.isBold ?: state.editingBannerMode) }
            var isItalic by remember(existing) { mutableStateOf(existing?.isItalic ?: false) }
            var isUnderline by remember(existing) { mutableStateOf(existing?.isUnderline ?: false) }
            var isBanner by remember(existing) { mutableStateOf(existing?.isBanner ?: state.editingBannerMode) }
            var textColor by remember(existing) { mutableIntStateOf(existing?.colorArgb ?: 0xFF111827.toInt()) }

            AlertDialog(
                onDismissRequest = viewModel::closePopovers,
                title = { Text(if (isBanner) "Chapter Banner Material" else "Formatted Text Box") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Enter text...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isBold = !isBold },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isBold) NoteLeafGreen.copy(alpha = 0.28f) else Color.Transparent)
                            ) { Icon(Icons.Default.FormatBold, "Bold") }
                            IconButton(
                                onClick = { isItalic = !isItalic },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isItalic) NoteLeafGreen.copy(alpha = 0.28f) else Color.Transparent)
                            ) { Icon(Icons.Default.FormatItalic, "Italic") }
                            IconButton(
                                onClick = { isUnderline = !isUnderline },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isUnderline) NoteLeafGreen.copy(alpha = 0.28f) else Color.Transparent)
                            ) { Icon(Icons.Default.FormatUnderlined, "Underline") }
                            StudioChip("Banner", isBanner) { isBanner = !isBanner }
                            if (text.isNotBlank()) {
                                IconButton(onClick = { onSpeakText(text) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Read aloud")
                                }
                            }
                        }
                        Text("Size: ${fontSize.toInt()}sp", fontSize = 12.sp)
                        Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 16f..64f)
                        ColorPaletteRow(selectedColorArgb = textColor, onSelect = { textColor = it })
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                val updated = existing?.copy(
                                    text = text,
                                    fontSizeSp = fontSize,
                                    colorArgb = textColor,
                                    isBold = isBold,
                                    isItalic = isItalic,
                                    isUnderline = isUnderline,
                                    isBanner = isBanner
                                ) ?: TextItem(
                                    x = 120f,
                                    y = 200f,
                                    text = text,
                                    fontSizeSp = fontSize,
                                    colorArgb = textColor,
                                    isBold = isBold,
                                    isItalic = isItalic,
                                    isUnderline = isUnderline,
                                    isBanner = isBanner
                                )
                                viewModel.saveTextItem(updated)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NoteLeafGreen, contentColor = Color(0xFF0E2107))
                    ) { Text("Insert") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::closePopovers) { Text("Cancel") }
                }
            )
        }

        OpenPopover.NOTEBOOKS -> {
            val fmt = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
            AlertDialog(
                onDismissRequest = viewModel::closePopovers,
                title = { Text("Saved Notebooks (${notebooks.size})") },
                text = {
                    if (notebooks.isEmpty()) {
                        Text("No saved notebooks yet. Use Menu > Save to store your lessons locally on your phone for free.")
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 320.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            notebooks.forEach { nb ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = 2.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openNotebook(nb.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(nb.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                "${nb.pageCount} pages · ${nb.subjectLabel} · ${fmt.format(Date(nb.updatedAt))}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteNotebook(nb.id) }) {
                                            Icon(Icons.Default.Delete, "Delete notebook", tint = AccentCoral)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = viewModel::closePopovers) { Text("Close") }
                }
            )
        }

        OpenPopover.SETTINGS -> {
            val s = state.settings
            AlertDialog(
                onDismissRequest = viewModel::closePopovers,
                title = { Text("Note 10 Settings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Top-Right Live Clock", fontSize = 13.sp)
                            Switch(checked = s.showClock, onCheckedChange = { v -> viewModel.updateSettings { it.copy(showClock = v) } })
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Toolbar Icon Labels", fontSize = 13.sp)
                            Switch(checked = s.showToolbarLabels, onCheckedChange = { v -> viewModel.updateSettings { it.copy(showToolbarLabels = v) } })
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Auto-Save Lessons Locally", fontSize = 13.sp)
                            Switch(checked = s.autoSave, onCheckedChange = { v -> viewModel.updateSettings { it.copy(autoSave = v) } })
                        }
                        Text("Read Aloud Speed: ${String.format("%.1fx", s.ttsSpeechRate)}", fontSize = 12.sp)
                        Slider(
                            value = s.ttsSpeechRate,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(ttsSpeechRate = v) } },
                            valueRange = 0.5f..2.0f
                        )
                        Text("Default Ink Color", fontSize = 12.sp)
                        ColorPaletteRow(selectedColorArgb = s.defaultPenColorArgb) { argb ->
                            viewModel.updateSettings { it.copy(defaultPenColorArgb = argb) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = viewModel::closePopovers) { Text("Done") }
                }
            )
        }

        OpenPopover.HYPERLINK_EDITOR -> {
            var url by remember { mutableStateOf(state.selectedItem?.hyperlink ?: "https://") }
            AlertDialog(
                onDismissRequest = viewModel::closePopovers,
                title = { Text("Attach Hyperlink") },
                text = {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL or Page number (e.g. page:2)") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.setSelectedHyperlink(url.trim()) }) { Text("Link") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setSelectedHyperlink(null) }) { Text("Remove") }
                }
            )
        }

        OpenPopover.HELP_GUIDE -> {
            AlertDialog(
                onDismissRequest = viewModel::closePopovers,
                title = { Text("Note 10 Quick Guide") },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 340.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("• 6 Smart Brushes: Marker, Paintbrush, Highlighter, Velocity Calligraphy Soft Pen, Fading Laser Pen, and Stamp Pen.", fontSize = 12.sp)
                        Text("• 2D & 3D Geometry: Smart Auto Shape Recognizer, 12 planar shapes with Incircle/Circumcircle/Radius overlays, plus 9 wireframe 3D solids.", fontSize = 12.sp)
                        Text("• 5 Subject Modes: Standard Green Board, Math Lab (Function Grapher, Compass, Ruler, Set Squares, Protractor, Calculator), English 4-Line Ruled Paper with Read Aloud, Physics Lab (Live Circuit & Resultant Force), and Chemistry Lab (118-Element Periodic Table, Bohr Atom, Tilt-to-Pour Glassware & Alcohol Lamp).", fontSize = 12.sp)
                        Text("• Presentation Suite: Spotlight, Velvet Screen Wrap Curtain, Magnifier, Clock, Calendar, Timer, Mini Writing Board, Mind Map, and Area Screenshot.", fontSize = 12.sp)
                        Text("• Multi-Page & Free Export: List / Crescent / Page Ball views, Page Groups, Flip Effects, plus 100% free local PNG, PDF, Print, and Room Notebook storage.", fontSize = 12.sp)
                    }
                },
                confirmButton = { TextButton(onClick = viewModel::closePopovers) { Text("Got It") } }
            )
        }

        OpenPopover.ABOUT -> {
            AlertDialog(
                onDismissRequest = viewModel::closePopovers,
                title = { Text("Note 10") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Version 10.0 · Interactive Teaching Whiteboard", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Built for Android phones and tablets — 100% free, offline-ready, with full classroom presentation and STEM teaching tools.", fontSize = 12.sp)
                    }
                },
                confirmButton = { TextButton(onClick = viewModel::closePopovers) { Text("Close") } }
            )
        }

        else -> Unit
    }
}
