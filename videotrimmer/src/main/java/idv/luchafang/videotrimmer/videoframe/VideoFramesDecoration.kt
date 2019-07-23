package idv.luchafang.videotrimmer.videoframe

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VideoFramesDecoration(
        private val horizontalMargin: Int,
        @ColorInt private val overlayColor: Int
) : RecyclerView.ItemDecoration() {

    private val overlayPaint = Paint().apply {
        color = overlayColor
        isAntiAlias = true
    }

    /* -------------------------------------------------------------------------------------------*/
    /* ItemDecoration */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val adaptor = parent.adapter ?: return
        when (parent.getChildAdapterPosition(view)) {
            0 ->
                outRect.left = horizontalMargin
            adaptor.itemCount - 1 ->
                outRect.right = horizontalMargin
        }
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return
        drawStartOverlay(canvas, parent, layoutManager)
        drawEndOverlay(canvas, parent, layoutManager)
    }

    /* -------------------------------------------------------------------------------------------*/
    /* Draw */
    private fun drawStartOverlay(canvas: Canvas, parent: RecyclerView, layoutManager: LinearLayoutManager) {
        val firstViewPos = layoutManager.findFirstVisibleItemPosition()
        val firstView = layoutManager.findViewByPosition(firstViewPos) ?: return

        val start = firstView.left
        val end = horizontalMargin

        drawOverlay(canvas, parent, start.toFloat(), end.toFloat())
    }

    private fun drawEndOverlay(canvas: Canvas, parent: RecyclerView, layoutManager: LinearLayoutManager) {
        val lastViewPos = layoutManager.findLastVisibleItemPosition()
        val lastView = layoutManager.findViewByPosition(lastViewPos) ?: return

        val start = parent.width - horizontalMargin
        val end = lastView.right

        drawOverlay(canvas, parent, start.toFloat(), end.toFloat())
    }

    private fun drawOverlay(canvas: Canvas, parent: RecyclerView, start: Float, end: Float) {
        if (end > start) {
            canvas.drawRect(
                    start,
                    parent.paddingTop.toFloat(),
                    end,
                    parent.height.toFloat() - parent.paddingBottom,
                    overlayPaint
            )
        }
    }
}