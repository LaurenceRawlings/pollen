package com.laurencerawlings.pollen.ui.account

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.adapter.TopicRecyclerAdapter
import com.laurencerawlings.pollen.model.User
import kotlinx.android.synthetic.main.activity_topics.*

class TopicsActivity : AppCompatActivity() {
    private lateinit var topicAdapter: TopicRecyclerAdapter

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topics)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

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

    fun addTopic(view: View) {
        var isValid = false
        val topic  = topic_input.text.toString()

        if (topic.isBlank()) {
            topic_input.error = "Topic cannot be blank"
        } else if (!(topic.all { it.isLetterOrDigit() || it.isWhitespace() })) {
            topic_input.error = "Topic must be alphanumeric"
        } else {
            isValid = true
        }

        if (isValid) {
            User.user?.addTopic(topic)
            topic_input.text?.clear()
        }
    }
}