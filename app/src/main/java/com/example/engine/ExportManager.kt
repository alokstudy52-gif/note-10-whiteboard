package com.example.engine

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.BoardSerializer
import com.example.model.BackgroundStyle
import com.example.model.BoardPage
import com.example.model.BohrAtomItem
import com.example.model.ChemApparatusItem
import com.example.model.EnglishLineItem
import com.example.model.ForceVectorItem
import com.example.model.FunctionGraphItem
import com.example.model.PenType
import com.example.model.PhysicsCircuitItem
import com.example.model.Shape2DItem
import com.example.model.Shape2DType
import com.example.model.Shape3DItem
import com.example.model.StrokeItem
import com.example.model.TextItem
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object ExportManager {

    fun renderPageToBitmap(
        page: BoardPage,
        width: Int = 1280,
        height: Int = 720
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawPageToAndroidCanvas(canvas, page, width.toFloat(), height.toFloat())
        return bitmap
    }

    fun exportPageAsPng(context: Context, page: BoardPage, fileName: String): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "${fileName.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.png")
        val bmp = renderPageToBitmap(page)
        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        bmp.recycle()
        return file
    }

    fun exportNotebookAsPdf(context: Context, pages: List<BoardPage>, fileName: String): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "${fileName.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.pdf")
        val pdf = PdfDocument()
        pages.forEachIndexed { index, page ->
            val pageInfo = PdfDocument.PageInfo.Builder(1280, 720, index + 1).create()
            val pdfPage = pdf.startPage(pageInfo)
            drawPageToAndroidCanvas(pdfPage.canvas, page, 1280f, 720f)
            pdf.finishPage(pdfPage)
        }
        FileOutputStream(file).use { out -> pdf.writeTo(out) }
        pdf.close()
        return file
    }

    fun exportNotebookAsN10(context: Context, pages: List<BoardPage>, fileName: String): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "${fileName.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.n10")
        file.writeText(BoardSerializer.serializePages(pages))
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun printNotebook(context: Context, pages: List<BoardPage>, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(pages.size)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pageRanges: Array<out PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdf = PdfDocument()
                pages.forEachIndexed { idx, page ->
                    val pageInfo = PdfDocument.PageInfo.Builder(842, 595, idx + 1).create()
                    val pdfPage = pdf.startPage(pageInfo)
                    drawPageToAndroidCanvas(pdfPage.canvas, page, 842f, 595f)
                    pdf.finishPage(pdfPage)
                }
                try {
                    FileOutputStream(destination.fileDescriptor).use { out -> pdf.writeTo(out) }
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                } finally {
                    pdf.close()
                }
            }
        }
        printManager.print("Note 10 - $jobName", adapter, PrintAttributes.Builder().build())
    }

    private fun drawPageToAndroidCanvas(canvas: Canvas, page: BoardPage, width: Float, height: Float) {
        val bgPaint = Paint().apply { color = page.backgroundColorArgb }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Subtle pattern overlay for PDF/PNG fidelity
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 0, 0, 0)
            strokeWidth = 1.2f
        }
        when (page.backgroundStyle) {
            BackgroundStyle.GRID -> {
                var x = 0f
                while (x < width) { canvas.drawLine(x, 0f, x, height, linePaint); x += 40f }
                var y = 0f
                while (y < height) { canvas.drawLine(0f, y, width, y, linePaint); y += 40f }
            }
            BackgroundStyle.RULED -> {
                var y = 60f
                while (y < height) { canvas.drawLine(0f, y, width, y, linePaint); y += 44f }
            }
            BackgroundStyle.ENGLISH_FOUR_LINE -> {
                var y = 60f
                while (y < height - 60f) {
                    canvas.drawLine(40f, y, width - 40f, y, linePaint)
                    canvas.drawLine(40f, y + 22f, width - 40f, y + 22f, linePaint)
                    canvas.drawLine(40f, y + 44f, width - 40f, y + 44f, linePaint)
                    canvas.drawLine(40f, y + 66f, width - 40f, y + 66f, linePaint)
                    y += 100f
                }
            }
            else -> Unit
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        page.items.sortedBy { it.zIndex }.forEach { item ->
            when (item) {
                is StrokeItem -> {
                    if (item.points.size >= 2) {
                        strokePaint.color = item.colorArgb
                        strokePaint.alpha = if (item.penType == PenType.HIGHLIGHTER) 115 else 255
                        strokePaint.strokeWidth = item.strokeWidth
                        val path = Path()
                        path.moveTo(item.points[0].x + item.offsetX, item.points[0].y + item.offsetY)
                        for (i in 1 until item.points.size) {
                            path.lineTo(item.points[i].x + item.offsetX, item.points[i].y + item.offsetY)
                        }
                        canvas.drawPath(path, strokePaint)
                    }
                }
                is Shape2DItem -> {
                    strokePaint.color = item.strokeColorArgb
                    strokePaint.alpha = 255
                    strokePaint.strokeWidth = item.strokeWidth
                    strokePaint.pathEffect = if (item.shapeType == Shape2DType.DOTTED_LINE) {
                        DashPathEffect(floatArrayOf(12f, 10f), 0f)
                    } else null
                    val rect = RectF(item.x, item.y, item.x + item.width, item.y + item.height)
                    if (item.fillColorArgb != null && !item.shapeType.isLineLike) {
                        fillPaint.color = item.fillColorArgb
                        fillPaint.alpha = (item.fillAlpha * 255).toInt()
                        if (item.shapeType == Shape2DType.CIRCLE || item.shapeType == Shape2DType.ELLIPSE) {
                            canvas.drawOval(rect, fillPaint)
                        } else {
                            canvas.drawRect(rect, fillPaint)
                        }
                    }
                    when (item.shapeType) {
                        Shape2DType.CIRCLE, Shape2DType.ELLIPSE -> canvas.drawOval(rect, strokePaint)
                        Shape2DType.LINE_SEGMENT, Shape2DType.DOTTED_LINE, Shape2DType.ARROW -> {
                            canvas.drawLine(item.x, item.y, item.x + item.width, item.y + item.height, strokePaint)
                        }
                        else -> canvas.drawRect(rect, strokePaint)
                    }
                    strokePaint.pathEffect = null
                }
                is Shape3DItem -> {
                    strokePaint.color = item.strokeColorArgb
                    strokePaint.alpha = 255
                    strokePaint.strokeWidth = item.strokeWidth
                    canvas.drawRect(item.x, item.y, item.x + item.width, item.y + item.height, strokePaint)
                }
                is TextItem -> {
                    textPaint.color = item.colorArgb
                    textPaint.textSize = item.fontSizeSp * 1.3f
                    textPaint.typeface = Typeface.create(
                        Typeface.SANS_SERIF,
                        when {
                            item.isBold && item.isItalic -> Typeface.BOLD_ITALIC
                            item.isBold -> Typeface.BOLD
                            item.isItalic -> Typeface.ITALIC
                            else -> Typeface.NORMAL
                        }
                    )
                    canvas.drawText(item.text, item.x, item.y + textPaint.textSize, textPaint)
                }
                is EnglishLineItem -> {
                    textPaint.color = item.colorArgb
                    textPaint.textSize = 36f
                    canvas.drawText(item.text, 64f, 104f + item.rowIndex * 100f, textPaint)
                }
                is BohrAtomItem -> {
                    val elem = PeriodicTableData.getByNumber(item.atomicNumber)
                    fillPaint.color = Color.BLACK
                    canvas.drawCircle(item.x, item.y, 26f, fillPaint)
                    textPaint.color = Color.WHITE
                    textPaint.textSize = 18f
                    canvas.drawText("+${elem.number}", item.x - 16f, item.y + 6f, textPaint)
                    textPaint.color = Color.BLACK
                    textPaint.textSize = 24f
                    canvas.drawText("${elem.name} (${elem.symbol})", item.x - 30f, item.y - 44f, textPaint)
                }
                is FunctionGraphItem -> {
                    strokePaint.color = Color.DKGRAY
                    strokePaint.strokeWidth = 2f
                    val cx = item.x + item.width / 2f
                    val cy = item.y + item.height / 2f
                    canvas.drawLine(item.x, cy, item.x + item.width, cy, strokePaint)
                    canvas.drawLine(cx, item.y, cx, item.y + item.height, strokePaint)
                    val path = Path()
                    var started = false
                    for (i in 0..160) {
                        val frac = i / 160.0
                        val xVal = item.xMin + frac * (item.xMax - item.xMin)
                        val yVal = GeometryEngine.evaluateFunction(item.expression, xVal)
                        if (yVal != null && yVal in item.yMin.toDouble()..item.yMax.toDouble()) {
                            val px = item.x + (frac * item.width).toFloat()
                            val py = item.y + ((item.yMax - yVal) / (item.yMax - item.yMin) * item.height).toFloat()
                            if (!started) { path.moveTo(px, py); started = true } else path.lineTo(px, py)
                        } else {
                            started = false
                        }
                    }
                    strokePaint.color = item.lineColorArgb
                    strokePaint.strokeWidth = 3.5f
                    canvas.drawPath(path, strokePaint)
                }
                is ChemApparatusItem -> {
                    strokePaint.color = Color.WHITE
                    strokePaint.strokeWidth = 3f
                    canvas.drawRect(item.x - 24f, item.y - 60f, item.x + 24f, item.y + 60f, strokePaint)
                }
                is PhysicsCircuitItem -> {
                    strokePaint.color = Color.WHITE
                    strokePaint.strokeWidth = 3f
                    canvas.drawCircle(item.x, item.y, 34f, strokePaint)
                    textPaint.color = Color.WHITE
                    textPaint.textSize = 18f
                    canvas.drawText(item.kind.label, item.x - 30f, item.y + 52f, textPaint)
                }
                is ForceVectorItem -> {
                    strokePaint.color = item.colorArgb
                    strokePaint.strokeWidth = 4f
                    canvas.drawLine(item.originX, item.originY, item.tipX, item.tipY, strokePaint)
                }
                else -> Unit
            }
        }
    }
}
