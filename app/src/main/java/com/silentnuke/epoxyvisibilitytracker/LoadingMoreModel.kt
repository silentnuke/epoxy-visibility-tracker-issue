package com.silentnuke.epoxyvisibilitytracker

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.VisibilityState
import timber.log.Timber
import java.lang.ref.WeakReference

const val LOAD_MORE = "loadingMore"

@EpoxyModelClass(layout = R.layout.item_loading_more)
abstract class LoadingMoreModel : EpoxyModelWithHolder<LoadingMoreHolder>() {

    @EpoxyAttribute
    lateinit var loadingState: LoadingState

    @EpoxyAttribute(DoNotHash)
    lateinit var callbacks: FeedAdapterCallbacks

    override fun bind(holder: LoadingMoreHolder) {
        Timber.d("bind $loadingState LoadingMoreModel(${this.hashCode()}) LoadingMoreHolder(${holder.hashCode()})")
        holder.loadingView.isVisible = loadingState != LoadingState.ERROR
        holder.errorTextView.isVisible = loadingState == LoadingState.ERROR
    }

    override fun onVisibilityStateChanged(visibilityState: Int, holder: LoadingMoreHolder) {
        super.onVisibilityStateChanged(visibilityState, holder)

        when (visibilityState) {
            VisibilityState.FOCUSED_VISIBLE, VisibilityState.FULL_IMPRESSION_VISIBLE -> {
                callbacks.onModelFullyVisible(
                    SelectedItem(
                        LOAD_MORE,
                        when (loadingState) {
                            LoadingState.ERROR -> CardState.ERROR
                            else -> CardState.LOADING
                        },
                        WeakReference(holder)
                    )
                )
            }
        }
    }
}

class LoadingMoreHolder : KotlinEpoxyHolder() {
    val loadingView by bind<View>(R.id.loading)
    val errorTextView by bind<TextView>(R.id.error)
}