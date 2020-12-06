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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (article == null) {
            return
        }

        supportActionBar?.title = article!!.source.name

        Picasso.get().load(article!!.urlToImage).into(article_thumbnail)
        article_headline.text = article!!.title
        article_description.text = article!!.description

        article_read_more.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(article!!.url))
            startActivity(browserIntent)
        }
    }

    companion object {
        var article: ArticleDto? = null
    }
}