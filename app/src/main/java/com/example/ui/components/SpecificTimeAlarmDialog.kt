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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpecificTimeAlarmDialog(
    onDismissRequest: () -> Unit,
    onConfirmExactTime: (triggerTimeMillis: Long, label: String) -> Unit,
    onConfirmInterval: (durationMinutes: Float, label: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Specific Time, 1: Duration
    var labelText by remember { mutableStateOf("") }

    // Specific time picker state (defaults to 1 hour from now)
    val calendar = remember {
        Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    // Keypad duration state (digits like "45" or "130")
    var keypadDigits by remember { mutableStateOf("30") }
    val (totalDurationMinutes, durationLabel) = remember(keypadDigits) {
        parseDigitsToDuration(keypadDigits)
    }

    // Calculate exact target time for Specific Time tab
    val targetExactMillis = remember(timePickerState.hour, timePickerState.minute) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
        }
        target.timeInMillis
    }

    val remainingExactMillis = targetExactMillis - System.currentTimeMillis()
    val exactHoursLeft = remainingExactMillis / (1000 * 60 * 60)
    val exactMinsLeft = (remainingExactMillis / (1000 * 60)) % 60

    // Calculate target time for Duration tab
    val targetDurationMillis = System.currentTimeMillis() + (totalDurationMinutes * 60 * 1000L)
    val durationTargetFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(targetDurationMillis))

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SlateDark,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, SlateBorder, RoundedCornerShape(28.dp))
                .testTag("specific_time_alarm_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Tabs: Specific Time vs Timer Duration
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateElevated,
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Specific Time", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Duration / Timer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // TAB 0: Specific Clock Time
                    Text(
                        text = "Rings in ${exactHoursLeft}h ${exactMinsLeft}m",
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = SlateElevated,
                            clockDialSelectedContentColor = VoidBlack,
                            clockDialUnselectedContentColor = FrostWhite,
                            selectorColor = NeonCyan,
                            periodSelectorBorderColor = SlateBorder,
                            periodSelectorSelectedContainerColor = NeonCyan,
                            periodSelectorUnselectedContainerColor = SlateElevated,
                            periodSelectorSelectedContentColor = VoidBlack,
                            periodSelectorUnselectedContentColor = FrostWhite,
                            timeSelectorSelectedContainerColor = NeonCyan,
                            timeSelectorUnselectedContainerColor = SlateElevated,
                            timeSelectorSelectedContentColor = VoidBlack,
                            timeSelectorUnselectedContentColor = FrostWhite
                        ),
                        modifier = Modifier.testTag("alarm_time_picker")
                    )
                } else {
                    // TAB 1: Number Grid Keypad Duration Picker (Timer App style)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (totalDurationMinutes > 0) "Rings at $durationTargetFormatted (in $durationLabel)" else "Enter duration",
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Numeric dial grid
                        TimerKeypadPicker(
                            digits = keypadDigits,
                            onDigitsChange = { keypadDigits = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick duration chips to punch in instantly
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
                            ).forEach { (label, code) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (keypadDigits == code) NeonCyan.copy(alpha = 0.2f) else SlateElevated)
                                        .border(
                                            1.dp,
                                            if (keypadDigits == code) NeonCyan else SlateBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { keypadDigits = code }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (keypadDigits == code) NeonCyan else FrostWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Label field
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_label_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
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
                            if (selectedTab == 0) {
                                val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(targetExactMillis))
                                val finalLabel = if (labelText.isBlank()) "Alarm at $timeFormatted" else labelText
                                onConfirmExactTime(targetExactMillis, finalLabel)
                            } else {
                                val mins = totalDurationMinutes.coerceAtLeast(1)
                                val finalLabel = if (labelText.isBlank()) "Interval ($durationLabel)" else labelText
                                onConfirmInterval(mins.toFloat(), finalLabel)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = VoidBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_set_alarm_button")
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
