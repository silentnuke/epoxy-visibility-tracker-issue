package com.silentnuke.epoxyvisibilitytracker

import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.VisibilityState
import timber.log.Timber
import java.lang.ref.WeakReference

@EpoxyModelClass(layout = R.layout.item_error_more)
abstract class ErrorMoreModel : EpoxyModelWithHolder<ErrorMoreHolder>() {

    @EpoxyAttribute
    lateinit var loadingState: LoadingState

    @EpoxyAttribute(DoNotHash)
    lateinit var callbacks: FeedAdapterCallbacks

    override fun bind(holder: ErrorMoreHolder) {
        Timber.d("bind $loadingState ErrorMoreModel(${this.hashCode()}) ErrorMoreHolder(${holder.hashCode()})")
    }

    override fun onVisibilityStateChanged(visibilityState: Int, holder: ErrorMoreHolder) {
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

class ErrorMoreHolder : KotlinEpoxyHolder() {
    val errorTextView by bind<TextView>(R.id.error)
}