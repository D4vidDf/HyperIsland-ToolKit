package com.d4viddf.hyperisland_kit.demo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class InspectedNotification(
    val key: String,
    val id: Int,
    val packageName: String,
    val appName: String = "",
    val postTime: Long,
    val title: String,
    val content: String,

    // Detailed Info
    val templateStyle: String?,
    val isOngoing: Boolean,
    val contentIntent: String?,
    val actions: List<InspectedAction>,
    val styleExtras: Map<String, String>,

    // HyperIsland Payload
    val hyperJson: String?,

    // Local Asset Paths
    val imagePaths: Map<String, String> = emptyMap(),

    // Resource Metadata
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
    val type: String,
    val source: String,
    val width: Int,
    val height: Int,
    val fileSize: String
)
