package com.example.giveawayreminder

// Notification Channel constants

// Name of Notification Channel for verbose notifications of background work
val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
    "RetrieverWorker Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "New Free Game Dropped!"
val NOTIFICATION_TITLE: CharSequence = "Giveaway Reminder"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1

// The name of the image manipulation work
const val NOTIFICATION_WORK_TAG = "notification_tag"

const val LIST_UPDATE_WORK_NAME = "list_update_work"
const val REMINDER_NOTIFICATION_WORK_NAME = "reminder_notification_work"

// Other keys
const val TAG_OUTPUT = "OUTPUT"


const val LAYOUT_PREFERENCE_NAME = "notification_preferences"