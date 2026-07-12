package com.example.myapplication.ui.seeker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun DrawerContent(
    nickname: String,
    onSearch: (String) -> Unit,
    onHistoryItemClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val drawerBackground = Color(0xFF171717) // 契合 ChatGPT 風格的優雅深灰
    val textColor = Color(0xFFECECEC)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(290.dp) // 稍微縮減寬度，讓右側主畫面露出的比例更好看
            .background(drawerBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())
        Spacer(modifier = Modifier.height(12.dp))

        // 💡 修正 1：改用 OutlinedTextFieldDefaults.colors 以及正確的 containerColor 參數
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; onSearch(it) },
            placeholder = { Text("搜尋對話...", color = Color(0xFF7D7D7D), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF7D7D7D)) },
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFF242424),
                unfocusedContainerColor = Color(0xFF242424),
                cursorColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 歷史對話紀錄
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item { HistoryGroupHeader("今天") }
            items(2) { HistoryItem("這是一個今天的對話標題", onHistoryItemClick) }

            item { HistoryGroupHeader("7 天內") }
            items(3) { HistoryItem("關於 Jetpack Compose 的動畫問題", onHistoryItemClick) }

            item { HistoryGroupHeader("30 天內") }
            items(4) { HistoryItem("Kotlin 協程的運用與實踐", onHistoryItemClick) }
        }

        // 底部個人資料列
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSettingsClick() }
                // 💡 修正 2：將 symmetric 改回正確的 horizontal
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(36.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(nickname.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = nickname,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "Settings",
                tint = Color(0xFF8E8E93)
            )
        }
        Spacer(modifier = Modifier.navigationBarsPadding()) // 確保相容底部虛擬導覽列
    }
}

@Composable
private fun HistoryGroupHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF666666),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp, start = 8.dp)
    )
}

@Composable
private fun HistoryItem(text: String, onClick: (String) -> Unit) {
    Text(
        text = text,
        color = Color(0xFFC5C5C5),
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(text) }
            .padding(horizontal = 8.dp, vertical = 10.dp)
    )
}