package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ChecklistItem
import com.example.data.model.NoteEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    PDF("PDF Document (.pdf)", "pdf", "application/pdf"),
    TXT("Plain Text (.txt)", "txt", "text/plain"),
    MARKDOWN("Markdown (.md)", "md", "text/markdown"),
    CSV("Spreadsheet / CSV (.csv)", "csv", "text/csv"),
    JSON("Structured Data (.json)", "json", "application/json")
}

object NotesExporter {

    fun exportAndShare(
        context: Context,
        note: NoteEntity,
        format: ExportFormat,
        checklists: List<ChecklistItem> = emptyList()
    ) {
        try {
            val cacheDir = File(context.cacheDir, "exported_notes")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val sanitizedTitle = note.title.ifBlank { "Untitled_Note" }
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "${sanitizedTitle}_${System.currentTimeMillis()}.${format.extension}"
            val outputFile = File(cacheDir, fileName)

            when (format) {
                ExportFormat.PDF -> generatePdf(outputFile, note, checklists)
                ExportFormat.TXT -> generateTxt(outputFile, note, checklists)
                ExportFormat.MARKDOWN -> generateMarkdown(outputFile, note, checklists)
                ExportFormat.CSV -> generateCsv(outputFile, note, checklists)
                ExportFormat.JSON -> generateJson(outputFile, note, checklists)
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Mercurynotes Note" })
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share note via ${format.displayName}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.localizedMessage ?: "Failed"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun generatePdf(file: File, note: NoteEntity, checklists: List<ChecklistItem>) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = android.graphics.Color.rgb(15, 23, 42)
            textSize = 22f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val metaPaint = Paint().apply {
            color = android.graphics.Color.rgb(100, 116, 139)
            textSize = 11f
        }

        val folderPaint = Paint().apply {
            color = android.graphics.Color.rgb(139, 92, 246)
            textSize = 12f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            color = android.graphics.Color.rgb(30, 41, 59)
            textSize = 13f
            typeface = Typeface.DEFAULT
        }

        val checkPaint = Paint().apply {
            color = android.graphics.Color.rgb(71, 85, 105)
            textSize = 13f
        }

        var y = 60f
        val marginX = 50f
        val maxWidth = 495f

        // Brand banner
        canvas.drawText("MERCURYNOTES • SECURE DOCUMENT EXPORT", marginX, y, metaPaint)
        y += 24f

        // Title
        val displayTitle = note.title.ifBlank { "Untitled Note" }
        canvas.drawText(displayTitle, marginX, y, titlePaint)
        y += 20f

        // Meta info
        val sdf = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(note.updatedAt))
        canvas.drawText("Folder: ${note.folderName}   •   Updated: $dateStr", marginX, y, folderPaint)
        y += 25f

        // Horizontal line
        val linePaint = Paint().apply {
            color = android.graphics.Color.rgb(226, 232, 240)
            strokeWidth = 1.5f
        }
        canvas.drawLine(marginX, y, marginX + maxWidth, y, linePaint)
        y += 25f

        // Checklists if any
        if (checklists.isNotEmpty()) {
            val headerPaint = Paint().apply {
                color = android.graphics.Color.rgb(15, 23, 42)
                textSize = 14f
                isFakeBoldText = true
            }
            canvas.drawText("Checklist Items:", marginX, y, headerPaint)
            y += 18f

            for (item in checklists) {
                val icon = if (item.isChecked) "[✓] " else "[  ] "
                canvas.drawText("$icon ${item.text}", marginX + 10f, y, checkPaint)
                y += 18f
                if (y > 780f) break
            }
            y += 15f
        }

        // Body content
        if (note.content.isNotBlank()) {
            val lines = note.content.split("\n")
            for (rawLine in lines) {
                // simple line wrapping
                var line = rawLine
                while (line.isNotEmpty()) {
                    val count = bodyPaint.breakText(line, true, maxWidth, null)
                    val sub = line.substring(0, count)
                    canvas.drawText(sub, marginX, y, bodyPaint)
                    y += 18f
                    line = line.substring(count)
                    if (y > 780f) break
                }
                if (y > 780f) break
            }
        }

        // Footer
        canvas.drawText("Generated with Mercurynotes by Rahul Shah", marginX, 810f, metaPaint)

        document.finishPage(page)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
    }

    private fun generateTxt(file: File, note: NoteEntity, checklists: List<ChecklistItem>) {
        val sb = StringBuilder()
        sb.appendLine(note.title.ifBlank { "Untitled Note" })
        sb.appendLine("=".repeat(note.title.length.coerceAtLeast(15)))
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sb.appendLine("Folder: ${note.folderName}")
        sb.appendLine("Date: ${sdf.format(Date(note.updatedAt))}")
        sb.appendLine()

        if (checklists.isNotEmpty()) {
            sb.appendLine("Checklist:")
            for (item in checklists) {
                val mark = if (item.isChecked) "[x]" else "[ ]"
                sb.appendLine("$mark ${item.text}")
            }
            sb.appendLine()
        }

        sb.appendLine(note.content)
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine("Exported from Mercurynotes")

        file.writeText(sb.toString())
    }

    private fun generateMarkdown(file: File, note: NoteEntity, checklists: List<ChecklistItem>) {
        val sb = StringBuilder()
        sb.appendLine("# ${note.title.ifBlank { "Untitled Note" }}")
        sb.appendLine()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sb.appendLine("> **Folder:** `${note.folderName}` | **Last Modified:** `${sdf.format(Date(note.updatedAt))}`")
        sb.appendLine()

        if (checklists.isNotEmpty()) {
            sb.appendLine("### Checklists")
            for (item in checklists) {
                val mark = if (item.isChecked) "x" else " "
                sb.appendLine("- [$mark] ${item.text}")
            }
            sb.appendLine()
        }

        sb.appendLine("### Notes")
        sb.appendLine(note.content)
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine("*Created with Mercurynotes by Rahul Shah*")

        file.writeText(sb.toString())
    }

    private fun generateCsv(file: File, note: NoteEntity, checklists: List<ChecklistItem>) {
        val sb = StringBuilder()
        sb.appendLine("\"Title\",\"Folder\",\"Updated\",\"Checklists\",\"Content\"")
        val escape = { s: String -> "\"${s.replace("\"", "\"\"")}\"" }

        val checkStr = checklists.joinToString("; ") { (if (it.isChecked) "[✓]" else "[ ]") + " " + it.text }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        sb.appendLine(
            "${escape(note.title)},${escape(note.folderName)},${escape(sdf.format(Date(note.updatedAt)))},${escape(checkStr)},${escape(note.content)}"
        )
        file.writeText(sb.toString())
    }

    private fun generateJson(file: File, note: NoteEntity, checklists: List<ChecklistItem>) {
        val escape = { s: String ->
            s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
        }

        val checklistsJson = checklists.joinToString(",") {
            """{"id":${it.id},"text":"${escape(it.text)}","isChecked":${it.isChecked}}"""
        }

        val json = """
        {
          "id": ${note.id},
          "title": "${escape(note.title)}",
          "content": "${escape(note.content)}",
          "folderId": ${note.folderId},
          "folderName": "${escape(note.folderName)}",
          "isPinned": ${note.isPinned},
          "isFavorite": ${note.isFavorite},
          "isLocked": ${note.isLocked},
          "colorTag": ${note.colorTag},
          "updatedAt": ${note.updatedAt},
          "checklists": [$checklistsJson],
          "exportedBy": "Mercurynotes by Rahul Shah"
        }
        """.trimIndent()

        file.writeText(json)
    }
}
