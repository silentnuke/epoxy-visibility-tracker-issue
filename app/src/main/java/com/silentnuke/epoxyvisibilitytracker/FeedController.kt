package com.silentnuke.epoxyvisibilitytracker

import com.airbnb.epoxy.TypedEpoxyController
import java.lang.ref.WeakReference

class FeedController(val callbacks: FeedAdapterCallbacks) : TypedEpoxyController<FeedState>() {

    override fun buildModels(state: FeedState) {
        state.items.forEach {
            image {
                id(it)
                image(it)
                callbacks(callbacks)
            }
        }

        if (state.loadingMoreState == LoadingState.LOADING) {
            loadingMore {
                id(LOAD_MORE)
                loadingState(state.loadingMoreState)
                callbacks(callbacks)
            }
        }
        if (state.loadingMoreState == LoadingState.ERROR) {
            errorMore {
                id(LOAD_MORE)
                loadingState(state.loadingMoreState)
                callbacks(callbacks)
            }
        }
    }
}

interface FeedAdapterCallbacks {
    fun onModelFullyVisible(item: SelectedItem)
}

data class FeedState(
    val items: List<String> = emptyList(),
    val loadingMoreState: LoadingState = LoadingState.IDLE
)

fun FeedState?.isNextDoseLoading(): Boolean {
    return this?.loadingMoreState == LoadingState.LOADING
}

data class SelectedItem(val id: String, val state: CardState, val holder: WeakReference<Any>)

enum class CardState {
    LOADING, CONTENT, ERROR
}