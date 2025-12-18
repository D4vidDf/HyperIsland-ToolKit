package com.d4viddf.hyperisland_kit.demo

import kotlinx.serialization.Serializable

@Serializable
data class InspectedNotification(
    val key: String,
    val id: Int,
    val packageName: String,
    val postTime: Long,
    val title: String,
    val content: String,

    // --- Detailed Info ---
    val templateStyle: String?,
    val isOngoing: Boolean,
    val contentIntent: String?,
    val actions: List<InspectedAction>,
    val styleExtras: Map<String, String>,

    // --- HyperIsland ---
    val hyperJson: String?,

    // --- Assets ---
    val imagePaths: Map<String, String> = emptyMap(), // Paths to local saved PNGs

    // [NEW] Metadata about the original resources
    val resourceMeta: Map<String, ResourceMeta> = emptyMap()
)

@Serializable
data class InspectedAction(
    val title: String,
    val iconKey: String?,
    val intentDescription: String?
)

@Serializable
data class ResourceMeta(
    val type: String,       // e.g., "RESOURCE", "URI", "BITMAP"
    val source: String,     // e.g., "com.android.systemui:drawable/ic_check" or "content://..."
    val width: Int,
    val height: Int,
    val fileSize: String    // Estimated size in KB
)