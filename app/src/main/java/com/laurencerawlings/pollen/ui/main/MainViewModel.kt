package com.laurencerawlings.pollen.ui.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dfl.newsapi.NewsApiRepository
import com.dfl.newsapi.enums.Category
import com.dfl.newsapi.enums.Country
import com.dfl.newsapi.enums.Language
import com.dfl.newsapi.enums.SortBy
import com.dfl.newsapi.model.ArticlesDto
import com.laurencerawlings.pollen.model.User
import io.reactivex.Single
import java.net.URLEncoder

class MainViewModel : ViewModel() {
    private val _index = MutableLiveData<Int>()
    private val newsApiRepository = NewsApiRepository("f14a38297ca6433b9f58c1d05932e6a5")

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun articles(): Single<ArticlesDto> {
        return when (_index.value) {
            0 -> getPersonalNews()
            1 -> getHeadlines()
            else -> getAllNews()
        }
    }

    @SuppressLint("CheckResult")
    private fun getPersonalNews(): Single<ArticlesDto> {
        var topics = ""

        User.user?.topicsObservable?.subscribe {
            topics = it.joinToString(" OR ")
        }

        Log.i("TOPICS", topics)

        return newsApiRepository.getEverything(q = topics, language = Language.EN, sortBy = SortBy.PUBLISHED_AT, pageSize = 100, page = 1)
    }

    private fun getHeadlines(): Single<ArticlesDto> {
        return newsApiRepository.getTopHeadlines(country = Country.GB, pageSize = 100, page = 1)
    }

    private fun getAllNews(): Single<ArticlesDto> {
        return newsApiRepository.getEverything(sources = "bbc-news", language = Language.EN, sortBy = SortBy.PUBLISHED_AT, pageSize = 100, page = 1)
    }
}