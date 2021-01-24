package com.aykuttasil.basicpaginationlist

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aykuttasil.basicpaginationlist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    sealed class ViewState {
        object Idle : ViewState()
        object Loading : ViewState()
        data class Error(val text: String) : ViewState()
        data class Success(val list: List<Person>) : ViewState()
    }

    private var viewState: ViewState = ViewState.Idle

    private fun updateViewState(state: ViewState) {
        viewState = state

        when (state) {
            ViewState.Idle -> {
            }
            ViewState.Loading -> {
                binding.swipeRefreshLayout.isRefreshing = true
            }
            is ViewState.Error -> {
                binding.txtError.text = state.text

                binding.swipeRefreshLayout.isRefreshing = false
                binding.txtError.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }
            is ViewState.Success -> {
                listAdapter.addItems(state.list)

                binding.swipeRefreshLayout.isRefreshing = false
                binding.txtError.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private lateinit var listLayoutManager: LinearLayoutManager
    private val listAdapter = PersonListAdapter()
    private val dataSource = DataSource()
    private var nextPage: String? = null
    private var mLoading = true
    private var mPreviousTotal = 0

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setListView()

        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                if (isRefreshing) {
                    refreshList()
                }
            }
            isRefreshing = true
        }

        dataSource.fetch(nextPage, completionHandler)
    }

    private fun setListView() {
        listLayoutManager = LinearLayoutManager(this@MainActivity)
        binding.recyclerView.apply {
            layoutManager = listLayoutManager
            adapter = listAdapter
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = recyclerView.childCount
                val totalItemCount = listLayoutManager.itemCount
                val firstVisibleItem = listLayoutManager.findFirstVisibleItemPosition()

                if (mLoading) {
                    if (totalItemCount > mPreviousTotal) {
                        mLoading = false
                        mPreviousTotal = totalItemCount
                    }
                }
                val visibleThreshold = 2
                if (!mLoading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
                    mLoading = true
                    Log.i("aaa", "Loading More...")
                    loadMore()
                }
            }
        })

    }

    private fun refreshList() {
        updateViewState(ViewState.Loading)
        nextPage = null
        listAdapter.clearList()
        mPreviousTotal = 0
        dataSource.fetch(nextPage, completionHandler)
    }

    fun loadMore() {
        dataSource.fetch(nextPage, completionHandler)
    }

    private val completionHandler = object : FetchCompletionHandler {
        override fun invoke(resp: FetchResponse?, error: FetchError?) {
            if (error != null) {
                updateViewState(ViewState.Error(error.errorDescription))
                Handler(Looper.getMainLooper()).postDelayed({
                    refreshList()
                }, 2000)
                return
            }

            if (resp != null) {
                if (resp.people.isNullOrEmpty()) {
                    updateViewState(ViewState.Error("No one is here!"))
                } else {
                    nextPage = resp.next
                    updateViewState(ViewState.Success(resp.people))
                }
            }
        }
    }
}