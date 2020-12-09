package com.laurencerawlings.pollen.ui.account

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.dfl.newsapi.enums.Country
import com.dfl.newsapi.enums.Language
import com.firebase.ui.auth.AuthUI
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.adapter.TopicRecyclerAdapter
import com.laurencerawlings.pollen.api.NewsRepository
import com.laurencerawlings.pollen.model.User
import com.laurencerawlings.pollen.ui.Utils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_topic_picker.view.*


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
                User.updateUser()
                Utils.showSnackbar(getString(R.string.message_logged_out), view)
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
        private var listener: OnSharedPreferenceChangeListener

        init {
            listener =
                OnSharedPreferenceChangeListener { preferences, key ->
                    when (key) {
                        getString(R.string.preferences_key_sources) -> {
                            User.user.setSources(
                                preferences.getStringSet(getString(R.string.preferences_key_sources), null)?.toTypedArray()!!
                            )
                        }
                        getString(R.string.preferences_key_country) -> {
                            User.user.setCountry(
                                preferences.getString(
                                    getString(R.string.preferences_key_country),
                                    Country.GB.toString()
                                )!!
                            )
                            updateSources()
                        }
                        getString(R.string.preferences_key_language) -> {
                            User.user.setLanguage(
                                preferences.getString(
                                    getString(R.string.preferences_key_language),
                                    Language.EN.toString()
                                )!!
                            )
                            updateSources()
                        }
                    }
                }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(listener)
            updateSources()

            findPreference<Preference>(getString(R.string.preferences_key_topics))?.setOnPreferenceClickListener {
                openTopicPickerDialog()
            }
        }

        private fun updateSources() {
            compositeDisposable.add(
                NewsRepository.getSources().subscribeOn(Schedulers.io()).subscribe { sources ->
                    activity?.runOnUiThread {
                        val sourceNames = ArrayList<String>()
                        val sourceIds = ArrayList<String>()
                        val sourcesPreference = findPreference<MultiSelectListPreference>(getString(R.string.preferences_key_sources))

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

        private fun openTopicPickerDialog(): Boolean {
            val inflater =
                context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val topicPickerLayout: View = inflater.inflate(R.layout.layout_topic_picker, null)
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setTitle(getString(R.string.topics_dialog_title))
            builder.setPositiveButton(getString(R.string.topics_dialog_ok)) { _, _ -> }
            builder.setView(topicPickerLayout)

            val layoutManager = FlexboxLayoutManager(context)
            layoutManager.flexDirection = FlexDirection.ROW

            topicPickerLayout.topics.layoutManager = layoutManager

            User.user.topicsObservable.subscribe {
                val topicAdapter = TopicRecyclerAdapter(it)
                topicPickerLayout.topics.adapter = topicAdapter
            }?.let {
                compositeDisposable.add(
                    it
                )
            }

            topicPickerLayout.topic_add.setOnClickListener {
                var isValid = false
                val topic = topicPickerLayout.topic_input.text.toString()

                if (topic.isBlank()) {
                    topicPickerLayout.topic_input.error = getString(R.string.message_topic_blank)
                } else if (!(topic.all { it.isLetterOrDigit() || it.isWhitespace() })) {
                    topicPickerLayout.topic_input.error = getString(R.string.message_topic_invalid)
                } else {
                    isValid = true
                }

                if (isValid) {
                    User.user.addTopic(topic)
                    topicPickerLayout.topic_input.text?.clear()
                }
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()

            return false
        }
    }
}