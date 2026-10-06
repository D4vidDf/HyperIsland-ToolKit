package com.d4viddf.hyperisland_kit.demo.ui.screens.demos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.d4viddf.hyperisland_kit.demo.data.model.DemoCategory
import com.d4viddf.hyperisland_kit.demo.ui.components.ExpressiveDemoCard
import com.d4viddf.hyperisland_kit.demo.ui.components.ExpressiveHeader
import com.d4viddf.hyperisland_kit.demo.ui.components.ExpressiveSearchBar
import com.d4viddf.hyperisland_kit.demo.utils.CheckPermissionLost
import com.d4viddf.hyperisland_kit.demo.utils.DemoNotificationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoListScreen(
    navController: NavController,
    viewModel: DemoListViewModel = viewModel()
) {
    CheckPermissionLost(navController = navController)

    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredDemos by viewModel.filteredDemos.collectAsState()
    val configurableOptions by viewModel.configurableOptions.collectAsState()

    var showConfigPanel by remember { mutableStateOf(false) }

    val categories = DemoCategory.entries.toList()
    val selectedCategoryIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                ExpressiveHeader(
                    title = "Demos",
                    subtitle = "Test Xiaomi HyperIsland Notifications & Templates",
                    badgeText = "${filteredDemos.size} Items"
                )

                Spacer(modifier = Modifier.height(14.dp))

                ExpressiveSearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholderText = "Search by name or feature..."
                )
            }
        }

        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 0.dp
            ) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    edgePadding = 20.dp,
                    containerColor = MaterialTheme.colorScheme.background,
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)) }
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedCategoryIndex == index,
                            onClick = { viewModel.onCategorySelect(category) },
                            text = {
                                Text(
                                    text = category.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Configurable Playground",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { showConfigPanel = !showConfigPanel }) {
                                Icon(
                                    imageVector = if (showConfigPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle"
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showConfigPanel,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                Text(
                                    text = "Timeout: ${configurableOptions.timeoutSeconds.toInt()}s",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Slider(
                                    value = configurableOptions.timeoutSeconds,
                                    onValueChange = {
                                        viewModel.updateConfigurableOptions(
                                            it,
                                            configurableOptions.enableFloat,
                                            configurableOptions.showInShade
                                        )
                                    },
                                    valueRange = 0f..60f,
                                    steps = 59
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Enable Floating Island",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = configurableOptions.enableFloat,
                                        onCheckedChange = {
                                            viewModel.updateConfigurableOptions(
                                                configurableOptions.timeoutSeconds,
                                                it,
                                                configurableOptions.showInShade
                                            )
                                        }
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Show in Notification Shade",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = configurableOptions.showInShade,
                                        onCheckedChange = {
                                            viewModel.updateConfigurableOptions(
                                                configurableOptions.timeoutSeconds,
                                                configurableOptions.enableFloat,
                                                it
                                            )
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        DemoNotificationManager.showConfigurableNotification(
                                            context,
                                            (configurableOptions.timeoutSeconds * 1000).toLong(),
                                            configurableOptions.enableFloat,
                                            configurableOptions.showInShade
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Send Configured Notification",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filteredDemos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No demos found for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredDemos, key = { it.id }) { demo ->
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    ExpressiveDemoCard(
                        item = demo,
                        onClick = { demo.action(context) }
                    )
                }
            }
        }
    }
}
