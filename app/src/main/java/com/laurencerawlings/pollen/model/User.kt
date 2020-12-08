package com.laurencerawlings.pollen.model

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.preference.PreferenceManager
import com.dfl.newsapi.enums.Country
import com.dfl.newsapi.enums.Language
import com.dfl.newsapi.model.ArticleDto
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.laurencerawlings.pollen.api.NewsRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class User(
    var id: String = "anon",
    var name: String = "Anonymous"
) {
    companion object {
        val db = Firebase.firestore
        private val anonUser = User()
        var user: User = anonUser

        var country: Country = Country.GB
        var language: Language = Language.EN

        fun articleKey(article: ArticleDto): String {
            val url = Uri.parse(article.url)
            return "${url.host?.replace(".", "-")}-${url.lastPathSegment}"
        }

        fun updateUser(context: Context, callback: (() -> Unit)? = null) {
            user = if (Firebase.auth.currentUser != null) {
                User(
                    Firebase.auth.currentUser!!.uid,
                    Firebase.auth.currentUser!!.displayName,
                    context
                ) {
                    if (callback != null) {
                        callback()
                    } else {
                        NewsRepository.updateAllFeeds()
                    }
                }
            } else {
                anonUser
            }
        }

        fun isAuthed(): Boolean {
            return Firebase.auth.currentUser != null
        }
    }

    var topics: ArrayList<String> = ArrayList()
    var sources: ArrayList<String> = ArrayList()

    private lateinit var userRef: DocumentReference
    private lateinit var localPreferences: SharedPreferences
    lateinit var topicsObservable: BehaviorSubject<ArrayList<String>>
    private val compositeDisposable = CompositeDisposable()

    constructor() : this("anon") {
        compositeDisposable.add(
            NewsRepository.getSources().subscribeOn(Schedulers.io()).subscribe { sources ->
                sources.sources.map {
                    this.sources.add(it.id)
                }
            })
    }

    constructor(id: String, name: String?, context: Context, callback: () -> Unit) : this() {
        this.id = id
        if (name != null) {
            this.name = name
        }
        userRef = db.collection("users").document(id)
        localPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        topicsObservable = BehaviorSubject.create()

        val topicsString = localPreferences.getString("topics", null)

        if (!topicsString.isNullOrBlank()) {
            topics = topicsString.split(",").map { it.trim() } as ArrayList<String>
        }

        userRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                syncPreferences(snapshot)
            }
        }

        userRef.get().addOnSuccessListener { snapshot ->
            syncPreferences(snapshot)
            callback()
        }
    }

    init {
        NewsRepository.updateAllFeeds()
    }

    private fun syncPreferences(snapshot: DocumentSnapshot) {
        if (!snapshot.exists()) {
            userRef.set(
                mapOf(
                    "country" to Country.GB.toString(),
                    "language" to Language.EN.toString(),
                    "topics" to arrayListOf<String>(),
                    "sources" to arrayListOf<String>()
                )
            )
        } else {
            topics = snapshot.data?.get("topics") as? ArrayList<String> ?: ArrayList()
            with(localPreferences.edit()) {
                putString("topics", topics.joinToString(","))
                apply()
            }

            sources = snapshot.data?.get("sources") as? ArrayList<String> ?: ArrayList()
            val countryString = snapshot.data?.get("country") as String
            val languageString = snapshot.data?.get("language") as String

            country = Country.valueOf(countryString)
            language = Language.valueOf(languageString)
            topicsObservable.onNext(topics)
        }
    }

    private inline fun requireAuth(f: () -> Unit) {
        if (isAuthed()) {
            f()
        }
    }

    // TODO: Require auth
    fun getBookmarks(): CollectionReference? {
        return if (isAuthed()) {
            userRef.collection("bookmarks")
        } else {
            null
        }
    }

    fun addBookmark(article: ArticleDto) = requireAuth {
        getBookmarks()?.document(articleKey(article))?.set(article)
    }

    fun removeBookmark(article: ArticleDto) = requireAuth {
        getBookmarks()?.document(articleKey(article))?.delete()
    }

    fun addTopic(topic: String) = requireAuth {
        userRef.update("topics", FieldValue.arrayUnion(topic))
    }

    fun removeTopic(topic: String) = requireAuth {
        userRef.update("topics", FieldValue.arrayRemove(topic))
    }

    fun setSources(sources: Array<String>) = requireAuth {
        userRef.update("sources", sources.toList())
    }

    fun setCountry(country: String) = requireAuth {
        userRef.update("country", country)
    }

    fun setLanguage(language: String) = requireAuth {
        userRef.update("language", language)
    }
}