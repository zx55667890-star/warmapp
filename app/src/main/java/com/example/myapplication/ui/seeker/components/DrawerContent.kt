package com.example.myapplication.ui.seeker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.myapplication.ui.theme.AppColors

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.SurfaceDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; onSearch(it) },
            placeholder = {
                Text("搜尋對話…", color = AppColors.TextMuted, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = AppColors.TextMuted
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.AccentGreen.copy(alpha = 0.3f),
                unfocusedBorderColor = AppColors.BorderGray.copy(alpha = 0.5f),
                focusedContainerColor = AppColors.SurfaceMedium,
                unfocusedContainerColor = AppColors.SurfaceMedium,
                cursorColor = AppColors.AccentGreen,
                focusedTextColor = AppColors.TextWhite,
                unfocusedTextColor = AppColors.TextWhite
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        HorizontalDivider(
            color = AppColors.BorderGray.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSettingsClick() }
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
                        .background(AppColors.AccentBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        nickname.take(1),
                        color = AppColors.DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = nickname,
                color = AppColors.TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Settings",
                tint = AppColors.TextGray
            )
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun HistoryGroupHeader(title: String) {
    Text(
        text = title,
        color = AppColors.TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp, start = 8.dp)
    )
}

@Composable
private fun HistoryItem(text: String, onClick: (String) -> Unit) {
    Text(
        text = text,
        color = AppColors.TextGray,
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