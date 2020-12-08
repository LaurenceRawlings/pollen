package com.laurencerawlings.pollen.api

import com.dfl.newsapi.NewsApiRepository
import com.dfl.newsapi.enums.SortBy
import com.dfl.newsapi.model.ArticlesDto
import com.dfl.newsapi.model.SourcesDto
import com.laurencerawlings.pollen.model.User
import io.reactivex.Single

class NewsRepository {
    companion object {
        private val newsApiRepository = NewsApiRepository("e4d6e296153f46c6993bb300c245463a")

        var headlineFeedUpdated = false
        var forYouFeedUpdated = false
        var everythingFeedUpdated = false

        fun updateAllFeeds() {
            headlineFeedUpdated = false
            forYouFeedUpdated = false
            everythingFeedUpdated = false
        }

        fun getPersonalNews(): Single<ArticlesDto> {
            forYouFeedUpdated = true
            return newsApiRepository.getEverything(
                q = User.user.topics.joinToString(" OR "),
                sources = User.user.sources.joinToString(","),
                language = User.language,
                sortBy = SortBy.PUBLISHED_AT,
                pageSize = 100,
                page = 1
            )
        }

        fun getHeadlines(): Single<ArticlesDto> {
            headlineFeedUpdated = true
            return newsApiRepository.getTopHeadlines(
                sources = User.user.sources.joinToString(","),
                pageSize = 100,
                page = 1
            )
        }

        fun getAllNews(): Single<ArticlesDto> {
            everythingFeedUpdated = true
            return newsApiRepository.getEverything(
                sources = User.user.sources.joinToString(","),
                language = User.language,
                sortBy = SortBy.PUBLISHED_AT,
                pageSize = 100,
                page = 1
            )
        }

        fun getSources(): Single<SourcesDto> {
            return newsApiRepository.getSources(
                country = User.country,
                language = User.language
            )
        }
    }
}