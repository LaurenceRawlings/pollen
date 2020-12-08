package com.laurencerawlings.pollen.ui.article

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dfl.newsapi.model.ArticleDto
import com.laurencerawlings.pollen.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_article.*


class ArticleActivity : AppCompatActivity() {
    companion object {
        var currentArticle: ArticleDto? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        if (currentArticle != null) {
            supportActionBar?.title = currentArticle!!.source.name

            if (!currentArticle!!.urlToImage.isNullOrEmpty()) {
                Picasso.get().load(currentArticle!!.urlToImage).into(article_thumbnail)
            }

            article_headline.text = currentArticle!!.title
            article_description.text = currentArticle!!.description

            article_read_more.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentArticle!!.url))
                startActivity(browserIntent)
            }
        }
    }
}