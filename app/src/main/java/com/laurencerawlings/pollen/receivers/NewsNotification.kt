package com.laurencerawlings.pollen.receivers

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class NewsNotification : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification: Notification? = intent!!.getParcelableExtra(NOTIFICATION)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val NOTIFICATION_ID = "news_notification_id"
        const val NOTIFICATION = "news_notification"
        const val NOTIFICATION_CHANNEL = "news_notification_channel"
        const val DELAY: Long = 21600000 //6 hours
    }
}