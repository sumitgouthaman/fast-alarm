package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.VoidBlack

/**
 * Google Clock Timer-style numeric dial keypad (1-9, 00, 0, Backspace).
 * Digits shift in from right to left into Hours and Minutes.
 */
@Composable
fun TimerKeypadPicker(
    digits: String,
    onDigitsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pad to 4 digits (HHMM)
    val padded = remember(digits) { digits.padStart(4, '0') }
    val hoursStr = padded.substring(0, 2)
    val minutesStr = padded.substring(2, 4)

    val hasHours = digits.length >= 3
    val hasMinutes = digits.isNotEmpty()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Digital Timer Display (like Google Clock Timer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SlateElevated)
                .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
                .padding(vertical = 14.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            // Hours
            Text(
                text = hoursStr,
                color = if (hasHours) NeonCyan else TextSubtle,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp
            )
            Text(
                text = "h ",
                color = if (hasHours) NeonCyan else TextSubtle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Minutes
            Text(
                text = minutesStr,
                color = if (hasMinutes) NeonCyan else TextSubtle,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp
            )
            Text(
                text = "m",
                color = if (hasMinutes) NeonCyan else TextSubtle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3x4 Dial Keypad Grid (1-9, 00, 0, Backspace)
        val buttonSize = 58.dp
        val spacing = 12.dp

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: 1, 2, 3
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                KeypadButton("1", buttonSize) { appendDigit("1", digits, onDigitsChange) }
                KeypadButton("2", buttonSize) { appendDigit("2", digits, onDigitsChange) }
                KeypadButton("3", buttonSize) { appendDigit("3", digits, onDigitsChange) }
            }

            // Row 2: 4, 5, 6
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                KeypadButton("4", buttonSize) { appendDigit("4", digits, onDigitsChange) }
                KeypadButton("5", buttonSize) { appendDigit("5", digits, onDigitsChange) }
                KeypadButton("6", buttonSize) { appendDigit("6", digits, onDigitsChange) }
            }

            // Row 3: 7, 8, 9
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                KeypadButton("7", buttonSize) { appendDigit("7", digits, onDigitsChange) }
                KeypadButton("8", buttonSize) { appendDigit("8", digits, onDigitsChange) }
                KeypadButton("9", buttonSize) { appendDigit("9", digits, onDigitsChange) }
            }

            // Row 4: 00, 0, Backspace
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                KeypadButton("00", buttonSize) {
                    if (digits.isNotEmpty() && digits.length <= 2) {
                        onDigitsChange(digits + "00")
                    }
                }
                KeypadButton("0", buttonSize) {
                    if (digits.isNotEmpty()) {
                        appendDigit("0", digits, onDigitsChange)
                    }
                }
                KeypadIconButton(buttonSize) {
                    if (digits.isNotEmpty()) {
                        onDigitsChange(digits.dropLast(1))
                    }
                }
            }
        }
    }
}

private fun appendDigit(d: String, current: String, onDigitsChange: (String) -> Unit) {
    if (current.length < 4) {
        // Prevent leading zeros when empty
        if (current.isEmpty() && d == "0") return
        onDigitsChange(current + d)
    }
}

@Composable
private fun KeypadButton(
    text: String,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(SlateElevated)
            .border(1.dp, SlateBorder, CircleShape)
            .clickable(onClick = onClick)
            .testTag("keypad_button_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = FrostWhite,
            fontSize = if (text == "00") 20.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun KeypadIconButton(
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(SlateElevated)
            .border(1.dp, SlateBorder, CircleShape)
            .clickable(onClick = onClick)
            .testTag("keypad_backspace"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            tint = FrostWhite,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Computes total minutes and formatted label from keypad input digits.
 */
fun parseDigitsToDuration(digits: String): Pair<Int, String> {
    if (digits.isBlank()) return 0 to "0m"
    val padded = digits.padStart(4, '0')
    val hours = padded.substring(0, 2).toInt()
    val mins = padded.substring(2, 4).toInt()
    val totalMinutes = hours * 60 + mins

    val label = when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
    return totalMinutes to label
}
