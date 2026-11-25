package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.ExportManager
import com.example.model.ActiveTool
import com.example.model.BackgroundStyle
import com.example.model.ImageItem
import com.example.model.OpenPopover
import com.example.model.PageFlipEffect
import com.example.model.WorkMode
import com.example.ui.canvas.WhiteboardCanvas
import com.example.ui.dock.BottomDock
import com.example.ui.dock.SelectedObjectActionBar
import com.example.ui.dock.ToolPopoverHost
import com.example.ui.dock.TopBoardHud
import com.example.ui.panels.MainMenuPopover
import com.example.ui.panels.Note10ModalDialogsHost
import com.example.ui.panels.PageManagerPanel
import com.example.ui.panels.ResourceLibraryPanel
import com.example.ui.panels.SubjectEdgeRail
import com.example.ui.panels.TreasureChestPanel
import com.example.ui.theme.DockBorder
import com.example.ui.theme.DockCharcoal
import com.example.ui.theme.NoteLeafGreen
import com.example.ui.tools.PresentationOverlaysLayer
import com.example.ui.tools.SubjectToolOverlaysLayer
import com.example.viewmodel.Note10ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

@Composable
fun Note10Screen(
    viewModel: Note10ViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val notebooks by viewModel.savedNotebooks.collectAsStateWithLifecycle()
    val resources by viewModel.savedResources.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Free On-Device Android TextToSpeech (Note3 English Read Aloud) ---
    var ttsReady by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        lateinit var engine: TextToSpeech
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine.language = Locale.US
                ttsReady = true
            }
        }
        ttsEngine = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            ttsEngine = null
            ttsReady = false
        }
    }

    LaunchedEffect(state.settings.ttsSpeechRate, ttsReady) {
        if (ttsReady) {
            ttsEngine?.setSpeechRate(state.settings.ttsSpeechRate)
        }
    }

    fun speakText(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val engine = ttsEngine
        if (ttsReady && engine != null) {
            engine.setSpeechRate(state.settings.ttsSpeechRate)
            engine.speak(clean, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
            viewModel.showToast("Reading aloud…")
        } else {
            viewModel.showToast("Text-to-Speech initializing…")
        }
    }

    // --- Zero-Permission Android Photo Picker Launchers (Play Policy Compliant) ---
    val insertPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val localUri = copyPickerUriToLocalFile(context, uri)
                if (localUri != null) {
                    viewModel.addBoardItem(
                        ImageItem(
                            uriString = localUri,
                            x = 120f,
                            y = 180f,
                            width = 280f,
                            height = 220f
                        ),
                        selectAfter = true
                    )
                    viewModel.selectTool(ActiveTool.SELECT)
                    viewModel.showToast("Image inserted on canvas")
                } else {
                    viewModel.showToast("Could not load selected image")
                }
            }
        }
    }

    val backgroundPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val localUri = copyPickerUriToLocalFile(context, uri)
                if (localUri != null) {
                    viewModel.setPageBackground(
                        style = BackgroundStyle.CUSTOM_IMAGE,
                        customBgUri = localUri
                    )
                    viewModel.closePopovers()
                    viewModel.showToast("Custom background applied")
                } else {
                    viewModel.showToast("Could not load background photo")
                }
            }
        }
    }

    val importN10Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Lesson"
                        if (content != null) fileName to content else null
                    }.getOrNull()
                }
                if (result != null) {
                    viewModel.importNotebookFromJson(result.first, result.second)
                } else {
                    viewModel.showToast("Could not read selected file")
                }
            }
        }
    }

    // --- English 4-Line Ruled Paper Row Editor State ---
    var editingEnglishRow by remember { mutableStateOf<Pair<Int, String>?>(null) }

    // --- Safe Export & Print Helpers (100% Free On-Device) ---
    fun exportPngAndShare(label: String = state.currentPage.title) {
        val page = state.currentPage
        scope.launch {
            runCatching {
                val file = withContext(Dispatchers.IO) {
                    ExportManager.exportPageAsPng(context, page, "${state.notebookName}_$label")
                }
                ExportManager.shareFile(context, file, "image/png", "Share Note 10 PNG")
                viewModel.showToast("PNG exported: ${file.name}")
            }.onFailure {
                viewModel.showToast("PNG export failed")
            }
        }
    }

    fun exportPdfAndShare() {
        val pages = state.pages
        val name = state.notebookName
        scope.launch {
            runCatching {
                val file = withContext(Dispatchers.IO) {
                    ExportManager.exportNotebookAsPdf(context, pages, name)
                }
                ExportManager.shareFile(context, file, "application/pdf", "Share Note 10 PDF")
                viewModel.showToast("PDF exported (${pages.size} pages)")
            }.onFailure {
                viewModel.showToast("PDF export failed")
            }
        }
    }

    fun exportN10AndShare() {
        val pages = state.pages
        val name = state.notebookName
        scope.launch {
            runCatching {
                val file = withContext(Dispatchers.IO) {
                    ExportManager.exportNotebookAsN10(context, pages, name)
                }
                ExportManager.shareFile(context, file, "application/json", "Share Note 10 Lesson (.n10)")
                viewModel.showToast("Exported .n10 file")
            }.onFailure {
                viewModel.showToast("File export failed")
            }
        }
    }

    fun printCurrentNotebook() {
        runCatching {
            ExportManager.printNotebook(context, state.pages, state.notebookName)
            viewModel.showToast("Opening system print preview…")
        }.onFailure {
            viewModel.showToast("Print service unavailable")
        }
    }

    fun openSafeHyperlink(rawLink: String) {
        val trimmed = rawLink.trim()
        if (trimmed.startsWith("page:", ignoreCase = true)) {
            val pageNum = trimmed.substringAfter(":").trim().toIntOrNull()
            if (pageNum != null && pageNum in 1..state.pages.size) {
                viewModel.goToPage(pageNum - 1)
                viewModel.showToast("Jumped to Page $pageNum")
            } else {
                viewModel.showToast("Page not found (1–${state.pages.size})")
            }
            return
        }
        val uri = runCatching { Uri.parse(trimmed) }.getOrNull()
        val scheme = uri?.scheme?.lowercase(Locale.ROOT)
        if (uri != null && (scheme == "https" || scheme == "http")) {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }.onFailure {
                viewModel.showToast("No browser found for link")
            }
        } else {
            viewModel.showToast("Use https://... or page:2")
        }
    }

    // --- System Back Button dismisses open popovers/panels gracefully ---
    BackHandler(enabled = state.openPopover != OpenPopover.NONE || state.subjectRailExpanded) {
        viewModel.closePopovers()
    }

    // --- Root Interactive Studio Container ---
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Animated Whiteboard Canvas (honors Note3 Page Flip Effects)
        AnimatedContent(
            targetState = state.currentPageIndex,
            transitionSpec = {
                val forward = targetState >= initialState
                when (state.currentPage.flipEffect) {
                    PageFlipEffect.NONE -> EnterTransition.None togetherWith ExitTransition.None
                    PageFlipEffect.FADE -> fadeIn(tween(240)) togetherWith fadeOut(tween(200))
                    PageFlipEffect.SLIDE -> {
                        if (forward) {
                            (slideInHorizontally(tween(280)) { it / 2 } + fadeIn(tween(240))) togetherWith
                                (slideOutHorizontally(tween(240)) { -it / 3 } + fadeOut(tween(200)))
                        } else {
                            (slideInHorizontally(tween(280)) { -it / 2 } + fadeIn(tween(240))) togetherWith
                                (slideOutHorizontally(tween(240)) { it / 3 } + fadeOut(tween(200)))
                        }
                    }
                    PageFlipEffect.SHUTTER -> {
                        (expandVertically(tween(280)) + fadeIn(tween(220))) togetherWith
                            (shrinkVertically(tween(240)) + fadeOut(tween(180)))
                    }
                    PageFlipEffect.RIPPLE_ZOOM -> {
                        (scaleIn(initialScale = 0.88f, animationSpec = tween(260)) + fadeIn(tween(220))) togetherWith
                            (scaleOut(targetScale = 1.08f, animationSpec = tween(220)) + fadeOut(tween(180)))
                    }
                }
            },
            label = "page_flip_transition",
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            WhiteboardCanvas(
                state = state,
                viewModel = viewModel,
                onSpeakText = ::speakText,
                onEditEnglishRow = { rowIdx, currentText ->
                    editingEnglishRow = rowIdx to currentText
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Floating Presentation Overlays (Spotlight, Curtain, Magnifier, Clock, Calendar, Timer, Mini Board, Mind Map, Screenshot, Encyclopedia)
        PresentationOverlaysLayer(
            state = state,
            viewModel = viewModel,
            onCaptureScreenshot = { exportPngAndShare("screenshot") }
        )

        // 3. Floating Subject Tool Overlays (Calculator, Ruler, 30°/45° Set Squares, Protractor, Compass, Function Plotter, Formula Editor, Periodic Table, Chem Equations)
        SubjectToolOverlaysLayer(
            state = state,
            viewModel = viewModel
        )

        // 4. Top Board HUD (Subject Pill + Work Mode Pill + Note3 Signature Top-Right Live Clock)
        if (state.workMode != WorkMode.FOCUS) {
            TopBoardHud(
                state = state,
                onModeChipClick = {
                    val nextMode = when (state.workMode) {
                        WorkMode.PRESENTING -> WorkMode.PREPARATION
                        WorkMode.PREPARATION -> WorkMode.FOCUS
                        WorkMode.FOCUS -> WorkMode.PRESENTING
                    }
                    viewModel.setWorkMode(nextMode)
                },
                onSubjectBadgeClick = viewModel::toggleSubjectRail,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // 5. Left-Edge Collapsible Subject Mode Rail (Note3 Fig 3-3)
        if (state.workMode != WorkMode.FOCUS) {
            SubjectEdgeRail(
                activeSubject = state.currentPage.subjectMode,
                expanded = state.subjectRailExpanded,
                onToggleExpand = viewModel::toggleSubjectRail,
                onSelectSubject = viewModel::switchSubjectMode,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        // 6. Floating Popovers & Side Panels
        val popoverBottomPadding = Modifier
            .navigationBarsPadding()
            .padding(bottom = 86.dp)

        when (state.openPopover) {
            OpenPopover.PEN,
            OpenPopover.ERASER,
            OpenPopover.SHAPE_2D,
            OpenPopover.SHAPE_3D,
            OpenPopover.ROAM -> {
                ToolPopoverHost(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .then(popoverBottomPadding)
                )
            }

            OpenPopover.MAIN_MENU -> {
                MainMenuPopover(
                    state = state,
                    viewModel = viewModel,
                    onImportPhoto = {
                        insertPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onImportN10 = {
                        importN10Launcher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                    },
                    onExportPng = { exportPngAndShare() },
                    onExportPdf = ::exportPdfAndShare,
                    onExportN10 = ::exportN10AndShare,
                    onPrint = ::printCurrentNotebook,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .then(popoverBottomPadding)
                )
            }

            OpenPopover.TREASURE_CHEST -> {
                TreasureChestPanel(
                    state = state,
                    viewModel = viewModel,
                    onPickBackgroundPhoto = {
                        backgroundPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .then(popoverBottomPadding)
                )
            }

            OpenPopover.RESOURCE_LIBRARY -> {
                ResourceLibraryPanel(
                    resources = resources,
                    viewModel = viewModel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .then(popoverBottomPadding)
                )
            }

            OpenPopover.PAGE_MANAGER -> {
                PageManagerPanel(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            else -> Unit
        }

        // 7. Contextual Selected Object Action Bar (Planar Geometry / Bohr / Chem / Physics / Clone / Lock / Link / Speak / Delete)
        if (state.selectedItem != null && state.openPopover == OpenPopover.NONE && state.workMode != WorkMode.FOCUS) {
            SelectedObjectActionBar(
                state = state,
                viewModel = viewModel,
                onSpeakText = ::speakText,
                onOpenHyperlink = ::openSafeHyperlink,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 86.dp)
            )
        }

        // 8. Floating 3-Zone Bottom Studio Dock
        BottomDock(
            state = state,
            viewModel = viewModel,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 9. Non-intrusive Status Toast Pill
        AnimatedVisibility(
            visible = state.statusToast != null,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 54.dp)
        ) {
            state.statusToast?.let { toastMsg ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DockCharcoal.copy(alpha = 0.94f),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .border(1.dp, NoteLeafGreen.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                        .testTag("status_toast")
                ) {
                    Text(
                        text = toastMsg,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // 10. Modal Dialogs (Formatted Text Editor, Saved Notebooks, Settings, Hyperlink, About)
        Note10ModalDialogsHost(
            state = state,
            notebooks = notebooks,
            viewModel = viewModel,
            onSpeakText = ::speakText
        )

        // 11. English 4-Line Ruled Paper Row Editor Dialog
        editingEnglishRow?.let { (rowIdx, initialText) ->
            var rowText by remember(rowIdx, initialText) { mutableStateOf(initialText) }
            AlertDialog(
                onDismissRequest = { editingEnglishRow = null },
                title = { Text("English Practice Line ${rowIdx + 1}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rowText,
                            onValueChange = { rowText = it },
                            label = { Text("Type word or sentence…") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (rowText.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(onClick = { speakText(rowText) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Read aloud")
                                }
                                Text("Tap speaker to hear pronunciation (free TTS)", fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addOrUpdateEnglishLine(rowIdx, rowText)
                            editingEnglishRow = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoteLeafGreen,
                            contentColor = Color(0xFF0E2107)
                        )
                    ) { Text("Save Line") }
                },
                dismissButton = {
                    TextButton(onClick = { editingEnglishRow = null }) { Text("Cancel") }
                }
            )
        }
    }
}

private suspend fun copyPickerUriToLocalFile(context: Context, sourceUri: Uri): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val mediaDir = File(context.filesDir, "media").apply { mkdirs() }
            val destFile = File(mediaDir, "img_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null
            Uri.fromFile(destFile).toString()
        }.getOrNull()
    }
