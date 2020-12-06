package com.laurencerawlings.pollen.model

import android.content.Context
import android.net.Uri
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.preference.PreferenceManager
import com.dfl.newsapi.enums.Country
import com.dfl.newsapi.enums.Language
import com.dfl.newsapi.model.ArticleDto
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.reactivex.subjects.BehaviorSubject

val db = Firebase.firestore
const val TAG = "User"

class User(val id: String, val name: String?, val context: Context) {
    private val userRef = db.collection("users").document(id)
    private val localPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var topics = ArrayList<String>()
    var sources = ArrayList<String>()
    var country = localPreferences.getString("country", "GB")?.let { Country.valueOf(it) }
        private set
    var language = localPreferences.getString("language", "EN")?.let { Language.valueOf(it) }
        private set

    val topicsObservable: BehaviorSubject<ArrayList<String>> = BehaviorSubject.create()

    init {
        topics = localPreferences.getString("topics", "")?.split(",")?.map { it.trim() } as ArrayList<String>

        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                if (!snapshot.exists()) {
                    userRef.set(mapOf("country" to "GB", "language" to "EN", "topics" to arrayListOf<String>(), "sources" to arrayListOf<String>()))
                    country = Country.GB
                    language = Language.EN
                    topics = ArrayList()
                    sources = ArrayList()
                } else {
                    topics = snapshot.data?.get("topics") as ArrayList<String>
                    with (localPreferences.edit()) {
                        putString("topics", topics.joinToString(","))
                        apply()
                    }

                    sources = snapshot.data?.get("sources") as ArrayList<String>
                    val countryString = snapshot.data?.get("country") as String
                    val languageString = snapshot.data?.get("language") as String

                    country = Country.valueOf(countryString)
                    language = Language.valueOf(languageString)
                    topicsObservable.onNext(topics)
                }
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

    fun addTopic(topic: String) {
        userRef.update("topics", FieldValue.arrayUnion(topic))
    }

    fun removeTopic(topic: String) {
        userRef.update("topics", FieldValue.arrayRemove(topic))
    }

    fun setSources(sources: Array<String>) {
        userRef.update("sources", sources.toList())
    }

    fun setCountry(country: String) {
        userRef.update("country", country)
    }

    fun setLanguage(language: String) {
        userRef.update("language", language)
    }

    companion object {
        var user: User? = null

        fun articleKey(article: ArticleDto): String {
            val url = Uri.parse(article.url)
            return "${url.host?.replace('.', '-')}-${url.lastPathSegment}"
        }

        fun updateUser(context: Context) {
            if (Firebase.auth.currentUser != null) {
                user = User(
                    Firebase.auth.currentUser!!.uid,
                    Firebase.auth.currentUser!!.displayName,
                    context
                )
            }
        }
    }
}