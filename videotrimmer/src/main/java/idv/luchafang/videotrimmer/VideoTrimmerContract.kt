package idv.luchafang.videotrimmer

import idv.luchafang.videotrimmer.data.TrimmerDraft
import java.io.File

interface VideoTrimmerContract {
    interface View {
        fun getSlidingWindowWidth(): Int
        fun setupAdaptor(video: File, frames: List<Long>, frameWidth: Int)
        fun setupSlidingWindow()

        fun restoreSlidingWindow(left: Float, right: Float)
        fun restoreVideoFrameList(framePosition: Int, frameOffset: Int)
    }

    interface Presenter {
        fun onViewAttached(view: View)
        fun onViewDetached()

        fun setVideo(video: File)
        fun setMaxDuration(millis: Long)
        fun setMinDuration(millis: Long)
        fun setFrameCountInWindow(count: Int)
        fun setOnSelectedRangeChangedListener(listener: VideoTrimmerView.OnSelectedRangeChangedListener)

        fun isValidState(): Boolean
        fun show()

        fun getTrimmerDraft(): TrimmerDraft
        fun restoreTrimmer(draft: TrimmerDraft)
    }
}