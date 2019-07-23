package idv.luchafang.videotrimmer.videoframe

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VideoFramesScrollListener(
        private val horizontalMargin: Int,
        private val callback: Callback
) : RecyclerView.OnScrollListener() {

    private var isIdle = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val adaptor = recyclerView.adapter ?: return
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val itemWidth = recyclerView.getChildAt(0)?.width ?: return

        val firstViewPos = layoutManager.findFirstCompletelyVisibleItemPosition()
        val firstView = layoutManager.findViewByPosition(firstViewPos) ?: return

        val offset = horizontalMargin + firstViewPos * itemWidth - firstView.left
        val contentWidth = itemWidth * adaptor.itemCount

        callback.onScrollVideoFrames(
                offset / contentWidth.toFloat(),
                firstViewPos,
                firstView.left
        )
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                isIdle = true
                callback.onScrollVideoFramesEnd()
            }

            RecyclerView.SCROLL_STATE_DRAGGING -> {
                if (isIdle) {
                    callback.onScrollVideoFramesStart()
                    isIdle = false
                }
            }
        }
    }

    interface Callback {
        fun onScrollVideoFramesStart()
        fun onScrollVideoFrames(offsetPercentage: Float, framePosition: Int, frameOffset: Int)
        fun onScrollVideoFramesEnd()
    }
}