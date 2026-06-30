package com.maximeze.browser.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximeze.browser.ui.theme.MaximizeViolet
import java.util.Calendar

private data class QuickLink(val emoji: String, val title: String, val url: String)

private val QUICK_LINKS = listOf(
    QuickLink("▶️", "YouTube", "https://youtube.com"),
    QuickLink("💙", "ВКонтакте", "https://vk.com"),
    QuickLink("✈️", "Telegram", "https://web.telegram.org"),
    QuickLink("🐙", "GitHub", "https://github.com"),
    QuickLink("📸", "Instagram", "https://instagram.com"),
    QuickLink("📖", "Вики", "https://ru.wikipedia.org"),
    QuickLink("🎵", "TikTok", "https://tiktok.com"),
    QuickLink("🐦", "Twitter", "https://twitter.com"),
)

@Composable
fun HomeScreen(
    onSearch: (String) -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val greeting = remember { getGreeting() }
    val time = remember { getCurrentTime() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0935),
                        Color(0xFF0D0914),
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 60.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Clock
            Text(
                text = time,
                fontSize = 72.sp,
                fontWeight = FontWeight.Thin,
                color = Color.White,
                letterSpacing = (-2).sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Greeting
            Text(
                text = greeting,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Light,
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Search bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f),
                tonalElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                "Поиск или адрес сайта",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 15.sp,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (query.isNotBlank()) {
                                if (query.contains(".") && !query.contains(" ")) onNavigate("https://$query")
                                else onSearch(query)
                                focusManager.clearFocus()
                            }
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = MaximizeViolet,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick links label
            Text(
                text = "Быстрые ссылки",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick links grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(QUICK_LINKS) { link ->
                    QuickLinkItem(link = link, onClick = { onNavigate(link.url) })
                }
            }
        }
    }
}

@Composable
private fun QuickLinkItem(link: QuickLink, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(link.emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = link.title,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 6 -> "Доброй ночи"
        hour < 12 -> "Доброе утро"
        hour < 18 -> "Добрый день"
        else -> "Добрый вечер"
    }
}

private fun getCurrentTime(): String {
    val cal = Calendar.getInstance()
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    return "%02d:%02d".format(h, m)
}
