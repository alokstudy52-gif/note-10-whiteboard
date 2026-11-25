package com.example.ui.tools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BohrAtomItem
import com.example.model.ChemApparatusItem
import com.example.model.FunctionGraphItem
import com.example.model.MindMapNode
import com.example.model.PenType
import com.example.model.Shape2DItem
import com.example.model.Shape3DItem
import com.example.model.StrokeItem
import com.example.model.StrokePoint
import com.example.model.TextItem
import com.example.ui.canvas.drawBoardBackground
import com.example.ui.canvas.drawBohrAtomItem
import com.example.ui.canvas.drawChemApparatusItem
import com.example.ui.canvas.drawFunctionGraphItem
import com.example.ui.canvas.drawShape2DItem
import com.example.ui.canvas.drawShape3DItem
import com.example.ui.canvas.drawStrokeItem
import com.example.ui.canvas.drawTextItem
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PresentationOverlaysLayer(
    state: Note10UiState,
    viewModel: Note10ViewModel,
    onCaptureScreenshot: () -> Unit
) {
    val ov = state.overlays

    if (ov.spotlightActive) {
        SpotlightOverlay(
            isRect = ov.spotlightIsRect,
            dimAlpha = ov.spotlightDimAlpha,
            onToggleShape = { viewModel.updateOverlays { it.copy(spotlightIsRect = !it.spotlightIsRect) } },
            onDimChange = { a -> viewModel.updateOverlays { it.copy(spotlightDimAlpha = a) } },
            onClose = { viewModel.updateOverlays { it.copy(spotlightActive = false) } }
        )
    }

    if (ov.screenWrapActive) {
        ScreenWrapCurtainOverlay(
            onClose = { viewModel.updateOverlays { it.copy(screenWrapActive = false) } }
        )
    }

    if (ov.magnifierActive) {
        MagnifierLensOverlay(
            state = state,
            zoom = ov.magnifierZoom,
            onZoomChange = { z -> viewModel.updateOverlays { it.copy(magnifierZoom = z) } },
            onClose = { viewModel.updateOverlays { it.copy(magnifierActive = false) } }
        )
    }

    if (ov.clockActive) {
        FloatingClockWidget(onClose = { viewModel.updateOverlays { it.copy(clockActive = false) } })
    }

    if (ov.calendarActive) {
        FloatingCalendarWidget(onClose = { viewModel.updateOverlays { it.copy(calendarActive = false) } })
    }

    if (ov.timerActive) {
        FloatingTimerWidget(onClose = { viewModel.updateOverlays { it.copy(timerActive = false) } })
    }

    if (ov.writingBoardActive) {
        MiniWritingBoardOverlay(
            onExportStroke = { pts ->
                viewModel.addBoardItem(
                    StrokeItem(
                        points = pts,
                        colorArgb = 0xFF111827.toInt(),
                        strokeWidth = 6f,
                        penType = PenType.MARKER
                    )
                )
                viewModel.showToast("Inserted sketch to page")
            },
            onClose = { viewModel.updateOverlays { it.copy(writingBoardActive = false) } }
        )
    }

    if (ov.mindMapActive) {
        MindMapAxisOverlay(
            nodes = state.currentPage.mindMapNodes,
            onAddNode = { label, note, big -> viewModel.addMindMapNode(label, note, big) },
            onDeleteNode = { viewModel.deleteMindMapNode(it) },
            onClose = { viewModel.updateOverlays { it.copy(mindMapActive = false) } }
        )
    }

    if (ov.screenshotSelectActive) {
        ScreenshotToolBar(
            onSnapFull = {
                onCaptureScreenshot()
                viewModel.updateOverlays { it.copy(screenshotSelectActive = false) }
            },
            onClose = { viewModel.updateOverlays { it.copy(screenshotSelectActive = false) } }
        )
    }

    if (ov.encyclopediaOpen) {
        EncyclopediaReferenceDialog(
            onInsertFormula = { txt ->
                viewModel.addBoardItem(TextItem(x = 120f, y = 200f, text = txt, fontSizeSp = 24f, isBold = true, bgColorArgb = 0xFFFFFFFF.toInt()))
                viewModel.updateOverlays { it.copy(encyclopediaOpen = false) }
            },
            onClose = { viewModel.updateOverlays { it.copy(encyclopediaOpen = false) } }
        )
    }
}

/**
 * Note3 Section 3.1.3.3.7 — Draggable Spotlight with round/rect toggle & dimness slider
 */
@Composable
private fun SpotlightOverlay(
    isRect: Boolean,
    dimAlpha: Float,
    onToggleShape: () -> Unit,
    onDimChange: (Float) -> Unit,
    onClose: () -> Unit
) {
    var centerX by remember { mutableFloatStateOf(420f) }
    var centerY by remember { mutableFloatStateOf(460f) }
    var radius by remember { mutableFloatStateOf(165f) }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        centerX += drag.x
                        centerY += drag.y
                    }
                }
        ) {
            val holePath = Path().apply {
                if (isRect) {
                    addRect(Rect(centerX - radius, centerY - radius * 0.72f, centerX + radius, centerY + radius * 0.72f))
                } else {
                    addOval(Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius))
                }
            }
            clipPath(holePath, clipOp = ClipOp.Difference) {
                drawRect(Color.Black.copy(alpha = dimAlpha))
            }
            drawPath(holePath, Color.White.copy(alpha = 0.85f), style = Stroke(4f))
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DockCharcoal.copy(alpha = 0.94f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
                .border(1.dp, DockBorder, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(onClick = onToggleShape, modifier = Modifier.size(34.dp)) {
                    Icon(
                        if (isRect) Icons.Default.RadioButtonUnchecked else Icons.Default.CropSquare,
                        "Toggle shape",
                        tint = NoteLeafGreen
                    )
                }
                Text("Size", color = DockOnSurfaceMuted, fontSize = 11.sp)
                Slider(value = radius, onValueChange = { radius = it }, valueRange = 80f..340f, modifier = Modifier.width(86.dp))
                Text("Dim", color = DockOnSurfaceMuted, fontSize = 11.sp)
                Slider(value = dimAlpha, onValueChange = onDimChange, valueRange = 0.35f..0.95f, modifier = Modifier.width(74.dp))
                IconButton(onClick = onClose, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Close, "Close spotlight", tint = AccentCoral)
                }
            }
        }
    }
}

/**
 * Note3 Section 3.1.3.3.6 — Crimson Velvet Screen Wrap Curtain
 */
@Composable
private fun ScreenWrapCurtainOverlay(onClose: () -> Unit) {
    var posX by remember { mutableFloatStateOf(40f) }
    var posY by remember { mutableFloatStateOf(160f) }
    var widthDp by remember { mutableFloatStateOf(300f) }
    var heightDp by remember { mutableFloatStateOf(240f) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 10.dp,
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .size(width = widthDp.dp, height = heightDp.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    posX += drag.x
                    posY += drag.y
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF450A0A),
                            Color(0xFF991B1B),
                            Color(0xFF5C0C0C),
                            Color(0xFF991B1B),
                            Color(0xFF450A0A),
                            Color(0xFF7F1D1D)
                        )
                    )
                )
                .border(2.dp, Color(0xFFD97706), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.55f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { widthDp = (widthDp + 40f).coerceAtMost(420f); heightDp = (heightDp + 40f).coerceAtMost(460f) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, "Expand curtain", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { posX = 40f; posY = 160f; widthDp = 300f; heightDp = 240f }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, "Reset curtain", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Close curtain", tint = AccentCoral, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = "Drag curtain to reveal",
                color = Color(0xFFFDE68A).copy(alpha = 0.78f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * Note3 Section 3.1.3.3.5 — Live Optical Magnifier Lens
 */
@Composable
private fun MagnifierLensOverlay(
    state: Note10UiState,
    zoom: Float,
    onZoomChange: (Float) -> Unit,
    onClose: () -> Unit
) {
    var lensX by remember { mutableFloatStateOf(160f) }
    var lensY by remember { mutableFloatStateOf(260f) }
    val lensSizeDp = 176.dp
    val page = state.currentPage

    Column(
        modifier = Modifier
            .offset { IntOffset(lensX.roundToInt(), lensY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    lensX += drag.x
                    lensY += drag.y
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(lensSizeDp)
                .clip(CircleShape)
                .border(5.dp, DockCharcoal, CircleShape)
                .border(2.dp, NoteLeafGreen, CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBoardBackground(page.backgroundStyle, Color(page.backgroundColorArgb))
                val centerBoardX = (lensX + size.width / 2f - state.panX) / state.zoomScale
                val centerBoardY = (lensY + size.height / 2f - state.panY) / state.zoomScale
                withTransform({
                    translate(size.width / 2f, size.height / 2f)
                    scale(zoom, zoom, Offset.Zero)
                    translate(-centerBoardX, -centerBoardY)
                }) {
                    page.items.sortedBy { it.zIndex }.forEach { item ->
                        when (item) {
                            is StrokeItem -> drawStrokeItem(item)
                            is Shape2DItem -> drawShape2DItem(item)
                            is Shape3DItem -> drawShape3DItem(item)
                            is TextItem -> drawTextItem(item)
                            is FunctionGraphItem -> drawFunctionGraphItem(item)
                            is BohrAtomItem -> drawBohrAtomItem(item)
                            is ChemApparatusItem -> drawChemApparatusItem(item)
                            else -> Unit
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DockCharcoal.copy(alpha = 0.92f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${String.format("%.1f", zoom)}x", color = NoteLeafGreen, fontFamily = JetBrainsMonoFamily, fontSize = 11.sp)
                Slider(value = zoom, onValueChange = onZoomChange, valueRange = 1.4f..4.0f, modifier = Modifier.width(84.dp))
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, "Close magnifier", tint = AccentCoral, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Note3 Section 3.1.3.3.3 — Draggable Analog + Digital Classroom Clock
 */
@Composable
private fun FloatingClockWidget(onClose: () -> Unit) {
    var posX by remember { mutableFloatStateOf(60f) }
    var posY by remember { mutableFloatStateOf(140f) }
    var cal by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            cal = Calendar.getInstance()
            delay(1000L)
        }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 10.dp,
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    posX += drag.x
                    posY += drag.y
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.width(156.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(cal.time),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, "Close clock", tint = AccentCoral, modifier = Modifier.size(16.dp))
                }
            }
            Canvas(modifier = Modifier.size(142.dp)) {
                val r = size.minDimension / 2f - 6f
                val c = center
                drawCircle(Color(0xFFF8FAFC), r, c)
                drawCircle(Color(0xFF1E293B), r, c, style = Stroke(5f))
                for (i in 0 until 12) {
                    val ang = Math.toRadians((i * 30 - 90).toDouble())
                    val inner = r - if (i % 3 == 0) 14f else 8f
                    drawLine(
                        Color(0xFF0F172A),
                        Offset(c.x + (inner * cos(ang)).toFloat(), c.y + (inner * sin(ang)).toFloat()),
                        Offset(c.x + ((r - 3f) * cos(ang)).toFloat(), c.y + ((r - 3f) * sin(ang)).toFloat()),
                        strokeWidth = if (i % 3 == 0) 3.5f else 2f
                    )
                }
                val sec = cal.get(Calendar.SECOND)
                val min = cal.get(Calendar.MINUTE) + sec / 60f
                val hr = (cal.get(Calendar.HOUR) % 12) + min / 60f

                fun hand(deg: Double, len: Float, color: Color, w: Float) {
                    val rad = Math.toRadians(deg - 90.0)
                    drawLine(color, c, Offset(c.x + (len * cos(rad)).toFloat(), c.y + (len * sin(rad)).toFloat()), w, StrokeCap.Round)
                }
                hand(hr * 30.0, r * 0.5f, Color(0xFF0F172A), 6f)
                hand(min * 6.0, r * 0.72f, Color(0xFF1E293B), 4f)
                hand(sec * 6.0, r * 0.82f, Color(0xFFEF4444), 2f)
                drawCircle(Color(0xFFEF4444), 5f, c)
            }
        }
    }
}

/**
 * Note3 Section 3.1.3.3.4 — Interactive Day / Full-Month Calendar Widget
 */
@Composable
private fun FloatingCalendarWidget(onClose: () -> Unit) {
    var posX by remember { mutableFloatStateOf(90f) }
    var posY by remember { mutableFloatStateOf(160f) }
    var expandedMonth by remember { mutableStateOf(false) }
    var monthOffset by remember { mutableIntStateOf(0) }

    val cal = remember(monthOffset) {
        Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
    }
    val today = remember { Calendar.getInstance() }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .width(if (expandedMonth) 256.dp else 174.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    posX += drag.x
                    posY += drag.y
                }
            }
    ) {
        Column {
            // Iconic Note3 black top calendar header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = { expandedMonth = !expandedMonth }, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.CalendarMonth, "Toggle month view", tint = NoteLeafGreen, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, "Close calendar", tint = AccentCoral, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (!expandedMonth) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedMonth = true }
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(today.time),
                        color = NoteLeafGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        today.get(Calendar.DAY_OF_MONTH).toString(),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(today.time),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { monthOffset-- }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev month")
                        }
                        Text(SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        IconButton(onClick = { monthOffset++ }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month")
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach {
                            Text(it, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    val firstCal = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                    val startOffset = firstCal.get(Calendar.DAY_OF_WEEK) - 1
                    val daysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7
                    for (row in 0 until totalCells / 7) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            for (col in 0 until 7) {
                                val dayNum = row * 7 + col - startOffset + 1
                                val isToday = monthOffset == 0 && dayNum == today.get(Calendar.DAY_OF_MONTH)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isToday) AccentAmber else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayNum in 1..daysInMonth) {
                                        Text(
                                            "$dayNum",
                                            fontSize = 11.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) Color.Black else Color(0xFF1E293B)
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

/**
 * Note3 Section 3.1.3.3.8 — Digital Flip-Style Classroom Timer & Countdown
 */
@Composable
private fun FloatingTimerWidget(onClose: () -> Unit) {
    var posX by remember { mutableFloatStateOf(70f) }
    var posY by remember { mutableFloatStateOf(150f) }
    var running by remember { mutableStateOf(false) }
    var isCountdown by remember { mutableStateOf(false) }
    var seconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(running, isCountdown) {
        while (running) {
            delay(1000L)
            if (isCountdown) {
                if (seconds > 0) seconds-- else running = false
            } else {
                seconds++
            }
        }
    }

    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFE2E8F0),
        shadowElevation = 10.dp,
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .border(1.5.dp, Color(0xFF94A3B8), RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    posX += drag.x
                    posY += drag.y
                }
            }
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.width(216.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isCountdown) "Countdown" else "Stopwatch", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, "Close timer", tint = AccentCoral, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                FlipTile(String.format("%02d", hrs))
                Text(":", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF0F172A))
                FlipTile(String.format("%02d", mins))
                Text(":", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF0F172A))
                FlipTile(String.format("%02d", secs))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { running = !running }, modifier = Modifier.clip(CircleShape).background(NoteLeafGreen).size(36.dp)) {
                    Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, "Start/Pause", tint = Color.Black)
                }
                IconButton(onClick = { running = false; seconds = 0; isCountdown = false }, modifier = Modifier.clip(CircleShape).background(Color(0xFFCBD5E1)).size(36.dp)) {
                    Icon(Icons.Default.Refresh, "Reset", tint = Color.Black)
                }
                TextButton(onClick = { isCountdown = true; seconds += 60 }) { Text("+1m", fontWeight = FontWeight.Bold) }
                TextButton(onClick = { isCountdown = true; seconds += 300 }) { Text("+5m", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun FlipTile(digits: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0F172A)) {
        Text(
            text = digits,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFEF08A),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/**
 * Note3 Section 3.1.3.3.2 — Pull-down Mini Writing Board
 */
@Composable
private fun MiniWritingBoardOverlay(
    onExportStroke: (List<StrokePoint>) -> Unit,
    onClose: () -> Unit
) {
    val strokes = remember { mutableStateListOf<List<StrokePoint>>() }
    val current = remember { mutableStateListOf<StrokePoint>() }

    Surface(
        shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
        color = Color(0xFFF8FAFC),
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 56.dp)
            .height(250.dp)
            .border(2.dp, DockCharcoal, RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
    ) {
        Column {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { p -> current.clear(); current.add(StrokePoint(p.x, p.y)) },
                            onDrag = { change, _ -> change.consume(); current.add(StrokePoint(change.position.x, change.position.y)) },
                            onDragEnd = {
                                if (current.size >= 2) strokes.add(current.toList())
                                current.clear()
                            }
                        )
                    }
            ) {
                (strokes + listOf(current)).forEach { pts ->
                    if (pts.size >= 2) {
                        for (i in 1 until pts.size) {
                            drawLine(Color(0xFF111827), Offset(pts[i - 1].x, pts[i - 1].y), Offset(pts[i].x, pts[i].y), 5f, StrokeCap.Round)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DockCharcoal)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mini Writing Board", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { strokes.clear() }) { Text("Clear", color = DockOnSurfaceMuted) }
                    TextButton(onClick = {
                        strokes.forEach { s -> onExportStroke(s.map { StrokePoint(it.x + 120f, it.y + 180f) }) }
                        strokes.clear()
                    }) { Text("Insert", color = NoteLeafGreen, fontWeight = FontWeight.Bold) }
                    IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, "Close mini board", tint = AccentCoral)
                    }
                }
            }
        }
    }
}

/**
 * Note3 Section 3.1.3.3.11 — Horizontal Guide-Axis Mind Map Bar
 */
@Composable
private fun MindMapAxisOverlay(
    nodes: List<MindMapNode>,
    onAddNode: (String, String, Boolean) -> Unit,
    onDeleteNode: (String) -> Unit,
    onClose: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var previewNode by remember { mutableStateOf<MindMapNode?>(null) }
    var labelInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var isBig by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DockCharcoal.copy(alpha = 0.88f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .border(1.dp, NoteLeafGreen, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Mind Map Axis (${nodes.size} nodes)", color = NoteLeafGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row {
                        IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, "Add node", tint = NoteLeafGreen)
                        }
                        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, "Close mind map", tint = AccentCoral)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(NoteLeafGreen.copy(alpha = 0.7f)))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (nodes.isEmpty()) {
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("+ Tap to add your first Mind Map node", color = Color.White, fontSize = 12.sp)
                            }
                        } else {
                            nodes.forEach { node ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (node.isBig) NoteLeafGreen else DockSurface,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { previewNode = node }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = if (node.isBig) 10.dp else 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            node.label,
                                            color = if (node.isBig) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (node.isBig) 13.sp else 11.sp
                                        )
                                        Icon(
                                            Icons.Default.Close,
                                            "Delete node",
                                            tint = if (node.isBig) Color(0xFF7F1D1D) else AccentCoral,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { onDeleteNode(node.id) }
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

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Mind Map Node") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = labelInput, onValueChange = { labelInput = it }, label = { Text("Topic Title") }, singleLine = true)
                    OutlinedTextField(value = noteInput, onValueChange = { noteInput = it }, label = { Text("Details / Note") }, minLines = 2)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { isBig = true }) { Text(if (isBig) "● Big Node" else "○ Big Node") }
                        TextButton(onClick = { isBig = false }) { Text(if (!isBig) "● Small Node" else "○ Small Node") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onAddNode(labelInput, noteInput, isBig)
                    labelInput = ""
                    noteInput = ""
                    showAddDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    previewNode?.let { node ->
        AlertDialog(
            onDismissRequest = { previewNode = null },
            title = { Text(node.label) },
            text = { Text(node.note.ifBlank { "No additional notes for this node." }) },
            confirmButton = { TextButton(onClick = { previewNode = null }) { Text("Close") } }
        )
    }
}

/**
 * Note3 Section 3.1.3.3.10 — Floating Screenshot Capture Bar
 */
@Composable
private fun ScreenshotToolBar(onSnapFull: () -> Unit, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DockCharcoal.copy(alpha = 0.95f),
            shadowElevation = 10.dp,
            modifier = Modifier
                .padding(top = 66.dp)
                .border(1.dp, AccentAmber, RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                Text("Board Capture", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onSnapFull,
                    colors = ButtonDefaults.buttonColors(containerColor = NoteLeafGreen, contentColor = Color.Black)
                ) {
                    Text("Snap & Share PNG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, "Close screenshot", tint = AccentCoral)
                }
            }
        }
    }
}

/**
 * Free Offline STEM Reference Encyclopedia (Note3 Section 3.1.5 replacement with zero network/API cost)
 */
@Composable
private fun EncyclopediaReferenceDialog(
    onInsertFormula: (String) -> Unit,
    onClose: () -> Unit
) {
    val entries = listOf(
        "Quadratic Formula" to "x = (-b ± √(b² - 4ac)) / 2a",
        "Pythagorean Theorem" to "a² + b² = c²",
        "Circle Area & Circumference" to "A = πr²   |   C = 2πr",
        "Euler's Identity" to "e^(iπ) + 1 = 0",
        "Newton's 2nd Law" to "F = m × a",
        "Ohm's Law" to "V = I × R   |   P = V × I",
        "Kinetic Energy" to "KE = ½ m v²",
        "Ideal Gas Law" to "PV = nRT",
        "Photosynthesis" to "6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂",
        "Water Electrolysis" to "2H₂O → 2H₂↑ + O₂↑"
    )
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Offline STEM Reference") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                entries.forEach { (title, formula) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInsertFormula("$title:\n$formula") }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(formula, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, color = Color(0xFF1E40AF))
                            Text("Tap to insert on whiteboard", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Close") } }
    )
}
