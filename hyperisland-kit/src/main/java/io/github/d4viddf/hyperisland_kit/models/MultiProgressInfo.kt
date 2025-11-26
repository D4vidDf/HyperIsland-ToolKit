package io.github.d4viddf.hyperisland_kit.models

import kotlinx.serialization.Serializable

@Serializable
data class MultiProgressInfo(
    val title: String,
    val progress: Int,
    val color: String? = null,
    val points: Int = 0
)