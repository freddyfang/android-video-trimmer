package idv.luchafang.videotrimmer

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout.HORIZONTAL
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import idv.luchafang.videotrimmer.data.TrimmerDraft
import idv.luchafang.videotrimmer.slidingwindow.SlidingWindowView
import idv.luchafang.videotrimmer.tools.dpToPx
import idv.luchafang.videotrimmer.videoframe.VideoFramesAdaptor
import idv.luchafang.videotrimmer.videoframe.VideoFramesDecoration
import idv.luchafang.videotrimmer.videoframe.VideoFramesScrollListener
import kotlinx.android.synthetic.main.layout_video_trimmer.view.*
import java.io.File
import kotlin.math.roundToInt

class VideoTrimmerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle), VideoTrimmerContract.View {

    @DrawableRes
    private var leftBarRes: Int = R.drawable.trimmer_left_bar
    @DrawableRes
    private var rightBarRes: Int = R.drawable.trimmer_right_bar

    private var barWidth: Float = dpToPx(context, 10f)
    private var borderWidth: Float = 0f

    @ColorInt
    private var borderColor: Int = Color.BLACK
    @ColorInt
    private var overlayColor: Int = Color.argb(120, 183, 191, 207)

    private var presenter: VideoTrimmerContract.Presenter? = null
    private var adaptor: VideoFramesAdaptor? = null

    /* -------------------------------------------------------------------------------------------*/
    /* Initialize */
    init {
        inflate(context, R.layout.layout_video_trimmer, this)
        obtainAttributes(attrs)
        initViews()
    }

    private fun obtainAttributes(attrs: AttributeSet?) {
        attrs ?: return

        val array = resources.obtainAttributes(attrs, R.styleable.VideoTrimmerView)
        try {
            leftBarRes = array.getResourceId(R.styleable.VideoTrimmerView_vtv_window_left_bar, leftBarRes)
            slidingWindowView.leftBarRes = leftBarRes

            rightBarRes = array.getResourceId(R.styleable.VideoTrimmerView_vtv_window_right_bar, rightBarRes)
            slidingWindowView.rightBarRes = rightBarRes

            barWidth = array.getDimension(R.styleable.VideoTrimmerView_vtv_window_bar_width, barWidth)
            slidingWindowView.barWidth = barWidth

            borderWidth = array.getDimension(R.styleable.VideoTrimmerView_vtv_window_border_width, borderWidth)
            slidingWindowView.borderWidth = borderWidth

            borderColor = array.getColor(R.styleable.VideoTrimmerView_vtv_window_border_color, borderColor)
            slidingWindowView.borderColor = borderColor

            overlayColor = array.getColor(R.styleable.VideoTrimmerView_vtv_overlay_color, overlayColor)
            slidingWindowView.overlayColor = overlayColor
        } finally {
            array.recycle()
        }
    }

    private fun initViews() {
        videoFrameListView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    }

    /* -------------------------------------------------------------------------------------------*/
    /* Attach / Detach */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter = obtainVideoTrimmerPresenter()
            .apply { onViewAttached(this@VideoTrimmerView) }
        onPresenterCreated()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter?.onViewDetached()
        presenter = null
    }

    private fun onPresenterCreated() {
        presenter?.let {
            slidingWindowView.listener = presenter as SlidingWindowView.Listener

            val horizontalMargin = (dpToPx(context, 11f) + barWidth).roundToInt()
            val decoration = VideoFramesDecoration(horizontalMargin, overlayColor)
            val scrollListener = VideoFramesScrollListener(
                horizontalMargin,
                presenter as VideoFramesScrollListener.Callback
            )

            videoFrameListView.addItemDecoration(decoration)
            videoFrameListView.addOnScrollListener(scrollListener)
        }
    }

    /* -------------------------------------------------------------------------------------------*/
    /* Public APIs */
    fun setVideo(video: File): VideoTrimmerView {
        presenter?.setVideo(video)
        return this
    }

    fun setMaxDuration(millis: Long): VideoTrimmerView {
        presenter?.setMaxDuration(millis)
        return this
    }

    fun setMinDuration(millis: Long): VideoTrimmerView {
        presenter?.setMinDuration(millis)
        return this
    }

    fun setFrameCountInWindow(count: Int): VideoTrimmerView {
        presenter?.setFrameCountInWindow(count)
        return this
    }

    fun setOnSelectedRangeChangedListener(listener: OnSelectedRangeChangedListener): VideoTrimmerView {
        presenter?.setOnSelectedRangeChangedListener(listener)
        return this
    }

    fun setExtraDragSpace(spaceInPx: Float): VideoTrimmerView {
        slidingWindowView.extraDragSpace = spaceInPx
        return this
    }

    fun show() {
        presenter?.show()
    }

    fun getTrimmerDraft(): TrimmerDraft? = presenter?.getTrimmerDraft()

    fun restoreTrimmer(draft: TrimmerDraft) {
        presenter?.restoreTrimmer(draft)
    }

    /* -------------------------------------------------------------------------------------------*/
    /* VideoTrimmerContract.View */
    override fun getSlidingWindowWidth(): Int {
        val screenWidth = resources.displayMetrics.widthPixels
        val margin = dpToPx(context, 11f)
        return screenWidth - 2 * (margin + barWidth).roundToInt()
    }

    override fun setupAdaptor(video: File, frames: List<Long>, frameWidth: Int) {
        adaptor = VideoFramesAdaptor(video, frames, frameWidth).also {
            videoFrameListView.adapter = it
        }
    }

    override fun setupSlidingWindow() {
        slidingWindowView.reset()
    }

    override fun restoreSlidingWindow(left: Float, right: Float) {
        slidingWindowView.setBarPositions(left, right)
    }

    override fun restoreVideoFrameList(framePosition: Int, frameOffset: Int) {
        val layoutManager = videoFrameListView.layoutManager as? LinearLayoutManager ?: return
        layoutManager.scrollToPositionWithOffset(framePosition, frameOffset)
    }

    /* -------------------------------------------------------------------------------------------*/
    /* Listener */
    interface OnSelectedRangeChangedListener {
        fun onSelectRangeStart()
        fun onSelectRange(startMillis: Long, endMillis: Long)
        fun onSelectRangeEnd(startMillis: Long, endMillis: Long)
    }
}