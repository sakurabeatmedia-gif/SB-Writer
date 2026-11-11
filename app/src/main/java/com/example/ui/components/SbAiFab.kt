package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.SbCrimson
import kotlin.math.roundToInt

@Composable
fun FloatingSbAiButton(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val initialX by viewModel.aiIconX.collectAsState()
    val initialY by viewModel.aiIconY.collectAsState()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val iconSizePx = with(density) { 62.dp.toPx() }

    // Posisi offset saat ini
    var offsetX by remember(initialX) {
        mutableFloatStateOf(
            if (initialX <= 0f) screenWidthPx - iconSizePx - with(density) { 16.dp.toPx() } else initialX
        )
    }
    var offsetY by remember(initialY) {
        mutableFloatStateOf(
            if (initialY <= 0f) screenHeightPx * 0.7f else initialY
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        viewModel.updateAiIconPosition(offsetX, offsetY)
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val newX = (offsetX + dragAmount.x).coerceIn(0f, screenWidthPx - iconSizePx)
                    val newY = (offsetY + dragAmount.y).coerceIn(with(density) { 60.dp.toPx() }, screenHeightPx - iconSizePx - with(density) { 70.dp.toPx() })
                    offsetX = newX
                    offsetY = newY
                }
            }
    ) {
        Surface(
            onClick = { viewModel.openAiSheet() },
            modifier = Modifier
                .size(62.dp)
                .scale(pulseScale)
                .shadow(12.dp, CircleShape)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(SbCrimson, Color.White, SbCrimson)
                    ),
                    shape = CircleShape
                )
                .testTag("floating_sb_ai_button"),
            shape = CircleShape,
            color = Color.Black
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_sb_logo),
                    contentDescription = "SB_AI Assistant Logo",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                )

                // Badge kecil "AI"
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .background(SbCrimson, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "SB_AI",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
