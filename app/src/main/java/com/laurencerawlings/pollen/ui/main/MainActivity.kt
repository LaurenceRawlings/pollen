package com.laurencerawlings.pollen.ui.main

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.adapter.MainTabAdapter
import com.laurencerawlings.pollen.model.User
import com.laurencerawlings.pollen.ui.account.AccountActivity
import com.laurencerawlings.pollen.ui.bookmarks.BookmarksActivity
import com.laurencerawlings.pollen.receivers.NewsNotification
import io.reactivex.plugins.RxJavaPlugins


class MainActivity : AppCompatActivity() {
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.PhoneBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    private var customLayout: AuthMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.activity_login)
        .setGoogleButtonId(R.id.google_button)
        .setEmailButtonId(R.id.email_button)
        .setPhoneButtonId(R.id.phone_button)
        .build()

    private enum class RC(val code: Int) {
        SIGN_IN(1),
        BOOKMARKS(2),
        SETTINGS(3)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sectionsPagerAdapter = MainTabAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        viewPager.offscreenPageLimit = 2
        viewPager.currentItem = 1
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        User.updateUser(this)

        RxJavaPlugins.setErrorHandler(Throwable::printStackTrace)
    }

    override fun onStop() {
        super.onStop()
        val localPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (localPreferences.getBoolean("notifications", true)) {
            scheduleNotification()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC.SIGN_IN.code) {
            IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val myToast = Toast.makeText(applicationContext, "Logged in!", Toast.LENGTH_SHORT)
                myToast.show()
                User.updateUser(this)
            } else {
                val myToast = Toast.makeText(
                    applicationContext,
                    "Log in failed!",
                    Toast.LENGTH_SHORT
                )
                myToast.show()
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                User.user = null
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_layout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_account -> {
                account()
            }
            R.id.app_bar_bookmarks -> {
                bookmarks()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun account() {
        if (Firebase.auth.currentUser != null) {
            startActivityForResult(Intent(this, AccountActivity::class.java), RC.SETTINGS.code)
        } else {
            signIn()
        }
    }

    private fun bookmarks() {
        if (Firebase.auth.currentUser != null) {
            startActivityForResult(Intent(this, BookmarksActivity::class.java), RC.BOOKMARKS.code)
        } else {
            signIn()
        }
    }

    private fun signIn() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.AppTheme_NoActionBar)
                .setAuthMethodPickerLayout(customLayout)
                .build(),
            RC.SIGN_IN.code
        )
    }

    private fun scheduleNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(NewsNotification.NOTIFICATION_CHANNEL, NewsNotification.NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT)

        manager.createNotificationChannel(channel)


        val builder = NotificationCompat.Builder(this, NewsNotification.NOTIFICATION_CHANNEL)
            .setContentTitle("New News")
            .setContentText("You have new news stories to check out!")
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_pollen)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val intent = Intent(this, this::class.java)
        val activity = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        builder.setContentIntent(activity)

        val notification: Notification = builder.build()
        val notificationIntent = Intent(this, NewsNotification::class.java)

        notificationIntent.putExtra(NewsNotification.NOTIFICATION_ID, 1)
        notificationIntent.putExtra(NewsNotification.NOTIFICATION, notification)

        val pendingIntent = PendingIntent.getBroadcast(this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val futureInMillis: Long = SystemClock.elapsedRealtime() + NewsNotification.DELAY
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent)
    }
}