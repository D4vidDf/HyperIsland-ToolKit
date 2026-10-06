package com.d4viddf.hyperisland_kit.demo.ui.screens.compatibility

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class CompatibilityUiState(
    val isSupported: Boolean = false,
    val commercialName: String = "",
    val deviceMarketName: String = "",
    val manufacturer: String = "",
    val model: String = "",
    val deviceCodename: String = "",
    val hardware: String = "",
    val product: String = "",
    val androidVersion: String = "",
    val sdkInt: Int = 0,
    val buildDisplay: String = "",
    val hyperOsVersion: String = ""
)

class CompatibilityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CompatibilityUiState())
    val uiState: StateFlow<CompatibilityUiState> = _uiState.asStateFlow()

    fun checkCompatibility(context: Context) {
        val supported = HyperIslandNotification.isSupported(context)
        val manufacturerFormatted = Build.MANUFACTURER.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        val commercial = getCommercialDeviceName()

        val fullMarketName = if (commercial != Build.MODEL) {
            "$commercial (${Build.MODEL})"
        } else {
            if (Build.MODEL.startsWith(manufacturerFormatted, ignoreCase = true)) {
                "${Build.MODEL} (${Build.DEVICE})"
            } else {
                "$manufacturerFormatted ${Build.MODEL} (${Build.DEVICE})"
            }
        }

        val hyperOsName = getSystemProperty("ro.miui.ui.version.name")
            ?: getSystemProperty("ro.build.version.incremental")
            ?: "N/A"

        _uiState.update {
            CompatibilityUiState(
                isSupported = supported,
                commercialName = commercial,
                deviceMarketName = fullMarketName,
                manufacturer = manufacturerFormatted,
                model = Build.MODEL,
                deviceCodename = Build.DEVICE,
                hardware = Build.HARDWARE,
                product = Build.PRODUCT,
                androidVersion = Build.VERSION.RELEASE,
                sdkInt = Build.VERSION.SDK_INT,
                buildDisplay = Build.DISPLAY,
                hyperOsVersion = hyperOsName
            )
        }
    }

    private fun getCommercialDeviceName(): String {
        val props = listOf(
            "ro.product.marketname",
            "ro.config.marketing_name",
            "ro.vendor.marketname",
            "ro.product.system.marketname",
            "ro.product.odm.marketname"
        )

        for (prop in props) {
            val valStr = getSystemProperty(prop)
            if (!valStr.isNullOrEmpty()) {
                return valStr
            }
        }

        val manufacturerFormatted = Build.MANUFACTURER.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        return if (Build.MODEL.startsWith(manufacturerFormatted, ignoreCase = true)) {
            Build.MODEL
        } else {
            "$manufacturerFormatted ${Build.MODEL}"
        }
    }

    private fun getSystemProperty(key: String): String? {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java)
            val result = get.invoke(c, key) as? String
            if (!result.isNullOrEmpty()) result else null
        } catch (e: Exception) {
            null
        }
    }
}
