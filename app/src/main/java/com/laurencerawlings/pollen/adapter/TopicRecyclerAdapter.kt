package com.laurencerawlings.pollen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.model.User
import kotlinx.android.synthetic.main.layout_topic.view.*

class TopicRecyclerAdapter(topics: List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items = topics

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TopicViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_topic,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TopicRecyclerAdapter.TopicViewHolder -> holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val topicChip: Chip = itemView.topic

        fun bind(topic: String) {
            topicChip.text = topic

            topicChip.setOnCloseIconClickListener {
                User.user.removeTopic(topic)
            }
        }
    }
}