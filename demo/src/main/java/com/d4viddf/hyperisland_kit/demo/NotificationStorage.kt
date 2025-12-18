package com.d4viddf.hyperisland_kit.demo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

object NotificationStorage {
    private const val ROOT_DIR = "inspector_v2"
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    // Save Notification + Images
    fun save(context: Context, notification: InspectedNotification, images: Map<String, Bitmap>) {
        try {
            val root = File(context.filesDir, ROOT_DIR)
            if (!root.exists()) root.mkdirs()

            // 1. Create a folder for this notification
            val notifFolder = File(root, notification.key)
            if (!notifFolder.exists()) notifFolder.mkdirs()

            // 2. Save Images to disk and update paths
            val savedPaths = mutableMapOf<String, String>()
            images.forEach { (name, bmp) ->
                val safeName = name.replace(Regex("[^a-zA-Z0-9.-]"), "_") + ".png"
                val imgFile = File(notifFolder, safeName)
                FileOutputStream(imgFile).use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                savedPaths[name] = imgFile.absolutePath
            }

            // 3. Save JSON with image paths
            val updatedNotification = notification.copy(imagePaths = savedPaths)
            val jsonFile = File(notifFolder, "data.json")
            jsonFile.writeText(json.encodeToString(updatedNotification))

        } catch (e: Exception) {
            Log.e("Storage", "Failed to save", e)
        }
    }

    // Load All
    fun loadAll(context: Context): List<Pair<InspectedNotification, Map<String, Bitmap>>> {
        val list = mutableListOf<Pair<InspectedNotification, Map<String, Bitmap>>>()
        val root = File(context.filesDir, ROOT_DIR)
        if (!root.exists()) return emptyList()

        root.listFiles()?.forEach { folder ->
            if (folder.isDirectory) {
                try {
                    val jsonFile = File(folder, "data.json")
                    if (jsonFile.exists()) {
                        val notif = json.decodeFromString<InspectedNotification>(jsonFile.readText())

                        // Load Images
                        val images = mutableMapOf<String, Bitmap>()
                        notif.imagePaths.forEach { (name, path) ->
                            val bmp = BitmapFactory.decodeFile(path)
                            if (bmp != null) images[name] = bmp
                        }

                        list.add(Pair(notif, images))
                    }
                } catch (e: Exception) {
                    Log.e("Storage", "Error loading ${folder.name}", e)
                }
            }
        }
        return list.sortedByDescending { it.first.postTime }
    }

    fun delete(context: Context, key: String) {
        val root = File(context.filesDir, ROOT_DIR)
        val folder = File(root, key)
        folder.deleteRecursively()
    }
}