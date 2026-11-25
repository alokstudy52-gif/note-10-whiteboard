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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ChemicalElement
import com.example.engine.PeriodicTableData
import com.example.model.FunctionGraphItem
import com.example.model.Shape2DItem
import com.example.model.Shape2DType
import com.example.model.TextItem
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
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SubjectToolOverlaysLayer(
    state: Note10UiState,
    viewModel: Note10ViewModel
) {
    val ov = state.overlays

    if (ov.calculatorActive) {
        FloatingCalculatorWidget(
            onInsertResult = { text ->
                viewModel.addBoardItem(TextItem(x = 140f, y = 200f, text = text, fontSizeSp = 30f, isBold = true))
                viewModel.showToast("Inserted calculation")
            },
            onClose = { viewModel.updateOverlays { it.copy(calculatorActive = false) } }
        )
    }

    if (ov.rulerActive) {
        InteractiveRulerOverlay(
            onStampLine = { x, y, len, angleDeg ->
                val rad = Math.toRadians(angleDeg.toDouble())
                viewModel.addBoardItem(
                    Shape2DItem(
                        x = x,
                        y = y,
                        width = (len * cos(rad)).toFloat(),
                        height = (len * sin(rad)).toFloat(),
                        shapeType = Shape2DType.LINE_SEGMENT,
                        strokeColorArgb = state.penColorArgb,
                        strokeWidth = 5f
                    )
                )
                viewModel.showToast("Measured line stamped")
            },
            onClose = { viewModel.updateOverlays { it.copy(rulerActive = false) } }
        )
    }

    if (ov.setSquare30Active) {
        InteractiveSetSquareOverlay(
            is30Degree = true,
            onClose = { viewModel.updateOverlays { it.copy(setSquare30Active = false) } }
        )
    }

    if (ov.setSquare45Active) {
        InteractiveSetSquareOverlay(
            is30Degree = false,
            onClose = { viewModel.updateOverlays { it.copy(setSquare45Active = false) } }
        )
    }

    if (ov.protractorActive) {
        InteractiveProtractorOverlay(
            onStampAngle = { x, y, deg ->
                viewModel.addBoardItem(
                    Shape2DItem(
                        x = x,
                        y = y,
                        width = 160f,
                        height = 120f,
                        shapeType = Shape2DType.ANGLE_MARK,
                        strokeColorArgb = state.penColorArgb,
                        strokeWidth = 4.5f,
                        angleDegrees = deg
                    )
                )
                viewModel.showToast("Inserted ${deg.toInt()}° angle")
            },
            onClose = { viewModel.updateOverlays { it.copy(protractorActive = false) } }
        )
    }

    if (ov.compassActive) {
        InteractiveCompassOverlay(
            onStampCompass = { cx, cy, r, sweep, isSector ->
                viewModel.addBoardItem(
                    Shape2DItem(
                        x = cx - r,
                        y = cy - r,
                        width = r * 2f,
                        height = r * 2f,
                        shapeType = Shape2DType.CIRCLE,
                        strokeColorArgb = state.penColorArgb,
                        fillColorArgb = if (isSector) 0xFF38BDF8.toInt() else null,
                        strokeWidth = 4.5f,
                        arcStartDeg = 0f,
                        arcSweepDeg = sweep,
                        isSector = isSector,
                        showCenter = true
                    )
                )
                viewModel.showToast("Compass figure drawn")
            },
            onClose = { viewModel.updateOverlays { it.copy(compassActive = false) } }
        )
    }

    if (ov.functionEditorOpen) {
        MathFunctionEditorDialog(
            onInsertGraph = { graph ->
                viewModel.addBoardItem(graph)
                viewModel.updateOverlays { it.copy(functionEditorOpen = false) }
            },
            onClose = { viewModel.updateOverlays { it.copy(functionEditorOpen = false) } }
        )
    }

    if (ov.mathFormulaEditorOpen) {
        FormulaBuilderDialog(
            title = "Math Formula Builder",
            symbols = listOf("x²", "x³", "√(", "π", "±", "≠", "≤", "≥", "∞", "∫", "∑", "θ", "α", "β", "Δ", "°", "÷", "×", "→"),
            presets = listOf("x² + y² = r²", "a² + b² = c²", "E = mc²", "sin²θ + cos²θ = 1"),
            onInsert = { text ->
                viewModel.addBoardItem(TextItem(x = 120f, y = 200f, text = text, fontSizeSp = 32f, isBold = true))
                viewModel.updateOverlays { it.copy(mathFormulaEditorOpen = false) }
            },
            onClose = { viewModel.updateOverlays { it.copy(mathFormulaEditorOpen = false) } }
        )
    }

    if (ov.chemFormulaEditorOpen) {
        FormulaBuilderDialog(
            title = "Chemical Equation Builder",
            symbols = listOf("₀", "₁", "₂", "₃", "₄", "₅", "₆", "⁺", "⁻", "→", "⇌", "↑", "↓", "Δ", " + "),
            presets = listOf("H₂O", "CO₂", "H₂SO₄", "NaOH + HCl → NaCl + H₂O", "2H₂ + O₂ → 2H₂O"),
            onInsert = { text ->
                viewModel.addBoardItem(TextItem(x = 110f, y = 210f, text = text, fontSizeSp = 30f, isBold = true))
                viewModel.updateOverlays { it.copy(chemFormulaEditorOpen = false) }
            },
            onClose = { viewModel.updateOverlays { it.copy(chemFormulaEditorOpen = false) } }
        )
    }

    if (ov.periodicTableOpen) {
        PeriodicTableDialog(
            onSelectElement = { elem ->
                viewModel.setSelectedBohrAtom(elem.number)
                viewModel.updateOverlays { it.copy(periodicTableOpen = false) }
                viewModel.showToast("Inserted ${elem.name} (${elem.symbol}) Bohr Atom")
            },
            onClose = { viewModel.updateOverlays { it.copy(periodicTableOpen = false) } }
        )
    }
}

/**
 * Note3 Section 3.3.2.2.4 — Floating Scientific & Memory Calculator (Fig 3-3-1-2-4)
 */
@Composable
private fun FloatingCalculatorWidget(
    onInsertResult: (String) -> Unit,
    onClose: () -> Unit
) {
    var posX by remember { mutableFloatStateOf(40f) }
    var posY by remember { mutableFloatStateOf(120f) }
    var display by remember { mutableStateOf("0") }
    var accumulator by remember { mutableDoubleStateOf(0.0) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var resetOnNext by remember { mutableStateOf(false) }
    var memory by remember { mutableDoubleStateOf(0.0) }

    fun formatNum(v: Double): String {
        return if (v == v.toLong().toDouble()) v.toLong().toString() else String.format("%.4f", v).trimEnd('0').trimEnd('.')
    }

    fun applyOp(op: String) {
        val current = display.toDoubleOrNull() ?: 0.0
        if (pendingOp != null && !resetOnNext) {
            accumulator = when (pendingOp) {
                "+" -> accumulator + current
                "-" -> accumulator - current
                "*" -> accumulator * current
                "/" -> if (current != 0.0) accumulator / current else 0.0
                else -> current
            }
            display = formatNum(accumulator)
        } else {
            accumulator = current
        }
        pendingOp = if (op == "=") null else op
        resetOnNext = true
    }

    fun press(key: String) {
        val cur = display.toDoubleOrNull() ?: 0.0
        when (key) {
            "C" -> { display = "0"; accumulator = 0.0; pendingOp = null; resetOnNext = false }
            "CE" -> { display = "0" }
            "⌫" -> { display = if (display.length > 1) display.dropLast(1) else "0" }
            "±" -> { display = formatNum(-cur) }
            "√" -> { display = if (cur >= 0) formatNum(sqrt(cur)) else "Err"; resetOnNext = true }
            "1/x" -> { display = if (cur != 0.0) formatNum(1.0 / cur) else "Err"; resetOnNext = true }
            "%" -> { display = formatNum(cur / 100.0); resetOnNext = true }
            "MC" -> memory = 0.0
            "MR" -> { display = formatNum(memory); resetOnNext = true }
            "MS" -> memory = cur
            "M+" -> memory += cur
            "M-" -> memory -= cur
            "+", "-", "*", "/", "=" -> applyOp(key)
            "." -> {
                if (resetOnNext) { display = "0."; resetOnNext = false }
                else if (!display.contains(".")) display += "."
            }
            else -> {
                display = if (resetOnNext || display == "0") {
                    resetOnNext = false
                    key
                } else {
                    (display + key).take(14)
                }
            }
        }
    }

    val rows = listOf(
        listOf("MC", "MR", "MS", "M+", "M-"),
        listOf("⌫", "CE", "C", "±", "√"),
        listOf("7", "8", "9", "/", "%"),
        listOf("4", "5", "6", "*", "1/x"),
        listOf("1", "2", "3", "-", "="),
        listOf("0", ".", "+")
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFE2E8F0),
        shadowElevation = 12.dp,
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .width(248.dp)
            .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, drag -> change.consume(); posX += drag.x; posY += drag.y }
            }
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Calculator", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onInsertResult(display) }) {
                        Text("Insert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, "Close calculator", tint = AccentCoral, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF64748B), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = display,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                )
            }
            rows.forEach { rowKeys ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rowKeys.forEach { k ->
                        val bg = when (k) {
                            "=" -> NoteLeafGreen
                            "C", "CE" -> Color(0xFFDC2626)
                            else -> Color(0xFF334155)
                        }
                        val fg = if (k == "=") Color.Black else Color.White
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bg,
                            modifier = Modifier
                                .weight(if (k == "0") 2f else 1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { press(k) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(k, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Note3 Section 3.3.2.2.5 — Translucent Rectilinear Scale (Ruler) with cm ticks & line stamping
 */
@Composable
private fun InteractiveRulerOverlay(
    onStampLine: (Float, Float, Float, Float) -> Unit,
    onClose: () -> Unit
) {
    var posX by remember { mutableFloatStateOf(40f) }
    var posY by remember { mutableFloatStateOf(260f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var lengthCm by remember { mutableFloatStateOf(8f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .graphicsLayer { rotationZ = rotation }
            .pointerInput(Unit) {
                detectDragGestures { change, drag -> change.consume(); posX += drag.x; posY += drag.y }
            }
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F5F9).copy(alpha = 0.84f),
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(width = (lengthCm * 34f).dp, height = 78.dp)
                .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
        ) {
            Box(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize()) {
                    val pxPerCm = size.width / lengthCm
                    val totalMm = (lengthCm * 10).toInt()
                    for (mm in 0..totalMm) {
                        val tx = mm * (pxPerCm / 10f)
                        val tickH = when {
                            mm % 10 == 0 -> 26f
                            mm % 5 == 0 -> 18f
                            else -> 11f
                        }
                        drawLine(Color(0xFF0F172A), Offset(tx, 0f), Offset(tx, tickH), 1.8f)
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, "Close ruler", tint = AccentCoral, modifier = Modifier.size(16.dp))
                    }
                    Text("${String.format("%.1f", lengthCm)} cm", fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Row {
                        IconButton(onClick = { rotation = (rotation + 15f) % 360f }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.RotateRight, "Rotate ruler", tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onStampLine(posX + 12f, posY, lengthCm * 84f, rotation) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Check, "Draw line", tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Note3 Section 3.3.2.2.6 & 3.3.2.2.7 — Translucent 30°-60°-90° & 45°-45°-90° Triangular Plates
 */
@Composable
private fun InteractiveSetSquareOverlay(
    is30Degree: Boolean,
    onClose: () -> Unit
) {
    var posX by remember { mutableFloatStateOf(70f) }
    var posY by remember { mutableFloatStateOf(220f) }
    var rotation by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .size(width = if (is30Degree) 230.dp else 190.dp, height = 190.dp)
            .graphicsLayer { rotationZ = rotation }
            .pointerInput(Unit) {
                detectDragGestures { change, drag -> change.consume(); posX += drag.x; posY += drag.y }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val tri = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, size.height)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(tri, Color(0xFFE0F2FE).copy(alpha = 0.72f))
            drawPath(tri, Color(0xFF0369A1), style = Stroke(3.5f))
            val inner = Path().apply {
                moveTo(size.width * 0.16f, size.height * 0.44f)
                lineTo(size.width * 0.16f, size.height * 0.82f)
                lineTo(size.width * 0.54f, size.height * 0.82f)
                close()
            }
            drawPath(inner, Color(0xFF0369A1).copy(alpha = 0.5f), style = Stroke(2f))
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (is30Degree) "30°/60°" else "45°/45°", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C4A6E))
            IconButton(onClick = { rotation = (rotation + 15f) % 360f }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.RotateRight, "Rotate", tint = Color(0xFF0C4A6E), modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, "Close", tint = AccentCoral, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * Note3 Section 3.3.2.2.8 — Interactive 180° Protractor with vernier angle & ✓ stamp (Fig 3-3-1-2-8-1)
 */
@Composable
private fun InteractiveProtractorOverlay(
    onStampAngle: (Float, Float, Float) -> Unit,
    onClose: () -> Unit
) {
    var posX by remember { mutableFloatStateOf(40f) }
    var posY by remember { mutableFloatStateOf(180f) }
    var angleDeg by remember { mutableFloatStateOf(45f) }

    Column(
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, drag -> change.consume(); posX += drag.x; posY += drag.y }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(width = 260.dp, height = 140.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val r = size.width / 2f - 8f
                val origin = Offset(size.width / 2f, size.height - 8f)
                drawArc(Color(0xFFF8FAFC).copy(alpha = 0.8f), 180f, 180f, true, Offset(origin.x - r, origin.y - r), Size(r * 2, r * 2))
                drawArc(Color(0xFF0F172A), 180f, 180f, true, Offset(origin.x - r, origin.y - r), Size(r * 2, r * 2), style = Stroke(3f))
                for (deg in 0..180 step 10) {
                    val rad = Math.toRadians(-deg.toDouble())
                    val tickLen = if (deg % 30 == 0) 20f else 11f
                    drawLine(
                        Color(0xFF1E293B),
                        Offset(origin.x + ((r - tickLen) * cos(rad)).toFloat(), origin.y + ((r - tickLen) * sin(rad)).toFloat()),
                        Offset(origin.x + (r * cos(rad)).toFloat(), origin.y + (r * sin(rad)).toFloat()),
                        strokeWidth = if (deg % 30 == 0) 2.5f else 1.4f
                    )
                }
                // Vernier arm
                val aRad = Math.toRadians(-angleDeg.toDouble())
                drawLine(Color(0xFFDC2626), origin, Offset(origin.x + r, origin.y), 4f, StrokeCap.Round)
                drawLine(Color(0xFFDC2626), origin, Offset(origin.x + (r * cos(aRad)).toFloat(), origin.y + (r * sin(aRad)).toFloat()), 4f, StrokeCap.Round)
            }
            Text(
                "${angleDeg.toInt()}°",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFDC2626),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
            )
        }
        Surface(shape = RoundedCornerShape(14.dp), color = DockCharcoal.copy(alpha = 0.94f)) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Slider(value = angleDeg, onValueChange = { angleDeg = it }, valueRange = 5f..175f, modifier = Modifier.width(136.dp))
                IconButton(onClick = { onStampAngle(posX + 120f, posY + 40f, angleDeg) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Check, "Insert angle", tint = NoteLeafGreen)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Close protractor", tint = AccentCoral)
                }
            }
        }
    }
}

/**
 * Note3 Section 3.2.2.2.3 — Interactive Compass (Circle, Arc, Sector)
 */
@Composable
private fun InteractiveCompassOverlay(
    onStampCompass: (Float, Float, Float, Float, Boolean) -> Unit,
    onClose: () -> Unit
) {
    var posX by remember { mutableFloatStateOf(90f) }
    var posY by remember { mutableFloatStateOf(200f) }
    var radiusPx by remember { mutableFloatStateOf(110f) }
    var modeIdx by remember { mutableStateOf(0) } // 0=Circle, 1=Arc, 2=Sector
    val modes = listOf("Circle", "Arc 120°", "Sector 90°")

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = DockCharcoal.copy(alpha = 0.92f),
        shadowElevation = 10.dp,
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .width(236.dp)
            .border(1.dp, NoteLeafGreen, RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, drag -> change.consume(); posX += drag.x; posY += drag.y }
            }
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Compass · r=${String.format("%.1f", radiusPx / 38f)}cm", color = NoteLeafGreen, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, "Close compass", tint = AccentCoral, modifier = Modifier.size(16.dp))
                }
            }
            Slider(value = radiusPx, onValueChange = { radiusPx = it }, valueRange = 45f..210f)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                modes.forEachIndexed { idx, label ->
                    StudioChip(label, modeIdx == idx) { modeIdx = idx }
                }
            }
            Button(
                onClick = {
                    val sweep = when (modeIdx) { 1 -> 120f; 2 -> 90f; else -> 360f }
                    onStampCompass(posX + 120f, posY + 140f, radiusPx, sweep, modeIdx == 2)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NoteLeafGreen, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Draw with Compass", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Note3 Section 3.3.2.2.3 — Math Function Graph Editor (Primary + Advanced + Coordinate Grid)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MathFunctionEditorDialog(
    onInsertGraph: (FunctionGraphItem) -> Unit,
    onClose: () -> Unit
) {
    var expression by remember { mutableStateOf("sin(x)") }
    var showGrid by remember { mutableStateOf(true) }
    var reverseX by remember { mutableStateOf(false) }
    var rangeSpan by remember { mutableFloatStateOf(10f) }

    val presets = listOf("sin(x)", "cos(x)", "tan(x)", "x^2", "0.2*x^3", "sqrt(abs(x))", "2*x + 1", "ln(abs(x)+0.1)")
    val tokens = listOf("sin(", "cos(", "tan(", "sqrt(", "abs(", "ln(", "x", "pi", "^", "(", ")", "+", "-", "*", "/")

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Function Grapher") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = expression,
                    onValueChange = { expression = it },
                    label = { Text("y = f(x)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Quick Functions (Primary Mode)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presets.forEach { p ->
                        StudioChip(p, expression == p) { expression = p }
                    }
                }
                Text("Advanced Function Keypad", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    tokens.forEach { tok ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DockSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { expression += tok }
                        ) {
                            Text(tok, color = Color.White, fontSize = 11.sp, fontFamily = JetBrainsMonoFamily, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Show Coordinate Grid", fontSize = 12.sp)
                    Switch(checked = showGrid, onCheckedChange = { showGrid = it })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Reverse X Axis", fontSize = 12.sp)
                    Switch(checked = reverseX, onCheckedChange = { reverseX = it })
                }
                Text("Axis Range: [-${rangeSpan.toInt()}, +${rangeSpan.toInt()}]", fontSize = 11.sp)
                Slider(value = rangeSpan, onValueChange = { rangeSpan = it }, valueRange = 4f..20f)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onInsertGraph(
                        FunctionGraphItem(
                            x = 60f,
                            y = 140f,
                            width = 420f,
                            height = 310f,
                            expression = expression.ifBlank { "sin(x)" },
                            xMin = -rangeSpan,
                            xMax = rangeSpan,
                            yMin = -(rangeSpan * 0.65f),
                            yMax = rangeSpan * 0.65f,
                            showGrid = showGrid,
                            reverseX = reverseX
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NoteLeafGreen, contentColor = Color.Black)
            ) { Text("Plot Graph") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormulaBuilderDialog(
    title: String,
    symbols: List<String>,
    presets: List<String>,
    onInsert: (String) -> Unit,
    onClose: () -> Unit
) {
    var formula by remember { mutableStateOf(presets.first()) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formula,
                    onValueChange = { formula = it },
                    label = { Text("Equation / Formula") },
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presets.forEach { p -> StudioChip(p, formula == p) { formula = p } }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    symbols.forEach { sym ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DockSurface,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { formula += sym }
                        ) {
                            Text(sym, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (formula.isNotBlank()) onInsert(formula) },
                colors = ButtonDefaults.buttonColors(containerColor = NoteLeafGreen, contentColor = Color.Black)
            ) { Text("Insert") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancel") } }
    )
}

/**
 * Note3 Section 3.3.4.2.2 — Interactive 118-Element Periodic Table of Elements
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeriodicTableDialog(
    onSelectElement: (ChemicalElement) -> Unit,
    onClose: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var highlighted by remember { mutableStateOf(PeriodicTableData.getByNumber(20)) }

    val filtered = remember(search) {
        if (search.isBlank()) PeriodicTableData.elements
        else PeriodicTableData.elements.filter {
            it.name.contains(search, ignoreCase = true) ||
                it.symbol.equals(search, ignoreCase = true) ||
                it.number.toString() == search.trim()
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Periodic Table (118 Elements)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search name, symbol, or atomic #") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Selected Element Detail Strip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(highlighted.category.colorArgb).copy(alpha = 0.18f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${highlighted.number} ${highlighted.symbol} · ${highlighted.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${highlighted.category.label} · Mass ${highlighted.mass} · Shells: ${highlighted.shells.joinToString("-")}", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onSelectElement(highlighted) },
                            colors = ButtonDefaults.buttonColors(containerColor = NoteLeafGreen, contentColor = Color.Black)
                        ) {
                            Text("Insert Atom", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        filtered.forEach { elem ->
                            val isSel = elem.number == highlighted.number
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(elem.category.colorArgb).copy(alpha = if (isSel) 1f else 0.78f),
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(if (isSel) 2.5.dp else 0.5.dp, if (isSel) AccentAmber else Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { highlighted = elem }
                            ) {
                                Column(
                                    modifier = Modifier.padding(3.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("${elem.number}", fontSize = 8.sp, color = Color.White.copy(alpha = 0.9f))
                                    Text(elem.symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(elem.name.take(6), fontSize = 7.sp, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Close") } }
    )
}
