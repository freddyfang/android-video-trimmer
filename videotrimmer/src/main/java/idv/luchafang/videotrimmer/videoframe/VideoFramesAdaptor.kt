package idv.luchafang.videotrimmer.videoframe

import android.os.AsyncTask
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import idv.luchafang.videotrimmer.tools.SetVideoThumbnailAsyncTask
import java.io.File

class VideoFramesAdaptor(
        private val video: File,
        private val frames: List<Long>,
        private val frameWidth: Int
) : RecyclerView.Adapter<VideoFramesAdaptor.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(frameWidth, MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.itemView as ImageView
        val frame = frames[position]

        SetVideoThumbnailAsyncTask(view, frame)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, video)
    }

    override fun getItemCount(): Int = frames.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}