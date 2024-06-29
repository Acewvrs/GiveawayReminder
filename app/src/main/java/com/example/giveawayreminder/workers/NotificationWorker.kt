package com.example.giveawayreminder.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.work.WorkerParameters
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import com.example.giveawayreminder.GameApplication.Companion.dataStore
import com.example.giveawayreminder.MainActivity
import com.example.giveawayreminder.NOTIFICATION_TITLE
import com.example.giveawayreminder.R
import com.example.giveawayreminder.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
import com.example.giveawayreminder.VERBOSE_NOTIFICATION_CHANNEL_NAME
import com.example.giveawayreminder.data.PreferenceKeys.IS_LIST_UPDATED
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val TAG = "RetrieverWorker"

class NotificationWorker(private val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        // send notification only if we have permission
        val permission = Manifest.permission.POST_NOTIFICATIONS

        val listUpdated = isListUpdated(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && listUpdated) {
            val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                NotificationHandler.createReminderNotification(context)
            }
        }
        return Result.success()
    }

    private fun isListUpdated(context: Context): Boolean {
        return runBlocking {
            val preferences = context.dataStore.data.first()
            preferences[IS_LIST_UPDATED] ?: false
        }
    }
}

object NotificationHandler {
    private const val CHANNEL_ID = "transactions_reminder_channel"
    fun createReminderNotification(context: Context) {
        //  No back-stack when launched
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT)

        createNotificationChannel(context) //safe to call

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // For launching the MainActivity
            .setAutoCancel(true) // Remove notification when tapped
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(1, builder.build())
        }
    }

    /**
     * Required on Android O+
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
            val descriptionText = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}




