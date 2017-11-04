package de.mchllngr.example.animated_selector_example.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.provider.Settings
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.Animatable2Compat
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import de.mchllngr.example.animated_selector_example.R

class DownloadStateView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private val LOADING_ANIMATION_DURATION_MILLIS = 3999
    }

    enum class State { INITIAL, LOADING, SUCCESS }

    private var currentState: State = State.INITIAL
    // We want to begin the loading-to-success animation only when the progress bar is at its initial start.
    // We achieve this by keeping track of the time the loading animation was started and by delaying the start of the
    // loading-to-success animation manually here. It's slightly hacky, but unfortunately there isn't much else we can do.
    private var loadingStartTimeMillis: Long = -1
    private var transitionRunnable: Runnable? = null

    var state: State
        get() = currentState
        set(nextState) = setState(nextState, false)

    init {
        setState(State.INITIAL, true)
    }

    private fun setState(nextState: State, force: Boolean) {
        if (!force && currentState == nextState) {
            return
        }
        resetView()
        updateState(nextState)
    }

    private fun resetView() {
        removeOldCallbacks()

        // reset view to current state (for when states gets changed to fast)
        when (currentState) {
            State.LOADING -> {
                // do not reset loading, because this would break the animation
            }
            State.INITIAL, State.SUCCESS -> {
                loadingStartTimeMillis = -1
                setImageDrawable(getStateImage(currentState))
                // no need to start a potential animation, because the (transition to the) next image would be set immediately anyway
            }
        }
    }

    private fun removeOldCallbacks() {
        if (transitionRunnable != null) removeCallbacks(transitionRunnable)
        transitionRunnable = null

        if (drawable != null && drawable is AnimatedVectorDrawableCompat)
            (drawable as AnimatedVectorDrawableCompat).clearAnimationCallbacks()
    }

    private fun updateState(nextState: State) {
        val nextImage = getStateImage(nextState)
        val transition = getTransitionAnimation(nextState)

        val runnable = Runnable {
            // prepare transition-animation and start if exists
            if (transition != null) {
                setImageDrawable(transition)
                transition.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        // set the next image at the end of the animation
                        setStateImage(nextState, nextImage)
                    }
                })
                transition.start()
            }
            // set the next image when no transition exists
            else {
                setStateImage(nextState, nextImage)
            }
        }

        // if a transition exists and the currentState is 'loading' delay the start so that the transition can start when the loading animation is in its initial state
        if (transition != null && currentState == State.LOADING && loadingStartTimeMillis != -1L) {
            val animatorDurationScale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
            val loadingAnimationDurationWithAnimatorScaleMillis = Math.round(LOADING_ANIMATION_DURATION_MILLIS * animatorDurationScale)
            val delayMillis = loadingAnimationDurationWithAnimatorScaleMillis - (System.currentTimeMillis() - loadingStartTimeMillis) % loadingAnimationDurationWithAnimatorScaleMillis
            postDelayed(runnable, delayMillis)
            transitionRunnable = runnable
        }
        // otherwise run now
        else {
            runnable.run()
        }
        currentState = nextState
    }

    @Suppress("RedundantExplicitType")
    private fun getTransitionAnimation(nextState: State): AnimatedVectorDrawableCompat? {
        @DrawableRes var transitionId: Int = 0
        if (currentState == State.INITIAL && nextState == State.LOADING)
            transitionId = R.drawable.avd_download_transition_initial_to_loading
        else if (currentState == State.LOADING && nextState == State.SUCCESS)
            transitionId = R.drawable.avd_download_transition_loading_to_success
        return if (transitionId > 0) AnimatedVectorDrawableCompat.create(context, transitionId) else null
    }

    private fun getStateImage(state: State): Drawable? {
        // get the image for the state (can be animatable)
        return when (state) {
            State.INITIAL -> VectorDrawableCompat.create(context.resources, R.drawable.vd_download_initial, context.theme)
            State.LOADING -> AnimatedVectorDrawableCompat.create(context, R.drawable.avd_download_loading)
            State.SUCCESS -> VectorDrawableCompat.create(context.resources, R.drawable.vd_download_success, context.theme)
        }
    }

    private fun setStateImage(nextState: State, nextImage: Drawable?) {
        setImageDrawable(nextImage)

        // start the animation if the next image is animatable
        if (nextImage != null && nextImage is AnimatedVectorDrawableCompat) {
            // if loading repeat animation and save animation start time
            loadingStartTimeMillis = if (nextState != State.LOADING) -1 else {
                nextImage.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        post { nextImage.start() }
                    }
                })
                System.currentTimeMillis()
            }
            nextImage.start()
        }
    }

    override fun onDetachedFromWindow() {
        removeOldCallbacks()
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.currentStatePos = currentState.ordinal
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        currentState = State.values()[savedState.currentStatePos]
        setState(currentState, true)
    }

    private class SavedState : View.BaseSavedState {

        companion object {
            @Suppress("unused")
            @JvmField val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

                override fun createFromParcel(input: Parcel): SavedState {
                    return SavedState(input)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }

        var currentStatePos = 0

        constructor(superState: Parcelable) : super(superState)

        internal constructor(input: Parcel) : super(input) {
            currentStatePos = input.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(currentStatePos)
        }
    }
}
