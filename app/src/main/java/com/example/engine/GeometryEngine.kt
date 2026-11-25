package com.example.engine

import androidx.compose.ui.geometry.Offset
import com.example.model.ForceVectorItem
import com.example.model.PhysicsCircuitItem
import com.example.model.PhysicsInstrumentKind
import com.example.model.Shape2DItem
import com.example.model.Shape2DType
import com.example.model.StrokePoint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object GeometryEngine {

    /**
     * Free on-device Smart Shape Recognizer (Note3 Section 3.1.9.1)
     * Classifies hand-drawn strokes into Circle, Ellipse, Triangle, Rectangle, Arrow, or Line Segment.
     */
    fun recognizeShape(
        points: List<StrokePoint>,
        strokeColorArgb: Int,
        fillColorArgb: Int?,
        strokeWidth: Float
    ): Shape2DItem? {
        if (points.size < 4) return null

        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        var pathLength = 0f

        for (i in points.indices) {
            val p = points[i]
            if (p.x < minX) minX = p.x
            if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y
            if (p.y > maxY) maxY = p.y
            if (i > 0) {
                pathLength += hypot(p.x - points[i - 1].x, p.y - points[i - 1].y)
            }
        }

        val w = maxX - minX
        val h = maxY - minY
        val diag = hypot(w, h)
        if (diag < 28f || pathLength < 30f) return null

        val first = points.first()
        val last = points.last()
        val endToEnd = hypot(last.x - first.x, last.y - first.y)

        // 1. Check Line Segment vs Arrow (open path with high straightness)
        if (endToEnd / pathLength > 0.84f) {
            return Shape2DItem(
                x = first.x,
                y = first.y,
                width = last.x - first.x,
                height = last.y - first.y,
                shapeType = Shape2DType.LINE_SEGMENT,
                strokeColorArgb = strokeColorArgb,
                fillColorArgb = null,
                strokeWidth = strokeWidth
            )
        }

        val isClosed = endToEnd < diag * 0.38f

        // Check Arrow: open path that goes mostly straight then hooks back near the tip
        if (!isClosed) {
            val simplifiedOpen = simplifyRdp(points, diag * 0.08f)
            if (simplifiedOpen.size in 3..5) {
                val baseLen = hypot(
                    simplifiedOpen[1].x - simplifiedOpen[0].x,
                    simplifiedOpen[1].y - simplifiedOpen[0].y
                )
                if (baseLen > diag * 0.55f) {
                    return Shape2DItem(
                        x = simplifiedOpen[0].x,
                        y = simplifiedOpen[0].y,
                        width = simplifiedOpen[1].x - simplifiedOpen[0].x,
                        height = simplifiedOpen[1].y - simplifiedOpen[0].y,
                        shapeType = Shape2DType.ARROW,
                        strokeColorArgb = strokeColorArgb,
                        fillColorArgb = null,
                        strokeWidth = strokeWidth
                    )
                }
            }
            // Fallback open straight-ish line
            if (endToEnd / pathLength > 0.65f) {
                return Shape2DItem(
                    x = first.x,
                    y = first.y,
                    width = last.x - first.x,
                    height = last.y - first.y,
                    shapeType = Shape2DType.LINE_SEGMENT,
                    strokeColorArgb = strokeColorArgb,
                    fillColorArgb = null,
                    strokeWidth = strokeWidth
                )
            }
        }

        // 2. Closed shape analysis: Circle/Ellipse vs Triangle vs Rectangle
        val cx = (minX + maxX) / 2f
        val cy = (minY + maxY) / 2f
        val rx = max(w / 2f, 1f)
        val ry = max(h / 2f, 1f)

        // Normalized radial error from ellipse (x-cx)^2/rx^2 + (y-cy)^2/ry^2 == 1
        var ellipseErrSum = 0f
        for (p in points) {
            val nx = (p.x - cx) / rx
            val ny = (p.y - cy) / ry
            val r = hypot(nx, ny)
            ellipseErrSum += abs(r - 1f)
        }
        val avgEllipseErr = ellipseErrSum / points.size

        // Check polygon corners via RDP
        val simplified = simplifyRdp(points, diag * 0.09f)
        val distinctCorners = if (simplified.size > 1 &&
            hypot(simplified.first().x - simplified.last().x, simplified.first().y - simplified.last().y) < diag * 0.25f
        ) {
            simplified.size - 1
        } else {
            simplified.size
        }

        val aspect = min(w, h) / max(w, h)

        val recognizedType = when {
            avgEllipseErr < 0.165f && distinctCorners >= 5 -> {
                if (aspect > 0.78f) Shape2DType.CIRCLE else Shape2DType.ELLIPSE
            }
            distinctCorners == 3 -> Shape2DType.ISOSCELES_TRIANGLE
            distinctCorners == 4 -> Shape2DType.RECTANGLE
            avgEllipseErr < 0.22f -> {
                if (aspect > 0.78f) Shape2DType.CIRCLE else Shape2DType.ELLIPSE
            }
            distinctCorners <= 3 -> Shape2DType.ISOSCELES_TRIANGLE
            else -> Shape2DType.RECTANGLE
        }

        return if (recognizedType == Shape2DType.CIRCLE) {
            val size = (w + h) / 2f
            Shape2DItem(
                x = cx - size / 2f,
                y = cy - size / 2f,
                width = size,
                height = size,
                shapeType = Shape2DType.CIRCLE,
                strokeColorArgb = strokeColorArgb,
                fillColorArgb = fillColorArgb,
                strokeWidth = strokeWidth
            )
        } else {
            Shape2DItem(
                x = minX,
                y = minY,
                width = w,
                height = h,
                shapeType = recognizedType,
                strokeColorArgb = strokeColorArgb,
                fillColorArgb = fillColorArgb,
                strokeWidth = strokeWidth
            )
        }
    }

    private fun simplifyRdp(points: List<StrokePoint>, epsilon: Float): List<StrokePoint> {
        if (points.size < 3) return points
        var maxDist = 0f
        var index = 0
        val start = points.first()
        val end = points.last()

        for (i in 1 until points.lastIndex) {
            val d = perpendicularDistance(points[i], start, end)
            if (d > maxDist) {
                maxDist = d
                index = i
            }
        }

        return if (maxDist > epsilon) {
            val left = simplifyRdp(points.subList(0, index + 1), epsilon)
            val right = simplifyRdp(points.subList(index, points.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(start, end)
        }
    }

    private fun perpendicularDistance(p: StrokePoint, a: StrokePoint, b: StrokePoint): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val denom = hypot(dx, dy)
        if (denom < 0.001f) return hypot(p.x - a.x, p.y - a.y)
        return abs(dy * p.x - dx * p.y + b.x * a.y - b.y * a.x) / denom
    }

    /**
     * Computes exact Euclidean Incircle (center, radius) for a triangle (A, B, C)
     */
    fun triangleIncircle(a: Offset, b: Offset, c: Offset): Pair<Offset, Float> {
        val sideA = hypot(b.x - c.x, b.y - c.y)
        val sideB = hypot(a.x - c.x, a.y - c.y)
        val sideC = hypot(a.x - b.x, a.y - b.y)
        val perimeter = sideA + sideB + sideC
        if (perimeter < 0.001f) return a to 0f
        val ix = (sideA * a.x + sideB * b.x + sideC * c.x) / perimeter
        val iy = (sideA * a.y + sideB * b.y + sideC * c.y) / perimeter
        val s = perimeter / 2f
        val area = sqrt(max(0f, s * (s - sideA) * (s - sideB) * (s - sideC)))
        val r = area / s
        return Offset(ix, iy) to r
    }

    /**
     * Computes exact Euclidean Circumcircle (center, radius) for a triangle (A, B, C)
     */
    fun triangleCircumcircle(a: Offset, b: Offset, c: Offset): Pair<Offset, Float> {
        val d = 2f * (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y))
        if (abs(d) < 0.001f) return a to 0f
        val aSq = a.x * a.x + a.y * a.y
        val bSq = b.x * b.x + b.y * b.y
        val cSq = c.x * c.x + c.y * c.y
        val ux = (aSq * (b.y - c.y) + bSq * (c.y - a.y) + cSq * (a.y - b.y)) / d
        val uy = (aSq * (c.x - b.x) + bSq * (a.x - c.x) + cSq * (b.x - a.x)) / d
        val center = Offset(ux, uy)
        val r = hypot(center.x - a.x, center.y - a.y)
        return center to r
    }

    /**
     * Evaluates a mathematical expression f(x) safely on-device (100% free).
     * Supports: +, -, *, /, ^, (), x, pi, e, sin, cos, tan, cot, sec, csc, asin, acos, atan, sqrt, abs, ln, log, exp
     */
    fun evaluateFunction(rawExpr: String, xValue: Double): Double? {
        val cleaned = rawExpr
            .lowercase()
            .replace("y=", "")
            .replace("f(x)=", "")
            .replace("π", "pi")
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")
        if (cleaned.isBlank()) return null
        return try {
            val parser = MathExprParser(cleaned, xValue)
            val result = parser.parseExpression()
            if (result.isNaN() || result.isInfinite()) null else result
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Live Ohm's Law Solver for Physics Electricity Lab (Note3 Section 3.3.3.2.1)
     */
    data class CircuitSimulation(
        val isPowered: Boolean,
        val voltageVolts: Float,
        val totalResistanceOhm: Float,
        val currentAmps: Float,
        val bulbBrightness: Float
    )

    fun solvePhysicsCircuit(items: List<PhysicsCircuitItem>): CircuitSimulation {
        val batteries = items.filter { it.kind == PhysicsInstrumentKind.BATTERY }
        val switches = items.filter { it.kind == PhysicsInstrumentKind.SWITCH }
        val bulbs = items.filter { it.kind == PhysicsInstrumentKind.BULB }
        val rheostats = items.filter { it.kind == PhysicsInstrumentKind.RHEOSTAT }

        val hasPower = batteries.isNotEmpty()
        val allSwitchesClosed = switches.isEmpty() || switches.all { it.isSwitchClosed }
        val totalVoltage = batteries.sumOf { it.voltageVolts.toDouble() }.toFloat()
        val bulbResistance = bulbs.size * 4f
        val rheostatResistance = rheostats.sumOf { it.resistanceOhm.toDouble() }.toFloat()
        val totalR = max(1f, bulbResistance + rheostatResistance)

        val powered = hasPower && allSwitchesClosed
        val current = if (powered) totalVoltage / totalR else 0f
        val brightness = if (powered) (current / 2.2f).coerceIn(0.15f, 1f) else 0f

        return CircuitSimulation(
            isPowered = powered,
            voltageVolts = if (powered) totalVoltage else 0f,
            totalResistanceOhm = totalR,
            currentAmps = current,
            bulbBrightness = brightness
        )
    }

    /**
     * Live Mechanics Resultant Force Composition (Note3 Section 3.3.3.2.2)
     */
    data class ResultantForce(
        val origin: Offset,
        val tip: Offset,
        val magnitudeNewtons: Float,
        val angleDeg: Float
    )

    fun computeResultantForce(vectors: List<ForceVectorItem>): ResultantForce? {
        if (vectors.size < 2) return null
        val origin = Offset(vectors.first().originX, vectors.first().originY)
        var sumDx = 0f
        var sumDy = 0f
        for (v in vectors) {
            sumDx += (v.tipX - v.originX)
            sumDy += (v.tipY - v.originY)
        }
        val tip = Offset(origin.x + sumDx, origin.y + sumDy)
        val mag = hypot(sumDx, sumDy) / 18f // 18px = 1 Newton visual scale
        val angle = ((-atan2(sumDy, sumDx) * 180.0 / PI) + 360.0) % 360.0
        return ResultantForce(origin, tip, mag, angle.toFloat())
    }
}

private class MathExprParser(
    private val input: String,
    private val xVal: Double
) {
    private var pos = 0

    fun parseExpression(): Double {
        var value = parseTerm()
        while (pos < input.length) {
            when (input[pos]) {
                '+' -> {
                    pos++
                    value += parseTerm()
                }
                '-' -> {
                    pos++
                    value -= parseTerm()
                }
                else -> break
            }
        }
        return value
    }

    private fun parseTerm(): Double {
        var value = parsePower()
        while (pos < input.length) {
            val ch = input[pos]
            when {
                ch == '*' -> {
                    pos++
                    value *= parsePower()
                }
                ch == '/' -> {
                    pos++
                    value /= parsePower()
                }
                // Implicit multiplication like 2x, 2sin(x), (x+1)(x-1)
                ch == 'x' || ch == '(' || ch.isLetter() -> {
                    value *= parsePower()
                }
                else -> break
            }
        }
        return value
    }

    private fun parsePower(): Double {
        val base = parseUnary()
        return if (pos < input.length && input[pos] == '^') {
            pos++
            base.pow(parsePower())
        } else {
            base
        }
    }

    private fun parseUnary(): Double {
        if (pos < input.length) {
            if (input[pos] == '+') {
                pos++
                return parseUnary()
            }
            if (input[pos] == '-') {
                pos++
                return -parseUnary()
            }
        }
        return parsePrimary()
    }

    private fun parsePrimary(): Double {
        if (pos >= input.length) return Double.NaN
        val ch = input[pos]

        if (ch == '(') {
            pos++
            val inner = parseExpression()
            if (pos < input.length && input[pos] == ')') pos++
            return inner
        }

        if (ch.isDigit() || ch == '.') {
            val start = pos
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
            return input.substring(start, pos).toDouble()
        }

        if (ch.isLetter()) {
            val start = pos
            while (pos < input.length && input[pos].isLetter()) pos++
            val token = input.substring(start, pos)
            return when (token) {
                "x" -> xVal
                "pi" -> PI
                "e" -> Math.E
                else -> {
                    val arg = if (pos < input.length && input[pos] == '(') {
                        pos++
                        val v = parseExpression()
                        if (pos < input.length && input[pos] == ')') pos++
                        v
                    } else {
                        parseUnary()
                    }
                    when (token) {
                        "sin" -> sin(arg)
                        "cos" -> cos(arg)
                        "tan" -> tan(arg)
                        "cot" -> 1.0 / tan(arg)
                        "sec" -> 1.0 / cos(arg)
                        "csc" -> 1.0 / sin(arg)
                        "asin", "arcsin" -> asin(arg)
                        "acos", "arccos" -> acos(arg)
                        "atan", "arctan" -> atan(arg)
                        "sqrt" -> sqrt(arg)
                        "abs" -> abs(arg)
                        "ln" -> ln(arg)
                        "log" -> log10(arg)
                        "exp" -> exp(arg)
                        else -> Double.NaN
                    }
                }
            }
        }
        return Double.NaN
    }
}
