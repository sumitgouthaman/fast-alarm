package com.example.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alarm.AlarmRingingActivity
import com.example.ui.components.ActiveAlarmCard
import com.example.ui.components.AddPresetDialog
import com.example.ui.components.IntervalChip
import com.example.ui.components.ManagePresetsDialog
import com.example.ui.components.SpecificTimeAlarmDialog
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.VoidBlack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSetAlarmDialog by remember { mutableStateOf(false) }
    var showAddPresetDialog by remember { mutableStateOf(false) }
    var showManagePresetsDialog by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }

    // Android 13+ Notification permission request
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* state handled by system */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val currentTimeString = remember(uiState.currentTimeMillis) {
        SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(Date(uiState.currentTimeMillis))
    }
    val currentAmPm = remember(uiState.currentTimeMillis) {
        SimpleDateFormat("a", Locale.getDefault()).format(Date(uiState.currentTimeMillis))
    }
    val currentDateString = remember(uiState.currentTimeMillis) {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(uiState.currentTimeMillis))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VoidBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top App Bar & Bedside Clock Header
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FAST ALARM",
                                color = NeonCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    // Quick Test Alarm Button
                    OutlinedButton(
                        onClick = { viewModel.testAlarmNow() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SlateElevated,
                            contentColor = AmberWarm
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(SlateBorder, AmberWarm.copy(alpha = 0.5f)))
                        ),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("test_alarm_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Ring", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bedside Live Clock Card (clean layout without redundant bottom dialog box)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF131826), Color(0xFF0C101A))
                            )
                        )
                        .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentDateString.uppercase(),
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = currentTimeString,
                                color = FrostWhite,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = " $currentAmPm",
                                color = NeonCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }

            // Firing Alarm Persistent Banner (if an alarm is currently ringing)
            if (uiState.isRinging) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmberWarm)
                            .clickable {
                                val intent = Intent(context, AlarmRingingActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                            .padding(16.dp)
                            .testTag("ringing_alarm_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = VoidBlack)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ALARM IS RINGING!",
                                        color = VoidBlack,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = uiState.ringingAlarmLabel ?: "Tap here to slide and dismiss",
                                        color = Color(0xFF2A2000),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = "DISMISS >>>",
                                color = VoidBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Permission Warning Banner if exact alarms are disallowed
            if (!uiState.canScheduleExactAlarms && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SlateElevated)
                            .border(1.dp, AmberWarm.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Exact Alarm Permission",
                                    color = AmberWarm,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enable exact alarms so intervals trigger on the exact minute.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            TextButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Enable", color = NeonCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 1: Active Alarms List with Dedicated "+" Set Alarm Button
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ACTIVE ALARMS",
                            color = FrostWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (uiState.activeAlarms.isNotEmpty()) NeonCyan else SlateElevated)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${uiState.activeAlarms.size}",
                                color = if (uiState.activeAlarms.isNotEmpty()) VoidBlack else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Dedicated Plus Button for setting exact time or custom alarm
                    OutlinedButton(
                        onClick = { showSetAlarmDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SlateElevated,
                            contentColor = NeonCyan
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(SlateBorder, NeonCyan.copy(alpha = 0.5f)))
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_alarm_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Alarm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Empty state or list of active alarms
            if (uiState.activeAlarms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SlateDark)
                            .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
                            .padding(26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(SlateElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No active interval alarms",
                                color = FrostWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap any shortcut interval below or tap '+ Alarm' to set a specific time.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(uiState.activeAlarms, key = { it.id }) { alarm ->
                    ActiveAlarmCard(
                        alarm = alarm,
                        currentTimeMillis = uiState.currentTimeMillis,
                        onToggleEnable = { enabled ->
                            viewModel.toggleAlarmEnabled(alarm, enabled)
                        },
                        onDelete = {
                            viewModel.cancelAlarm(alarm)
                        }
                    )
                }
            }

            // Section 2: One-Tap Quick Intervals Shortcuts with 3-Dot Menu
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ONE-TAP INTERVALS",
                            color = FrostWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tap any preset to arm an instant alarm",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    // 3-dot overflow menu for managing and adding presets
                    Box {
                        IconButton(
                            onClick = { showPresetMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SlateElevated)
                                .testTag("preset_more_menu_button")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Preset options", tint = FrostWhite, modifier = Modifier.size(18.dp))
                        }

                        DropdownMenu(
                            expanded = showPresetMenu,
                            onDismissRequest = { showPresetMenu = false },
                            modifier = Modifier
                                .background(SlateDark)
                                .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Preset Interval", color = FrostWhite, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showPresetMenu = false
                                    showAddPresetDialog = true
                                },
                                modifier = Modifier.testTag("menu_add_preset")
                            )
                            DropdownMenuItem(
                                text = { Text("Edit / Remove Presets", color = FrostWhite, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = FrostWhite, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showPresetMenu = false
                                    showManagePresetsDialog = true
                                },
                                modifier = Modifier.testTag("menu_manage_presets")
                            )
                            DropdownMenuItem(
                                text = { Text("Reset to Defaults", color = FrostWhite, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null, tint = AmberWarm, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showPresetMenu = false
                                    viewModel.resetPresetsToDefault()
                                },
                                modifier = Modifier.testTag("menu_reset_presets")
                            )
                        }
                    }
                }
            }

            // Presets Grid (2 columns)
            items(uiState.presets.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { preset ->
                        IntervalChip(
                            preset = preset,
                            onClick = {
                                viewModel.setQuickIntervalAlarm(preset.durationMinutes, "Interval (${preset.label})")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        // Quick Add tile if odd number of presets
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(78.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SlateElevated)
                                .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
                                .clickable { showAddPresetDialog = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "+ Shortcut",
                                        color = FrostWhite,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = "Add preset", color = TextMuted, fontSize = 12.sp)
                                }
                                Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Specific Time & Duration Set Alarm Dialog (opened from "+" button in Active Alarms)
    if (showSetAlarmDialog) {
        SpecificTimeAlarmDialog(
            onDismissRequest = { showSetAlarmDialog = false },
            onConfirmExactTime = { triggerTimeMillis, label ->
                viewModel.setExactTimeAlarm(triggerTimeMillis, label)
                showSetAlarmDialog = false
            },
            onConfirmInterval = { durationMinutes, label ->
                viewModel.setQuickIntervalAlarm(durationMinutes, label)
                showSetAlarmDialog = false
            }
        )
    }

    // Add Preset Interval Dialog (opened from 3-dot menu in One-Tap Intervals)
    if (showAddPresetDialog) {
        AddPresetDialog(
            onDismissRequest = { showAddPresetDialog = false },
            onConfirmAdd = { label, durationMinutes ->
                viewModel.addPreset(label, durationMinutes)
                showAddPresetDialog = false
            }
        )
    }

    // Configure Presets Management Dialog
    if (showManagePresetsDialog) {
        ManagePresetsDialog(
            presets = uiState.presets,
            onAddPreset = { label, durationMinutes ->
                viewModel.addPreset(label, durationMinutes)
            },
            onDeletePreset = { id ->
                viewModel.deletePreset(id)
            },
            onResetDefaults = {
                viewModel.resetPresetsToDefault()
            },
            onDismissRequest = { showManagePresetsDialog = false }
        )
    }
}
