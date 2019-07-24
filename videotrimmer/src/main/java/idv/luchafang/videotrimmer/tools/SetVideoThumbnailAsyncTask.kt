package idv.luchafang.videotrimmer.tools

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.widget.ImageView
import java.io.File
import java.lang.ref.WeakReference

internal class SetVideoThumbnailAsyncTask @JvmOverloads constructor(
        view: ImageView,
        private val timeMs: Long = 0L,
        private val size: Int = 512,
        private val fadeDuration: Long = 0L
) : AsyncTask<File, Void, Bitmap>() {

    private val viewRef = WeakReference<ImageView>(view)

    override fun doInBackground(vararg files: File?): Bitmap? {
        val filePath = files.takeIf { files.isNotEmpty() }
                ?.get(0)?.path
                ?: return null

        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(filePath)

            val timeUs = if (timeMs == 0L) -1 else timeMs * 1000
            val bitmap = retriever.getFrameAtTime(timeUs)
            scaleBitmap(bitmap, size)
        } catch (e: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        val view = viewRef.get() ?: return

        result?.let {
            if (fadeDuration == 0L) {
                view.setImageBitmap(it)
                return@let
            }

            val fadeOut = animateAlpha(view, 1f, 0f, fadeDuration, autoPlay = false, listener = fadeOutEndListener(view, result))
            val fadeIn = animateAlpha(view, 0f, 1f, fadeDuration, autoPlay = false)

            val animators = AnimatorSet()
            animators.playSequentially(fadeOut, fadeIn)
            animators.start()
        }
    }

    private fun fadeOutEndListener(view: ImageView, result: Bitmap): AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            view.setImageBitmap(result)
        }
    }
}