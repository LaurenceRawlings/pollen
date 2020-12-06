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
    private val newsApiRepository = NewsApiRepository("f14a38297ca6433b9f58c1d05932e6a5")

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

    @SuppressLint("CheckResult")
    private fun getPersonalNews(): Single<ArticlesDto> {
        return newsApiRepository.getEverything(q = User.user?.topics?.joinToString(" OR "), language = User.user?.language, sortBy = SortBy.PUBLISHED_AT, pageSize = 100, page = 1)
    }

    private fun getHeadlines(): Single<ArticlesDto> {
        return newsApiRepository.getTopHeadlines(country = User.user?.country, pageSize = 100, page = 1)
    }

    private fun getAllNews(): Single<ArticlesDto> {
        return newsApiRepository.getEverything(sources = "bbc-news", language = User.user?.language, sortBy = SortBy.PUBLISHED_AT, pageSize = 100, page = 1)
    }
}