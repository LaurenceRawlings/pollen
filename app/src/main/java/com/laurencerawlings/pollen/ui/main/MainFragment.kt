package com.laurencerawlings.pollen.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.adapter.ArticleRecyclerAdapter
import com.laurencerawlings.pollen.model.User
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {
    companion object {
        private const val ARG_TAB_NUMBER = "tab_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): MainFragment {
            return MainFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB_NUMBER, sectionNumber)
                }
            }
        }
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var articleAdapter: ArticleRecyclerAdapter
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_TAB_NUMBER) ?: 1)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.layoutManager = LinearLayoutManager(activity)
    }

    override fun onResume() {
        super.onResume()
        if (!mainViewModel.isUpdated()) {
            if (arguments?.getInt(ARG_TAB_NUMBER) == 1) {
                val topics = User.user?.topics
                if (topics.isNullOrEmpty()) {
                    topics_error.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                } else {
                    topics_error.visibility = View.GONE
                    recycler_view.visibility = View.VISIBLE
                    update()
                }
            } else {
                update()
            }
        }
    }

    private fun update() {
        compositeDisposable.add(
            mainViewModel.articles().subscribeOn(Schedulers.io()).subscribe { articles ->
                activity?.runOnUiThread {
                    articleAdapter = ArticleRecyclerAdapter(articles.articles)
                    recycler_view.adapter = articleAdapter
                }
            })
    }
}