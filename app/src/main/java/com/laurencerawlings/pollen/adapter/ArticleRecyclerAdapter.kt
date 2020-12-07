package com.laurencerawlings.pollen.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dfl.newsapi.model.ArticleDto
import com.google.android.material.snackbar.Snackbar
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.model.User
import com.laurencerawlings.pollen.ui.article.ArticleActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_article_card.view.*
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ArticleRecyclerAdapter(articles: List<ArticleDto>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = articles

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

    inner class ArticleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val thumbnail: ImageView = itemView.article_thumbnail
        private val sourceIcon: ImageView = itemView.article_source_icon
        private val source: TextView = itemView.article_source
        private val headline = itemView.article_headline
        private val details = itemView.article_details
        private val bookmarked = itemView.bookmark
        private val share = itemView.share_article
        private val card = itemView.article_card

        fun bind(article: ArticleDto) {
            source.text = article.source.name
            headline.text = article.title

            Picasso.get().load(article.urlToImage).fit().centerCrop().into(thumbnail)
            Picasso.get().load("http://" + URL(article.url).host + "/favicon.ico").into(sourceIcon)

            val hours = hoursPassed(article.publishedAt)

            when {
                hours < 1 -> {
                    details.text = "now"
                }
                hours >= 24 -> {
                    details.text = "${hours/24}d ago"
                }
                else -> {
                    details.text = "${hours}h ago"
                }
            }

            setBookmarked(article, bookmarked)

            bookmarked.setOnClickListener {
                if (User.user != null) {
                    if (bookmarked.isChecked) {
                        User.user!!.addBookmark(article)
                    } else {
                        User.user!!.removeBookmark(article)
                    }
                } else {
                    Snackbar.make(
                        it,
                        "You must be signed in to bookmark articles...",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    bookmarked.isChecked = false
                }

            }

            share.setOnClickListener {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, article.url)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                it.context.startActivity(shareIntent)
            }

            card.setOnClickListener {
                ArticleActivity.article = article
                it.context.startActivity(Intent(it.context, ArticleActivity::class.java))
            }
        }

        private fun stringToDate(date: String): Date? {
            var sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
            return try {
                sdf.parse(date)
            } catch (_: ParseException) {
                try {
                    sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ms'Z'", Locale.ROOT)
                    sdf.parse(date)
                } catch (_: ParseException) {
                    try {
                        sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
                        sdf.parse(date.split('+')[0])
                    } catch (_: ParseException) {
                        null
                    }
                }
            }
        }

        private fun hoursPassed(date: String): Int {
            val now = Date()
            val then = stringToDate(date)

            val diffInMS: Long = kotlin.math.abs(now.time - (then?.time ?: 0))
            return TimeUnit.HOURS.convert(diffInMS, TimeUnit.MILLISECONDS).toInt()
        }

        private fun setBookmarked(article: ArticleDto, checkBox: CheckBox) {
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