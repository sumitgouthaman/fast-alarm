package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlideToDismissBar(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val thumbSize = 56.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(SlateElevated)
            .testTag("slide_to_dismiss_bar"),
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val maxDragPx = with(density) { (maxWidth - thumbSize - 8.dp).toPx() }

        // Filled track background behind slider thumb
        val currentProgress = if (maxDragPx > 0) (dragOffset.value / maxDragPx).coerceIn(0f, 1f) else 0f
        val filledWidthDp = with(density) { (dragOffset.value + thumbSize.toPx() / 2).toDp() }

        if (dragOffset.value > 0f) {
            Box(
                modifier = Modifier
                    .width(filledWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.25f),
                                AmberWarm.copy(alpha = 0.45f)
                            )
                        )
                    )
            )
        }

        // Shimmering instruction text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (currentProgress > 0.6f) "RELEASE TO SILENCE" else "SLIDE TO DISMISS >>>",
                color = if (currentProgress > 0.6f) AmberWarm else Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 24.dp)
            )
        }

        // Draggable thumb knob
        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.value.roundToInt() + 4, 0) }
                .padding(vertical = 4.dp)
                .size(thumbSize)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            AmberWarm,
                            NeonCyan
                        )
                    )
                )
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val newOffset = (dragOffset.value + delta).coerceIn(0f, maxDragPx)
                            dragOffset.snapTo(newOffset)
                        }
                    },
                    onDragStopped = {
                        if (dragOffset.value >= maxDragPx * 0.82f) {
                            coroutineScope.launch {
                                dragOffset.animateTo(maxDragPx, tween(100))
                                onDismiss()
                            }
                        } else {
                            coroutineScope.launch {
                                dragOffset.animateTo(0f, tween(250))
                            }
                        }
                    }
                )
                .testTag("dismiss_slider_knob"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Slide thumb",
                tint = VoidBlack,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
