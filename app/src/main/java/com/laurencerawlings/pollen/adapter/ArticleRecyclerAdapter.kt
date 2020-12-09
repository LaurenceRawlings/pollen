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
        private val image: ImageView = itemView.article_card_image
        private val sourceFavicon: ImageView = itemView.article_source_icon
        private val sourceName: TextView = itemView.article_source
        private val title = itemView.article_card_title
        private val time = itemView.article_card_time
        private val bookmarked = itemView.article_card_bookmarked
        private val share = itemView.article_card_share
        private val container = itemView.article_card

        fun bind(article: ArticleDto) {
            sourceName.text = article.source.name
            title.text = article.title

            if (!article.urlToImage.isNullOrEmpty()) {
                Picasso.get().load(article.urlToImage).fit().centerCrop().into(image)
                Picasso.get().load(faviconUrl(article.url)).fit().centerCrop().into(sourceFavicon)
            }

            time.text = timeString(article.publishedAt)

            setBookmarked(article, bookmarked)

            bookmarked.setOnClickListener {
                if (User.isAuthed()) {
                    if (bookmarked.isChecked) {
                        User.user.addBookmark(article)
                        Utils.showSnackbar(
                            itemView.context.getString(R.string.message_bookmark_added),
                            it
                        )
                    } else {
                        User.user.removeBookmark(article)
                        Utils.showSnackbar(
                            itemView.context.getString(R.string.message_bookmark_removed),
                            it
                        )
                    }
                } else {
                    Utils.showSnackbar(
                        itemView.context.getString(R.string.message_bookmark_sign_in),
                        it
                    )
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

            container.setOnClickListener {
                MainActivity.currentArticle = article
                openArticleDialog(it.context)
            }
        }

        private fun timeString(publishedAt: String): String {
            val hours = Utils.hoursPassed(publishedAt)

            return when {
                hours < 1 -> {
                    itemView.context.getString(R.string.time_now)
                }
                hours >= 24 -> {
                    itemView.context.getString(R.string.time_days).format(hours / 24)
                }
                else -> {
                    itemView.context.getString(R.string.time_hours).format(hours)
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

        private fun openArticleDialog(context: Context) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            val articleLayout: View = inflater.inflate(R.layout.layout_article_popup, null)

            builder.setView(articleLayout)

            if (MainActivity.currentArticle != null) {
                if (MainActivity.currentArticle!!.urlToImage.isNotEmpty()) {
                    Picasso.get().load(MainActivity.currentArticle!!.urlToImage).fit().centerCrop()
                        .into(articleLayout.article_popup_image)
                    Picasso.get().load(faviconUrl(MainActivity.currentArticle!!.url)).fit()
                        .centerCrop().into(articleLayout.article_source_icon)
                }

                articleLayout.article_source.text = MainActivity.currentArticle!!.source.name
                articleLayout.article_popup_time.text =
                    context.getString(R.string.article_dialog_time)
                        .format(timeString(MainActivity.currentArticle!!.publishedAt))
                articleLayout.article_popup_title.text = MainActivity.currentArticle!!.title
                articleLayout.article_popup_description.text =
                    MainActivity.currentArticle!!.description
            }

            builder.setPositiveButton(context.getString(R.string.article_dialog_close)) { _, _ -> }
            builder.setNeutralButton(context.getString(R.string.article_dialog_read_more)) { _, _ ->
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.currentArticle!!.url))
                startActivity(context, browserIntent, null)
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
}