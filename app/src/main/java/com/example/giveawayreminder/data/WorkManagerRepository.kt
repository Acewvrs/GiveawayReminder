package com.example.giveawayreminder.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.giveawayreminder.LIST_UPDATE_WORK_NAME
import com.example.giveawayreminder.NOTIFICATION_WORK_TAG
import com.example.giveawayreminder.REMINDER_NOTIFICATION_WORK_NAME
import com.example.giveawayreminder.TAG_OUTPUT
import com.example.giveawayreminder.workers.NotificationWorker
import com.example.giveawayreminder.workers.RetrieverWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

interface WorkerRepository {
    fun sendNotification() // testing only
    fun retrieveGameData() // testing only
    fun setNotificationInterval(context: Context, repeatIntervalHour: Long, hourOfDay: Int)
    fun setListUpdateInterval(context: Context, repeatIntervalHour: Long)
}

class WorkManagerRepository(context: Context) : WorkerRepository {
    val workManager = WorkManager.getInstance(context)

    /***
    send notification once
    testing only
    ***/
    override fun sendNotification() {
        // Add WorkRequest to send notification
        val notificationBuilder = OneTimeWorkRequestBuilder<NotificationWorker>()

        // Start the work
        workManager.enqueue(notificationBuilder.build())
    }

    // get updated giveaway games list once
    override fun retrieveGameData() {
        val retrieverBuilder = OneTimeWorkRequestBuilder<RetrieverWorker>()
        workManager.enqueue(retrieverBuilder.build())
    }

    // send notification periodically
    override fun setNotificationInterval(context: Context, repeatIntervalHour: Long, hourOfDay: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay) // uses the 24-hour clock. E.g., at 10:04:15.250 PM the HOUR_OF_DAY is 22.
            set(Calendar.MINUTE, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        // cancel previous work
        workManager.cancelUniqueWork(REMINDER_NOTIFICATION_WORK_NAME)

        val notificationRequest = PeriodicWorkRequestBuilder<NotificationWorker>(repeatIntervalHour, TimeUnit.HOURS)
            .addTag(NOTIFICATION_WORK_TAG)
            .setInitialDelay(target.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        workManager
            .enqueueUniquePeriodicWork(
                REMINDER_NOTIFICATION_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                notificationRequest
            )
    }

    /***
    get updated giveaway games list periodically
    testing only
     ***/
    override fun setListUpdateInterval(context: Context, repeatIntervalHour: Long) {
        val notificationRequest = PeriodicWorkRequestBuilder<RetrieverWorker>(repeatIntervalHour, TimeUnit.HOURS)
            .addTag(TAG_OUTPUT)
            .build()

        // cancel previous work
        workManager.cancelUniqueWork(LIST_UPDATE_WORK_NAME)

        workManager
            .enqueueUniquePeriodicWork(
                LIST_UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                notificationRequest
            )
    }
}