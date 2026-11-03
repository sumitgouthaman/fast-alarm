package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomIntervalDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (durationMinutes: Float, label: String, saveAsPreset: Boolean) -> Unit
) {
    var keypadDigits by remember { mutableStateOf("") }
    val (durationMinutesInt, durationLabel) = remember(keypadDigits) {
        parseDigitsToDuration(keypadDigits)
    }
    var labelText by remember { mutableStateOf("") }
    var saveAsPreset by remember { mutableStateOf(false) }

    val totalMinutes = durationMinutesInt
    val targetTimeMillis = System.currentTimeMillis() + (totalMinutes * 60 * 1000L)
    val targetTimeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(targetTimeMillis))

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = SlateDark,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, SlateBorder, RoundedCornerShape(26.dp))
                .testTag("custom_interval_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Timer Duration",
                    color = FrostWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (totalMinutes > 0) "Will ring at $targetTimeFormatted ($durationLabel)" else "Enter duration on dial below",
                    color = if (totalMinutes > 0) NeonCyan else TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Timer Number Keypad Grid
                TimerKeypadPicker(
                    digits = keypadDigits,
                    onDigitsChange = { newDigits ->
                        keypadDigits = newDigits
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick presets
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "15m" to "15",
                        "30m" to "30",
                        "45m" to "45",
                        "1h" to "100",
                        "1.5h" to "130",
                        "2h" to "200"
                    ).forEach { (chipLabel, code) ->
                        val isSelected = keypadDigits == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SlateElevated)
                                .border(1.dp, if (isSelected) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    keypadDigits = code
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipLabel,
                                color = if (isSelected) NeonCyan else FrostWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Label TextField
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("Alarm Label (Optional)", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = FrostWhite,
                        unfocusedTextColor = FrostWhite,
                        cursorColor = NeonCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Save as Preset Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { saveAsPreset = !saveAsPreset },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = saveAsPreset,
                        onCheckedChange = { saveAsPreset = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonCyan,
                            checkmarkColor = VoidBlack,
                            uncheckedColor = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add to Home Screen Shortcuts",
                        color = FrostWhite,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val finalLabel = if (labelText.isBlank()) "Interval ($durationLabel)" else labelText
                            onConfirm(totalMinutes.toFloat(), finalLabel, saveAsPreset)
                        },
                        enabled = totalMinutes > 0 && keypadDigits.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = VoidBlack,
                            disabledContainerColor = SlateElevated,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Arm Alarm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
