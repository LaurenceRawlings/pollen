package com.laurencerawlings.pollen.ui.account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.adapter.TopicRecyclerAdapter
import com.laurencerawlings.pollen.model.User
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {
    private lateinit var topicAdapter: TopicRecyclerAdapter

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW

        topics.layoutManager = layoutManager

        User.user?.topicsObservable?.subscribe {
            updateTopics(it)
        }
    }

    private fun updateTopics(topicList: ArrayList<String>) {
        topicAdapter = TopicRecyclerAdapter(topicList)
        topics.adapter = topicAdapter
    }

    fun logout(view: View) {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                finish()
            }
    }

    fun delete(view: View) {
        AuthUI.getInstance()
            .delete(this)
            .addOnCompleteListener {
                finish()
            }
    }

    fun addTopic(view: View) {
        User.user?.addTopic(topic_input.text.toString())
        topic_input.text?.clear()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}