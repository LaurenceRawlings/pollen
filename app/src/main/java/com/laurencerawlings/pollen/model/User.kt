package com.laurencerawlings.pollen.model

import com.dfl.newsapi.model.ArticleDto
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.net.Uri
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue

val db = Firebase.firestore

class User(val id: String, val name: String?) {
    private val userRef = db.collection("users").document(id)

    fun getBookmarks(): CollectionReference {
        return userRef.collection("bookmarks")
    }

    fun addBookmark(article: ArticleDto) {
        getBookmarks().document(articleKey(article)).set(article)
    }

    fun removeBookmark(article: ArticleDto) {
        getBookmarks().document(articleKey(article)).delete()
    }

    fun addTopic(topic: String) {
        userRef.update("topics", FieldValue.arrayUnion(topic))
    }

    fun removeTopic(topic: String) {
        userRef.update("topics", FieldValue.arrayRemove(topic))
    }

    companion object {
        var user: User? = null

        fun articleKey(article: ArticleDto): String {
            val url = Uri.parse(article.url)
            return "${url.host?.replace('.', '-')}-${url.lastPathSegment}"
        }
    }
}