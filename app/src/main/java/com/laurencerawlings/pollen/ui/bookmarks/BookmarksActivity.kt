package com.laurencerawlings.pollen.ui.bookmarks

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.adapter.ArticleRecyclerAdapter
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*

class BookmarksActivity : AppCompatActivity() {
    private lateinit var bookmarkViewModel: BookmarkViewModel
    private lateinit var articleAdapter: ArticleRecyclerAdapter

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookmarkViewModel = ViewModelProviders.of(this).get(BookmarkViewModel::class.java)

        bookmarkViewModel.bookmarks()?.subscribeOn(Schedulers.io())?.subscribe { articles ->
            this.runOnUiThread {
                articleAdapter = ArticleRecyclerAdapter(articles)
                recycler_view.adapter = articleAdapter
            }
        }
    }

}