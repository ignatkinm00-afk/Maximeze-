package com.maximeze.browser.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximeze.browser.data.model.Tab
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabGridScreen(
    tabs: List<Tab>,
    onTabSelect: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onNewTab: () -> Unit,
    onDismiss: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Вкладки",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                        )
                        Text(
                            "${tabs.size} открыто",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Done, contentDescription = "Готово")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onNewTab()
                    onDismiss()
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Новая вкладка") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(tabs, key = { it.id }) { tab ->
                TabCard(
                    tab = tab,
                    onSelect = {
                        onTabSelect(tab.id)
                        onDismiss()
                    },
                    onClose = { onTabClose(tab.id) },
                )
            }
        }
    }
}

@Composable
private fun TabCard(
    tab: Tab,
    onSelect: () -> Unit,
    onClose: () -> Unit,
) {
    val isActive = tab.isActive
    val isIncognito = tab.isIncognito

    val borderStroke = if (isActive) {
        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clickable(onClick = onSelect),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 6.dp else 2.dp,
        ),
        shape = RoundedCornerShape(16.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(
                        if (isIncognito)
                            Brush.horizontalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF2D1B69)))
                        else if (isActive)
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer,
                                )
                            )
                        else
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                )
                            )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isIncognito) {
                        Icon(
                            Icons.Default.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFA78BFA),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = tab.title.ifBlank { if (isIncognito) "Инкогнито" else "Новая вкладка" },
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isIncognito) Color.White
                                else if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Закрыть",
                            modifier = Modifier.size(12.dp),
                            tint = if (isIncognito) Color.White.copy(alpha = 0.7f)
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Preview area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isIncognito) Color(0xFF0F0F1A)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(12.dp),
                ) {
                    // Favicon placeholder — big letter or incognito icon
                    val displayChar = getDisplayChar(tab.url, tab.title)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isIncognito) Color(0xFF2D1B69)
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isIncognito) {
                            Icon(
                                Icons.Default.DarkMode,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFA78BFA),
                            )
                        } else {
                            Text(
                                text = displayChar,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }

                    val displayUrl = getDisplayHost(tab.url)
                    if (displayUrl.isNotBlank()) {
                        Text(
                            text = displayUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isIncognito) Color.White.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp,
                        )
                    }
                }

                // Active badge
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

private fun getDisplayChar(url: String, title: String): String {
    val host = try { URI(url).host?.removePrefix("www.") } catch (_: Exception) { null }
    val source = host ?: title.ifBlank { url }
    return source.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

private fun getDisplayHost(url: String): String {
    if (url.isBlank() || url == "about:blank") return ""
    return try {
        URI(url).host?.removePrefix("www.") ?: url
    } catch (_: Exception) {
        url
    }
}
