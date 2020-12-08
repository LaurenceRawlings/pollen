package com.laurencerawlings.pollen.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.tabs.TabLayout
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.adapter.MainTabAdapter
import com.laurencerawlings.pollen.api.NewsRepository
import com.laurencerawlings.pollen.model.User
import com.laurencerawlings.pollen.receivers.NewsNotification
import com.laurencerawlings.pollen.ui.Utils
import com.laurencerawlings.pollen.ui.account.AccountActivity
import com.laurencerawlings.pollen.ui.bookmarks.BookmarksActivity
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        private val signInProviders = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        private val customSignInLayout: AuthMethodPickerLayout =
            AuthMethodPickerLayout.Builder(R.layout.activity_login)
                .setGoogleButtonId(R.id.google_button)
                .setEmailButtonId(R.id.email_button)
                .setPhoneButtonId(R.id.phone_button)
                .build()

        private enum class RequestCodes(val code: Int) {
            SIGN_IN(1),
            BOOKMARKS(2),
            SETTINGS(3)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        User.updateUser(this) { setupTabs() }

        RxJavaPlugins.setErrorHandler(Throwable::printStackTrace)
    }

    override fun onStop() {
        super.onStop()
        scheduleNotification()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCodes.SIGN_IN.code) {
            IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                Utils.showSnackbar("Logged in!", findViewById(R.id.content))
            } else {
                Utils.showSnackbar("Log in failed!", findViewById(R.id.content))
            }

            User.updateUser(this)
        } else if (requestCode == RequestCodes.SETTINGS.code) {
            IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_FIRST_USER) {
                NewsRepository.updateAllFeeds()
                finish();
                startActivity(intent)
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
                openAccountActivity()
            }
            R.id.app_bar_bookmarks -> {
                openBookmarksActivity()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun onAddTopicsClicked(view: View) {
        openAccountActivity()
    }

    private fun setupTabs() {
        val sectionsPagerAdapter = MainTabAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = view_pager
        val tabs: TabLayout = tabs

        viewPager.adapter = sectionsPagerAdapter
        viewPager.offscreenPageLimit = 2
        viewPager.currentItem = 1
        tabs.setupWithViewPager(viewPager)
    }

    private fun openAccountActivity() {
        if (User.isAuthed()) {
            startActivityForResult(
                Intent(this, AccountActivity::class.java),
                RequestCodes.SETTINGS.code
            )
        } else {
            openSignInActivity()
        }
    }

    private fun openBookmarksActivity() {
        if (User.isAuthed()) {
            startActivityForResult(
                Intent(this, BookmarksActivity::class.java),
                RequestCodes.BOOKMARKS.code
            )
        } else {
            openSignInActivity()
        }
    }

    private fun openSignInActivity() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(signInProviders)
                .setTheme(R.style.AppTheme_NoActionBar)
                .setAuthMethodPickerLayout(customSignInLayout)
                .build(),
            RequestCodes.SIGN_IN.code
        )
    }

    private fun scheduleNotification() {
        val localPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (localPreferences.getBoolean("notifications", true)) {
            val delayHours = localPreferences.getString("notification-interval", "6")!!.toLong()
            val delayMilliseconds = delayHours.times(3600000)

            val calendar = Calendar.getInstance()
            val notificationHour = calendar.get(Calendar.HOUR_OF_DAY) + delayHours

            if (notificationHour > 5 || notificationHour < 23) {
                NewsNotification.schedule(applicationContext, this, delayMilliseconds)
            }
        }
    }
}