package com.d4viddf.hyperisland_kit.demo.ui.screens.demos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.hyperisland_kit.demo.data.model.DemoCategory
import com.d4viddf.hyperisland_kit.demo.data.model.DemoItem
import com.d4viddf.hyperisland_kit.demo.utils.DemoNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ConfigurableOptions(
    val timeoutSeconds: Float = 5f,
    val enableFloat: Boolean = true,
    val showInShade: Boolean = true
)

class DemoListViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(DemoCategory.ALL)
    val selectedCategory: StateFlow<DemoCategory> = _selectedCategory.asStateFlow()

    private val _configurableOptions = MutableStateFlow(ConfigurableOptions())
    val configurableOptions: StateFlow<ConfigurableOptions> = _configurableOptions.asStateFlow()

    private val allDemos: List<DemoItem> = listOf(
        // Templates
        DemoItem("t1", "1. Weather", "BaseInfo Type 1 (Red Alert)", DemoCategory.TEMPLATES, isRecommended = true) { DemoNotificationManager.showTemplate1_Weather(it) },
        DemoItem("t2", "2. Payment", "BaseInfo Type 2 (Right Icon). Replicates Xiaomi Bill Payment.", DemoCategory.TEMPLATES, isRecommended = true) { DemoNotificationManager.showTemplate2_Payment(it) },
        DemoItem("t3", "3. IM / Chat", "ChatInfo (Messaging & Video Call)", DemoCategory.TEMPLATES, isRecommended = true) { DemoNotificationManager.showTemplate3_Chat(it) },
        DemoItem("t4", "4. Taxi Queue", "BaseInfo 2 + MultiProgress", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate4_TaxiQueue(it) },
        DemoItem("t5", "5. Dining Queue", "BaseInfo 1 + ProgressBar", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate5_DiningQueue(it) },
        DemoItem("t6", "6. Parking", "BaseInfo 2 + ProgressBar", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate6_Parking(it) },
        DemoItem("t7", "7. Uploading", "ChatInfo + Circular Progress", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate7_Upload(it) },
        DemoItem("t8", "8. Coupon", "ChatInfo + HintAction (Top Button)", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate8_Coupon(it) },
        DemoItem("t9", "9. Movie Ticket", "BaseInfo 2 + HintTimer (Top Timer)", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate9_Movie(it) },
        DemoItem("t10", "10. Pickup", "BaseInfo 2 + HintAction", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate10_Pickup(it) },
        DemoItem("t11", "11. Sports/Run", "HighlightInfo + HintTimer", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate11_Sports(it) },
        DemoItem("t12", "12. Call", "ChatInfo + Standard Actions", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate12_Call(it) },
        DemoItem("t13", "13. Recording", "HighlightInfo + Stop Action", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate13_Recording(it) },
        DemoItem("t14", "14. Navigation", "IconTextInfo (Turn Right)", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate14_Navigation(it) },
        DemoItem("t15", "15. Recorder", "AnimTextInfo (Voice Wave)", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate15_Recorder(it) },
        DemoItem("t16", "16. Code", "IconTextInfo + Copy Button", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate16_Code(it) },
        DemoItem("t17", "17. Promo", "HighlightInfoV3 (Pricing)", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate17_Promo(it) },
        DemoItem("t18", "18. File Request", "IconTextInfo + Text Buttons", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate18_FileRequest(it) },
        DemoItem("t19", "19. Cover Media", "CoverInfo + HintAction", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate19_Cover(it) },
        DemoItem("t20", "20. Data Usage", "IconTextInfo + Linear Progress", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate20_Data(it) },
        DemoItem("t21", "21. Game Download", "ChatInfo + Linear Progress", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate21_Game(it) },
        DemoItem("t22", "22. IoT Status", "IconTextInfo + Progress", DemoCategory.TEMPLATES) { DemoNotificationManager.showTemplate22_IoT(it) },
        DemoItem("t23", "23. Drag & Share", "Hold notification to drag and share", DemoCategory.TEMPLATES, isRecommended = true) { DemoNotificationManager.showTemplate23_DragShare(it) },

        // Customization
        DemoItem("c1", "Colored Text Buttons", "Text-only actions with custom background colors.", DemoCategory.CUSTOMIZATION) { DemoNotificationManager.showRawColoredTextButtons(it) },
        DemoItem("c2", "Icon Buttons (Rounded)", "Icon-only actions with generated circular background.", DemoCategory.CUSTOMIZATION) { DemoNotificationManager.showRawIconButtons(it) },
        DemoItem("c3", "Mix: Progress + Color", "Circular progress button next to a colored button.", DemoCategory.CUSTOMIZATION) { DemoNotificationManager.showRawProgressAndColorButton(it) },
        DemoItem("c4", "Background Color", "Notification with custom colored background.", DemoCategory.CUSTOMIZATION) { DemoNotificationManager.showRawBgInfo(it) },

        // Standard Demos
        DemoItem("s1", "App Open Demo", "Basic notification. Drag island to open app.", DemoCategory.STANDARD) { DemoNotificationManager.showAppOpenNotification(it) },
        DemoItem("s2", "Chat Info", "Chat style with text action.", DemoCategory.STANDARD) { DemoNotificationManager.showChatNotification(it) },
        DemoItem("s3", "Simple Island", "Small icon left, text right.", DemoCategory.STANDARD) { DemoNotificationManager.showSimpleSmallIslandNotification(it) },
        DemoItem("s4", "Right Image", "Expanded island with image on right side.", DemoCategory.STANDARD) { DemoNotificationManager.showRightImageNotification(it) },
        DemoItem("s5", "Split Info", "Content on both left and right sides.", DemoCategory.STANDARD) { DemoNotificationManager.showSplitIslandNotification(it) },
        DemoItem("s6", "Hint Info", "Small hint floating above notification.", DemoCategory.STANDARD) { DemoNotificationManager.showHintInfoNotification(it) },
        DemoItem("s7", "Multi Node Progress", "Segmented 'Step 2 of 4' progress bar.", DemoCategory.STANDARD) { DemoNotificationManager.showMultiNodeProgressNotification(it) },
        DemoItem("s8", "Icon Progress Bar", "Linear progress with icons (Delivery).", DemoCategory.STANDARD) { DemoNotificationManager.showProgressBarNotification(it) },
        DemoItem("s9", "Circular Progress", "Circular progress on big/small island.", DemoCategory.STANDARD) { DemoNotificationManager.showCircularProgressNotification(it) },
        DemoItem("s10", "Countdown Timer", "15-minute countdown timer.", DemoCategory.STANDARD) { DemoNotificationManager.showCountdownNotification(it) },
        DemoItem("s11", "Count-Up Timer", "Timer counting up.", DemoCategory.STANDARD) { DemoNotificationManager.showCountUpNotification(it) },
        DemoItem("s12", "Multi-Action", "Stop (Progress) + Close buttons.", DemoCategory.STANDARD) { DemoNotificationManager.showMultiActionNotification(it) },

        // Custom Views
        DemoItem("v1", "HyperIsland DIY (Custom View)", "Uses miui.focus.rv and param.custom to render RemoteViews.", DemoCategory.CUSTOM_VIEWS, isRecommended = true) { DemoNotificationManager.showFocusDiyNotification(it) },
        DemoItem("v2", "Music Player (DIY)", "Custom RemoteViews with Timeline, Controls & Dynamic Color.", DemoCategory.CUSTOM_VIEWS, isRecommended = true) { DemoNotificationManager.showMusicPlayerDemo(it) }
    )

    val filteredDemos: StateFlow<List<DemoItem>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        allDemos.filter { item ->
            val matchesQuery = query.isEmpty() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)

            val matchesCategory = category == DemoCategory.ALL || item.category == category

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = allDemos
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: DemoCategory) {
        _selectedCategory.value = category
    }

    fun updateConfigurableOptions(timeoutSeconds: Float, enableFloat: Boolean, showInShade: Boolean) {
        _configurableOptions.value = ConfigurableOptions(timeoutSeconds, enableFloat, showInShade)
    }
}
