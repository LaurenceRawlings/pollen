package com.laurencerawlings.pollen.ui.bookmarks

import androidx.lifecycle.ViewModel
import com.dfl.newsapi.model.ArticleDto
import io.reactivex.Single

class BookmarkViewModel : ViewModel() {
    // TODO: Get bookmarks from database

    fun bookmarks(): Single<ArrayList<ArticleDto>>? {
        return null
    }
}