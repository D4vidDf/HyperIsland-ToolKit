package com.d4viddf.hyperisland_kit.demo.ui.screens.inspector

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d4viddf.hyperisland_kit.demo.data.model.InspectedAction
import com.d4viddf.hyperisland_kit.demo.data.model.ResourceMeta
import com.d4viddf.hyperisland_kit.demo.utils.ReportExporter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationDetailScreen(
    notificationKey: String,
    viewModel: NotificationInspectorViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val groups by viewModel.displayGroups.collectAsState()

    val group = groups.find { grp -> grp.updates.any { it.notification.key == notificationKey } }

    if (group == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Stream Detail") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Notification stream not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var selectedStepIndex by remember(group.groupId) { mutableIntStateOf(0) }
    if (selectedStepIndex >= group.updates.size) selectedStepIndex = 0

    val currentItem = group.updates[selectedStepIndex]
    var selectedTab by remember { mutableIntStateOf(0) }
    var fullScreenAsset by remember { mutableStateOf<Pair<String, Pair<Bitmap, ResourceMeta?>>?>(null) }
    var showExportMenu by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Overview",
        "Message",
        "Assets (${currentItem.images.size})",
        "Actions (${currentItem.notification.actions.size})",
        "Payload (JSON)",
        "Extras (${currentItem.notification.styleExtras.size})",
        "Timeline (${group.updateCount})"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = group.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Step ${group.updateCount - selectedStepIndex} of ${group.updateCount} (${formatTime(currentItem.notification.postTime)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmarkGroup(context, group) }) {
                        Icon(
                            imageVector = if (group.isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Bookmark Stream",
                            tint = if (group.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Export Report")
                        }

                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export PDF Stream Report") },
                                leadingIcon = { Icon(Icons.Rounded.PictureAsPdf, null) },
                                onClick = {
                                    showExportMenu = false
                                    ReportExporter.exportGroupPdfReport(context, group)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Markdown Stream Report") },
                                leadingIcon = { Icon(Icons.Rounded.Code, null) },
                                onClick = {
                                    showExportMenu = false
                                    ReportExporter.exportGroupMarkdownReport(context, group)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(68.dp),
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val appIcon = currentItem.images["Small Icon"] ?: currentItem.images["Large Icon"]
                                    if (appIcon != null) {
                                        Image(
                                            bitmap = appIcon.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                                        )
                                    } else {
                                        Icon(
                                            Icons.Rounded.Apps,
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = group.appName.ifEmpty { group.packageName },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = group.packageName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailItem(Icons.Rounded.History, "Posted Time", formatTimeFull(currentItem.notification.postTime))
                            DetailItem(Icons.Rounded.Numbers, "Notification ID", currentItem.notification.id.toString())
                            DetailItem(Icons.Rounded.Style, "Template Style", currentItem.notification.templateStyle ?: "Standard")
                            DetailItem(Icons.Rounded.Layers, "Stream Updates", "${group.updateCount} Total Updates")
                        }
                    }
                }
            }

            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 2.dp
                ) {
                    SecondaryScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.background
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val notif = currentItem.notification
                            val infoList = listOf(
                                Icons.Rounded.Apps to ("App Name" to group.appName),
                                Icons.Rounded.Layers to ("Package Name" to notif.packageName),
                                Icons.Rounded.Numbers to ("Notification ID" to notif.id.toString()),
                                Icons.Rounded.History to ("Stream Updates" to "${group.updateCount} Steps"),
                                Icons.Rounded.CheckCircle to ("Ongoing Flag" to notif.isOngoing.toString()),
                                Icons.Rounded.Key to ("Step Unique Key" to notif.key),
                                Icons.Rounded.Description to ("Content Intent" to (notif.contentIntent ?: "None"))
                            )

                            InfoSectionTitle("System & Stream Diagnostics")
                            infoList.forEachIndexed { idx, (icon, pair) ->
                                GroupedDetailItem(icon, pair.first, pair.second, idx, infoList.size)
                            }
                        }
                    }
                }
                1 -> {
                    item {
                        val notif = currentItem.notification
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoSectionTitle("Notification Message")

                            GroupedSurfaceItem(index = 0, size = 1) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = notif.title.ifEmpty { "(No Title)" },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (notif.content.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = notif.content,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (notif.templateStyle != null) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ) {
                                                Text(notif.templateStyle, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        if (notif.isOngoing) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            ) {
                                                Text("Ongoing", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            InfoSectionTitle("Extracted Visual Assets (${currentItem.images.size})")

                            if (currentItem.images.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(currentItem.images.toList()) { (key, bitmap) ->
                                        val meta = currentItem.notification.resourceMeta[key]
                                        AssetCard(key, bitmap, meta, onClick = {
                                            fullScreenAsset = Pair(key, Pair(bitmap, meta))
                                        })
                                    }
                                }
                            } else {
                                EmptyState("No image assets extracted for this step")
                            }
                        }
                    }
                }
                3 -> {
                    val actions = currentItem.notification.actions
                    if (actions.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                InfoSectionTitle("Notification Actions (${actions.size})")
                                actions.forEachIndexed { index, action ->
                                    val iconBmp = action.iconKey?.let { currentItem.images[it] }
                                    GroupedSurfaceItem(index = index, size = actions.size) {
                                        ActionRow(action = action, icon = iconBmp)
                                    }
                                }
                            }
                        }
                    } else {
                        item { EmptyState("No action buttons on this notification step") }
                    }
                }
                4 -> {
                    val notif = currentItem.notification
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (notif.hyperJson != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    InfoSectionTitle("Xiaomi HyperIsland JSON Payload")
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("JSON", notif.hyperJson)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "JSON copied!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                JsonViewerCard(json = notif.hyperJson)
                            } else {
                                EmptyState("This notification step does not contain a Xiaomi HyperIsland payload.")
                            }
                        }
                    }
                }
                5 -> {
                    val styleExtras = currentItem.notification.styleExtras
                    if (styleExtras.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                InfoSectionTitle("Style Extras & Attributes")
                                styleExtras.toList().forEachIndexed { index, (k, v) ->
                                    GroupedDetailItem(
                                        icon = Icons.Rounded.Extension,
                                        label = k,
                                        value = v,
                                        index = index,
                                        size = styleExtras.size
                                    )
                                }
                            }
                        }
                    } else {
                        item { EmptyState("No style extras present") }
                    }
                }
                6 -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            InfoSectionTitle("Stream History (${group.updateCount} Updates)")
                            group.updates.forEachIndexed { index, stepItem ->
                                val isSelected = index == selectedStepIndex
                                val stepNum = group.updateCount - index
                                val stepTitle = stepItem.notification.title.ifEmpty { "Update $stepNum" }

                                GroupedSurfaceItem(
                                    index = index,
                                    size = group.updates.size,
                                    onClick = { selectedStepIndex = index }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "$stepNum",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stepTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = stepItem.notification.content.ifEmpty { "No text content" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = formatTime(stepItem.notification.postTime),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (fullScreenAsset != null) {
        val (assetKey, assetPair) = fullScreenAsset!!
        val (bitmap, meta) = assetPair

        AssetViewerDialog(
            key = assetKey,
            bitmap = bitmap,
            meta = meta,
            onDismiss = { fullScreenAsset = null }
        )
    }
}

private enum class AssetBgMode { DARK, LIGHT, CHECKERBOARD }

@Composable
private fun AssetViewerDialog(
    key: String,
    bitmap: Bitmap,
    meta: ResourceMeta?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var bgMode by remember { mutableStateOf(AssetBgMode.DARK) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when (bgMode) {
                        AssetBgMode.DARK -> Color(0xFF121212)
                        AssetBgMode.LIGHT -> Color(0xFFF5F5F5)
                        AssetBgMode.CHECKERBOARD -> Color.Transparent
                    }
                )
        ) {
            if (bgMode == AssetBgMode.CHECKERBOARD) {
                CheckerboardBackground(modifier = Modifier.fillMaxSize())
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 6f)
                            val maxOffset = 600f * scale
                            offset = Offset(
                                (offset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                (offset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (meta != null) "${meta.type} • ${meta.width}x${meta.height} (${meta.fileSize})" else "${bitmap.width}x${bitmap.height} px",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalIconButton(
                            onClick = { saveBitmapToGallery(context, bitmap, key) }
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = "Save Image")
                        }

                        FilledTonalIconButton(
                            onClick = onDismiss
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close")
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { scale = (scale - 0.5f).coerceAtLeast(0.5f) }
                    ) {
                        Icon(Icons.Rounded.ZoomOut, contentDescription = "Zoom Out")
                    }

                    Text(
                        text = "${(scale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = { scale = (scale + 0.5f).coerceAtMost(6f) }
                    ) {
                        Icon(Icons.Rounded.ZoomIn, contentDescription = "Zoom In")
                    }

                    IconButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        }
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = "Reset Zoom")
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    IconButton(
                        onClick = { bgMode = AssetBgMode.DARK }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DarkMode,
                            contentDescription = "Dark Background",
                            tint = if (bgMode == AssetBgMode.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { bgMode = AssetBgMode.LIGHT }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LightMode,
                            contentDescription = "Light Background",
                            tint = if (bgMode == AssetBgMode.LIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { bgMode = AssetBgMode.CHECKERBOARD }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GridOn,
                            contentDescription = "Checkerboard Background",
                            tint = if (bgMode == AssetBgMode.CHECKERBOARD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckerboardBackground(modifier: Modifier = Modifier, squareSize: Float = 24f) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val rows = (height / squareSize).toInt() + 1
        val cols = (width / squareSize).toInt() + 1

        val color1 = Color(0xFFCCCCCC)
        val color2 = Color(0xFFFFFFFF)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val color = if ((r + c) % 2 == 0) color1 else color2
                drawRect(
                    color = color,
                    topLeft = Offset(c * squareSize, r * squareSize),
                    size = Size(squareSize, squareSize)
                )
            }
        }
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String) {
    try {
        val fileName = "HyperIsland_${title.replace(Regex("[^a-zA-Z0-9.-]"), "_")}_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HyperIsland")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(context, "Saved to Pictures/HyperIsland", Toast.LENGTH_SHORT).show()
            }
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val hyperDir = File(picturesDir, "HyperIsland").apply { if (!exists()) mkdirs() }
            val file = File(hyperDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Toast.makeText(context, "Saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save asset", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SelectionContainer {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun GroupedDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    index: Int,
    size: Int,
    modifier: Modifier = Modifier
) {
    val shape = getGroupedShape(index, size)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            DetailItem(icon, label, value)
        }
    }
}

@Composable
fun GroupedSurfaceItem(
    index: Int,
    size: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = getGroupedShape(index, size)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        onClick = onClick ?: {}
    ) {
        content()
    }
}

private fun getGroupedShape(index: Int, size: Int): RoundedCornerShape {
    val cornerLarge = 24.dp
    val cornerSmall = 4.dp
    return when {
        size == 1 -> RoundedCornerShape(cornerLarge)
        index == 0 -> RoundedCornerShape(topStart = cornerLarge, topEnd = cornerLarge, bottomStart = cornerSmall, bottomEnd = cornerSmall)
        index == size - 1 -> RoundedCornerShape(topStart = cornerSmall, topEnd = cornerSmall, bottomStart = cornerLarge, bottomEnd = cornerLarge)
        else -> RoundedCornerShape(cornerSmall)
    }
}

@Composable
fun InfoSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(end = 24.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AssetCard(key: String, bitmap: Bitmap, meta: ResourceMeta?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier
            .width(140.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(0xFF222222)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(key, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

            if (meta != null) {
                Text(meta.type, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(meta.source, fontSize = 9.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${meta.width}x${meta.height} (${meta.fileSize})", fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun ActionRow(action: InspectedAction, icon: Bitmap?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(bitmap = icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(28.dp))
        } else {
            Box(
                Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(action.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(action.intentDescription ?: "No Intent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun JsonViewerCard(json: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28)),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        SelectionContainer {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Text(
                    text = highlightJson(json),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

private fun highlightJson(json: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = json.lines()
        lines.forEachIndexed { index, line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                withStyle(SpanStyle(color = Color(0xFFE5C07B))) {
                    append(parts[0])
                }
                append(":")
                val value = parts[1]
                val color = when {
                    value.contains("\"") -> Color(0xFF98C379)
                    value.contains("true") || value.contains("false") -> Color(0xFFD19A66)
                    value.trim().all { it.isDigit() || it == '.' } -> Color(0xFF61AFEF)
                    else -> Color(0xFFAABBCC)
                }
                withStyle(SpanStyle(color = color)) {
                    append(value)
                }
            } else {
                withStyle(SpanStyle(color = Color(0xFFAABBCC))) {
                    append(line)
                }
            }
            if (index < lines.size - 1) append("\n")
        }
    }
}

private fun formatTime(time: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
private fun formatTimeFull(time: Long): String = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(time))
