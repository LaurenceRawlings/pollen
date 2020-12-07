package com.laurencerawlings.pollen.ui.account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import com.firebase.ui.auth.AuthUI
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.api.NewsRepository
import com.laurencerawlings.pollen.model.User
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AccountActivity : AppCompatActivity() {
    @SuppressLint("CheckResult")
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

            findPreference<ListPreference>("country")?.setOnPreferenceChangeListener { _, _ ->
                updateSources()
            }

            findPreference<ListPreference>("language")?.setOnPreferenceChangeListener { _, _ ->
                updateSources()
            }

            updateSources()
        }

        @SuppressLint("CheckResult")
        private fun updateSources(): Boolean {
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

            return true
        }
    }
}