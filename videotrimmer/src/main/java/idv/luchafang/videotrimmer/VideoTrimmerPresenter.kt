package idv.luchafang.videotrimmer

import idv.luchafang.videotrimmer.data.TrimmerDraft
import idv.luchafang.videotrimmer.slidingwindow.SlidingWindowView
import idv.luchafang.videotrimmer.tools.extractVideoLength
import idv.luchafang.videotrimmer.videoframe.VideoFramesScrollListener
import java.io.File
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToLong

class VideoTrimmerPresenter : VideoTrimmerContract.Presenter,
    SlidingWindowView.Listener,
    VideoFramesScrollListener.Callback {

    private var view: VideoTrimmerContract.View? = null

    private var video: File? = null
    private var maxDuration = 30_000L
    private var minDuration = 3_000L
    private var frameCountInWindow = 10

    private var onSelectedRangeChangedListener: VideoTrimmerView.OnSelectedRangeChangedListener? = null

    private var videoLength = 0L
    private var videoWindowLength = 0L

    private var rawStartMillis = 0L
    private var rawEndMillis = 0L
    private var offsetMillis = 0L

    private var framePosition = 0
    private var frameOffset = 0

    private val startMillis
        get() = min(rawStartMillis + offsetMillis, videoLength)
    private val endMillis
        get() = min(rawEndMillis + offsetMillis, videoLength)

    /* -------------------------------------------------------------------------------------------*/
    /* Presenter */
    override fun onViewAttached(view: VideoTrimmerContract.View) {
        this.view = view
    }

    override fun onViewDetached() {
        this.view = null
    }

    /* -------------------------------------------------------------------------------------------*/
    /* Builder */
    override fun setVideo(video: File) {
        this.video = video
    }

    override fun setMaxDuration(millis: Long) {
        this.maxDuration = millis
    }

    override fun setMinDuration(millis: Long) {
        this.minDuration = millis
    }

    override fun setFrameCountInWindow(count: Int) {
        this.frameCountInWindow = count
    }

    override fun setOnSelectedRangeChangedListener(listener: VideoTrimmerView.OnSelectedRangeChangedListener) {
        this.onSelectedRangeChangedListener = listener
    }

    override fun isValidState(): Boolean {
        return video != null
                && maxDuration > 0L
                && minDuration > 0L
                && maxDuration >= minDuration
    }

    override fun show() {
        if (!isValidState()) {
            return
        }

        val video = this.video ?: return
        videoLength = extractVideoLength(video.path)

        if (videoLength < minDuration) {
            // TODO
            return
        }

        videoWindowLength = min(
            videoLength,
            maxDuration
        )

        val step = videoWindowLength / frameCountInWindow
        val frames = mutableListOf<Long>()
        for (i in 0 until videoLength step step) {
            frames.add(i)

            if (videoLength == videoWindowLength
                && frames.size == frameCountInWindow
            ) {
                break
            }
        }

        val windowWidth = view?.getSlidingWindowWidth() ?: return
        val frameWidth = windowWidth.toFloat() / frameCountInWindow

        rawStartMillis = 0L
        rawEndMillis = videoWindowLength

        view?.setupSlidingWindow()
        view?.setupAdaptor(video, frames, ceil(frameWidth).toInt())

        onSelectedRangeChangedListener?.onSelectRangeEnd(rawStartMillis, rawEndMillis)
    }

    /* -------------------------------------------------------------------------------------------*/
    /* Trimmer Draft */
    override fun getTrimmerDraft(): TrimmerDraft = TrimmerDraft(
        video?.path ?: "",
        rawStartMillis,
        rawEndMillis,
        offsetMillis,
        framePosition,
        frameOffset
    )

    override fun restoreTrimmer(draft: TrimmerDraft) {
        if (draft.rawStartMillis < 0L
            || draft.rawEndMillis <= 0L
            || draft.rawEndMillis - draft.rawStartMillis < minDuration
        ) {
            return
        }

        restoreRangeBar(draft)
        restoreFrameListOffset(draft)
    }

    private fun restoreRangeBar(draft: TrimmerDraft) {
        this.rawStartMillis = draft.rawStartMillis
        this.rawEndMillis = draft.rawEndMillis

        val left = draft.rawStartMillis / videoWindowLength.toFloat()
        val right = draft.rawEndMillis / videoWindowLength.toFloat()

        view?.restoreSlidingWindow(left, right)
    }

    private fun restoreFrameListOffset(draft: TrimmerDraft) {
        this.offsetMillis = draft.offsetMillis
        this.framePosition = draft.framePosition
        this.frameOffset = draft.frameOffset

        view?.restoreVideoFrameList(framePosition, frameOffset)
    }

    /* -------------------------------------------------------------------------------------------*/
    /* SlidingWindowView.Listener */
    override fun onDragRangeBarStart() {
        onSelectedRangeChangedListener?.onSelectRangeStart()
    }

    override fun onDragRangeBar(left: Float, right: Float): Boolean {
        calculateSelectedArea(left, right)
        val duration = rawEndMillis - rawStartMillis

        if (duration < minDuration) {
            return false
        }

        onSelectedRangeChangedListener?.onSelectRange(rawStartMillis, rawEndMillis)

        return true
    }

    override fun onDragRangeBarEnd(left: Float, right: Float) {
        calculateSelectedArea(left, right)
        onSelectedRangeChangedListener?.onSelectRangeEnd(rawStartMillis, rawEndMillis)
    }

    /* -------------------------------------------------------------------------------------------*/
    /* VideoFramesScrollListener.Callback */
    override fun onScrollVideoFramesStart() {
        if (videoWindowLength == videoLength) {
            return
        }

        onSelectedRangeChangedListener?.onSelectRangeStart()
    }

    override fun onScrollVideoFrames(offsetPercentage: Float, framePosition: Int, frameOffset: Int) {
        if (videoWindowLength == videoLength) {
            return
        }

        offsetMillis = (videoLength * offsetPercentage).roundToLong()
        onSelectedRangeChangedListener?.onSelectRange(startMillis, endMillis)

        this.framePosition = framePosition
        this.frameOffset = frameOffset
    }

    override fun onScrollVideoFramesEnd() {
        if (videoWindowLength == videoLength) {
            return
        }

        onSelectedRangeChangedListener?.onSelectRangeEnd(startMillis, endMillis)
    }

    /* -------------------------------------------------------------------------------------------*/
    /* Internal helpers */
    private fun calculateSelectedArea(left: Float, right: Float) {
        rawStartMillis = (left * videoWindowLength).roundToLong()
        rawEndMillis = (right * videoWindowLength).roundToLong()
    }
}