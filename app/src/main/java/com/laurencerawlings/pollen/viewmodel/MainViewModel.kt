package com.laurencerawlings.pollen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dfl.newsapi.NewsApiRepository
import com.dfl.newsapi.enums.Category
import com.dfl.newsapi.enums.Country
import com.dfl.newsapi.enums.Language
import com.dfl.newsapi.enums.SortBy
import com.dfl.newsapi.model.ArticlesDto
import io.reactivex.Single

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

    private fun getPersonalNews(): Single<ArticlesDto> {
        return newsApiRepository.getEverything(q = "bitcoin", sources = "bbc-news", domains = null, language = Language.EN, sortBy = SortBy.POPULARITY, pageSize = 20, page = 1)
    }

    private fun getHeadlines(): Single<ArticlesDto> {
        return newsApiRepository.getTopHeadlines(category = Category.GENERAL, country = Country.GB, pageSize = 20, page = 1)
    }

    private fun getAllNews(): Single<ArticlesDto> {
        return newsApiRepository.getEverything(q = "bitcoin", sources = "bbc-news", domains = null, language = Language.EN, sortBy = SortBy.POPULARITY, pageSize = 20, page = 1)
    }
}