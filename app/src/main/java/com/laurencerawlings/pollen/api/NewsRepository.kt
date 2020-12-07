package com.laurencerawlings.pollen.api

import com.dfl.newsapi.NewsApiRepository
import com.dfl.newsapi.enums.SortBy
import com.dfl.newsapi.model.ArticlesDto
import com.dfl.newsapi.model.SourcesDto
import com.laurencerawlings.pollen.model.User
import io.reactivex.Single

class NewsRepository {
    companion object {
        private val newsApiRepository = NewsApiRepository("49294c53574d438c8565fd205a6948b5")

        var headlinesUpdated = false
        var personalUpdated = false
        var allUpdated = false

        fun getPersonalNews(): Single<ArticlesDto> {
            personalUpdated = true
            return newsApiRepository.getEverything(q = User.user?.topics?.joinToString(" OR "),
                sources = User.user?.sources?.joinToString(","),
                language = User.user?.language,
                sortBy = SortBy.PUBLISHED_AT,
                pageSize = 100,
                page = 1)
        }

        fun getHeadlines(): Single<ArticlesDto> {
            headlinesUpdated = true
            return newsApiRepository.getTopHeadlines(sources = User.user?.sources?.joinToString(","),
                pageSize = 100,
                page = 1)
        }

        fun getAllNews(): Single<ArticlesDto> {
            allUpdated = true
            return newsApiRepository.getEverything(sources = User.user?.sources?.joinToString(","),
                language = User.user?.language,
                sortBy = SortBy.PUBLISHED_AT,
                pageSize = 100,
                page = 1)
        }

        fun getSources(): Single<SourcesDto> {
            return newsApiRepository.getSources(country = User.user?.country, language = User.user?.language)
        }
    }
}