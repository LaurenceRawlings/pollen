package com.laurencerawlings.pollen.ui.account

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.firebase.ui.auth.AuthUI
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.api.NewsRepository
import com.laurencerawlings.pollen.model.User
import com.laurencerawlings.pollen.ui.Utils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    fun logout(view: View) {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                User.updateUser(this)
                Utils.showSnackbar("Logged out", view)
                setResult(Activity.RESULT_FIRST_USER)
                finish()
            }
    }

    fun delete(view: View) {
        AuthUI.getInstance()
            .delete(this)
            .addOnCompleteListener {
                logout(view)
            }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val compositeDisposable = CompositeDisposable()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener { preferences, key ->
                    when (key) {
                        "topics" -> {
                            NewsRepository.forYouFeedUpdated = false
                        }
                        "sources" -> {
                            User.user.setSources(
                                preferences.getStringSet("sources", null)?.toTypedArray()!!
                            )
                            NewsRepository.updateAllFeeds()
                        }
                        "country" -> {
                            User.user.setCountry(preferences.getString("country", "GB")!!)
                            NewsRepository.headlineFeedUpdated = false
                            updateSources()
                        }
                        "language" -> {
                            User.user.setLanguage(preferences.getString("language", "EN")!!)
                            NewsRepository.forYouFeedUpdated = false
                            NewsRepository.everythingFeedUpdated = false
                            updateSources()
                        }
                    }
                }

            updateSources()
        }

        private fun updateSources() {
            compositeDisposable.add(
                NewsRepository.getSources().subscribeOn(Schedulers.io()).subscribe { sources ->
                    activity?.runOnUiThread {
                        val sourceNames = ArrayList<String>()
                        val sourceIds = ArrayList<String>()
                        val sourcesPreference = findPreference<MultiSelectListPreference>("sources")

                        sources.sources.map {
                            sourceNames.add(it.name)
                            sourceIds.add(it.id)
                        }

                        sourcesPreference?.entryValues = sourceIds.toTypedArray()
                        sourcesPreference?.entries = sourceNames.toTypedArray()
                        sourcesPreference?.setDefaultValue(sourceIds.toTypedArray())
                    }
                })
        }
    }
}