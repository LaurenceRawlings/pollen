package com.laurencerawlings.pollen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dfl.newsapi.model.ArticleDto
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_article_card.view.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ArticleRecyclerAdapter(articles: List<ArticleDto>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items =  articles

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_article_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ArticleViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ArticleViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val thumbnail: ImageView = itemView.article_thumbnail
        private val sourceIcon: ImageView = itemView.article_source_icon
        private val source: TextView = itemView.article_source
        private val headline = itemView.article_headline
        private val details = itemView.article_details
        private val bookmarked = itemView.bookmark

        fun bind(article: ArticleDto) {
            source.text = article.source.name ?: "Source Unknown"
            headline.text = article.title

            Picasso.get().load(article.urlToImage).into(thumbnail)
            Picasso.get().load("http://" + URL(article.url).host + "/favicon.ico").into(sourceIcon)

            val hours = hoursPassed(article.publishedAt).toString()

            details.text = "${hours}h ago"

            setBookmarked(article, bookmarked)

            bookmarked.setOnCheckedChangeListener { _, isChecked ->
                if (User.user != null) {
                    if (isChecked) {
                        User.user!!.addBookmark(article)
                    } else {
                        User.user!!.removeBookmark(article)
                    }
                } else {
                    // TODO: Login
                }

            }
        }

        private fun stringToDate(date: String): Date? {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
            return sdf.parse(date)
        }

        private fun hoursPassed(date: String): Int {
            val tz = TimeZone.getTimeZone("UTC")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)

            sdf.timeZone = tz
            val now = Date()
            val then = stringToDate(date)

            val diffInMS: Long = kotlin.math.abs(now.time - (then?.time ?: 0))
            return TimeUnit.HOURS.convert(diffInMS, TimeUnit.MILLISECONDS).toInt()
        }

        fun setBookmarked(article: ArticleDto, checkBox: CheckBox) {
            if (User.user != null) {
                val bookmarkRef = User.user!!.getBookmarks().document(User.articleKey(article))
                bookmarkRef.get().addOnSuccessListener {
                    checkBox.isChecked = it.exists()
                }
            } else {
                checkBox.isChecked = false
            }
        }
    }
}