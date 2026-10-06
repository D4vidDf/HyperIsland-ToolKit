package com.d4viddf.hyperisland_kit.demo.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.d4viddf.hyperisland_kit.demo.ui.screens.inspector.InspectorGroup
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private const val TAG = "ReportExporter"

    fun exportGroupMarkdownReport(context: Context, group: InspectorGroup) {
        try {
            val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "HyperIsland_Report_${group.packageName}_ID${group.id}_$dateStr.md"

            val mdContent = buildString {
                appendLine("# Xiaomi HyperIsland Notification Stream Report")
                appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                appendLine()
                appendLine("## App & System Info")
                appendLine("- **App Name:** ${group.appName}")
                appendLine("- **Package Name:** `${group.packageName}`")
                appendLine("- **Notification ID:** `${group.id}`")
                appendLine("- **Total Sequential Updates:** ${group.updateCount}")
                appendLine("- **Device:** `${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})`")
                appendLine("- **Android Version:** Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine()

                group.updates.reversed().forEachIndexed { index, item ->
                    val notif = item.notification
                    appendLine("---")
                    appendLine("## Step ${index + 1} of ${group.updateCount} (${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(notif.postTime))})")
                    appendLine("- **Title:** ${notif.title}")
                    appendLine("- **Content:** ${notif.content}")
                    appendLine("- **Template Style:** `${notif.templateStyle ?: "Standard"}`")
                    appendLine("- **Ongoing:** `${notif.isOngoing}`")
                    appendLine("- **Content Intent:** `${notif.contentIntent ?: "None"}`")
                    appendLine("- **Unique Step Key:** `${notif.key}`")
                    appendLine()

                    if (notif.actions.isNotEmpty()) {
                        appendLine("### Actions (${notif.actions.size})")
                        notif.actions.forEachIndexed { i, action ->
                            appendLine("${i + 1}. **${action.title}** - Intent: `${action.intentDescription ?: "None"}`")
                        }
                        appendLine()
                    }

                    if (item.images.isNotEmpty()) {
                        appendLine("### Visual Assets (${item.images.size})")
                        item.images.forEach { (key, _) ->
                            val meta = notif.resourceMeta[key]
                            appendLine("- **$key:** Type `${meta?.type ?: "UNKNOWN"}` (${meta?.width ?: 0}x${meta?.height ?: 0}, ${meta?.fileSize ?: "?"})")
                        }
                        appendLine()
                    }

                    if (notif.styleExtras.isNotEmpty()) {
                        appendLine("### Style Extras")
                        notif.styleExtras.forEach { (k, v) ->
                            appendLine("- **$k:** `$v`")
                        }
                        appendLine()
                    }

                    if (notif.hyperJson != null) {
                        appendLine("### Xiaomi HyperIsland Payload (JSON)")
                        appendLine("```json")
                        appendLine(notif.hyperJson)
                        appendLine("```")
                        appendLine()
                    }
                }
            }

            val exportDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val mdFile = File(exportDir, fileName)
            mdFile.writeText(mdContent)

            shareFile(context, mdFile, "text/markdown", "Share Markdown Stream Report")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to export Markdown stream report", e)
        }
    }

    fun exportGroupPdfReport(context: Context, group: InspectorGroup) {
        try {
            val pdfDoc = PdfDocument()
            val writer = PdfReportWriter(pdfDoc)

            val primaryColor = Color.rgb(91, 73, 216)
            val textColor = Color.BLACK
            val labelColor = Color.rgb(80, 80, 80)

            val paintTitle = Paint().apply {
                isAntiAlias = true
                color = primaryColor
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val paintSub = Paint().apply {
                isAntiAlias = true
                color = labelColor
                textSize = 10f
                typeface = Typeface.DEFAULT
            }

            val paintHeader = Paint().apply {
                isAntiAlias = true
                color = primaryColor
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val paintBodyBold = Paint().apply {
                isAntiAlias = true
                color = textColor
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val paintBody = Paint().apply {
                isAntiAlias = true
                color = textColor
                textSize = 10f
                typeface = Typeface.DEFAULT
            }

            val paintCode = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(220, 220, 235)
                textSize = 8.5f
                typeface = Typeface.MONOSPACE
            }

            val paintBgBox = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(240, 237, 255)
            }

            val paintJsonBg = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(30, 30, 42)
            }

            // Header Section
            writer.ensureSpace(80f)
            writer.canvas.drawText("Xiaomi HyperIsland Notification Report", writer.startX, writer.y, paintTitle)
            writer.y += 18f
            writer.canvas.drawText("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}", writer.startX, writer.y, paintSub)
            writer.y += 20f

            // Info Box
            writer.canvas.drawRoundRect(writer.startX, writer.y, writer.startX + writer.contentWidth, writer.y + 60f, 8f, 8f, paintBgBox)
            writer.canvas.drawText("App: ${group.appName} (${group.packageName})", writer.startX + 12f, writer.y + 22f, paintBodyBold)
            writer.canvas.drawText("ID: ${group.id} | Total Stream Updates: ${group.updateCount} | Device: ${Build.MANUFACTURER} ${Build.MODEL}", writer.startX + 12f, writer.y + 44f, paintBody)
            writer.y += 75f

            // Sequential Updates
            group.updates.reversed().forEachIndexed { index, item ->
                val notif = item.notification

                writer.ensureSpace(50f)
                writer.canvas.drawText("Step ${index + 1} of ${group.updateCount} — ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(notif.postTime))}", writer.startX, writer.y, paintHeader)
                writer.y += 18f

                // Title & Content Lines
                writer.drawWrappedText("Title: ${notif.title.ifEmpty { "(No Title)" }}", paintBodyBold, lineHeight = 14f)
                if (notif.content.isNotEmpty()) {
                    writer.drawWrappedText("Content: ${notif.content}", paintBody, lineHeight = 14f)
                }
                writer.drawWrappedText("Template Style: ${notif.templateStyle ?: "Standard"} | Ongoing: ${notif.isOngoing}", paintSub, lineHeight = 14f)
                writer.y += 6f

                // Actions
                if (notif.actions.isNotEmpty()) {
                    writer.ensureSpace(30f)
                    writer.canvas.drawText("Actions (${notif.actions.size}):", writer.startX, writer.y, paintBodyBold)
                    writer.y += 14f
                    notif.actions.forEachIndexed { aIdx, act ->
                        writer.drawWrappedText("  ${aIdx + 1}. ${act.title} -> ${act.intentDescription ?: "None"}", paintBody, lineHeight = 13f)
                    }
                    writer.y += 6f
                }

                // Visual Assets Summary
                if (item.images.isNotEmpty()) {
                    writer.ensureSpace(30f)
                    writer.canvas.drawText("Visual Assets (${item.images.size}):", writer.startX, writer.y, paintBodyBold)
                    writer.y += 14f
                    item.images.forEach { (key, _) ->
                        val meta = notif.resourceMeta[key]
                        val metaStr = "Type: ${meta?.type ?: "BITMAP"} (${meta?.width ?: 0}x${meta?.height ?: 0}, ${meta?.fileSize ?: "?"})"
                        writer.drawWrappedText("  • $key: $metaStr", paintSub, lineHeight = 13f)
                    }
                    writer.y += 6f
                }

                // HyperIsland JSON Payload
                if (notif.hyperJson != null) {
                    writer.ensureSpace(30f)
                    writer.canvas.drawText("Xiaomi HyperIsland JSON Payload:", writer.startX, writer.y, paintBodyBold)
                    writer.y += 16f

                    val jsonLines = notif.hyperJson.lines()

                    // Draw Full JSON payload across pages as needed
                    for (line in jsonLines) {
                        writer.ensureSpace(13f)

                        // Draw dark background strip for code block
                        writer.canvas.drawRect(writer.startX, writer.y - 10f, writer.startX + writer.contentWidth, writer.y + 4f, paintJsonBg)

                        // Wrap long lines if needed
                        val chunkLength = 88
                        if (line.length > chunkLength) {
                            val chunks = line.chunked(chunkLength)
                            for (chunk in chunks) {
                                writer.ensureSpace(13f)
                                writer.canvas.drawRect(writer.startX, writer.y - 10f, writer.startX + writer.contentWidth, writer.y + 4f, paintJsonBg)
                                writer.canvas.drawText(chunk, writer.startX + 8f, writer.y, paintCode)
                                writer.y += 13f
                            }
                        } else {
                            writer.canvas.drawText(line, writer.startX + 8f, writer.y, paintCode)
                            writer.y += 13f
                        }
                    }
                    writer.y += 12f
                } else {
                    writer.y += 10f
                }

                writer.ensureSpace(15f)
                writer.canvas.drawLine(writer.startX, writer.y, writer.startX + writer.contentWidth, writer.y, Paint().apply { color = Color.LTGRAY; strokeWidth = 0.8f })
                writer.y += 15f
            }

            writer.finish()

            val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "HyperIsland_Report_${group.packageName}_ID${group.id}_$dateStr.pdf"
            val exportDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val pdfFile = File(exportDir, fileName)

            FileOutputStream(pdfFile).use { out -> pdfDoc.writeTo(out) }
            pdfDoc.close()

            shareFile(context, pdfFile, "application/pdf", "Share PDF Stream Report")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to export PDF stream report", e)
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

private class PdfReportWriter(private val pdfDoc: PdfDocument) {
    val pageWidth = 595
    val pageHeight = 842
    val startX = 30f
    val contentWidth = 535f
    private var pageNumber = 1

    private var currentPage: PdfDocument.Page = pdfDoc.startPage(
        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    )
    val canvas: Canvas get() = currentPage.canvas
    var y = 40f

    fun ensureSpace(neededHeight: Float) {
        if (y + neededHeight > pageHeight - 40f) {
            pdfDoc.finishPage(currentPage)
            pageNumber++
            currentPage = pdfDoc.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            y = 40f
        }
    }

    fun drawWrappedText(text: String, paint: Paint, maxCharsPerLine: Int = 80, lineHeight: Float = 14f) {
        if (text.length <= maxCharsPerLine) {
            ensureSpace(lineHeight)
            canvas.drawText(text, startX, y, paint)
            y += lineHeight
        } else {
            val chunks = text.chunked(maxCharsPerLine)
            for (chunk in chunks) {
                ensureSpace(lineHeight)
                canvas.drawText(chunk, startX, y, paint)
                y += lineHeight
            }
        }
    }

    fun finish() {
        pdfDoc.finishPage(currentPage)
    }
}
