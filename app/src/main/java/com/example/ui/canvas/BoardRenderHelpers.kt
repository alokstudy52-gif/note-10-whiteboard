package com.example.ui.canvas

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import com.example.engine.GeometryEngine
import com.example.engine.PeriodicTableData
import com.example.model.BackgroundStyle
import com.example.model.BohrAtomItem
import com.example.model.ChemApparatusItem
import com.example.model.ChemApparatusKind
import com.example.model.ForceVectorItem
import com.example.model.FunctionGraphItem
import com.example.model.PenType
import com.example.model.PhysicsCircuitItem
import com.example.model.PhysicsInstrumentKind
import com.example.model.Shape2DItem
import com.example.model.Shape2DType
import com.example.model.Shape3DItem
import com.example.model.Shape3DType
import com.example.model.StrokeItem
import com.example.model.TextItem
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

fun DrawScope.drawBoardBackground(style: BackgroundStyle, baseColor: Color) {
    when (style) {
        BackgroundStyle.SCENIC_INK -> {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFF3F4F6), Color(0xFFE5E7EB), Color(0xFFD1D5DB))
                )
            )
            // Soft misty mountain ink wash silhouettes at bottom
            val mountain = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, size.height * 0.78f)
                quadraticTo(size.width * 0.22f, size.height * 0.62f, size.width * 0.48f, size.height * 0.76f)
                quadraticTo(size.width * 0.74f, size.height * 0.88f, size.width, size.height * 0.68f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(mountain, Color(0xFF475569).copy(alpha = 0.16f))
        }
        BackgroundStyle.SCENIC_SKY -> {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFDBEAFE), Color(0xFFEFF6FF), Color(0xFFFFFFFF))
                )
            )
        }
        else -> {
            drawRect(color = baseColor)
        }
    }

    val isDark = (baseColor.red * 0.299f + baseColor.green * 0.587f + baseColor.blue * 0.114f) < 0.45f
    val lineColor = if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.14f)

    when (style) {
        BackgroundStyle.GRID -> {
            val step = 44f
            var x = 0f
            while (x <= size.width) {
                drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.2f)
                x += step
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.2f)
                y += step
            }
        }
        BackgroundStyle.RULED -> {
            val step = 52f
            var y = 96f
            while (y < size.height) {
                drawLine(lineColor, Offset(24f, y), Offset(size.width - 24f, y), strokeWidth = 1.5f)
                y += step
            }
            drawLine(Color(0xFFEF4444).copy(alpha = 0.25f), Offset(72f, 0f), Offset(72f, size.height), 2f)
        }
        BackgroundStyle.ENGLISH_FOUR_LINE -> {
            // Iconic Note3 English 4-line calligraphy ruled paper (Fig 3-3-2-2)
            var topY = 92f
            val bandGap = 116f
            val subGap = 22f
            val redLine = Color(0xFFDC2626).copy(alpha = 0.35f)
            val blueLine = Color(0xFF2563EB).copy(alpha = 0.32f)
            while (topY + subGap * 3 < size.height - 96f) {
                drawLine(blueLine, Offset(36f, topY), Offset(size.width - 36f, topY), 1.4f)
                drawLine(redLine, Offset(36f, topY + subGap), Offset(size.width - 36f, topY + subGap), 1.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                drawLine(redLine, Offset(36f, topY + subGap * 2), Offset(size.width - 36f, topY + subGap * 2), 1.8f)
                drawLine(blueLine, Offset(36f, topY + subGap * 3), Offset(size.width - 36f, topY + subGap * 3), 1.4f)
                topY += bandGap
            }
        }
        BackgroundStyle.MUSIC_STAVE -> {
            var topY = 100f
            while (topY + 64f < size.height - 90f) {
                for (i in 0..4) {
                    val ly = topY + i * 14f
                    drawLine(lineColor, Offset(32f, ly), Offset(size.width - 32f, ly), 1.6f)
                }
                topY += 120f
            }
        }
        BackgroundStyle.COORDINATE_AXES -> {
            val step = 40f
            var x = 0f
            while (x <= size.width) {
                drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
            val cx = size.width / 2f
            val cy = size.height / 2f
            val axisColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF1F2937)
            drawLine(axisColor, Offset(24f, cy), Offset(size.width - 24f, cy), 3f)
            drawLine(axisColor, Offset(cx, 24f), Offset(cx, size.height - 24f), 3f)
        }
        BackgroundStyle.CHESSBOARD -> {
            val cell = 64f
            val cols = (size.width / cell).toInt() + 1
            val rows = (size.height / cell).toInt() + 1
            for (r in 0..rows) {
                for (c in 0..cols) {
                    if ((r + c) % 2 == 0) {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.09f),
                            topLeft = Offset(c * cell, r * cell),
                            size = Size(cell, cell)
                        )
                    }
                }
            }
        }
        else -> Unit
    }
}

fun DrawScope.drawStrokeItem(item: StrokeItem) {
    if (item.points.size < 2) return
    val baseColor = Color(item.colorArgb)

    withTransform({
        translate(item.offsetX, item.offsetY)
        if (item.scale != 1f || item.rotationDeg != 0f) {
            val cx = item.points.map { it.x }.average().toFloat()
            val cy = item.points.map { it.y }.average().toFloat()
            rotate(item.rotationDeg, Offset(cx, cy))
            scale(item.scale, item.scale, Offset(cx, cy))
        }
    }) {
        when (item.penType) {
            PenType.STAMP -> {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = item.colorArgb
                    textSize = (item.strokeWidth * 3.8f).coerceIn(22f, 84f)
                    textAlign = Paint.Align.CENTER
                }
                var distAccum = 999f
                val spacing = (item.strokeWidth * 4.5f).coerceAtLeast(36f)
                for (i in item.points.indices) {
                    val p = item.points[i]
                    if (i > 0) {
                        distAccum += hypot(p.x - item.points[i - 1].x, p.y - item.points[i - 1].y)
                    }
                    if (distAccum >= spacing) {
                        drawContext.canvas.nativeCanvas.drawText(item.stampKind.glyph, p.x, p.y + paint.textSize / 3f, paint)
                        distAccum = 0f
                    }
                }
            }
            PenType.CALLIGRAPHY -> {
                // Soft Pen velocity-sensitive variable stroke thickness (Note3 Page 5 & 58)
                for (i in 1 until item.points.size) {
                    val p0 = item.points[i - 1]
                    val p1 = item.points[i]
                    val w = (item.strokeWidth * ((p0.widthFactor + p1.widthFactor) * 0.5f)).coerceIn(2f, item.strokeWidth * 2.1f)
                    drawLine(
                        color = baseColor,
                        start = Offset(p0.x, p0.y),
                        end = Offset(p1.x, p1.y),
                        strokeWidth = w,
                        cap = StrokeCap.Round
                    )
                }
            }
            PenType.HIGHLIGHTER -> {
                // Angled chisel translucent highlighter
                val path = buildSmoothPath(item.points)
                drawPath(
                    path = path,
                    color = baseColor.copy(alpha = 0.36f),
                    style = Stroke(width = item.strokeWidth * 2.8f, cap = StrokeCap.Square, join = StrokeJoin.Round)
                )
            }
            PenType.PAINTBRUSH -> {
                val path = buildSmoothPath(item.points)
                drawPath(
                    path = path,
                    color = baseColor.copy(alpha = 0.28f),
                    style = Stroke(width = item.strokeWidth * 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = baseColor.copy(alpha = 0.92f),
                    style = Stroke(width = item.strokeWidth * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            else -> {
                val path = buildSmoothPath(item.points)
                drawPath(
                    path = path,
                    color = baseColor,
                    style = Stroke(width = item.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

private fun buildSmoothPath(points: List<com.example.model.StrokePoint>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]
        val midX = (prev.x + curr.x) / 2f
        val midY = (prev.y + curr.y) / 2f
        path.quadraticTo(prev.x, prev.y, midX, midY)
    }
    path.lineTo(points.last().x, points.last().y)
    return path
}

fun DrawScope.drawShape2DItem(item: Shape2DItem) {
    val strokeColor = Color(item.strokeColorArgb)
    val fillColor = item.fillColorArgb?.let { Color(it).copy(alpha = item.fillAlpha) }
    val cx = item.x + item.width / 2f
    val cy = item.y + item.height / 2f

    rotate(item.rotationDeg, Offset(cx, cy)) {
        val strokeStyle = Stroke(
            width = item.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = if (item.shapeType == Shape2DType.DOTTED_LINE) {
                PathEffect.dashPathEffect(floatArrayOf(14f, 12f))
            } else null
        )

        val left = minOf(item.x, item.x + item.width)
        val top = minOf(item.y, item.y + item.height)
        val w = abs(item.width)
        val h = abs(item.height)

        when (item.shapeType) {
            Shape2DType.CIRCLE, Shape2DType.ELLIPSE -> {
                if (item.arcSweepDeg < 359f) {
                    if (item.isSector && fillColor != null) {
                        drawArc(fillColor, item.arcStartDeg, item.arcSweepDeg, useCenter = true, topLeft = Offset(left, top), size = Size(w, h))
                    }
                    drawArc(strokeColor, item.arcStartDeg, item.arcSweepDeg, useCenter = item.isSector, topLeft = Offset(left, top), size = Size(w, h), style = strokeStyle)
                } else {
                    if (fillColor != null) drawOval(fillColor, Offset(left, top), Size(w, h))
                    drawOval(strokeColor, Offset(left, top), Size(w, h), style = strokeStyle)
                }
                if (item.showCenter) {
                    drawCircle(Color(0xFFEF4444), radius = 6f, center = Offset(left + w / 2f, top + h / 2f))
                }
                if (item.showRadius) {
                    drawLine(Color(0xFFEF4444), Offset(left + w / 2f, top + h / 2f), Offset(left + w, top + h / 2f), strokeWidth = 3f)
                }
            }
            Shape2DType.RECTANGLE -> {
                if (fillColor != null) drawRect(fillColor, Offset(left, top), Size(w, h))
                drawRect(strokeColor, Offset(left, top), Size(w, h), style = strokeStyle)
                // Note3 Fig 3-1-6-3-3 & 3-1-6-3-4: Rectangular double-edge-contact circle & circumcircle
                if (item.showIncircle) {
                    val r = min(w, h) / 2f
                    drawCircle(strokeColor.copy(alpha = 0.85f), radius = r, center = Offset(left + w / 2f, top + h / 2f), style = Stroke(3f))
                }
                if (item.showCircumcircle) {
                    val r = hypot(w, h) / 2f
                    drawCircle(strokeColor.copy(alpha = 0.85f), radius = r, center = Offset(left + w / 2f, top + h / 2f), style = Stroke(3f))
                }
            }
            Shape2DType.PARALLELOGRAM -> {
                val skew = w * 0.22f
                val path = Path().apply {
                    moveTo(left + skew, top)
                    lineTo(left + w, top)
                    lineTo(left + w - skew, top + h)
                    lineTo(left, top + h)
                    close()
                }
                if (fillColor != null) drawPath(path, fillColor)
                drawPath(path, strokeColor, style = strokeStyle)
            }
            Shape2DType.TRAPEZOID -> {
                val inset = w * 0.2f
                val path = Path().apply {
                    moveTo(left + inset, top)
                    lineTo(left + w - inset, top)
                    lineTo(left + w, top + h)
                    lineTo(left, top + h)
                    close()
                }
                if (fillColor != null) drawPath(path, fillColor)
                drawPath(path, strokeColor, style = strokeStyle)
            }
            Shape2DType.ISOSCELES_TRIANGLE, Shape2DType.RIGHT_TRIANGLE -> {
                val a = if (item.shapeType == Shape2DType.RIGHT_TRIANGLE) Offset(left, top) else Offset(left + w / 2f, top)
                val b = Offset(left, top + h)
                val c = Offset(left + w, top + h)
                val path = Path().apply {
                    moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); close()
                }
                if (fillColor != null) drawPath(path, fillColor)
                drawPath(path, strokeColor, style = strokeStyle)
                // Note3 Fig 3-1-6-3-5 & 3-1-6-3-6: Triangular Incircle & Circumcircle
                if (item.showIncircle) {
                    val (ic, ir) = GeometryEngine.triangleIncircle(a, b, c)
                    drawCircle(strokeColor.copy(alpha = 0.85f), ir, ic, style = Stroke(3f))
                }
                if (item.showCircumcircle) {
                    val (cc, cr) = GeometryEngine.triangleCircumcircle(a, b, c)
                    drawCircle(strokeColor.copy(alpha = 0.85f), cr, cc, style = Stroke(3f))
                }
            }
            Shape2DType.LINE_SEGMENT, Shape2DType.DOTTED_LINE -> {
                drawLine(strokeColor, Offset(item.x, item.y), Offset(item.x + item.width, item.y + item.height), strokeWidth = item.strokeWidth, cap = StrokeCap.Round, pathEffect = strokeStyle.pathEffect)
            }
            Shape2DType.WAVE_LINE -> {
                val start = Offset(item.x, item.y)
                val end = Offset(item.x + item.width, item.y + item.height)
                val dist = hypot(end.x - start.x, end.y - start.y).coerceAtLeast(1f)
                val angle = atan2(end.y - start.y, end.x - start.x)
                val steps = (dist / 8f).toInt().coerceIn(8, 120)
                val path = Path()
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val along = t * dist
                    val perp = sin(along * 0.14f) * 12f
                    val px = start.x + along * cos(angle) - perp * sin(angle)
                    val py = start.y + along * sin(angle) + perp * cos(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                drawPath(path, strokeColor, style = Stroke(item.strokeWidth, cap = StrokeCap.Round))
            }
            Shape2DType.ARROW -> {
                val start = Offset(item.x, item.y)
                val end = Offset(item.x + item.width, item.y + item.height)
                drawArrowLine(start, end, strokeColor, item.strokeWidth)
            }
            Shape2DType.ANGLE_MARK -> {
                val vertex = Offset(item.x, item.y + item.height)
                val armLen = maxOf(abs(item.width), 90f)
                val end1 = Offset(vertex.x + armLen, vertex.y)
                val rad = Math.toRadians(-item.angleDegrees.toDouble())
                val end2 = Offset(vertex.x + (armLen * cos(rad)).toFloat(), vertex.y + (armLen * sin(rad)).toFloat())
                drawLine(strokeColor, vertex, end1, item.strokeWidth, StrokeCap.Round)
                drawLine(strokeColor, vertex, end2, item.strokeWidth, StrokeCap.Round)
                drawArc(
                    color = strokeColor,
                    startAngle = -item.angleDegrees,
                    sweepAngle = item.angleDegrees,
                    useCenter = false,
                    topLeft = Offset(vertex.x - 36f, vertex.y - 36f),
                    size = Size(72f, 72f),
                    style = Stroke(2.5f)
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = item.strokeColorArgb
                    textSize = 26f
                    typeface = Typeface.DEFAULT_BOLD
                }
                drawContext.canvas.nativeCanvas.drawText("${item.angleDegrees.toInt()}°", vertex.x + 44f, vertex.y - 12f, paint)
            }
            Shape2DType.SMART_RECOGNIZER -> {
                drawRect(strokeColor, Offset(left, top), Size(w, h), style = strokeStyle)
            }
        }
    }
}

fun DrawScope.drawArrowLine(start: Offset, end: Offset, color: Color, width: Float) {
    drawLine(color, start, end, strokeWidth = width, cap = StrokeCap.Round)
    val angle = atan2(end.y - start.y, end.x - start.x)
    val headLen = (width * 3.8f).coerceIn(16f, 34f)
    val p1 = Offset(end.x - headLen * cos(angle - 0.45f), end.y - headLen * sin(angle - 0.45f))
    val p2 = Offset(end.x - headLen * cos(angle + 0.45f), end.y - headLen * sin(angle + 0.45f))
    drawLine(color, end, p1, strokeWidth = width, cap = StrokeCap.Round)
    drawLine(color, end, p2, strokeWidth = width, cap = StrokeCap.Round)
}

fun DrawScope.drawShape3DItem(item: Shape3DItem) {
    val stroke = Color(item.strokeColorArgb)
    val dashed = Stroke(width = item.strokeWidth * 0.75f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
    val solid = Stroke(width = item.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val fill = item.fillColorArgb?.let { Color(it).copy(alpha = 0.22f) }

    val x = item.x
    val y = item.y
    val w = item.width
    val h = item.height

    rotate(item.rotationDeg, Offset(x + w / 2f, y + h / 2f)) {
        when (item.shape3DType) {
            Shape3DType.CUBOID -> {
                val dx = w * 0.24f
                val dy = h * 0.22f
                val fw = w - dx
                val fh = h - dy
                if (fill != null) {
                    drawRect(fill, Offset(x, y + dy), Size(fw, fh))
                }
                // Back hidden edges
                drawLine(stroke, Offset(x + dx, y), Offset(x + dx, y + fh), strokeWidth = item.strokeWidth * 0.7f, pathEffect = dashed.pathEffect)
                drawLine(stroke, Offset(x + dx, y + fh), Offset(x + w, y + fh), strokeWidth = item.strokeWidth * 0.7f, pathEffect = dashed.pathEffect)
                drawLine(stroke, Offset(x, y + h), Offset(x + dx, y + fh), strokeWidth = item.strokeWidth * 0.7f, pathEffect = dashed.pathEffect)
                // Visible edges
                drawRect(stroke, Offset(x, y + dy), Size(fw, fh), style = solid)
                drawLine(stroke, Offset(x, y + dy), Offset(x + dx, y), item.strokeWidth)
                drawLine(stroke, Offset(x + fw, y + dy), Offset(x + w, y), item.strokeWidth)
                drawLine(stroke, Offset(x + fw, y + h), Offset(x + w, y + fh), item.strokeWidth)
                drawLine(stroke, Offset(x + dx, y), Offset(x + w, y), item.strokeWidth)
                drawLine(stroke, Offset(x + w, y), Offset(x + w, y + fh), item.strokeWidth)
            }
            Shape3DType.CYLINDER -> {
                val ry = h * 0.14f
                if (fill != null) {
                    drawRect(fill, Offset(x, y + ry), Size(w, h - ry * 2))
                }
                drawOval(stroke, Offset(x, y), Size(w, ry * 2), style = solid)
                drawLine(stroke, Offset(x, y + ry), Offset(x, y + h - ry), item.strokeWidth)
                drawLine(stroke, Offset(x + w, y + ry), Offset(x + w, y + h - ry), item.strokeWidth)
                drawArc(stroke, 0f, 180f, false, Offset(x, y + h - ry * 2), Size(w, ry * 2), style = solid)
                drawArc(stroke, 180f, 180f, false, Offset(x, y + h - ry * 2), Size(w, ry * 2), style = dashed)
            }
            Shape3DType.CONE -> {
                val ry = h * 0.14f
                val apex = Offset(x + w / 2f, y)
                drawLine(stroke, apex, Offset(x, y + h - ry), item.strokeWidth)
                drawLine(stroke, apex, Offset(x + w, y + h - ry), item.strokeWidth)
                drawArc(stroke, 0f, 180f, false, Offset(x, y + h - ry * 2), Size(w, ry * 2), style = solid)
                drawArc(stroke, 180f, 180f, false, Offset(x, y + h - ry * 2), Size(w, ry * 2), style = dashed)
            }
            Shape3DType.TRUNCATED_CONE -> {
                val topInset = w * 0.22f
                val ry = h * 0.11f
                drawOval(stroke, Offset(x + topInset, y), Size(w - topInset * 2, ry * 2), style = solid)
                drawLine(stroke, Offset(x + topInset, y + ry), Offset(x, y + h - ry), item.strokeWidth)
                drawLine(stroke, Offset(x + w - topInset, y + ry), Offset(x + w, y + h - ry), item.strokeWidth)
                drawArc(stroke, 0f, 180f, false, Offset(x, y + h - ry * 2), Size(w, ry * 2), style = solid)
                drawArc(stroke, 180f, 180f, false, Offset(x, y + h - ry * 2), Size(w, ry * 2), style = dashed)
            }
            Shape3DType.SPHERE -> {
                val d = min(w, h)
                val ox = x + (w - d) / 2f
                val oy = y + (h - d) / 2f
                if (fill != null) drawCircle(fill, d / 2f, Offset(ox + d / 2f, oy + d / 2f))
                drawCircle(stroke, d / 2f, Offset(ox + d / 2f, oy + d / 2f), style = solid)
                drawArc(stroke, 0f, 180f, false, Offset(ox, oy + d * 0.35f), Size(d, d * 0.3f), style = solid)
                drawArc(stroke, 180f, 180f, false, Offset(ox, oy + d * 0.35f), Size(d, d * 0.3f), style = dashed)
            }
            Shape3DType.HEMISPHERE -> {
                val d = min(w, h)
                val ox = x + (w - d) / 2f
                val oy = y + (h - d * 0.7f) / 2f
                drawArc(stroke, 180f, 180f, false, Offset(ox, oy), Size(d, d), style = solid)
                drawOval(stroke, Offset(ox, oy + d * 0.36f), Size(d, d * 0.28f), style = solid)
            }
            Shape3DType.TRIANGULAR_PYRAMID -> {
                val apex = Offset(x + w * 0.5f, y)
                val p1 = Offset(x, y + h * 0.85f)
                val p2 = Offset(x + w * 0.55f, y + h)
                val p3 = Offset(x + w, y + h * 0.72f)
                drawLine(stroke, apex, p1, item.strokeWidth)
                drawLine(stroke, apex, p2, item.strokeWidth)
                drawLine(stroke, apex, p3, item.strokeWidth)
                drawLine(stroke, p1, p2, item.strokeWidth)
                drawLine(stroke, p2, p3, item.strokeWidth)
                drawLine(stroke, p1, p3, item.strokeWidth * 0.75f, pathEffect = dashed.pathEffect)
            }
            Shape3DType.RECTANGULAR_PYRAMID -> {
                val apex = Offset(x + w * 0.5f, y)
                val b1 = Offset(x, y + h)
                val b2 = Offset(x + w * 0.72f, y + h)
                val b3 = Offset(x + w, y + h * 0.74f)
                val b4 = Offset(x + w * 0.28f, y + h * 0.74f)
                drawLine(stroke, apex, b1, item.strokeWidth)
                drawLine(stroke, apex, b2, item.strokeWidth)
                drawLine(stroke, apex, b3, item.strokeWidth)
                drawLine(stroke, apex, b4, item.strokeWidth * 0.75f, pathEffect = dashed.pathEffect)
                drawLine(stroke, b1, b2, item.strokeWidth)
                drawLine(stroke, b2, b3, item.strokeWidth)
                drawLine(stroke, b3, b4, item.strokeWidth * 0.75f, pathEffect = dashed.pathEffect)
                drawLine(stroke, b4, b1, item.strokeWidth * 0.75f, pathEffect = dashed.pathEffect)
            }
            Shape3DType.DIHEDRON -> {
                // Folded dihedral planes (Note3 Fig 3-3-1-1)
                val spineTop = Offset(x + w * 0.48f, y + h * 0.14f)
                val spineBot = Offset(x + w * 0.48f, y + h * 0.86f)
                val pLeftTop = Offset(x, y)
                val pLeftBot = Offset(x, y + h * 0.72f)
                val pRightTop = Offset(x + w, y + h * 0.28f)
                val pRightBot = Offset(x + w, y + h)
                drawLine(stroke, spineTop, spineBot, item.strokeWidth)
                drawLine(stroke, spineTop, pLeftTop, item.strokeWidth)
                drawLine(stroke, pLeftTop, pLeftBot, item.strokeWidth)
                drawLine(stroke, pLeftBot, spineBot, item.strokeWidth)
                drawLine(stroke, spineTop, pRightTop, item.strokeWidth)
                drawLine(stroke, pRightTop, pRightBot, item.strokeWidth)
                drawLine(stroke, pRightBot, spineBot, item.strokeWidth)
            }
        }
    }
}

fun DrawScope.drawTextItem(item: TextItem) {
    withTransform({
        translate(item.x, item.y)
        rotate(item.rotationDeg, Offset.Zero)
        scale(item.scale, item.scale, Offset.Zero)
    }) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = item.colorArgb
            textSize = item.fontSizeSp * 1.45f
            isUnderlineText = item.isUnderline
            typeface = Typeface.create(
                Typeface.SANS_SERIF,
                when {
                    item.isBold && item.isItalic -> Typeface.BOLD_ITALIC
                    item.isBold -> Typeface.BOLD
                    item.isItalic -> Typeface.ITALIC
                    else -> Typeface.NORMAL
                }
            )
        }
        val lines = item.text.split("\n")
        val maxLineW = lines.maxOfOrNull { textPaint.measureText(it) } ?: 60f
        val lineH = textPaint.textSize * 1.28f
        val totalH = lineH * lines.size + 18f

        if (item.isBanner) {
            // Note3 Fig 3-1-1-2-5 General Material chapter ribbon banner
            val bannerW = maxLineW + 64f
            val bannerH = totalH + 18f
            val accent = Color(item.bannerAccentArgb)
            drawRoundRect(accent, Offset(-10f, -6f), Size(bannerW, bannerH), CornerRadius(14f, 14f))
            drawRoundRect(Color.White.copy(alpha = 0.94f), Offset(0f, 2f), Size(bannerW - 18f, bannerH - 16f), CornerRadius(10f, 10f))
            drawRect(accent, Offset(0f, 2f), Size(10f, bannerH - 16f))
        } else if (item.bgColorArgb != null) {
            drawRoundRect(
                Color(item.bgColorArgb).copy(alpha = 0.85f),
                Offset(-10f, -8f),
                Size(maxLineW + 24f, totalH),
                CornerRadius(10f, 10f)
            )
        }

        lines.forEachIndexed { idx, line ->
            val xOff = if (item.isBanner) 22f else 0f
            drawContext.canvas.nativeCanvas.drawText(line, xOff, lineH * (idx + 0.85f), textPaint)
        }
    }
}

fun DrawScope.drawFunctionGraphItem(item: FunctionGraphItem) {
    // Card backdrop
    drawRoundRect(
        color = Color.White.copy(alpha = 0.94f),
        topLeft = Offset(item.x, item.y),
        size = Size(item.width, item.height),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = Color(0xFF94A3B8),
        topLeft = Offset(item.x, item.y),
        size = Size(item.width, item.height),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(2f)
    )

    val pad = 28f
    val left = item.x + pad
    val top = item.y + pad + 12f
    val w = item.width - pad * 2
    val h = item.height - pad * 2 - 12f

    fun mapX(xv: Float): Float {
        val frac = (xv - item.xMin) / (item.xMax - item.xMin)
        return if (item.reverseX) left + (1f - frac) * w else left + frac * w
    }
    fun mapY(yv: Float): Float {
        val frac = (yv - item.yMin) / (item.yMax - item.yMin)
        return if (item.reverseY) top + frac * h else top + (1f - frac) * h
    }

    if (item.showGrid) {
        val gridCol = Color(0xFFE2E8F0)
        val xStep = ((item.xMax - item.xMin) / 10f).coerceAtLeast(1f)
        var gx = item.xMin
        while (gx <= item.xMax + 0.01f) {
            val px = mapX(gx)
            drawLine(gridCol, Offset(px, top), Offset(px, top + h), 1.2f)
            gx += xStep
        }
        val yStep = ((item.yMax - item.yMin) / 8f).coerceAtLeast(1f)
        var gy = item.yMin
        while (gy <= item.yMax + 0.01f) {
            val py = mapY(gy)
            drawLine(gridCol, Offset(left, py), Offset(left + w, py), 1.2f)
            gy += yStep
        }
    }

    val zeroX = mapX(0f).coerceIn(left, left + w)
    val zeroY = mapY(0f).coerceIn(top, top + h)
    drawArrowLine(Offset(left, zeroY), Offset(left + w, zeroY), Color(0xFF1E293B), 2.6f)
    drawArrowLine(Offset(zeroX, top + h), Offset(zeroX, top), Color(0xFF1E293B), 2.6f)

    // Plot curve
    val path = Path()
    var started = false
    val samples = 180
    for (i in 0..samples) {
        val frac = i.toDouble() / samples
        val xv = item.xMin + frac * (item.xMax - item.xMin)
        val yv = GeometryEngine.evaluateFunction(item.expression, xv)
        if (yv != null && yv >= item.yMin - 1 && yv <= item.yMax + 1) {
            val px = mapX(xv.toFloat())
            val py = mapY(yv.toFloat()).coerceIn(top, top + h)
            if (!started) {
                path.moveTo(px, py)
                started = true
            } else {
                path.lineTo(px, py)
            }
        } else {
            started = false
        }
    }
    drawPath(path, Color(item.lineColorArgb), style = Stroke(3.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = item.lineColorArgb
        textSize = 24f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
    drawContext.canvas.nativeCanvas.drawText("y = ${item.expression}", left + 6f, item.y + 28f, labelPaint)
}

fun DrawScope.drawBohrAtomItem(item: BohrAtomItem) {
    // Iconic Note3 Chemistry Atomic Structure Analyzer (Fig 3-3-4-2-1-1)
    val elem = PeriodicTableData.getByNumber(item.atomicNumber)
    val cx = item.x
    val cy = item.y
    val s = item.scale

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 30f * s
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#1E293B")
        textSize = 24f * s
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val nucleusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 22f * s
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    val shellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 20f * s
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    drawContext.canvas.nativeCanvas.drawText(elem.name, cx - 44f * s, cy - 68f * s, titlePaint)
    drawContext.canvas.nativeCanvas.drawText(elem.symbol, cx - 44f * s, cy - 38f * s, subPaint)

    // Nucleus circle (+Z)
    val nucR = 28f * s
    drawCircle(Color(0xFF0F172A), nucR, Offset(cx, cy))
    drawContext.canvas.nativeCanvas.drawText("+${elem.number}", cx, cy + 8f * s, nucleusPaint)

    // Concentric right-side Bohr shell arcs with electron counts (e.g., 2 8 8 2 for Calcium!)
    elem.shells.forEachIndexed { idx, count ->
        val r = (nucR + 24f * s) + idx * 24f * s
        val sweep = (96f - idx * 8f).coerceAtLeast(48f)
        drawArc(
            color = Color(0xFF0F172A),
            startAngle = -sweep / 2f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(3f * s, cap = StrokeCap.Round)
        )
        // Small gap behind shell number
        drawCircle(Color(0xFFE0F2FE).copy(alpha = 0.85f), 11f * s, Offset(cx + r, cy))
        drawContext.canvas.nativeCanvas.drawText(count.toString(), cx + r, cy + 7f * s, shellPaint)
    }
}

fun DrawScope.drawChemApparatusItem(item: ChemApparatusItem) {
    val cx = item.x
    val cy = item.y
    val s = item.scale
    val glassStroke = Color.White.copy(alpha = 0.92f)
    val glassFill = Color.White.copy(alpha = 0.16f)
    val liquid = Color(item.liquidColorArgb).copy(alpha = 0.78f)

    // If pouring, draw a dynamic pouring stream vertically downward from mouth!
    if (item.kind.canPour && !item.isPlugged && abs(item.rotationDeg) > 45f && item.liquidLevel > 0.02f) {
        val mouthX = cx + if (item.rotationDeg > 0) 64f * s else -64f * s
        val mouthY = cy - 10f * s
        drawLine(
            color = liquid,
            start = Offset(mouthX, mouthY),
            end = Offset(mouthX, mouthY + 150f * s),
            strokeWidth = 8f * s,
            cap = StrokeCap.Round
        )
    }

    rotate(item.rotationDeg, Offset(cx, cy)) {
        when (item.kind) {
            ChemApparatusKind.TEST_TUBE -> {
                val w = 36f * s
                val h = 136f * s
                val left = cx - w / 2f
                val top = cy - h / 2f
                drawRoundRect(glassFill, Offset(left, top), Size(w, h), CornerRadius(w / 2f, w / 2f))
                val liqH = (h - 14f * s) * item.liquidLevel.coerceIn(0f, 1f)
                if (liqH > 2f) {
                    drawRoundRect(liquid, Offset(left + 3f, top + h - liqH - 3f), Size(w - 6f, liqH), CornerRadius(w / 2.2f, w / 2.2f))
                }
                drawRoundRect(glassStroke, Offset(left, top), Size(w, h), CornerRadius(w / 2f, w / 2f), style = Stroke(3.2f * s))
                // Rim
                drawLine(glassStroke, Offset(left - 5f, top), Offset(left + w + 5f, top), 4f * s, StrokeCap.Round)
                if (item.isPlugged) {
                    drawRoundRect(Color(0xFF334155), Offset(left + 3f, top - 16f * s), Size(w - 6f, 22f * s), CornerRadius(5f, 5f))
                }
            }
            ChemApparatusKind.BEAKER -> {
                val w = 96f * s
                val h = 114f * s
                val left = cx - w / 2f
                val top = cy - h / 2f
                drawRoundRect(glassFill, Offset(left, top), Size(w, h), CornerRadius(8f, 8f))
                val liqH = (h - 12f * s) * item.liquidLevel.coerceIn(0f, 1f)
                if (liqH > 2f) {
                    drawRect(liquid, Offset(left + 4f, top + h - liqH - 3f), Size(w - 8f, liqH))
                }
                drawRoundRect(glassStroke, Offset(left, top), Size(w, h), CornerRadius(8f, 8f), style = Stroke(3.2f * s))
                // Measurement ticks
                for (i in 1..4) {
                    val ty = top + h - i * (h / 5f)
                    drawLine(glassStroke.copy(alpha = 0.7f), Offset(left + 8f, ty), Offset(left + 26f, ty), 2f)
                }
            }
            ChemApparatusKind.CONICAL_FLASK -> {
                val neckW = 28f * s
                val baseW = 104f * s
                val h = 128f * s
                val top = cy - h / 2f
                val flaskPath = Path().apply {
                    moveTo(cx - neckW / 2f, top)
                    lineTo(cx - neckW / 2f, top + h * 0.34f)
                    lineTo(cx - baseW / 2f, top + h)
                    lineTo(cx + baseW / 2f, top + h)
                    lineTo(cx + neckW / 2f, top + h * 0.34f)
                    lineTo(cx + neckW / 2f, top)
                }
                drawPath(flaskPath, glassFill)
                val liqFrac = item.liquidLevel.coerceIn(0f, 1f)
                if (liqFrac > 0.02f) {
                    val liqTopY = top + h - (h * 0.62f * liqFrac)
                    val ratio = ((liqTopY - (top + h * 0.34f)) / (h * 0.66f)).coerceIn(0f, 1f)
                    val halfW = (neckW / 2f) + (baseW / 2f - neckW / 2f) * ratio
                    val liqPath = Path().apply {
                        moveTo(cx - halfW, liqTopY)
                        lineTo(cx - baseW / 2f + 3f, top + h - 3f)
                        lineTo(cx + baseW / 2f - 3f, top + h - 3f)
                        lineTo(cx + halfW, liqTopY)
                        close()
                    }
                    drawPath(liqPath, liquid)
                }
                drawPath(flaskPath, glassStroke, style = Stroke(3.2f * s, join = StrokeJoin.Round))
                if (item.isPlugged) {
                    drawRoundRect(Color(0xFF334155), Offset(cx - neckW / 2f + 2f, top - 16f * s), Size(neckW - 4f, 20f * s), CornerRadius(4f, 4f))
                }
            }
            ChemApparatusKind.ALCOHOL_LAMP -> {
                val baseW = 86f * s
                val baseH = 68f * s
                val top = cy - baseH / 2f + 16f * s
                drawRoundRect(glassFill, Offset(cx - baseW / 2f, top), Size(baseW, baseH), CornerRadius(18f, 18f))
                drawRect(Color(0xFFBAE6FD).copy(alpha = 0.55f), Offset(cx - baseW / 2f + 5f, top + baseH * 0.45f), Size(baseW - 10f, baseH * 0.5f))
                drawRoundRect(glassStroke, Offset(cx - baseW / 2f, top), Size(baseW, baseH), CornerRadius(18f, 18f), style = Stroke(3f * s))
                // Burner collar & wick
                drawRect(Color(0xFFCBD5E1), Offset(cx - 12f * s, top - 14f * s), Size(24f * s, 14f * s))
                drawLine(Color(0xFFFDE68A), Offset(cx, top - 14f * s), Offset(cx, top - 24f * s), 5f * s)
                if (item.isLit) {
                    // Outer orange flame + inner bright yellow core
                    val outerFlame = Path().apply {
                        moveTo(cx, top - 78f * s)
                        quadraticTo(cx + 22f * s, top - 42f * s, cx, top - 22f * s)
                        quadraticTo(cx - 22f * s, top - 42f * s, cx, top - 78f * s)
                        close()
                    }
                    val innerFlame = Path().apply {
                        moveTo(cx, top - 56f * s)
                        quadraticTo(cx + 11f * s, top - 36f * s, cx, top - 24f * s)
                        quadraticTo(cx - 11f * s, top - 36f * s, cx, top - 56f * s)
                        close()
                    }
                    drawPath(outerFlame, Color(0xFFF97316).copy(alpha = 0.92f))
                    drawPath(innerFlame, Color(0xFFFEF08A))
                }
            }
        }
    }
}

fun DrawScope.drawPhysicsCircuitLayer(
    instruments: List<PhysicsCircuitItem>,
    sim: GeometryEngine.CircuitSimulation
) {
    if (instruments.size >= 2) {
        // Draw clean dark-copper connecting circuit loop wires between instruments
        val wireColor = if (sim.isPowered) Color(0xFFFACC15) else Color(0xFF0F172A)
        for (i in instruments.indices) {
            val a = instruments[i]
            val b = instruments[(i + 1) % instruments.size]
            val path = Path().apply {
                moveTo(a.x, a.y + 34f)
                quadraticTo((a.x + b.x) / 2f, maxOf(a.y, b.y) + 64f, b.x, b.y + 34f)
            }
            drawPath(path, wireColor, style = Stroke(3.8f, cap = StrokeCap.Round))
        }
    }

    instruments.forEach { item ->
        val cx = item.x
        val cy = item.y
        val s = item.scale
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 20f * s
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        when (item.kind) {
            PhysicsInstrumentKind.BATTERY -> {
                drawRoundRect(Color(0xFF334155), Offset(cx - 42f * s, cy - 28f * s), Size(84f * s, 56f * s), CornerRadius(10f, 10f))
                drawRoundRect(Color(0xFF94A3B8), Offset(cx - 42f * s, cy - 28f * s), Size(84f * s, 56f * s), CornerRadius(10f, 10f), style = Stroke(2.5f))
                drawCircle(Color(0xFFEF4444), 6f * s, Offset(cx - 22f * s, cy - 28f * s))
                drawCircle(Color(0xFF0F172A), 6f * s, Offset(cx + 22f * s, cy - 28f * s))
                drawContext.canvas.nativeCanvas.drawText("${item.voltageVolts.toInt()}V DC", cx, cy + 8f * s, labelPaint)
            }
            PhysicsInstrumentKind.SWITCH -> {
                drawRoundRect(Color(0xFF475569), Offset(cx - 38f * s, cy - 16f * s), Size(76f * s, 32f * s), CornerRadius(8f, 8f))
                val pLeft = Offset(cx - 24f * s, cy)
                val pRight = Offset(cx + 24f * s, cy)
                drawCircle(Color(0xFFF59E0B), 6f * s, pLeft)
                drawCircle(Color(0xFFF59E0B), 6f * s, pRight)
                val bladeEnd = if (item.isSwitchClosed) pRight else Offset(cx + 16f * s, cy - 26f * s)
                drawLine(Color(0xFFEF4444), pLeft, bladeEnd, 5f * s, StrokeCap.Round)
                drawContext.canvas.nativeCanvas.drawText(if (item.isSwitchClosed) "CLOSED" else "OPEN", cx, cy + 36f * s, labelPaint)
            }
            PhysicsInstrumentKind.BULB -> {
                if (sim.isPowered && sim.bulbBrightness > 0.05f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFEF08A).copy(alpha = sim.bulbBrightness * 0.9f), Color.Transparent),
                            center = Offset(cx, cy - 8f * s),
                            radius = 74f * s
                        ),
                        radius = 74f * s,
                        center = Offset(cx, cy - 8f * s)
                    )
                }
                val bulbFill = if (sim.isPowered) Color(0xFFFDE047).copy(alpha = 0.4f + sim.bulbBrightness * 0.6f) else Color(0xFFCBD5E1).copy(alpha = 0.3f)
                drawCircle(bulbFill, 28f * s, Offset(cx, cy - 8f * s))
                drawCircle(Color.White, 28f * s, Offset(cx, cy - 8f * s), style = Stroke(2.5f))
                drawRect(Color(0xFF64748B), Offset(cx - 14f * s, cy + 18f * s), Size(28f * s, 16f * s))
                drawContext.canvas.nativeCanvas.drawText(if (sim.isPowered) "ON" else "OFF", cx, cy + 54f * s, labelPaint)
            }
            PhysicsInstrumentKind.AMMETER, PhysicsInstrumentKind.VOLTMETER -> {
                val isAmp = item.kind == PhysicsInstrumentKind.AMMETER
                drawRoundRect(Color(0xFFF1F5F9), Offset(cx - 38f * s, cy - 34f * s), Size(76f * s, 68f * s), CornerRadius(10f, 10f))
                drawRoundRect(Color(0xFF334155), Offset(cx - 38f * s, cy - 34f * s), Size(76f * s, 68f * s), CornerRadius(10f, 10f), style = Stroke(3f))
                drawArc(Color(0xFF1E293B), 200f, 140f, false, Offset(cx - 26f * s, cy - 22f * s), Size(52f * s, 52f * s), style = Stroke(2.5f))
                val frac = if (isAmp) (sim.currentAmps / 3.0f).coerceIn(0f, 1f) else (sim.voltageVolts / 24.0f).coerceIn(0f, 1f)
                val needleDeg = 200.0 + frac * 140.0
                val rad = Math.toRadians(needleDeg)
                val pivot = Offset(cx, cy + 12f * s)
                drawLine(Color(0xFFDC2626), pivot, Offset(pivot.x + (24f * s * cos(rad)).toFloat(), pivot.y + (24f * s * sin(rad)).toFloat()), 3f * s, StrokeCap.Round)
                val meterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#0F172A")
                    textSize = 18f * s
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val readout = if (isAmp) String.format("A %.2f", sim.currentAmps) else String.format("V %.1f", sim.voltageVolts)
                drawContext.canvas.nativeCanvas.drawText(readout, cx, cy + 28f * s, meterPaint)
            }
            PhysicsInstrumentKind.RHEOSTAT -> {
                drawRoundRect(Color(0xFF475569), Offset(cx - 52f * s, cy - 16f * s), Size(104f * s, 32f * s), CornerRadius(6f, 6f))
                // Coil windings
                var wx = cx - 44f * s
                while (wx < cx + 44f * s) {
                    drawLine(Color(0xFFCBD5E1), Offset(wx, cy - 12f * s), Offset(wx, cy + 12f * s), 2f)
                    wx += 6f * s
                }
                // Slider wiper proportional to resistanceOhm (1..30)
                val frac = ((item.resistanceOhm - 1f) / 29f).coerceIn(0f, 1f)
                val wiperX = (cx - 42f * s) + frac * (84f * s)
                drawRect(Color(0xFFF59E0B), Offset(wiperX - 8f * s, cy - 26f * s), Size(16f * s, 14f * s))
                drawContext.canvas.nativeCanvas.drawText("${item.resistanceOhm.toInt()} Ω", cx, cy + 36f * s, labelPaint)
            }
        }
    }
}

fun DrawScope.drawForceVectorLayer(vectors: List<ForceVectorItem>) {
    vectors.forEach { v ->
        drawArrowLine(Offset(v.originX, v.originY), Offset(v.tipX, v.tipY), Color(v.colorArgb), 5.5f)
        val mag = hypot(v.tipX - v.originX, v.tipY - v.originY) / 18f
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = v.colorArgb
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }
        drawContext.canvas.nativeCanvas.drawText("${v.label} (%.1fN)".format(mag), v.tipX + 10f, v.tipY - 8f, p)
    }

    val resultant = GeometryEngine.computeResultantForce(vectors) ?: return
    // Draw dashed parallelogram projection lines for 2 vectors (Note3 Fig 3-3-3-2-2)
    if (vectors.size == 2) {
        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        drawLine(Color.White.copy(alpha = 0.55f), Offset(vectors[0].tipX, vectors[0].tipY), resultant.tip, 2.4f, pathEffect = dash)
        drawLine(Color.White.copy(alpha = 0.55f), Offset(vectors[1].tipX, vectors[1].tipY), resultant.tip, 2.4f, pathEffect = dash)
    }
    drawArrowLine(resultant.origin, resultant.tip, Color(0xFFEF4444), 7f)
    val resPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FCA5A5")
        textSize = 26f
        typeface = Typeface.DEFAULT_BOLD
    }
    drawContext.canvas.nativeCanvas.drawText(
        "R = %.1fN ∠%.0f°".format(resultant.magnitudeNewtons, resultant.angleDeg),
        resultant.tip.x + 12f,
        resultant.tip.y + 6f,
        resPaint
    )
}
