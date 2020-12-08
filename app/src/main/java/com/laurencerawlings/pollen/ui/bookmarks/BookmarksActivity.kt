package com.laurencerawlings.pollen.ui.bookmarks

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dfl.newsapi.model.ArticleDto
import com.dfl.newsapi.model.SourceDto
import com.google.firebase.firestore.Query
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.adapter.ArticleRecyclerAdapter
import com.laurencerawlings.pollen.model.User
import kotlinx.android.synthetic.main.activity_bookmarks.*

class BookmarksActivity : AppCompatActivity() {
    private lateinit var articleAdapter: ArticleRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        User.user.getBookmarks()?.orderBy("publishedAt", Query.Direction.DESCENDING)?.get()
            ?.addOnSuccessListener { bookmarks ->
                this.runOnUiThread {
                    val bookmarkList = ArrayList<ArticleDto>()

                    for (bookmark in bookmarks) {
                        val source = bookmark.get("source") as? HashMap<String, String> ?: HashMap()
                        val sId = source["id"] ?: ""
                        val sName = source["name"] ?: ""
                        val sDescription = source["description"] ?: ""
                        val sUrl = source["url"] ?: ""
                        val sCategory = source["category"] ?: ""
                        val sLanguage = source["language"] ?: ""
                        val sCountry = source["country"] ?: ""

                        val author = bookmark.get("author") as? String ?: ""
                        val title = bookmark.get("title") as? String ?: ""
                        val description = bookmark.get("description") as? String ?: ""
                        val url = bookmark.get("url") as? String ?: ""
                        val urlToImage = bookmark.get("urlToImage") as? String ?: ""
                        val publishedAt = bookmark.get("publishedAt") as? String ?: ""

                        bookmarkList.add(
                            ArticleDto(
                                SourceDto(
                                    sId,
                                    sName,
                                    sDescription,
                                    sUrl,
                                    sCategory,
                                    sLanguage,
                                    sCountry
                                ),
                                author,
                                title,
                                description,
                                url,
                                urlToImage,
                                publishedAt
                            )
                        )
                    }

                    articleAdapter = ArticleRecyclerAdapter(bookmarkList)
                    recycler_view.adapter = articleAdapter
                }
            }
    }
}