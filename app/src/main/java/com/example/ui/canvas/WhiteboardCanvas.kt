package com.example.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.engine.GeometryEngine
import com.example.model.ActiveTool
import com.example.model.BackgroundStyle
import com.example.model.BoardItem
import com.example.model.BohrAtomItem
import com.example.model.ChemApparatusItem
import com.example.model.EnglishLineItem
import com.example.model.EraserType
import com.example.model.ForceVectorItem
import com.example.model.FunctionGraphItem
import com.example.model.ImageItem
import com.example.model.PenType
import com.example.model.PhysicsCircuitItem
import com.example.model.Shape2DItem
import com.example.model.Shape2DType
import com.example.model.Shape3DItem
import com.example.model.StrokeItem
import com.example.model.StrokePoint
import com.example.model.SubjectMode
import com.example.model.TextItem
import com.example.viewmodel.Note10UiState
import com.example.viewmodel.Note10ViewModel
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun WhiteboardCanvas(
    state: Note10UiState,
    viewModel: Note10ViewModel,
    onSpeakText: (String) -> Unit,
    onEditEnglishRow: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val page = state.currentPage
    val density = LocalDensity.current

    // Transient live gesture states (high-FPS, zero ViewModel churn mid-stroke)
    val livePoints = remember { mutableStateListOf<StrokePoint>() }
    var lastPointTimeMs by remember { mutableStateOf(0L) }
    var dragStartBoard by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentBoard by remember { mutableStateOf<Offset?>(null) }
    var preDragItemsSnapshot by remember { mutableStateOf<List<BoardItem>?>(null) }
    var snapGuideX by remember { mutableStateOf<Float?>(null) }
    var snapGuideY by remember { mutableStateOf<Float?>(null) }

    fun screenToBoard(screenPos: Offset): Offset {
        return Offset(
            (screenPos.x - state.panX) / state.zoomScale,
            (screenPos.y - state.panY) / state.zoomScale
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("whiteboard_canvas")
            .pointerInput(state.activeTool, state.zoomScale, state.panX, state.panY) {
                if (state.activeTool == ActiveTool.ROAM) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        viewModel.updatePanZoom(
                            zoom = state.zoomScale * zoom,
                            panX = state.panX + pan.x,
                            panY = state.panY + pan.y
                        )
                    }
                }
            }
            .pointerInput(
                state.activeTool,
                state.penType,
                state.eraserType,
                state.shape2DType,
                state.shape3DType,
                state.zoomScale,
                state.panX,
                state.panY,
                page.id
            ) {
                if (state.activeTool == ActiveTool.ROAM) return@pointerInput
                detectTapGestures(
                    onTap = { tapOffset ->
                        viewModel.closePopovers()
                        val boardPt = screenToBoard(tapOffset)
                        when (state.activeTool) {
                            ActiveTool.SELECT -> {
                                viewModel.selectItemAt(boardPt)
                            }
                            ActiveTool.TEXT -> {
                                viewModel.openTextEditor(existing = null, isBanner = false)
                            }
                            ActiveTool.ERASER -> {
                                if (state.eraserType == EraserType.LINE) {
                                    viewModel.eraseAtPoint(boardPt)
                                }
                            }
                            else -> {
                                if (page.subjectMode == SubjectMode.ENGLISH && state.activeTool == ActiveTool.SELECT) {
                                    val row = ((boardPt.y - 92f) / 116f).toInt().coerceIn(0, 6)
                                    val existing = page.items.filterIsInstance<EnglishLineItem>().firstOrNull { it.rowIndex == row }
                                    onEditEnglishRow(row, existing?.text ?: "")
                                }
                            }
                        }
                    },
                    onDoubleTap = { tapOffset ->
                        val boardPt = screenToBoard(tapOffset)
                        if (state.activeTool == ActiveTool.SELECT && viewModel.selectItemAt(boardPt)) {
                            val sel = viewModel.uiState.value.selectedItem
                            if (sel is TextItem) {
                                viewModel.openTextEditor(existing = sel, isBanner = sel.isBanner)
                            }
                        } else if (page.subjectMode == SubjectMode.ENGLISH) {
                            val row = ((boardPt.y - 92f) / 116f).toInt().coerceIn(0, 6)
                            val existing = page.items.filterIsInstance<EnglishLineItem>().firstOrNull { it.rowIndex == row }
                            onEditEnglishRow(row, existing?.text ?: "")
                        }
                    }
                )
            }
            .pointerInput(
                state.activeTool,
                state.penType,
                state.eraserType,
                state.shape2DType,
                state.shape3DType,
                state.shapeStrokeColorArgb,
                state.shapeFillColorArgb,
                state.shapeStrokeWidth,
                state.zoomScale,
                state.panX,
                state.panY,
                page.id
            ) {
                if (state.activeTool == ActiveTool.ROAM) return@pointerInput
                detectDragGestures(
                    onDragStart = { startOffset ->
                        viewModel.closePopovers()
                        val bp = screenToBoard(startOffset)
                        dragStartBoard = bp
                        dragCurrentBoard = bp
                        when (state.activeTool) {
                            ActiveTool.PEN -> {
                                livePoints.clear()
                                livePoints.add(StrokePoint(bp.x, bp.y, 1f))
                                lastPointTimeMs = System.currentTimeMillis()
                            }
                            ActiveTool.SHAPE_2D -> {
                                if (state.shape2DType == Shape2DType.SMART_RECOGNIZER) {
                                    livePoints.clear()
                                    livePoints.add(StrokePoint(bp.x, bp.y, 1f))
                                }
                            }
                            ActiveTool.SELECT -> {
                                viewModel.selectItemAt(bp)
                                preDragItemsSnapshot = viewModel.uiState.value.currentPage.items
                            }
                            ActiveTool.ERASER -> {
                                if (state.eraserType == EraserType.LINE) {
                                    viewModel.eraseAtPoint(bp)
                                }
                            }
                            else -> Unit
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val bp = screenToBoard(change.position)
                        dragCurrentBoard = bp
                        when (state.activeTool) {
                            ActiveTool.PEN -> {
                                val now = System.currentTimeMillis()
                                val dt = (now - lastPointTimeMs).coerceAtLeast(8L)
                                val last = livePoints.lastOrNull()
                                val dist = if (last != null) hypot(bp.x - last.x, bp.y - last.y) else 0f
                                val velocity = dist / dt.toFloat() // px per ms
                                // Soft Pen Calligraphy: slow = thick (1.6x), fast = thin (0.45x)
                                val widthFactor = (1.65f - velocity * 0.55f).coerceIn(0.45f, 1.75f)
                                if (dist > 2.5f) {
                                    livePoints.add(StrokePoint(bp.x, bp.y, widthFactor))
                                    lastPointTimeMs = now
                                }
                            }
                            ActiveTool.SHAPE_2D -> {
                                if (state.shape2DType == Shape2DType.SMART_RECOGNIZER) {
                                    livePoints.add(StrokePoint(bp.x, bp.y, 1f))
                                }
                            }
                            ActiveTool.ERASER -> {
                                if (state.eraserType == EraserType.LINE) {
                                    viewModel.eraseAtPoint(bp)
                                }
                            }
                            ActiveTool.SELECT -> {
                                val dx = dragAmount.x / state.zoomScale
                                val dy = dragAmount.y / state.zoomScale
                                viewModel.moveSelectedItem(dx, dy)
                                // Smart Alignment Guide detection (Note3 Fig 3-1-6-2)
                                val sel = viewModel.uiState.value.selectedItem
                                if (sel != null) {
                                    val c = viewModel.getItemCenter(sel)
                                    val others = viewModel.uiState.value.currentPage.items.filter { it.id != sel.id }
                                    snapGuideX = others.map { viewModel.getItemCenter(it).x }.firstOrNull { abs(it - c.x) < 12f }
                                    snapGuideY = others.map { viewModel.getItemCenter(it).y }.firstOrNull { abs(it - c.y) < 12f }
                                }
                            }
                            else -> Unit
                        }
                    },
                    onDragEnd = {
                        val start = dragStartBoard
                        val end = dragCurrentBoard
                        when (state.activeTool) {
                            ActiveTool.PEN -> {
                                viewModel.commitStroke(livePoints.toList())
                                livePoints.clear()
                            }
                            ActiveTool.SHAPE_2D -> {
                                if (state.shape2DType == Shape2DType.SMART_RECOGNIZER) {
                                    viewModel.commitSmartShapeStroke(livePoints.toList())
                                    livePoints.clear()
                                } else if (start != null && end != null) {
                                    val w = end.x - start.x
                                    val h = end.y - start.y
                                    if (hypot(w, h) > 18f) {
                                        val isLine = state.shape2DType.isLineLike
                                        val item = Shape2DItem(
                                            x = if (isLine) start.x else minOf(start.x, end.x),
                                            y = if (isLine) start.y else minOf(start.y, end.y),
                                            width = if (isLine) w else abs(w),
                                            height = if (isLine) h else abs(h),
                                            shapeType = state.shape2DType,
                                            strokeColorArgb = state.shapeStrokeColorArgb,
                                            fillColorArgb = if (isLine) null else state.shapeFillColorArgb,
                                            strokeWidth = state.shapeStrokeWidth
                                        )
                                        viewModel.addBoardItem(item)
                                    }
                                }
                            }
                            ActiveTool.SHAPE_3D -> {
                                if (start != null && end != null) {
                                    val w = abs(end.x - start.x)
                                    val h = abs(end.y - start.y)
                                    if (hypot(w, h) > 24f) {
                                        viewModel.addBoardItem(
                                            Shape3DItem(
                                                x = minOf(start.x, end.x),
                                                y = minOf(start.y, end.y),
                                                width = w.coerceAtLeast(64f),
                                                height = h.coerceAtLeast(64f),
                                                shape3DType = state.shape3DType,
                                                strokeColorArgb = state.shapeStrokeColorArgb,
                                                fillColorArgb = state.shapeFillColorArgb,
                                                strokeWidth = state.shapeStrokeWidth
                                            )
                                        )
                                    }
                                }
                            }
                            ActiveTool.ERASER -> {
                                if (state.eraserType == EraserType.SELECT_AREA && start != null && end != null) {
                                    viewModel.eraseInRect(
                                        minOf(start.x, end.x),
                                        minOf(start.y, end.y),
                                        maxOf(start.x, end.x),
                                        maxOf(start.y, end.y)
                                    )
                                }
                            }
                            ActiveTool.FORCE_VECTOR -> {
                                if (start != null && end != null) {
                                    viewModel.commitForceVector(start, end)
                                }
                            }
                            ActiveTool.SELECT -> {
                                preDragItemsSnapshot?.let { viewModel.commitMoveSnapshot(it) }
                                preDragItemsSnapshot = null
                                snapGuideX = null
                                snapGuideY = null
                            }
                            else -> Unit
                        }
                        dragStartBoard = null
                        dragCurrentBoard = null
                    },
                    onDragCancel = {
                        livePoints.clear()
                        dragStartBoard = null
                        dragCurrentBoard = null
                        snapGuideX = null
                        snapGuideY = null
                    }
                )
            }
    ) {
        // 1. Custom Background Image Layer (if selected via Photo Picker)
        if (page.backgroundStyle == BackgroundStyle.CUSTOM_IMAGE && !page.customBgUri.isNullOrBlank()) {
            AsyncImage(
                model = page.customBgUri,
                contentDescription = "Custom whiteboard background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Inserted Photo Images on Canvas (rendered with pan/zoom transform)
        page.items.filterIsInstance<ImageItem>().forEach { img ->
            val isSelected = img.id == state.selectedItemId
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (img.x * state.zoomScale + state.panX).roundToInt(),
                            (img.y * state.zoomScale + state.panY).roundToInt()
                        )
                    }
                    .size(
                        with(density) { (img.width * state.zoomScale).toDp() },
                        with(density) { (img.height * state.zoomScale).toDp() }
                    )
                    .graphicsLayer { rotationZ = img.rotationDeg }
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isSelected) Modifier.border(2.5.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                        else Modifier
                    )
            ) {
                AsyncImage(
                    model = img.uriString,
                    contentDescription = "Inserted whiteboard picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 3. Main Hardware-Accelerated Whiteboard Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (page.backgroundStyle != BackgroundStyle.CUSTOM_IMAGE || page.customBgUri.isNullOrBlank()) {
                drawBoardBackground(page.backgroundStyle, Color(page.backgroundColorArgb))
            }

            withTransform({
                translate(state.panX, state.panY)
                scale(state.zoomScale, state.zoomScale, Offset.Zero)
            }) {
                // Physics circuit connected instruments layer
                val physicsInstruments = page.items.filterIsInstance<PhysicsCircuitItem>()
                if (physicsInstruments.isNotEmpty()) {
                    val sim = GeometryEngine.solvePhysicsCircuit(physicsInstruments)
                    drawPhysicsCircuitLayer(physicsInstruments, sim)
                }

                // Physics Mechanics Resultant Force Vectors layer
                val forceVectors = page.items.filterIsInstance<ForceVectorItem>()
                if (forceVectors.isNotEmpty()) {
                    drawForceVectorLayer(forceVectors)
                }

                // All standard sorted items
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

                // Fading Laser Pen trails (Note3 Section 3.1.7)
                val now = System.currentTimeMillis()
                state.laserTrails.forEach { trail ->
                    val age = (now - trail.createdAtMs).coerceIn(0L, 2000L)
                    val alpha = (1f - age / 2000f).coerceIn(0f, 1f)
                    if (trail.points.size >= 2 && alpha > 0.02f) {
                        val laserStroke = StrokeItem(
                            points = trail.points,
                            colorArgb = Color(trail.colorArgb).copy(alpha = alpha).hashCode(),
                            strokeWidth = 9f,
                            penType = PenType.MARKER
                        )
                        // Glowing red/accent core + outer halo
                        for (i in 1 until trail.points.size) {
                            val a = Offset(trail.points[i - 1].x, trail.points[i - 1].y)
                            val b = Offset(trail.points[i].x, trail.points[i].y)
                            drawLine(Color(0xFFEF4444).copy(alpha = alpha * 0.45f), a, b, 18f, StrokeCap.Round)
                            drawLine(Color.White.copy(alpha = alpha), a, b, 6f, StrokeCap.Round)
                        }
                        laserStroke.id // keep reference clean
                    }
                }

                // Live in-progress stroke / shape / area eraser preview
                if (livePoints.size >= 2) {
                    if (state.activeTool == ActiveTool.PEN) {
                        drawStrokeItem(
                            StrokeItem(
                                points = livePoints,
                                colorArgb = state.penColorArgb,
                                strokeWidth = state.penStrokeWidth,
                                penType = if (state.penType == PenType.LASER) PenType.MARKER else state.penType,
                                stampKind = state.stampKind
                            )
                        )
                    } else if (state.activeTool == ActiveTool.SHAPE_2D && state.shape2DType == Shape2DType.SMART_RECOGNIZER) {
                        drawStrokeItem(
                            StrokeItem(
                                points = livePoints,
                                colorArgb = state.shapeStrokeColorArgb,
                                strokeWidth = state.shapeStrokeWidth,
                                penType = PenType.MARKER
                            )
                        )
                    }
                }

                val ds = dragStartBoard
                val dc = dragCurrentBoard
                if (ds != null && dc != null) {
                    when (state.activeTool) {
                        ActiveTool.SHAPE_2D -> if (state.shape2DType != Shape2DType.SMART_RECOGNIZER) {
                            val isLine = state.shape2DType.isLineLike
                            drawShape2DItem(
                                Shape2DItem(
                                    x = if (isLine) ds.x else minOf(ds.x, dc.x),
                                    y = if (isLine) ds.y else minOf(ds.y, dc.y),
                                    width = if (isLine) dc.x - ds.x else abs(dc.x - ds.x),
                                    height = if (isLine) dc.y - ds.y else abs(dc.y - ds.y),
                                    shapeType = state.shape2DType,
                                    strokeColorArgb = state.shapeStrokeColorArgb,
                                    fillColorArgb = if (isLine) null else state.shapeFillColorArgb,
                                    strokeWidth = state.shapeStrokeWidth
                                )
                            )
                        }
                        ActiveTool.SHAPE_3D -> {
                            drawShape3DItem(
                                Shape3DItem(
                                    x = minOf(ds.x, dc.x),
                                    y = minOf(ds.y, dc.y),
                                    width = abs(dc.x - ds.x).coerceAtLeast(10f),
                                    height = abs(dc.y - ds.y).coerceAtLeast(10f),
                                    shape3DType = state.shape3DType,
                                    strokeColorArgb = state.shapeStrokeColorArgb,
                                    fillColorArgb = state.shapeFillColorArgb,
                                    strokeWidth = state.shapeStrokeWidth
                                )
                            )
                        }
                        ActiveTool.ERASER -> if (state.eraserType == EraserType.SELECT_AREA) {
                            drawRect(
                                color = Color(0xFFEF4444).copy(alpha = 0.14f),
                                topLeft = Offset(minOf(ds.x, dc.x), minOf(ds.y, dc.y)),
                                size = Size(abs(dc.x - ds.x), abs(dc.y - ds.y))
                            )
                            drawRect(
                                color = Color(0xFFEF4444),
                                topLeft = Offset(minOf(ds.x, dc.x), minOf(ds.y, dc.y)),
                                size = Size(abs(dc.x - ds.x), abs(dc.y - ds.y)),
                                style = Stroke(2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                            )
                        }
                        ActiveTool.FORCE_VECTOR -> {
                            drawArrowLine(ds, dc, Color(0xFFFACC15), 6f)
                        }
                        else -> Unit
                    }
                }

                // Smart Alignment Snap Lines (Note3 Fig 3-1-6-2)
                snapGuideX?.let { gx ->
                    drawLine(
                        Color(0xFFEF4444).copy(alpha = 0.7f),
                        Offset(gx, -500f),
                        Offset(gx, 3000f),
                        2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                }
                snapGuideY?.let { gy ->
                    drawLine(
                        Color(0xFFEF4444).copy(alpha = 0.7f),
                        Offset(-500f, gy),
                        Offset(3000f, gy),
                        2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                }

                // Selected Object dashed frame highlight
                state.selectedItem?.let { sel ->
                    val center = viewModel.getItemCenter(sel)
                    val boxColor = if (sel.locked) Color(0xFFEF4444) else Color(0xFFF59E0B)
                    drawCircle(
                        color = boxColor.copy(alpha = 0.14f),
                        radius = 48f,
                        center = center
                    )
                    drawCircle(
                        color = boxColor,
                        radius = 48f,
                        center = center,
                        style = Stroke(2.6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                    )
                }
            }
        }

        // 4. Interactive English Ruled-Paper Line Strips (Note3 Fig 3-3-2-2)
        if (page.subjectMode == SubjectMode.ENGLISH && page.backgroundStyle == BackgroundStyle.ENGLISH_FOUR_LINE) {
            val englishLines = page.items.filterIsInstance<EnglishLineItem>()
            for (row in 0..4) {
                val existing = englishLines.firstOrNull { it.rowIndex == row }
                val yPx = (92f + row * 116f) * state.zoomScale + state.panY
                Row(
                    modifier = Modifier
                        .offset { IntOffset(20, yPx.roundToInt()) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (existing != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1A1E24).copy(alpha = 0.85f),
                            shadowElevation = 2.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.deleteEnglishLine(existing.id) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Delete line", tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { onSpeakText(existing.text) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Read aloud", tint = Color(0xFF86EFAC), modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { onEditEnglishRow(row, existing.text) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Edit, "Edit word", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Text(
                            text = existing.text,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(existing.colorArgb),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEditEnglishRow(row, existing.text) }
                                .padding(horizontal = 8.dp)
                        )
                    } else if (state.activeTool == ActiveTool.SELECT) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2E7D32).copy(alpha = 0.14f),
                            modifier = Modifier
                                .clickable { onEditEnglishRow(row, "") }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ Type line ${row + 1}",
                                fontSize = 12.sp,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
