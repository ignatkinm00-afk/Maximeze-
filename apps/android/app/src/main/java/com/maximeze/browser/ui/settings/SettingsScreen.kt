package com.maximeze.browser.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximeze.browser.data.model.SearchEngine
import com.maximeze.browser.ui.BrowserSettings
import com.maximeze.browser.ui.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: BrowserSettings,
    onUpdateSettings: (BrowserSettings) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showSearchEngineDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {

            // ── Внешний вид ──────────────────────────────────────────────
            item { SectionHeader(title = "Внешний вид", icon = Icons.Default.Palette) }

            item {
                SettingsClickItem(
                    title = "Тема",
                    subtitle = when (settings.theme) {
                        ThemeMode.SYSTEM -> "Системная"
                        ThemeMode.LIGHT -> "Светлая"
                        ThemeMode.DARK -> "Тёмная"
                    },
                    icon = Icons.Default.DarkMode,
                    onClick = { showThemeDialog = true },
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Размер текста", style = MaterialTheme.typography.bodyMedium)
                            Text("${(settings.fontSize * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Slider(
                        value = settings.fontSize,
                        onValueChange = { onUpdateSettings(settings.copy(fontSize = it)) },
                        valueRange = 0.75f..2.0f,
                        steps = 4,
                        modifier = Modifier.padding(start = 36.dp),
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // ── Поиск ────────────────────────────────────────────────────
            item { SectionHeader(title = "Поиск", icon = Icons.Default.Search) }

            item {
                SettingsClickItem(
                    title = "Поисковая система",
                    subtitle = when (settings.searchEngine) {
                        SearchEngine.GOOGLE -> "Google"
                        SearchEngine.BING -> "Bing"
                        SearchEngine.DUCKDUCKGO -> "DuckDuckGo"
                    },
                    icon = Icons.Default.ManageSearch,
                    onClick = { showSearchEngineDialog = true },
                )
            }

            item {
                SettingsToggleItem(
                    title = "Поисковые подсказки",
                    subtitle = "Показывать предложения при вводе",
                    icon = Icons.Default.Lightbulb,
                    checked = settings.showSearchSuggestions,
                    onCheckedChange = { onUpdateSettings(settings.copy(showSearchSuggestions = it)) },
                )
            }

            item {
                SettingsToggleItem(
                    title = "Безопасный поиск",
                    subtitle = "Фильтровать результаты для взрослых",
                    icon = Icons.Default.Shield,
                    checked = settings.safeSearch,
                    onCheckedChange = { onUpdateSettings(settings.copy(safeSearch = it)) },
                )
            }

            // ── Конфиденциальность ───────────────────────────────────────
            item { SectionHeader(title = "Конфиденциальность", icon = Icons.Default.PrivacyTip) }

            item {
                SettingsToggleItem(
                    title = "Блокировать рекламу",
                    subtitle = "Убирать рекламные блоки на страницах",
                    icon = Icons.Default.Block,
                    checked = settings.blockAds,
                    onCheckedChange = { onUpdateSettings(settings.copy(blockAds = it)) },
                )
            }

            item {
                SettingsToggleItem(
                    title = "Блокировать трекеры",
                    subtitle = "Защита от слежки сторонних сервисов",
                    icon = Icons.Default.TrackChanges,
                    checked = settings.blockTrackers,
                    onCheckedChange = { onUpdateSettings(settings.copy(blockTrackers = it)) },
                )
            }

            item {
                SettingsToggleItem(
                    title = "Не отслеживать (DNT)",
                    subtitle = "Отправлять запрос об отказе от слежки",
                    icon = Icons.Default.VisibilityOff,
                    checked = settings.doNotTrack,
                    onCheckedChange = { onUpdateSettings(settings.copy(doNotTrack = it)) },
                )
            }

            item {
                SettingsToggleItem(
                    title = "Принудительный HTTPS",
                    subtitle = "Переключать сайты на безопасное соединение",
                    icon = Icons.Default.Lock,
                    checked = settings.httpsOnly,
                    onCheckedChange = { onUpdateSettings(settings.copy(httpsOnly = it)) },
                )
            }

            // ── Содержимое страниц ───────────────────────────────────────
            item { SectionHeader(title = "Содержимое страниц", icon = Icons.Default.Code) }

            item {
                SettingsToggleItem(
                    title = "JavaScript",
                    subtitle = "Разрешить сайтам выполнять скрипты",
                    icon = Icons.Default.Javascript,
                    checked = settings.javaScriptEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(javaScriptEnabled = it)) },
                )
            }

            item {
                SettingsToggleItem(
                    title = "Куки",
                    subtitle = "Разрешить сайтам сохранять куки",
                    icon = Icons.Default.Cookie,
                    checked = settings.cookiesEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(cookiesEnabled = it)) },
                )
            }

            // ── Загрузки ─────────────────────────────────────────────────
            item { SectionHeader(title = "Загрузки", icon = Icons.Default.Download) }

            item {
                SettingsToggleItem(
                    title = "Спрашивать место сохранения",
                    subtitle = "Запрашивать папку для каждой загрузки",
                    icon = Icons.Default.FolderOpen,
                    checked = settings.askDownloadLocation,
                    onCheckedChange = { onUpdateSettings(settings.copy(askDownloadLocation = it)) },
                )
            }

            // ── Дополнительно ────────────────────────────────────────────
            item { SectionHeader(title = "Дополнительно", icon = Icons.Default.Settings) }

            item {
                SettingsToggleItem(
                    title = "Открывать ссылки в фоне",
                    subtitle = "Не переключаться при открытии новых ссылок",
                    icon = Icons.Default.OpenInNew,
                    checked = settings.openLinksInBackground,
                    onCheckedChange = { onUpdateSettings(settings.copy(openLinksInBackground = it)) },
                )
            }

            item {
                SettingsToggleItem(
                    title = "Очищать данные при выходе",
                    subtitle = "Удалять историю и куки при закрытии",
                    icon = Icons.Default.CleaningServices,
                    checked = settings.clearDataOnExit,
                    onCheckedChange = { onUpdateSettings(settings.copy(clearDataOnExit = it)) },
                )
            }

            // ── Данные браузера ──────────────────────────────────────────
            item { SectionHeader(title = "Данные браузера", icon = Icons.Default.DeleteSweep) }

            item {
                ListItem(
                    headlineContent = { Text("Очистить историю", color = MaterialTheme.colorScheme.error) },
                    supportingContent = { Text("Удалить все посещённые страницы") },
                    leadingContent = {
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.clickable { showClearDataDialog = true },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // ── О браузере ───────────────────────────────────────────────
            item { SectionHeader(title = "О браузере", icon = Icons.Default.Info) }

            item {
                ListItem(
                    headlineContent = { Text("Maximeze Browser") },
                    supportingContent = { Text("Версия 0.1.0 — Открытый исходный код") },
                    leadingContent = {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // ── Диалоги ──────────────────────────────────────────────────────────

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Тема оформления") },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateSettings(settings.copy(theme = mode))
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.theme == mode,
                                onClick = {
                                    onUpdateSettings(settings.copy(theme = mode))
                                    showThemeDialog = false
                                },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(when (mode) {
                                ThemeMode.SYSTEM -> "Системная"
                                ThemeMode.LIGHT -> "Светлая"
                                ThemeMode.DARK -> "Тёмная"
                            })
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Отмена") }
            },
        )
    }

    if (showSearchEngineDialog) {
        AlertDialog(
            onDismissRequest = { showSearchEngineDialog = false },
            title = { Text("Поисковая система") },
            text = {
                Column {
                    SearchEngine.entries.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateSettings(settings.copy(searchEngine = engine))
                                    showSearchEngineDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.searchEngine == engine,
                                onClick = {
                                    onUpdateSettings(settings.copy(searchEngine = engine))
                                    showSearchEngineDialog = false
                                },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(engine.displayName)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSearchEngineDialog = false }) { Text("Отмена") }
            },
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Очистить данные?") },
            text = { Text("История посещений будет удалена безвозвратно.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDataDialog = false
                    }
                ) {
                    Text("Очистить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) },
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun SettingsClickItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
