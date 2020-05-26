package com.silentnuke.epoxyvisibilitytracker

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.VisibilityState
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import timber.log.Timber
import java.lang.ref.WeakReference

@EpoxyModelClass(layout = R.layout.item_image)
abstract class ImageModel : EpoxyModelWithHolder<ImageHolder>() {

    @EpoxyAttribute
    lateinit var image: String

    @EpoxyAttribute(DoNotHash)
    lateinit var callbacks: FeedAdapterCallbacks

    override fun bind(holder: ImageHolder) {
        Timber.d("bind $image ImageModel(${this.hashCode()}) ImageHolder(${holder.hashCode()})")
        holder.image = image
        holder.callbacks = callbacks
        holder.loadImage()
    }

    override fun unbind(holder: ImageHolder) {
        holder.restoreState()
    }

    override fun onVisibilityStateChanged(visibilityState: Int, holder: ImageHolder) {
        super.onVisibilityStateChanged(visibilityState, holder)

        when (visibilityState) {
            VisibilityState.FOCUSED_VISIBLE, VisibilityState.FULL_IMPRESSION_VISIBLE -> {
                holder.onFocused()
                callbacks.onModelFullyVisible(SelectedItem(image, holder.cardState!!, WeakReference(holder)))
            }
            VisibilityState.UNFOCUSED_VISIBLE -> {
                holder.onUnfocused()
            }
        }
    }

}

class ImageHolder : KotlinEpoxyHolder() {

    private val imageView by bind<ImageView>(R.id.image)
    private val loadingView by bind<ProgressBar>(R.id.loading)
    private val errorTextView by bind<TextView>(R.id.error)

    var callbacks: FeedAdapterCallbacks? = null
    var image: String? = null
    var cardState: CardState? = null
    private var isFocused: Boolean = false

    fun loadImage() {
        val currentImage = image ?: return
        showLoading()

        Glide.with(imageView)
            .asBitmap()
            .load(image)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    verifyImage(currentImage) {
                        showError()
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    verifyImage(currentImage) {
                        showContent()
                    }
                    return false
                }
            })
            .into(imageView)
    }

    fun onFocused() {
        isFocused = true
    }

    fun onUnfocused() {
        isFocused = false
    }

    fun restoreState() {
        isFocused = false
        callbacks = null
        image = null
        cardState = null
    }

    private fun showLoading() {
        cardState = CardState.LOADING
        loadingView.visibility = View.VISIBLE
        errorTextView.visibility = View.INVISIBLE
        imageView.visibility = View.INVISIBLE
        notifyVisibilityChange()
    }

    private fun showError() {
        cardState = CardState.ERROR
        loadingView.visibility = View.INVISIBLE
        errorTextView.visibility = View.VISIBLE
        imageView.visibility = View.VISIBLE
        notifyVisibilityChange()
    }

    private fun showContent() {
        cardState = CardState.CONTENT

        loadingView.visibility = View.INVISIBLE
        errorTextView.visibility = View.INVISIBLE
        imageView.visibility = View.VISIBLE
        notifyVisibilityChange()
    }

    private fun notifyVisibilityChange() {
        if (isFocused) {
            callbacks!!.onModelFullyVisible(SelectedItem(image!!, cardState!!, WeakReference(this)))
        }
    }

    private inline fun verifyImage(previous: String, function: ImageHolder.() -> Unit) {
        if (image == previous) {
            function.invoke(this)
        }
    }

}