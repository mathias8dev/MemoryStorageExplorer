package com.mathias8dev.memoriesstoragexplorer.domain.enums

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.models.toFile
import com.mathias8dev.memoriesstoragexplorer.domain.utils.otherwise
import kotlinx.parcelize.Parcelize

private fun compareDirectoriesFirst(first: MediaInfo, second: MediaInfo, comparator: () -> Int): Int {
    val firstFile = first.toFile()
    val secondFile = second.toFile()

    // Directories should appear before files
    if (firstFile?.isDirectory == true && secondFile?.isDirectory == false) return -1
    if (firstFile?.isDirectory == false && secondFile?.isDirectory == true) return 1

    // If both are directories or both are files, compare based on provided comparator
    return comparator()
}

@Parcelize
enum class SortMode(
    @DrawableRes val iconRes: Int? = null,
    val nameRes: Int,
) : Parcelable, Comparator<MediaInfo> {

    NAME_AZ(
        iconRes = R.drawable.ic_az_sort_ascending,
        nameRes = R.string.name_az
    ) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                first.name?.compareTo(second.name.orEmpty()).otherwise(0)
            }
        }
    },
    NAME_ZA(
        iconRes = R.drawable.ic_za_sort_descending,
        nameRes = R.string.name_za
    ) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                second.name?.compareTo(first.name.orEmpty()) ?: 0
            }
        }
    },
    SIZE_SMALLER(
        iconRes = R.drawable.ic_sort_from_bottom_to_top,
        nameRes = R.string.size_smaller
    ) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                first.size.compareTo(second.size)
            }
        }
    },
    SIZE_BIGGER(
        iconRes = R.drawable.ic_sort_from_top_to_bottom,
        nameRes = R.string.size_bigger
    ) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                second.size.compareTo(first.size)
            }
        }
    },
    DATE_OLDER(
        iconRes = R.drawable.ic_time_past,
        nameRes = R.string.date_older
    ) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                first.toFile()?.lastModified()?.compareTo(second.toFile()?.lastModified().otherwise(0)) ?: 0
            }
        }
    },
    DATE_NEWER(
        iconRes = R.drawable.ic_time_forward,
        nameRes = R.string.date_newer
    ) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                second.toFile()?.lastModified()?.compareTo(first.toFile()?.lastModified().otherwise(0)) ?: 0
            }
        }
    },
    TYPE_ASCENDING(
        iconRes = R.drawable.ic_puzzle_piece,
        nameRes = R.string.type_ascending
    ) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                val extComparison = first.toFile()?.extension?.compareTo(second.toFile()?.extension.orEmpty()) ?: 0
                if (extComparison != 0) extComparison else first.name?.compareTo(second.name.orEmpty()) ?: 1
            }
        }
    },
    TYPE_DESCENDING(
        iconRes = R.drawable.ic_puzzle_piece_2,
        nameRes = R.string.type_descending
    ) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                val extComparison = second.toFile()?.extension?.compareTo(first.toFile()?.extension.orEmpty()) ?: 0
                if (extComparison != 0) extComparison else second.name?.compareTo(first.name.orEmpty()) ?: 1
            }
        }
    },
    SHUFFLED(
        iconRes = R.drawable.ic_shuffle,
        nameRes = R.string.shuffled
    ) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                listOf(-1, 0, 1).random()
            }
        }
    };

}
