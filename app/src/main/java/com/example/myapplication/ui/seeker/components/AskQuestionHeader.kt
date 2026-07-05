package com.example.myapplication.ui.seeker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors

@Composable
fun AskQuestionHeader(nickname: String, modifier: Modifier = Modifier, onMenuClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("✨", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$nickname，今天遇到什麼問題？",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextWhite
                )
            }
        }

        if (onMenuClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onMenuClick)
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.width(24.dp).height(2.dp).background(Color(0xFFCCCCCC), RoundedCornerShape(1.dp)))
                    Box(modifier = Modifier.width(24.dp).height(2.dp).background(Color(0xFFCCCCCC), RoundedCornerShape(1.dp)))
                }
            }
        }
    }
}
