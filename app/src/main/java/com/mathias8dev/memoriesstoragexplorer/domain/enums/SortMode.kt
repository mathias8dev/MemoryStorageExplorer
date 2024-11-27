package com.mathias8dev.memoriesstoragexplorer.domain.enums

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.models.toFile
import com.mathias8dev.memoriesstoragexplorer.domain.utils.otherwise
import kotlinx.parcelize.Parcelize
import java.util.Random

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

    NAME_AZ(nameRes = R.string.name_az) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                first.name?.compareTo(second.name.orEmpty()).otherwise(0)
            }
        }
    },
    NAME_ZA(nameRes = R.string.name_za) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                second.name?.compareTo(first.name.orEmpty()) ?: 0
            }
        }
    },
    SIZE_SMALLER(nameRes = R.string.size_smaller) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                first.size.compareTo(second.size)
            }
        }
    },
    SIZE_BIGGER(nameRes = R.string.size_bigger) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                second.size.compareTo(first.size)
            }
        }
    },
    DATE_OLDER(nameRes = R.string.date_older) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                first.toFile()?.lastModified()?.compareTo(second.toFile()?.lastModified().otherwise(0)) ?: 0
            }
        }
    },
    DATE_NEWER(nameRes = R.string.date_newer) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                second.toFile()?.lastModified()?.compareTo(first.toFile()?.lastModified().otherwise(0)) ?: 0
            }
        }
    },
    TYPE_ASCENDING(nameRes = R.string.type_ascending) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                val extComparison = first.toFile()?.extension?.compareTo(second.toFile()?.extension.orEmpty()) ?: 0
                if (extComparison != 0) extComparison else first.name?.compareTo(second.name.orEmpty()) ?: 1
            }
        }
    },
    TYPE_DESCENDING(nameRes = R.string.type_descending) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                val extComparison = second.toFile()?.extension?.compareTo(first.toFile()?.extension.orEmpty()) ?: 0
                if (extComparison != 0) extComparison else second.name?.compareTo(first.name.orEmpty()) ?: 1
            }
        }
    },
    SHUFFLED(nameRes = R.string.shuffled) {
        override fun compare(first: MediaInfo, second: MediaInfo): Int {
            return compareDirectoriesFirst(first, second) {
                val random = Random()
                random.nextInt(2)
            }
        }
    };

}
