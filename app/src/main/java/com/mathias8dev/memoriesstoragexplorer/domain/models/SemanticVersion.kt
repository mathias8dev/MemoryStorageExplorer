package com.mathias8dev.memoriesstoragexplorer.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Parcelable, Comparable<SemanticVersion> {

    override fun compareTo(other: SemanticVersion): Int {
        val majorComparison = this.major.compareTo(other.major)
        if (majorComparison != 0) return majorComparison

        val minorComparison = this.minor.compareTo(other.minor)
        if (minorComparison != 0) return minorComparison

        return this.patch.compareTo(other.patch)
    }

    companion object {
        fun from(version: String?): SemanticVersion? {
            return version?.let {
                kotlin.runCatching {
                    val parts = version.split(".")
                    return SemanticVersion(
                        major = parts[0].toInt(),
                        minor = parts[1].toInt(),
                        patch = parts[2].toInt()
                    )
                }.getOrNull()
            }
        }
    }
}
