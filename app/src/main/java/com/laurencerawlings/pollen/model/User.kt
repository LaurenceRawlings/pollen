package com.laurencerawlings.pollen.model

import com.dfl.newsapi.model.ArticleDto
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

val db = Firebase.firestore
const val TAG = "User"

class User(val id: String, val name: String?) {
    private val userRef = db.collection("users").document(id)

    private var topics = ArrayList<String>()

    val topicsObservable: BehaviorSubject<ArrayList<String>> = BehaviorSubject.create()

    init {
        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                topics = snapshot.data?.get("topics") as ArrayList<String>
                topicsObservable.onNext(topics)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    fun getBookmarks(): CollectionReference {
        return userRef.collection("bookmarks")
    }

    fun addBookmark(article: ArticleDto) {
        getBookmarks().document(articleKey(article)).set(article)
    }

    fun removeBookmark(article: ArticleDto) {
        getBookmarks().document(articleKey(article)).delete()
    }

    fun getUser(): DocumentReference {
        return userRef
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

        fun updateUser() {
            if (Firebase.auth.currentUser != null) {
                User.user = User(Firebase.auth.currentUser!!.uid, Firebase.auth.currentUser!!.displayName)
            }
        }
    }
}