package com.d4viddf.hyperisland_kit.demo.data.model

import android.content.Context

enum class DemoCategory(val title: String) {
    ALL("All"),
    TEMPLATES("Official Templates"),
    CUSTOMIZATION("Customization"),
    STANDARD("Standard Demos"),
    CUSTOM_VIEWS("HyperIsland DIY")
}

data class DemoItem(
    val id: String,
    val title: String,
    val description: String,
    val category: DemoCategory,
    val isRecommended: Boolean = false,
    val action: (Context) -> Unit
)
