package com.laurencerawlings.pollen.ui.main

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dfl.newsapi.NewsApiRepository
import com.dfl.newsapi.enums.SortBy
import com.dfl.newsapi.model.ArticlesDto
import com.laurencerawlings.pollen.model.User
import io.reactivex.Single

class MainViewModel : ViewModel() {
    private val _index = MutableLiveData<Int>()

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun articles(): Single<ArticlesDto> {
        return when (_index.value) {
            0 -> getHeadlines()
            1 -> getPersonalNews()
            else -> getAllNews()
        }
    }

    fun isUpdated(): Boolean {
        return when (_index.value) {
            0 -> headlinesUpdated
            1 -> personalUpdated
            else -> allUpdated
        }
    }

    companion object {
        private val newsApiRepository = NewsApiRepository("f14a38297ca6433b9f58c1d05932e6a5")

        var headlinesUpdated = false
        var personalUpdated = false
        var allUpdated = false

        private fun getPersonalNews(): Single<ArticlesDto> {
            personalUpdated = true
            return newsApiRepository.getEverything(q = User.user?.topics?.joinToString(" OR "), language = User.user?.language, sortBy = SortBy.PUBLISHED_AT, pageSize = 100, page = 1)
        }

        private fun getHeadlines(): Single<ArticlesDto> {
            headlinesUpdated = true
            return newsApiRepository.getTopHeadlines(country = User.user?.country, pageSize = 100, page = 1)
        }

        private fun getAllNews(): Single<ArticlesDto> {
            allUpdated = true
            return newsApiRepository.getEverything(sources = "bbc-news", language = User.user?.language, sortBy = SortBy.PUBLISHED_AT, pageSize = 100, page = 1)
        }
    }
}