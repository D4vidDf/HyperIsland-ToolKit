package com.d4viddf.hyperisland_kit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ... (HyperIslandPayload, ParamV2, BaseInfo, SmallWindowInfo, ChatInfo, TimerInfo, HyperActionRef, ProgressInfo remain the same) ...
@Serializable
data class HyperIslandPayload(
    @SerialName("param_v2")
    val paramV2: ParamV2
)

@Serializable
data class ParamV2(
    val protocol: Int = 3,
    val business: String,
    val updatable: Boolean = true,
    val ticker: String,
    @SerialName("isShownNotification")
    val isShownNotification: Boolean = true,
    @SerialName("islandFirstFloat")
    val islandFirstFloat: Boolean = true,
    @SerialName("smallWindowInfo")
    val smallWindowInfo: SmallWindowInfo? = null,
    @SerialName("param_island")
    val paramIsland: ParamIsland? = null,
    val chatInfo: ChatInfo? = null,
    val baseInfo: BaseInfo? = null,
    val actions: List<HyperActionRef>? = null,
    val progressInfo: ProgressInfo? = null
)

@Serializable
data class BaseInfo(
    val type: Int = 1,
    val title: String,
    val subTitle: String? = null,
    val content: String,
    @SerialName("picFunction")
    val picFunction: String? = null
)

@Serializable
data class SmallWindowInfo(
    val targetPage: String
)

@Serializable
data class ChatInfo(
    val type: Int = 1,
    val title: String,
    val content: String? = null,
    @SerialName("picFunction")
    val picFunction: String? = null,
    val actions: List<HyperActionRef>? = null,
    val timerInfo: TimerInfo? = null
)

@Serializable
data class TimerInfo(
    @SerialName("timerType")
    val timerType: Int,
    @SerialName("timerWhen")
    val timerWhen: Long,
    @SerialName("timerTotal")
    val timerTotal: Long,
    @SerialName("timerSystemCurrent")
    val timerSystemCurrent: Long
)

@Serializable
data class HyperActionRef(
    val type: Int,
    val action: String,
    val progressInfo: ProgressInfo? = null,
    val actionTitle: String? = null,
    @SerialName("actionIntent")
    val actionIntent: String? = null
)

// This is for LINEAR progress (ParamV2) or BUTTON progress (HyperActionRef)
@Serializable
data class ProgressInfo(
    val progress: Int,
    @SerialName("colorProgress")
    val colorProgress: String? = null
)

// --- Island States (Summary/Expanded) ---

@Serializable
data class ParamIsland(
    @SerialName("islandProperty")
    val islandProperty: Int = 1,
    @SerialName("bigIslandArea")
    val bigIslandArea: BigIslandArea? = null,
    @SerialName("smallIslandArea")
    val smallIslandArea: SmallIslandArea? = null
)

// --- THIS CLASS IS MODIFIED ---
@Serializable
data class BigIslandArea(
    @SerialName("imageTextInfoLeft")
    val imageTextInfoLeft: ImageTextInfoLeft? = null,
    @SerialName("sameWidthDigitInfo")
    val sameWidthDigitInfo: SameWidthDigitInfo? = null,
    // --- MOVED progressTextInfo HERE ---
    @SerialName("progressTextInfo")
    val progressTextInfo: ProgressTextInfo? = null // For circular progress
)

@Serializable
data class SameWidthDigitInfo(
    val timerInfo: TimerInfo,
    @SerialName("showHighlightColor")
    val showHighlightColor: Boolean = true
)

// --- A/B Zone Components for Summary State ---

// --- THIS CLASS IS MODIFIED ---
@Serializable
data class SmallIslandArea(
    @SerialName("imageTextInfoLeft")
    val imageTextInfoLeft: ImageTextInfoLeft? = null,
    @SerialName("imageTextInfoRight")
    val imageTextInfoRight: ImageTextInfoRight? = null,
    // --- ADDED THIS based on docs  ---
    @SerialName("combinePicInfo")
    val combinePicInfo: CombinePicInfo? = null,
    val picInfo: PicInfo? = null
)

// --- ADDED THIS NEW DATA CLASS ---
// For small island circular progress
@Serializable
data class CombinePicInfo(
    val picInfo: PicInfo,
    @SerialName("progressInfo")
    val progressInfo: CircularProgressInfo
)

// --- ADDED THIS NEW DATA CLASS ---
@Serializable
data class CircularProgressInfo(
    val progress: Int,
    @SerialName("colorReach")
    val colorReach: String? = null,
    @SerialName("colorUnReach")
    val colorUnReach: String? = null,
    @SerialName("isCCW")
    val isCCW: Boolean = false
)

// --- THIS CLASS IS MODIFIED ---
// For big island circular progress [cite: 30-39]
@Serializable
data class ProgressTextInfo(
    // --- USES CircularProgressInfo ---
    @SerialName("progressInfo")
    val progressInfo: CircularProgressInfo,
    val textInfo: TextInfo? = null
)

// ... (ImageTextInfoLeft, ImageTextInfoRight, PicInfo, TextInfo remain the same) ...
@Serializable
data class ImageTextInfoLeft(
    val type: Int = 1,
    val picInfo: PicInfo? = null,
    val textInfo: TextInfo? = null
)

@Serializable
data class ImageTextInfoRight(
    val type: Int = 2,
    val picInfo: PicInfo? = null,
    val textInfo: TextInfo? = null
)

@Serializable
data class PicInfo(
    val type: Int = 1,
    val pic: String
)

@Serializable
data class TextInfo(
    val title: String,
    val content: String? = null,
    @SerialName("showHighlightColor")
    val showHighlightColor: Boolean = false,
    val narrowFont: Boolean? = null
)