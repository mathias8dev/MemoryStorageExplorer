package com.mathias8dev.memoriesstoragexplorer.domain

import android.os.Parcelable
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.ui.utils.mimeData
import kotlinx.parcelize.Parcelize
import java.io.File


interface FilterQuery : Parcelable {

    fun filter(files: List<File>): List<File>
    fun filter(file: File): Boolean
    fun filter(mediaInfo: MediaInfo, bucket: Boolean = true): Boolean
    fun filter(group: Map<String, List<MediaInfo>>, bucket: Boolean = true): Map<String, List<MediaInfo>>
    fun filter(entry: Map.Entry<String, List<MediaInfo>>, bucket: Boolean = true): Boolean

    @Parcelize
    data class SearchTerm(val term: String) : FilterQuery {
        override fun filter(files: List<File>): List<File> {
            return files.filter { it.name.contains(term, ignoreCase = true) }
        }

        override fun filter(file: File): Boolean {
            return file.name.contains(term, ignoreCase = true)
        }

        override fun filter(mediaInfo: MediaInfo, bucket: Boolean): Boolean {
            return if (bucket) mediaInfo.bucketName?.contains(term, ignoreCase = true) == true
            else mediaInfo.name?.contains(term, ignoreCase = true) == true
        }

        override fun filter(group: Map<String, List<MediaInfo>>, bucket: Boolean): Map<String, List<MediaInfo>> {
            return if (bucket) group.filter { it.key.contains(term, ignoreCase = true) }
            else group.filter { it.value.any { mediaInfo -> filter(mediaInfo, false) } }
        }

        override fun filter(entry: Map.Entry<String, List<MediaInfo>>, bucket: Boolean): Boolean {
            return if (bucket) entry.key.contains(term, ignoreCase = true)
            else entry.value.any { filter(it, false) }
        }
    }

    @Parcelize
    data class MimeTypes(val mimeTypes: List<String>) : FilterQuery {
        override fun filter(files: List<File>): List<File> {
            return files.filter { file ->
                mimeTypes.any { mimeType ->
                    file.isDirectory || file.mimeData?.mimeType?.contains(mimeType, ignoreCase = true) == true
                }
            }
        }

        override fun filter(file: File): Boolean {
            return mimeTypes.any { mimeType ->
                file.isDirectory || file.mimeData?.mimeType?.contains(mimeType, ignoreCase = true) == true
            }
        }

        override fun filter(mediaInfo: MediaInfo, bucket: Boolean): Boolean {
            return if (bucket) true
            else mediaInfo.privateContentUri?.let { filter(it.toFile()) } == true
        }

        override fun filter(group: Map<String, List<MediaInfo>>, bucket: Boolean): Map<String, List<MediaInfo>> {
            return if (bucket) group
            else group.filter { it.value.any { mediaInfo -> filter(mediaInfo, false) } }
        }

        override fun filter(entry: Map.Entry<String, List<MediaInfo>>, bucket: Boolean): Boolean {
            return if (bucket) true
            else entry.value.any { filter(it, false) }
        }
    }

    @Parcelize
    data object Nothing : FilterQuery {
        override fun filter(files: List<File>): List<File> {
            return files
        }

        override fun filter(file: File): Boolean {
            return true
        }

        override fun filter(mediaInfo: MediaInfo, bucket: Boolean): Boolean {
            return true
        }

        override fun filter(group: Map<String, List<MediaInfo>>, bucket: Boolean): Map<String, List<MediaInfo>> {
            return group
        }

        override fun filter(entry: Map.Entry<String, List<MediaInfo>>, bucket: Boolean): Boolean {
            return true
        }
    }
}


