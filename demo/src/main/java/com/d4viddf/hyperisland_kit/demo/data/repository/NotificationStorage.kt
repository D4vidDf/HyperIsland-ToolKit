package com.d4viddf.hyperisland_kit.demo.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import com.d4viddf.hyperisland_kit.demo.data.model.InspectedNotification
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

object NotificationStorage {
    private const val TAG = "NotificationStorage"
    private const val ROOT_DIR = "inspector_v2"
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun save(context: Context, notification: InspectedNotification, images: Map<String, Bitmap>) {
        try {
            val root = File(context.filesDir, ROOT_DIR)
            if (!root.exists()) root.mkdirs()

            val notifFolder = File(root, notification.key)
            if (!notifFolder.exists()) notifFolder.mkdirs()

            val savedPaths = mutableMapOf<String, String>()
            images.forEach { (name, bmp) ->
                try {
                    val safeName = name.replace(Regex("[^a-zA-Z0-9.-]"), "_") + ".png"
                    val imgFile = File(notifFolder, safeName)

                    val softwareBmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bmp.config == Bitmap.Config.HARDWARE) {
                        bmp.copy(Bitmap.Config.ARGB_8888, false) ?: bmp
                    } else {
                        bmp
                    }

                    FileOutputStream(imgFile).use { out ->
                        softwareBmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    savedPaths[name] = imgFile.absolutePath
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to save bitmap image '$name' for key ${notification.key}", e)
                }
            }

            val updatedNotification = notification.copy(imagePaths = savedPaths)
            val jsonFile = File(notifFolder, "data.json")
            val serialized = json.encodeToString(updatedNotification)
            jsonFile.writeText(serialized)

            Log.d(TAG, "Successfully saved notification data.json to storage: ${notification.key}")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save notification: ${notification.key}", e)
        }
    }

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

                        val images = mutableMapOf<String, Bitmap>()
                        notif.imagePaths.forEach { (name, path) ->
                            try {
                                val bmp = BitmapFactory.decodeFile(path)
                                if (bmp != null) images[name] = bmp
                            } catch (e: Exception) {
                                Log.w(TAG, "Could not load image $name from $path", e)
                            }
                        }

                        list.add(Pair(notif, images))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading notification folder ${folder.name}", e)
                }
            }
        }
        Log.d(TAG, "Loaded ${list.size} saved notifications from storage")
        return list.sortedByDescending { it.first.postTime }
    }

    fun delete(context: Context, key: String) {
        try {
            val root = File(context.filesDir, ROOT_DIR)
            val folder = File(root, key)
            if (folder.exists()) {
                folder.deleteRecursively()
                Log.d(TAG, "Deleted notification folder from storage: $key")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete notification folder: $key", e)
        }
    }
}
