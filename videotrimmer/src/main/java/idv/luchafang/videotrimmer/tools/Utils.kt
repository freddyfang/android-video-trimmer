package idv.luchafang.videotrimmer.tools

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import kotlin.math.max
import kotlin.math.roundToInt

internal fun dpToPx(context: Context, dp: Float): Float {
    val density = context.resources.displayMetrics.density
    return dp * density
}

internal fun scaleBitmap(bitmap: Bitmap, size: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val max = max(width, height)

    return if (max > size) {
        val scale = size.toFloat() / max
        val scaledWidth = (width * scale).roundToInt()
        val scaledHeight = (height * scale).roundToInt()
        Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    } else bitmap
}

internal fun animateAlpha(
    view: View,
    from: Float = view.alpha,
    to: Float,
    duration: Long,
    interpolator: Interpolator = LinearInterpolator(),
    listener: Animator.AnimatorListener? = null,
    repeatMode: Int? = null,
    repeatCount: Int = 0,
    autoPlay: Boolean = true
): Animator = ObjectAnimator.ofFloat(view, "alpha", from, to).apply {
    this.duration = duration
    this.interpolator = interpolator
    listener?.let { this.addListener(it) }

    repeatMode?.let { mode ->
        this.repeatMode = mode
        this.repeatCount = repeatCount
    }

    if (autoPlay) {
        start()
    }
}

internal fun extractVideoLength(videoPath: String): Long {
    val retriever = try {
        MediaMetadataRetriever()
            .apply { setDataSource(videoPath) }
    } catch (e: IllegalArgumentException) {
        return 0L
    }

    val length = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    retriever.release()

    return length?.toLong() ?: 0L
}