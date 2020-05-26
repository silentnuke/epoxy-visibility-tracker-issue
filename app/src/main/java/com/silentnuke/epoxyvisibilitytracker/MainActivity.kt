package com.silentnuke.epoxyvisibilitytracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var feedStateLiveData = MutableLiveData(FeedState())
    private lateinit var layoutManager: LinearLayoutManager
    private var currentItem: SelectedItem? = null
    private lateinit var feedController: FeedController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())
        feedController = FeedController(object : FeedAdapterCallbacks {
            override fun onModelFullyVisible(item: SelectedItem) {
                Timber.d("onModelFullyVisible $item")
                setCurrentItem(item)
            }
        })
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        feed.layoutManager = layoutManager
        PagerSnapHelper().attachToRecyclerView(feed)
        val visibilityTracker = EpoxyVisibilityTracker()
        visibilityTracker.attach(feed)
        feed.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                preLoadItems()
            }
        })
        feed.setController(feedController)
        feedStateLiveData.observe(this, Observer {
            feedController.setData(it)
        })
        retry.setOnClickListener {
            currentItem?.let {
                if (it.id == LOAD_MORE) {
                    loadMore(failLoadMore.isChecked)
                } else {
                    (it.holder.get() as? ImageHolder)?.loadImage()
                }
            }
        }
        loadMore()
        failLoadMore.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                preLoadItems()
            }
        }
    }

    private fun preLoadItems() {
        val position = layoutManager.findLastVisibleItemPosition()
        if (position == RecyclerView.NO_POSITION) return
        val itemCount = layoutManager.itemCount
        if ((itemCount - position - 1) <= 5) {
            loadMore(failLoadMore.isChecked)
        }
    }

    private fun loadMore(shouldFail: Boolean = false) {
        if (feedStateLiveData.value.isNextDoseLoading()) {
            return
        }
        Timber.d("loadMore started")
        feedStateLiveData.value =
            feedStateLiveData.value!!.copy(loadingMoreState = LoadingState.LOADING)

        lifecycleScope.launchWhenCreated {
            val result = with(Dispatchers.IO) {
                delay(3_000)
                return@with if (shouldFail) {
                    null
                } else {
                    val result = mutableListOf<String>()
                    for (i in 1..(feedStateLiveData.value!!.items.size + 10)) {
                        result.add("https://i.picsum.photos/id/${i}/200/300.jpg")
                    }
                    result
                }
            }

            if (result != null) {
                Timber.d("loadMore success $result")
                feedStateLiveData.value = feedStateLiveData.value!!.copy(
                    items = result,
                    loadingMoreState = LoadingState.IDLE
                )
            } else {
                Timber.d("loadMore failed")
                feedStateLiveData.value =
                    feedStateLiveData.value!!.copy(loadingMoreState = LoadingState.ERROR)
            }
        }
    }

    private fun setCurrentItem(item: SelectedItem) {
        currentItem = item
        when (item.state) {
            CardState.LOADING -> showLoading()
            CardState.CONTENT -> showContent(item.id)
            CardState.ERROR -> showError()
        }
    }

    private fun showLoading() {
        bottom_panel.state.setText(R.string.loading)
        bottom_panel.state.isVisible = true
        bottom_panel.retry.isVisible = false
    }

    private fun showError() {
        bottom_panel.state.isVisible = false
        bottom_panel.retry.isVisible = true
    }

    private fun showContent(image: String) {
        bottom_panel.state.text = image
        bottom_panel.state.isVisible = true
        bottom_panel.retry.isVisible = false
    }
}
