package io.github.d4viddf.hyperisland_kit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    // --- ADDED BACK: Top-level actions dictionary ---
    val actions: List<HyperActionRef>? = null,
    val progressInfo: ProgressInfo? = null
)


@Serializable
data class SmallWindowInfo(
    val targetPage: String
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

@Serializable
data class BigIslandArea(
    @SerialName("imageTextInfoLeft")
    val imageTextInfoLeft: ImageTextInfoLeft? = null,
    @SerialName("imageTextInfoRight")
    val imageTextInfoRight: ImageTextInfoRight? = null,
    @SerialName("sameWidthDigitInfo")
    val sameWidthDigitInfo: SameWidthDigitInfo? = null,
    @SerialName("progressTextInfo")
    val progressTextInfo: ProgressTextInfo? = null,
    // BigIsland usually takes simple refs, but keeping this flexible
    val actions: List<SimpleActionRef>? = null
)

@Serializable
data class SameWidthDigitInfo(
    val timerInfo: TimerInfo,
    @SerialName("showHighlightColor")
    val showHighlightColor: Boolean = true
)

@Serializable
data class SmallIslandArea(
    @SerialName("imageTextInfoLeft")
    val imageTextInfoLeft: ImageTextInfoLeft? = null,
    @SerialName("imageTextInfoRight")
    val imageTextInfoRight: ImageTextInfoRight? = null,
    @SerialName("combinePicInfo")
    val combinePicInfo: CombinePicInfo? = null,
    val picInfo: PicInfo? = null
)

@Serializable
data class CombinePicInfo(
    val picInfo: PicInfo,
    @SerialName("progressInfo")
    val progressInfo: CircularProgressInfo
)

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

@Serializable
data class ProgressTextInfo(
    @SerialName("progressInfo")
    val progressInfo: CircularProgressInfo,
    val textInfo: TextInfo? = null
)

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