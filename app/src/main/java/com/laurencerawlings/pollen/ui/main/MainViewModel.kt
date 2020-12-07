package com.laurencerawlings.pollen.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dfl.newsapi.model.ArticlesDto
import com.laurencerawlings.pollen.api.NewsRepository
import io.reactivex.Single

class MainViewModel : ViewModel() {
    private val _index = MutableLiveData<Int>()

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun articles(): Single<ArticlesDto> {
        return when (_index.value) {
            0 -> NewsRepository.getHeadlines()
            1 -> NewsRepository.getPersonalNews()
            else -> NewsRepository.getAllNews()
        }
    }

    fun isUpdated(): Boolean {
        return when (_index.value) {
            0 -> NewsRepository.headlinesUpdated
            1 -> NewsRepository.personalUpdated
            else -> NewsRepository.allUpdated
        }
    }
}