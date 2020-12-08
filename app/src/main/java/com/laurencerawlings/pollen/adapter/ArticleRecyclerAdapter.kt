package com.laurencerawlings.pollen.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.dfl.newsapi.model.ArticleDto
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.model.User
import com.laurencerawlings.pollen.ui.Utils
import com.laurencerawlings.pollen.ui.main.MainActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_article_card.view.*
import kotlinx.android.synthetic.main.layout_article_card.view.article_headline
import kotlinx.android.synthetic.main.layout_article_card.view.article_thumbnail
import kotlinx.android.synthetic.main.layout_article_popup.view.*
import kotlinx.android.synthetic.main.layout_article_source.view.*
import java.net.URL


class ArticleRecyclerAdapter(articles: List<ArticleDto>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        when (holder) {
            is ArticleViewHolder -> holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

            if (!article.urlToImage.isNullOrEmpty()) {
                Picasso.get().load(article.urlToImage).fit().centerCrop().into(thumbnail)
                Picasso.get().load(faviconUrl(article.url)).fit().centerCrop().into(sourceIcon)
            }

            details.text = timeString(article.publishedAt)

            setBookmarked(article, bookmarked)

            bookmarked.setOnClickListener {
                if (User.isAuthed()) {
                    if (bookmarked.isChecked) {
                        User.user.addBookmark(article)
                        Utils.showSnackbar("Bookmark added", it)
                    } else {
                        User.user.removeBookmark(article)
                        Utils.showSnackbar("Bookmark removed", it)
                    }
                } else {
                    Utils.showSnackbar("Sign in to bookmark articles", it)
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
                MainActivity.currentArticle = article
                openPopup(it.context)
            }
        }

        private fun timeString(publishedAt: String): String {
            val hours = Utils.hoursPassed(publishedAt)

            return when {
                hours < 1 -> {
                    "now"
                }
                hours >= 24 -> {
                    "${hours / 24}d ago"
                }
                else -> {
                    "${hours}h ago"
                }
            }
        }

        private fun faviconUrl(url: String): String {
            return "http://" + URL(url).host + "/favicon.ico"
        }

        private fun setBookmarked(article: ArticleDto, checkBox: CheckBox) {
            User.user.getBookmarks()?.document(User.articleKey(article))?.get()
                ?.addOnSuccessListener {
                    checkBox.isChecked = it.exists()
                }
        }

        private fun openPopup(context: Context) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            val articleLayout: View = inflater.inflate(R.layout.layout_article_popup, null)
            builder.setView(articleLayout)

            if (MainActivity.currentArticle != null) {
                if (MainActivity.currentArticle!!.urlToImage.isNotEmpty()) {
                    Picasso.get().load(MainActivity.currentArticle!!.urlToImage).fit().centerCrop()
                        .into(articleLayout.article_thumbnail)
                    Picasso.get().load(faviconUrl(MainActivity.currentArticle!!.url)).fit()
                        .centerCrop().into(articleLayout.article_source_icon)
                }

                articleLayout.article_source.text = MainActivity.currentArticle!!.source.name
                articleLayout.article_time.text =
                    "• " + timeString(MainActivity.currentArticle!!.publishedAt)
                articleLayout.article_headline.text = MainActivity.currentArticle!!.title
                articleLayout.article_description.text = MainActivity.currentArticle!!.description
            }

            builder.setPositiveButton("Close") { _, _ -> }
            builder.setNeutralButton("Read more") { _, _ ->
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.currentArticle!!.url))
                startActivity(context, browserIntent, null)
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
}