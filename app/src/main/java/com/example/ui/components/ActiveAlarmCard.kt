package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AlarmEntity
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VoidBlack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveAlarmCard(
    alarm: AlarmEntity,
    currentTimeMillis: Long,
    onToggleEnable: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingMillis = (alarm.triggerTimeMillis - currentTimeMillis).coerceAtLeast(0L)
    val isFiring = alarm.isFiring || (remainingMillis <= 0L && alarm.isEnabled && !alarm.isDismissed)

    val totalDurationMillis = (alarm.durationMinutes * 60 * 1000).toLong().coerceAtLeast(1000L)
    val elapsedMillis = totalDurationMillis - remainingMillis
    val progress = (elapsedMillis.toFloat() / totalDurationMillis).coerceIn(0f, 1f)

    val targetTimeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(alarm.triggerTimeMillis))

    val hoursRemaining = remainingMillis / (1000 * 60 * 60)
    val minutesRemaining = (remainingMillis / (1000 * 60)) % 60
    val secondsRemaining = (remainingMillis / 1000) % 60

    val countdownText = when {
        isFiring -> "RINGING NOW"
        hoursRemaining > 0 -> String.format(Locale.getDefault(), "%02d:%02d:%02d", hoursRemaining, minutesRemaining, secondsRemaining)
        else -> String.format(Locale.getDefault(), "%02d:%02d", minutesRemaining, secondsRemaining)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "firingGlow")
    val firingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "firingAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SlateDark)
            .border(
                width = if (isFiring) 2.dp else 1.dp,
                brush = if (isFiring) {
                    Brush.horizontalGradient(listOf(AmberWarm.copy(alpha = firingAlpha), CrimsonAlert))
                } else {
                    Brush.verticalGradient(listOf(SlateBorder, NeonCyan.copy(alpha = 0.2f)))
                },
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
            .testTag("active_alarm_card_${alarm.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Label & Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isFiring) AmberWarm.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFiring) Icons.Default.NotificationsActive else Icons.Default.Alarm,
                            contentDescription = null,
                            tint = if (isFiring) AmberWarm else NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = alarm.label,
                            color = FrostWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Target: $targetTimeString",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = onToggleEnable,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.35f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SlateElevated
                        ),
                        modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_alarm_${alarm.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel alarm",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Digital Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (isFiring) "STATUS" else "REMAINING",
                        color = if (isFiring) AmberWarm else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = countdownText,
                        color = if (isFiring) AmberWarm else NeonCyan,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (alarm.isEnabled) NeonCyan.copy(alpha = 0.12f) else SlateElevated)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (!alarm.isEnabled) "PAUSED" else if (isFiring) "RINGING" else "ARMED",
                        color = if (!alarm.isEnabled) TextMuted else if (isFiring) AmberWarm else NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { if (alarm.isEnabled) progress else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isFiring) AmberWarm else NeonCyan,
                trackColor = SlateElevated,
            )
        }
    }
}
