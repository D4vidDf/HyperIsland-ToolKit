package com.d4viddf.hyperisland_kit.demo.ui.screens.inspector

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d4viddf.hyperisland_kit.demo.ui.components.ExpressiveHeader
import com.d4viddf.hyperisland_kit.demo.ui.components.ExpressiveSearchBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationInspectorScreen(
    onSelectNotification: (String) -> Unit,
    viewModel: NotificationInspectorViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600

    val hasPermission by viewModel.hasPermission.collectAsState()
    val isListenerConnected by viewModel.isListenerConnected.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val filterPkg by viewModel.filterPkg.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val displayGroups by viewModel.displayGroups.collectAsState()

    var selectedKeyForExpanded by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermission(context)
                viewModel.refreshSaved(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermission(context)
        viewModel.refreshSaved(context)
    }

    if (selectedKeyForExpanded == null && displayGroups.isNotEmpty()) {
        selectedKeyForExpanded = displayGroups.first().latest.key
    }

    if (isExpanded) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
            ) {
                InspectorListContent(
                    hasPermission = hasPermission,
                    isListenerConnected = isListenerConnected,
                    filterMode = filterMode,
                    filterPkg = filterPkg,
                    searchQuery = searchQuery,
                    displayGroups = displayGroups,
                    selectedKey = selectedKeyForExpanded,
                    onGroupClick = { selectedKeyForExpanded = it.latest.key },
                    onSetFilterMode = { viewModel.setFilterMode(context, it) },
                    onSetFilterPkg = viewModel::setFilterPkg,
                    onSetSearchQuery = viewModel::setSearchQuery,
                    onToggleBookmarkGroup = { viewModel.toggleBookmarkGroup(context, it) },
                    onClearLiveLog = viewModel::clearLiveLog,
                    onReconnectListener = { viewModel.reconnectListener(context) },
                    onGrantPermissionClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (selectedKeyForExpanded != null) {
                    NotificationDetailScreen(
                        notificationKey = selectedKeyForExpanded!!,
                        viewModel = viewModel,
                        onBackClick = {}
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select a notification stream to inspect details",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    } else {
        InspectorListContent(
            hasPermission = hasPermission,
            isListenerConnected = isListenerConnected,
            filterMode = filterMode,
            filterPkg = filterPkg,
            searchQuery = searchQuery,
            displayGroups = displayGroups,
            selectedKey = null,
            onGroupClick = { onSelectNotification(it.latest.key) },
            onSetFilterMode = { viewModel.setFilterMode(context, it) },
            onSetFilterPkg = viewModel::setFilterPkg,
            onSetSearchQuery = viewModel::setSearchQuery,
            onToggleBookmarkGroup = { viewModel.toggleBookmarkGroup(context, it) },
            onClearLiveLog = viewModel::clearLiveLog,
            onReconnectListener = { viewModel.reconnectListener(context) },
            onGrantPermissionClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        )
    }
}

@Composable
private fun InspectorListContent(
    hasPermission: Boolean,
    isListenerConnected: Boolean,
    filterMode: InspectorFilter,
    filterPkg: String?,
    searchQuery: String,
    displayGroups: List<InspectorGroup>,
    selectedKey: String?,
    onGroupClick: (InspectorGroup) -> Unit,
    onSetFilterMode: (InspectorFilter) -> Unit,
    onSetFilterPkg: (String?) -> Unit,
    onSetSearchQuery: (String) -> Unit,
    onToggleBookmarkGroup: (InspectorGroup) -> Unit,
    onClearLiveLog: () -> Unit,
    onReconnectListener: () -> Unit,
    onGrantPermissionClick: () -> Unit
) {
    val listenerBadge = if (hasPermission && isListenerConnected) "Active" else if (hasPermission) "Inactive" else "Disabled"

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                ExpressiveHeader(
                    title = "Inspector",
                    subtitle = "Real-time HyperOS Payload Logs",
                    badgeText = "$listenerBadge (${displayGroups.size})"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExpressiveSearchBar(
                    query = searchQuery,
                    onQueryChange = onSetSearchQuery,
                    placeholderText = "Filter by app name, title..."
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            FilterChip(
                                selected = filterMode == InspectorFilter.LIVE_ALL,
                                onClick = { onSetFilterMode(InspectorFilter.LIVE_ALL) },
                                label = { Text("All") },
                                shape = MaterialTheme.shapes.medium,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterMode == InspectorFilter.LIVE_HYPER,
                                onClick = { onSetFilterMode(InspectorFilter.LIVE_HYPER) },
                                label = { Text("Hyper") },
                                leadingIcon = { Icon(Icons.Default.BugReport, null, modifier = Modifier.size(14.dp)) },
                                shape = MaterialTheme.shapes.medium,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterMode == InspectorFilter.SAVED,
                                onClick = { onSetFilterMode(InspectorFilter.SAVED) },
                                label = { Text("Saved") },
                                leadingIcon = { Icon(Icons.Default.Save, null, modifier = Modifier.size(14.dp)) },
                                shape = MaterialTheme.shapes.medium,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    if (filterPkg != null) {
                        IconButton(onClick = { onSetFilterPkg(null) }) {
                            Icon(Icons.Default.FilterListOff, "Clear Package Filter")
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (filterMode != InspectorFilter.SAVED && hasPermission) {
                FloatingActionButton(
                    onClick = onClearLiveLog,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.DeleteSweep, "Clear Logs")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!hasPermission && filterMode != InspectorFilter.SAVED) {
                PermissionRequestCard(onClick = onGrantPermissionClick)
            } else if (hasPermission && !isListenerConnected && filterMode != InspectorFilter.SAVED) {
                ListenerDisconnectedCard(
                    onReconnect = onReconnectListener,
                    onOpenSettings = onGrantPermissionClick
                )
            }

            if (displayGroups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!hasPermission) "Grant listener permission in settings"
                        else if (!isListenerConnected) "Listener service is disconnected. Tap 'Restart Service' above."
                        else "No notifications received yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayGroups, key = { it.groupId }) { group ->
                        InspectorGroupCard(
                            group = group,
                            isSelected = selectedKey == group.latest.key,
                            onClick = { onGroupClick(group) },
                            onToggleBookmark = { onToggleBookmarkGroup(group) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectorGroupCard(
    group: InspectorGroup,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val notif = group.latest
    val isHyper = notif.hyperJson != null

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        isHyper -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isHyper -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (isSelected) 4.dp else 1.dp,
        shadowElevation = if (isSelected) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            if (isHyper) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val appIcon = group.latestImages["Small Icon"] ?: group.latestImages["Large Icon"]
                    if (appIcon != null) {
                        Image(
                            bitmap = appIcon.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = group.packageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = formatTime(notif.postTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (group.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (group.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = notif.title.ifEmpty { "Notification" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (notif.content.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notif.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (group.updateCount > 1) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${group.updateCount} Updates", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (isHyper) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text("HYPER", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (notif.templateStyle != null) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(notif.templateStyle.replace("Style", ""), fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListenerDisconnectedCard(
    onReconnect: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notification Listener Service Inactive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "The listener is currently disconnected by Android. Tap to restart service.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onReconnect,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restart Service")
                }
                OutlinedButton(
                    onClick = onOpenSettings,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Settings")
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Listener Permission Missing", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Text("Tap here to grant listener access", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

private fun formatTime(time: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
