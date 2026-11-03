package com.example.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.QuickIntervalApp
import com.example.R
import com.example.data.AlarmEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpcomingAlarmsNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun updateUpcomingAlarmsNotification(activeAlarms: List<AlarmEntity>) {
        val now = System.currentTimeMillis()
        val upcomingAlarms = activeAlarms
            .filter { it.isEnabled && !it.isDismissed && it.triggerTimeMillis > now }
            .sortedBy { it.triggerTimeMillis }

        if (upcomingAlarms.isEmpty()) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        val firstAlarm = upcomingAlarms.first()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val firstAlarmTimeStr = timeFormat.format(Date(firstAlarm.triggerTimeMillis))
        val firstRemainingStr = formatRemaining(firstAlarm.triggerTimeMillis - now)

        val title = if (upcomingAlarms.size == 1) {
            "Next Alarm: $firstAlarmTimeStr (in $firstRemainingStr)"
        } else {
            "${upcomingAlarms.size} Active Alarms • Next in $firstRemainingStr"
        }

        val contentText = "$firstAlarmTimeStr • ${firstAlarm.label}"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.setBigContentTitle(title)
        for (alarm in upcomingAlarms.take(5)) {
            val formattedTime = timeFormat.format(Date(alarm.triggerTimeMillis))
            val remaining = formatRemaining(alarm.triggerTimeMillis - now)
            inboxStyle.addLine("⏰ $formattedTime — ${alarm.label} (in $remaining)")
        }
        if (upcomingAlarms.size > 5) {
            inboxStyle.addLine("+ ${upcomingAlarms.size - 5} more")
        }

        val notification = NotificationCompat.Builder(context, QuickIntervalApp.UPCOMING_ALARMS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setSubText("Fast Alarm")
            .setContentText(contentText)
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun formatRemaining(millis: Long): String {
        val totalSec = (millis / 1000).coerceAtLeast(0)
        val hours = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            mins > 0 -> "${mins}m"
            else -> "< 1m"
        }
    }

    companion object {
        const val NOTIFICATION_ID = 8001
        const val REQUEST_CODE_OPEN_APP = 8002
    }
}
