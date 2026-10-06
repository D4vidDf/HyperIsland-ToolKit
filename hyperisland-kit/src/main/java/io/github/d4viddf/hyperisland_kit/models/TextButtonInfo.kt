package io.github.d4viddf.hyperisland_kit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model for the "textButton" component (Component 18/Custom Specification).
 * Matches the structure: [{"actionTitle":..., "actionBgColor":...}]
 */
@Serializable
data class TextButtonInfo(
    val type: Int = 1, // Usually 1 for standard text buttons
    val actionTitle: String,
    val actionIcon: String? = null, // Key for the icon (e.g. "miui.focus.pic_...")
    val actionIconDark: String? = null,

    // Colors
    val actionBgColor: String? = null,
    val actionBgColorDark: String? = null,
    val actionTitleColor: String? = null,
    val actionTitleColorDark: String? = null,

    // Intent Config
    val actionIntentType: Int = 1,
    val actionIntent: String? = null,
    @SerialName("action")
    val action: String? = null,
)