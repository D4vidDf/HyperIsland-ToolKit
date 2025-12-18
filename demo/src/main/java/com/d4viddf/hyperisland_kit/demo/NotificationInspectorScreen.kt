package com.d4viddf.hyperisland_kit.demo

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Data Wrappers ---
enum class InspectorFilter { LIVE_ALL, LIVE_HYPER, SAVED }

data class InspectorItem(
    val notification: InspectedNotification,
    val images: Map<String, Bitmap>,
    val isSaved: Boolean
)

// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationInspectorScreen() {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // -- State --
    var hasPermission by remember { mutableStateOf(checkPermission(context)) }
    var filterMode by remember { mutableStateOf(InspectorFilter.LIVE_HYPER) }
    var filterPkg by remember { mutableStateOf<String?>(null) } // null = All packages

    // -- Data Loading --
    val liveData by NotificationLogRepository.notifications.collectAsState()
    var savedData by remember { mutableStateOf<List<Pair<InspectedNotification, Map<String, Bitmap>>>>(emptyList()) }

    // Reload saved data when switching tabs
    LaunchedEffect(filterMode) {
        if (filterMode == InspectorFilter.SAVED) {
            savedData = NotificationStorage.loadAll(context)
        }
    }

    // Filter Logic
    val displayList = remember(liveData, savedData, filterMode, filterPkg) {
        val source = if (filterMode == InspectorFilter.SAVED) savedData else liveData

        source.map { InspectorItem(it.first, it.second, filterMode == InspectorFilter.SAVED) }
            .filter { item ->
                // 1. Filter by Mode (Live Hyper vs All)
                if (filterMode == InspectorFilter.LIVE_HYPER && item.notification.hyperJson == null) return@filter false

                // 2. Filter by Package
                if (filterPkg != null && item.notification.packageName != filterPkg) return@filter false

                true
            }
    }

    // -- Selection State --
    var selectedItem by remember { mutableStateOf<InspectorItem?>(null) }
    var fullScreenImage by remember { mutableStateOf<Bitmap?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Re-check permission on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) hasPermission = checkPermission(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Inspector", fontWeight = FontWeight.Bold) },
                    actions = {
                        if (filterPkg != null) {
                            IconButton(onClick = { filterPkg = null }) {
                                Icon(Icons.Default.FilterListOff, "Clear Filter")
                            }
                        }
                    }
                )
                // Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterMode == InspectorFilter.LIVE_ALL,
                            onClick = { filterMode = InspectorFilter.LIVE_ALL },
                            label = { Text("Live (All)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterMode == InspectorFilter.LIVE_HYPER,
                            onClick = { filterMode = InspectorFilter.LIVE_HYPER },
                            label = { Text("Live (Hyper)") },
                            leadingIcon = { Icon(Icons.Default.BugReport, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterMode == InspectorFilter.SAVED,
                            onClick = { filterMode = InspectorFilter.SAVED },
                            label = { Text("Saved") },
                            leadingIcon = { Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = Color.LightGray.copy(alpha = 0.3f)
                )
            }
        },
        floatingActionButton = {
            if (filterMode != InspectorFilter.SAVED && hasPermission) {
                FloatingActionButton(
                    onClick = { NotificationLogRepository.clear() },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(Icons.Default.DeleteSweep, "Clear Live")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Permission Warning
            if (!hasPermission && filterMode != InspectorFilter.SAVED) {
                PermissionRequestCard { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
            }

            if (displayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (hasPermission) "No notifications found" else "Waiting for permission...",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayList) { item ->
                        InspectorCard(
                            item = item,
                            onClick = { selectedItem = item },
                            onToggleBookmark = {
                                if (item.isSaved) {
                                    NotificationStorage.delete(context, item.notification.key)
                                    savedData = NotificationStorage.loadAll(context) // refresh saved list
                                } else {
                                    NotificationStorage.save(context, item.notification, item.images)
                                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- Bottom Sheet Details ---
    if (selectedItem != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedItem = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            InspectorDetailSheet(
                item = selectedItem!!,
                onImageClick = { fullScreenImage = it }
            )
        }
    }

    // --- Full Screen Image Overlay ---
    if (fullScreenImage != null) {
        Dialog(
            onDismissRequest = { fullScreenImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fullScreenImage = null }
            ) {
                Image(
                    bitmap = fullScreenImage!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullScreenImage = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }
            }
        }
    }
}

// --- Composable Components ---

@Composable
fun InspectorCard(
    item: InspectorItem,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val notif = item.notification
    val isHyper = notif.hyperJson != null

    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHyper) MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.15f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App Icon
                val appIcon = item.images["Small Icon"] ?: item.images["Large Icon"]
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = notif.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                Text(
                    text = formatTime(notif.postTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Bookmark Icon
                Icon(
                    imageVector = if (item.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (item.isSaved) MaterialTheme.colorScheme.primary else Color.LightGray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onToggleBookmark() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            Text(notif.title, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 16.sp)
            if (notif.content.isNotEmpty()) {
                Text(notif.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.Gray)
            }

            // Tags Row
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isHyper) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("HYPER", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (notif.templateStyle != null) {
                    Badge(containerColor = Color.LightGray.copy(alpha=0.5f), contentColor = Color.Black) {
                        Text(notif.templateStyle.replace("Style", ""), fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InspectorDetailSheet(item: InspectorItem, onImageClick: (Bitmap) -> Unit) {
    val notif = item.notification
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp) // Bottom padding for navigation bar
    ) {
        // --- Header ---
        Text(notif.packageName, fontSize = 12.sp, color = Color.Gray)
        Text("Notification Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        // --- 1. Visual Assets (Horizontal Scroll) ---
        if (item.images.isNotEmpty()) {
            SectionTitle("Visual Assets & Metadata")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(item.images.toList()) { (key, bmp) ->
                    val meta = notif.resourceMeta[key]
                    AssetCard(
                        key = key,
                        bitmap = bmp,
                        meta = meta,
                        onClick = { onImageClick(bmp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- 2. Action Buttons ---
        if (notif.actions.isNotEmpty()) {
            SectionTitle("Actions")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))) {
                Column {
                    notif.actions.forEachIndexed { index, action ->
                        ActionRow(action, item.images["Action $index Icon"])
                        if (index < notif.actions.size - 1) {
                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                color = Color.LightGray.copy(alpha=0.2f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- 3. Style Extras ---
        if (notif.styleExtras.isNotEmpty()) {
            SectionTitle("Style Extras (${notif.templateStyle ?: "Unknown"})")
            notif.styleExtras.forEach { (k, v) ->
                DetailRow(k, v)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- 4. HyperIsland JSON (The Core Request) ---
        if (notif.hyperJson != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                SectionTitle("HyperIsland Payload", modifier = Modifier.weight(1f))

                // Copy Button
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = android.content.ClipData.newPlainText("JSON", notif.hyperJson)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "JSON Copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, "Copy JSON", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Syntax Highlighted JSON Viewer
            JsonViewer(json = notif.hyperJson)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- 5. Raw Info ---
        SectionTitle("Intent Info")
        DetailRow("Content Intent", notif.contentIntent ?: "None")
        DetailRow("Post Time", formatTimeFull(notif.postTime))
        DetailRow("ID", notif.id.toString())
        DetailRow("Key", notif.key)
    }
}

// --- Sub-Components ---

@Composable
fun AssetCard(key: String, bitmap: Bitmap, meta: ResourceMeta?, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        // Checkerboard background for transparency
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFEEEEEE)) // Simple gray for now
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(key, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

        if (meta != null) {
            Text(meta.type, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(meta.source, fontSize = 9.sp, color = Color.Gray, maxLines = 2, lineHeight = 10.sp, overflow = TextOverflow.Ellipsis)
            Text("${meta.width}x${meta.height} (${meta.fileSize})", fontSize = 9.sp, color = Color.Gray)
        } else {
            Text("Metadata unavailable", fontSize = 9.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun ActionRow(action: InspectedAction, icon: Bitmap?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(bitmap = icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(24.dp))
        } else {
            Box(Modifier.size(24.dp).background(Color.Gray.copy(alpha=0.3f), CircleShape))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(action.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(action.intentDescription ?: "No Intent", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        SelectionContainer {
            Text(value, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
        HorizontalDivider(
            modifier = Modifier.padding(top=4.dp),
            thickness = DividerDefaults.Thickness,
            color = Color.LightGray.copy(alpha=0.2f)
        )
    }
}

/**
 * A syntax-highlighting JSON viewer.
 * Highlights Keys in Orange and Strings/Values in Green/Blue.
 */
@Composable
fun JsonViewer(json: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), // Dark Editor bg
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Horizontal scroll for long lines
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

fun highlightJson(json: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = json.lines()
        lines.forEachIndexed { index, line ->
            // Simple Regex for Key-Value pairs: "key": "value" or "key": number
            val parts = line.split(":", limit = 2)

            if (parts.size == 2) {
                // Formatting Key
                withStyle(SpanStyle(color = Color(0xFFCC7832))) { // Orange for Key
                    append(parts[0])
                }
                append(":")

                // Formatting Value
                val value = parts[1]
                val color = when {
                    value.contains("\"") -> Color(0xFF6A8759) // Green for Strings
                    value.contains("true") || value.contains("false") -> Color(0xFFCC7832) // Orange keywords
                    value.trim().all { it.isDigit() || it == '.' } -> Color(0xFF6897BB) // Blue for Numbers
                    else -> Color(0xFFA9B7C6) // Default grey
                }

                withStyle(SpanStyle(color = color)) {
                    append(value)
                }
            } else {
                // Brackets or plain text
                withStyle(SpanStyle(color = Color(0xFFA9B7C6))) {
                    append(line)
                }
            }
            if (index < lines.size - 1) append("\n")
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun PermissionRequestCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Permission Missing", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Text("Tap to grant Notification Access", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

// --- Helpers ---
private fun checkPermission(context: Context): Boolean {
    val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return listeners != null && listeners.contains(context.packageName)
}

private fun formatTime(time: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
private fun formatTimeFull(time: Long): String = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(Date(time))