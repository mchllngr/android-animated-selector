package de.mchllngr.example.animated_selector_example.view

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import de.mchllngr.example.animated_selector_example.R

class DownloadStateViewAnimatedSelector @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    enum class State(val attrId: Int) {
        INITIAL(R.attr.state_initial),
        LOADING(R.attr.state_loading),
        SUCCESS(R.attr.state_success)
    }

    var state: State
        get() = getCurrentState()
        set(nextState) = setNewState(nextState)

    init {
        setImageResource(R.drawable.asl_download)
    }

    private fun getCurrentState(): State {
        // our stateSet gets added at the end, therefore it's the first entry > 0 in our drawableState
        val offsetStart = drawableState.size - State.values().size
        val position = (drawableState.size - 1 downTo offsetStart)
                .firstOrNull { drawableState[it] > 0 }
                ?.let { it - offsetStart }
                ?: -1

        return getStateForPosition(position)
    }

    private fun getStateForPosition(position: Int): State {
        return State.values().firstOrNull { position == it.ordinal } ?: State.INITIAL
    }

    private fun setNewState(nextState: State) {
        val stateSet = intArrayOf(
                getValueForState(State.INITIAL, nextState),
                getValueForState(State.LOADING, nextState),
                getValueForState(State.SUCCESS, nextState)
        )
        setImageState(stateSet, true)
    }

    private fun getValueForState(state: State, stateToBeSelected: State): Int {
        return (if (state == stateToBeSelected) 1 else -1) * state.attrId
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.currentStatePos = getCurrentState().ordinal
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        this.state = State.values()[savedState.currentStatePos]
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
