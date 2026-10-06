package com.d4viddf.hyperisland_kit.demo.ui.screens.inspector

import android.content.Context
import android.graphics.Bitmap
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.hyperisland_kit.demo.data.model.InspectedNotification
import com.d4viddf.hyperisland_kit.demo.data.repository.NotificationLogRepository
import com.d4viddf.hyperisland_kit.demo.data.repository.NotificationStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class InspectorFilter { LIVE_ALL, LIVE_HYPER, SAVED }

data class InspectorItem(
    val notification: InspectedNotification,
    val images: Map<String, Bitmap>,
    val isSaved: Boolean
)

data class InspectorGroup(
    val groupId: String,
    val packageName: String,
    val appName: String,
    val id: Int,
    val updates: List<InspectorItem>
) {
    val latest: InspectedNotification get() = updates.first().notification
    val latestImages: Map<String, Bitmap> get() = updates.first().images
    val updateCount: Int get() = updates.size
    val isSaved: Boolean get() = updates.any { it.isSaved }
}

class NotificationInspectorViewModel : ViewModel() {

    private val _filterMode = MutableStateFlow(InspectorFilter.LIVE_ALL)
    val filterMode: StateFlow<InspectorFilter> = _filterMode.asStateFlow()

    private val _filterPkg = MutableStateFlow<String?>(null)
    val filterPkg: StateFlow<String?> = _filterPkg.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    val isListenerConnected: StateFlow<Boolean> = com.d4viddf.hyperisland_kit.demo.data.service.HyperIslandListenerService.isConnected

    private val _savedData = MutableStateFlow<List<Pair<InspectedNotification, Map<String, Bitmap>>>>(emptyList())

    val displayGroups: StateFlow<List<InspectorGroup>> = combine(
        NotificationLogRepository.notifications,
        _savedData,
        _filterMode,
        _filterPkg,
        _searchQuery
    ) { live, saved, mode, pkg, query ->
        val savedKeys = saved.map { it.first.key }.toSet()
        val savedGroupIds = saved.map { "${it.first.packageName}_${it.first.id}" }.toSet()
        val source = if (mode == InspectorFilter.SAVED) saved else live

        val items = source.map { (notif, images) ->
            val isItemSaved = savedKeys.contains(notif.key) || savedGroupIds.contains("${notif.packageName}_${notif.id}")
            InspectorItem(notif, images, isItemSaved)
        }.filter { item ->
            if (mode == InspectorFilter.LIVE_HYPER && item.notification.hyperJson == null) return@filter false
            if (pkg != null && item.notification.packageName != pkg) return@filter false
            if (query.isNotEmpty()) {
                val notif = item.notification
                val matchesQuery = notif.title.contains(query, ignoreCase = true) ||
                        notif.content.contains(query, ignoreCase = true) ||
                        notif.appName.contains(query, ignoreCase = true) ||
                        notif.packageName.contains(query, ignoreCase = true)
                if (!matchesQuery) return@filter false
            }
            true
        }

        items.groupBy { "${it.notification.packageName}_${it.notification.id}" }
            .map { (groupId, groupItems) ->
                val sorted = groupItems.sortedByDescending { it.notification.postTime }
                val firstNotif = sorted.first().notification
                val appLabel = if (firstNotif.appName.isNotEmpty()) firstNotif.appName else firstNotif.packageName

                InspectorGroup(
                    groupId = groupId,
                    packageName = firstNotif.packageName,
                    appName = appLabel,
                    id = firstNotif.id,
                    updates = sorted
                )
            }
            .sortedByDescending { it.latest.postTime }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun checkPermission(context: Context) {
        val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        _hasPermission.value = listeners != null && listeners.contains(context.packageName)
        refreshSaved(context)
    }

    fun setFilterMode(context: Context, mode: InspectorFilter) {
        _filterMode.value = mode
        refreshSaved(context)
    }

    fun setFilterPkg(pkg: String?) {
        _filterPkg.value = pkg
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshSaved(context: Context) {
        _savedData.value = NotificationStorage.loadAll(context)
    }

    fun toggleBookmarkGroup(context: Context, group: InspectorGroup) {
        val currentlySaved = group.isSaved
        group.updates.forEach { item ->
            if (currentlySaved) {
                NotificationStorage.delete(context, item.notification.key)
            } else {
                NotificationStorage.save(context, item.notification, item.images)
            }
        }
        refreshSaved(context)

        val msg = if (currentlySaved) "Removed from bookmarks" else "Saved to bookmarks"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun reconnectListener(context: Context) {
        com.d4viddf.hyperisland_kit.demo.data.service.HyperIslandListenerService.requestReconnect(context)
        Toast.makeText(context, "Requesting Notification Listener reconnect...", Toast.LENGTH_SHORT).show()
    }

    fun clearLiveLog() {
        NotificationLogRepository.clear()
    }
}
