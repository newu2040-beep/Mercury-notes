package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.ChecklistItem
import com.example.data.model.NoteEntity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

data class ImportedNoteData(
    val title: String,
    val content: String,
    val checklists: List<ChecklistItem> = emptyList(),
    val imageUri: String? = null,
    val attachedFileName: String? = null
)

object FileImporter {

    fun getFileName(context: Context, uri: Uri): String {
        var name = "imported_file"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    fun parseImportedUri(context: Context, uri: Uri): ImportedNoteData {
        val fileName = getFileName(context, uri)
        val ext = fileName.substringAfterLast(".", "").lowercase()

        return try {
            when (ext) {
                "txt", "md", "markdown", "log" -> {
                    val content = readTextStream(context, uri)
                    val title = fileName.substringBeforeLast(".")
                    ImportedNoteData(title = title, content = content, attachedFileName = fileName)
                }
                "csv" -> {
                    val content = readTextStream(context, uri)
                    val (title, body, checklists) = parseCsvToNote(fileName, content)
                    ImportedNoteData(title = title, content = body, checklists = checklists, attachedFileName = fileName)
                }
                "json" -> {
                    val jsonStr = readTextStream(context, uri)
                    parseJsonToNote(fileName, jsonStr)
                }
                "zip" -> {
                    parseZipFile(context, uri, fileName)
                }
                "ttf", "otf" -> {
                    val fontFile = copyFileToInternal(context, uri, "fonts", fileName)
                    ImportedNoteData(
                        title = "Custom Font: ${fileName.substringBeforeLast(".")}",
                        content = "Imported custom typeface font file: $fileName\nLocation: ${fontFile.absolutePath}",
                        attachedFileName = fileName
                    )
                }
                "png", "jpg", "jpeg", "webp", "gif" -> {
                    ImportedNoteData(
                        title = fileName.substringBeforeLast("."),
                        content = "Imported photo attachment: $fileName",
                        imageUri = uri.toString(),
                        attachedFileName = fileName
                    )
                }
                "pdf" -> {
                    ImportedNoteData(
                        title = fileName.substringBeforeLast("."),
                        content = "📄 Imported PDF Document: $fileName\nStored in attachments.",
                        attachedFileName = fileName
                    )
                }
                else -> {
                    val raw = try { readTextStream(context, uri) } catch (e: Exception) { "" }
                    ImportedNoteData(
                        title = fileName.substringBeforeLast("."),
                        content = if (raw.isNotBlank()) raw else "Imported file attachment: $fileName",
                        attachedFileName = fileName
                    )
                }
            }
        } catch (e: Exception) {
            ImportedNoteData(
                title = fileName.substringBeforeLast("."),
                content = "Error reading file: ${e.localizedMessage}",
                attachedFileName = fileName
            )
        }
    }

    private fun readTextStream(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        } ?: ""
    }

    private fun parseCsvToNote(fileName: String, csvContent: String): Triple<String, String, List<ChecklistItem>> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        val title = fileName.substringBeforeLast(".")
        val checklists = mutableListOf<ChecklistItem>()
        val bodySb = StringBuilder()

        for ((index, line) in lines.withIndex()) {
            if (index == 0 && line.lowercase().contains("title") && line.contains(",")) {
                bodySb.appendLine("Headers: $line")
            } else {
                val clean = line.replace("\"", "").trim()
                if (clean.isNotBlank()) {
                    checklists.add(ChecklistItem(id = (index + 1).toString(), text = clean, isChecked = false))
                }
            }
        }

        return Triple(title, bodySb.toString(), checklists)
    }

    private fun parseJsonToNote(fileName: String, jsonStr: String): ImportedNoteData {
        return try {
            val obj = JSONObject(jsonStr)
            val title = obj.optString("title", fileName.substringBeforeLast("."))
            val content = obj.optString("content", jsonStr)
            val checklists = mutableListOf<ChecklistItem>()
            val checkArray = obj.optJSONArray("checklists")
            if (checkArray != null) {
                for (i in 0 until checkArray.length()) {
                    val itemObj = checkArray.getJSONObject(i)
                    checklists.add(
                        ChecklistItem(
                            id = itemObj.optString("id", (i + 1).toString()),
                            text = itemObj.optString("text", "Task ${i + 1}"),
                            isChecked = itemObj.optBoolean("isChecked", false)
                        )
                    )
                }
            }
            ImportedNoteData(title = title, content = content, checklists = checklists, attachedFileName = fileName)
        } catch (e: Exception) {
            ImportedNoteData(title = fileName.substringBeforeLast("."), content = jsonStr, attachedFileName = fileName)
        }
    }

    private fun parseZipFile(context: Context, uri: Uri, fileName: String): ImportedNoteData {
        val sb = StringBuilder()
        sb.appendLine("📦 Extracted ZIP Archive: $fileName")
        sb.appendLine("----------------------------------------")
        var entriesCount = 0

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        entriesCount++
                        sb.appendLine("• ${entry.name} (${entry.size} bytes)")
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
        sb.appendLine()
        sb.appendLine("Total Files in Archive: $entriesCount")
        return ImportedNoteData(
            title = fileName.substringBeforeLast("."),
            content = sb.toString(),
            attachedFileName = fileName
        )
    }

    fun copyFileToInternal(context: Context, uri: Uri, folder: String, fileName: String): File {
        val targetDir = File(context.filesDir, folder)
        if (!targetDir.exists()) targetDir.mkdirs()
        val destFile = File(targetDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        return destFile
    }
}
