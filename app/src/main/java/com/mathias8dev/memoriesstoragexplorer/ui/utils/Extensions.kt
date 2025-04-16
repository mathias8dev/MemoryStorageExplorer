package com.mathias8dev.memoriesstoragexplorer.ui.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.GetFileNameUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.GetSizeFromPathUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.utils.koinInject
import com.mathias8dev.memoriesstoragexplorer.domain.utils.tryOrNull
import com.mathias8dev.memoriesstoragexplorer.ui.composables.SelectedPathView
import de.datlag.mimemagic.MimeData
import de.datlag.mimemagic.MimeSuffix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

val File.mimeData: MimeData?
    get() = runCatching { MimeData.fromFile(this) }.getOrNull()

suspend fun File.asContentSize(): Long = withContext(Dispatchers.IO) {
    val getSizeFromPathUseCase by koinInject<GetSizeFromPathUseCase>()
    getSizeFromPathUseCase.invoke(absolutePath)
}

suspend fun File.asSelectedPathView(walk: Boolean = false): SelectedPathView? {
    val getFileNameUseCase by koinInject<GetFileNameUseCase>()
    return tryOrNull {
        if (this.exists()) {
            val foldersCount = this.listFiles()?.count { it.isDirectory } ?: 0
            val filesCount = if (walk) this.walk().count { it.isFile } else this.listFiles()?.count { it.isFile } ?: 0
            SelectedPathView(
                path = absolutePath,
                foldersCount = foldersCount,
                filesCount = filesCount,
                name = getFileNameUseCase(absolutePath)
            )
        } else null
    }
}


fun String.isCodeFile(): Boolean {
    return this.equals("json", true) ||
            this.equals(".html", true) ||
            this.equals(".htm", true) ||
            this.equals("c", true) ||
            this.equals("kt", true) ||
            this.equals("java", true) ||
            this.equals("py", true) ||
            this.equals("js", true) ||
            this.equals("css", true) ||
            this.equals("scss", true) ||
            this.equals("sass", true) ||
            this.equals("less", true) ||
            this.equals("php", true) ||
            this.equals("sql", true) ||
            this.equals("xml", true) ||
            this.equals("yaml", true) ||
            this.equals("yml", true) ||
            this.equals("md", true)
}

fun String.isPdfDocument(): Boolean {
    return this.equals("pdf", true)
}

fun String.isTorrent(): Boolean {
    return this.equals("torrent", true)
}

fun File.isMusicDirectory(): Boolean {
    return this.isDirectory &&
            (this.name.contains("audio", true) ||
                    this.name.contains("music", true) ||
                    this.name.contains("musique", true) ||
                    this.name.contains("sound", true) ||
                    this.name.contains("son", true) ||
                    this.name.contains("mp3", true) ||
                    this.name.contains("alarms", true))
}

fun String.isWordDocument() = this == MimeSuffix.DOC || this == MimeSuffix.DOCX || this == MimeSuffix.DOCM

fun String.isExcelDocument() =
    this == MimeSuffix.XLS || this == MimeSuffix.XLSX || this == MimeSuffix.XLTX || this == MimeSuffix.XLSM

fun String.isAndroidApk() = this.equals("apk", true)

fun File.apkFileIcon(context: Context): Drawable? {
    val path = this.absolutePath
    return runCatching {
        val packageInfo = context.packageManager.getPackageArchiveInfo(path, 0)

        // the secret are these two lines....
        packageInfo?.let { pi ->
            pi.applicationInfo?.sourceDir = path
            pi.applicationInfo?.publicSourceDir = path
            pi.applicationInfo?.loadIcon(context.packageManager)
        }
    }.getOrNull()
}


fun LocalDateTime.toFileFormat(): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm a")
    return this.format(formatter)
}


fun File.isImageMimeType(): Boolean {
    return this.extension == "jpg" ||
            this.extension == "png" ||
            this.extension == "jpeg" ||
            this.extension == "bmp" ||
            this.extension == "webp" ||
            (this.extension == "gif" && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) ||
            (this.extension == "heif" && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
}

fun File.isTextMimeType(): Boolean {
    return this.extension == "txt"
}

fun File.toReadableSize(): String {
    return this.length().asFileReadableSize()
}

fun File.toIconResource(context: Context): Any {
    val cachedMimeData = this.mimeData
    return when {
        this.isDirectory -> R.drawable.ic_folder
        this.extension.isTorrent() -> R.drawable.ic_utorrent_logo
        this.extension.isCodeFile() -> R.drawable.ic_code_icon
        cachedMimeData?.isDocument == true && this.extension.isPdfDocument() -> R.drawable.ic_pdf_icon
        cachedMimeData?.isDocument == true && cachedMimeData.suffix?.isWordDocument() == true -> R.drawable.ic_word_icon
        cachedMimeData?.isDocument == true && cachedMimeData.suffix?.isExcelDocument() == true -> R.drawable.ic_excel_icon
        cachedMimeData?.isText == true || cachedMimeData?.isDocument == true -> R.drawable.ic_text_icon
        cachedMimeData?.isAudio == true -> R.drawable.ic_music_icon
        this.extension.isAndroidApk() -> this.apkFileIcon(context) ?: R.drawable.ic_archives_icon
        cachedMimeData?.isArchive == true -> R.drawable.ic_archives_icon
        else -> R.drawable.ic_question_mark
    }
}

fun Number.asFileReadableSize(): String {
    val length = this.toLong()
    val kbLimit = 1024F
    val moLimit = 1024F * 1024F
    val goLimit = 1024F * 1024F * 1024F

    fun formatSize(size: Float, unit: String): String {
        return if (size.toInt().times(100) == size.times(100).roundToInt()) {
            // No decimal part, remove dot
            "${size.toInt()}$unit"
        } else {
            // Format with 2 decimal places
            String.format(Locale.getDefault(), "%.2f", size) + unit
        }
    }

    return when {
        length > goLimit -> formatSize(length / goLimit, "Go")
        length > moLimit -> formatSize(length / moLimit, "Mo")
        length > kbLimit -> formatSize(length / kbLimit, "Kb")
        else -> "$length Octets"
    }
}


suspend fun File.asContentSchemeUri(context: Context): Uri? {
    return suspendCoroutine { continuation ->
        object : MediaScannerConnection.MediaScannerConnectionClient {
            var connection: MediaScannerConnection? = null

            init {
                connection = MediaScannerConnection(context, this)
                connection?.connect()
            }

            override fun onMediaScannerConnected() {
                connection?.scanFile(absolutePath, null)
            }

            override fun onScanCompleted(path: String, uri: Uri?) {
                connection?.disconnect()

                continuation.resume(uri)
            }
        }
    }
}

fun File.isSystemFile(): Boolean {
    return !this.absolutePath.startsWith(Environment.getExternalStorageDirectory().absolutePath)
}


@Stable
fun Modifier.on(
    condition: Boolean,
    use: Modifier.(currentModifier: Modifier) -> Modifier
): Modifier {
    return if (condition) use(this) else this
}


fun Dp.toPx(): Float = (this.value * Resources.getSystem().displayMetrics.density)
fun Number.toPx(): Float = (this.toFloat() * Resources.getSystem().displayMetrics.density)

@Composable
fun Number.pxToDp() = with(LocalDensity.current) { this@pxToDp.toInt().toDp() }

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }


fun <T> Boolean.select(first: T, second: T): T = if (this) first else second

internal fun PaddingValues.copy(
    layoutDirection: LayoutDirection,
    start: Dp? = null,
    top: Dp? = null,
    end: Dp? = null,
    bottom: Dp? = null,
) = PaddingValues(
    start = start ?: calculateStartPadding(layoutDirection),
    top = top ?: calculateTopPadding(),
    end = end ?: calculateEndPadding(layoutDirection),
    bottom = bottom ?: calculateBottomPadding(),
)
