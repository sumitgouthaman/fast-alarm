package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.data.IntervalPresetEntity
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VoidBlack

@Composable
fun ManagePresetsDialog(
    presets: List<IntervalPresetEntity>,
    onAddPreset: (label: String, durationMinutes: Float) -> Unit,
    onDeletePreset: (id: Int) -> Unit,
    onResetDefaults: () -> Unit,
    onDismissRequest: () -> Unit
) {
    var newLabel by remember { mutableStateOf("") }
    var newMinutesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                .testTag("manage_presets_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Configure Presets",
                    color = FrostWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize the quick interval buttons on your home screen",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // New preset entry form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        label = { Text("Label (e.g. 45m)", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("preset_label_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = newMinutesText,
                        onValueChange = { newMinutesText = it },
                        label = { Text("Minutes", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preset_minutes_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val mins = newMinutesText.toFloatOrNull()
                            if (newLabel.isBlank() || mins == null || mins <= 0) {
                                errorMessage = "Enter valid label & minutes"
                            } else {
                                onAddPreset(newLabel.trim(), mins)
                                newLabel = ""
                                newMinutesText = ""
                                errorMessage = null
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonCyan)
                            .testTag("add_preset_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add preset", tint = VoidBlack)
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = errorMessage!!, color = CrimsonAlert, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Presets List
                Text(
                    text = "ACTIVE PRESETS (${presets.size})",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(presets, key = { it.id }) { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateElevated)
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = preset.label,
                                    color = NeonCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${preset.durationMinutes} minutes",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(
                                onClick = { onDeletePreset(preset.id) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("delete_preset_${preset.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete ${preset.label}",
                                    tint = CrimsonAlert,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions row: Reset Defaults & Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onResetDefaults,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(SlateBorder)
                        ),
                        modifier = Modifier.testTag("reset_presets_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Defaults", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlateElevated,
                            contentColor = FrostWhite
                        ),
                        modifier = Modifier.testTag("presets_done_button")
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
