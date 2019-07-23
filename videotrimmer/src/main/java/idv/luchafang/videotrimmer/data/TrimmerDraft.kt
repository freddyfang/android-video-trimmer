package idv.luchafang.videotrimmer.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrimmerDraft(
    val path: String,
    val rawStartMillis: Long,
    val rawEndMillis: Long,
    val offsetMillis: Long,
    val framePosition: Int = 0,
    val frameOffset: Int = 0,
    val createdTime: Long = System.currentTimeMillis()
): Parcelable, Comparable<TrimmerDraft> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrimmerDraft

        return path == other.path
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun compareTo(other: TrimmerDraft): Int {
        return when {
            this === other -> 0
            this.createdTime == other.createdTime -> 0
            this.createdTime < other.createdTime -> 1
            else -> -1
        }
    }
}