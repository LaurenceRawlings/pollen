package com.laurencerawlings.pollen.receivers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.api.NewsRepository


class NewsNotification : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification: Notification? = intent!!.getParcelableExtra(NOTIFICATION)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)

        NewsRepository.headlinesUpdated = false
        NewsRepository.personalUpdated = false
        NewsRepository.allUpdated = false

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = "notification_id"
        private const val NOTIFICATION = "notification"
        private const val NOTIFICATION_CHANNEL = "pollen"

        init {

        }

        fun schedule(context: Context, intentActivity: Activity, delay: Long) {
            val futureInMillis: Long = SystemClock.elapsedRealtime() + delay

            val manager = intentActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT)

            manager.createNotificationChannel(channel)


            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                .setContentTitle("New News")
                .setContentText("You have new news stories to check out!")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_pollen)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            val intent = Intent(context, intentActivity::class.java)
            val activity = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT)

            builder.setContentIntent(activity)

            val notification: Notification = builder.build()
            val notificationIntent = Intent(context, NewsNotification::class.java)

            notificationIntent.putExtra(NOTIFICATION_ID, 1)
            notificationIntent.putExtra(NOTIFICATION, notification)

            val pendingIntent = PendingIntent.getBroadcast(context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            val alarmManager = intentActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent)
        }
    }
}