package com.mathias8dev.memoriesstoragexplorer.ui.composables

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.LocalSnackbarHostState
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.utils.toLocalDateTime
import com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaInfo.MediaInfoComposable
import com.mathias8dev.memoriesstoragexplorer.ui.screens.settings.isDarkModeActivated
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toFileFormat
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toReadableSize
import kotlinx.coroutines.launch

@Composable
fun dialogBackgroundColor(): Color {
    return if (isDarkModeActivated()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
}

@Composable
fun MediaPropertiesDialog(
    media: List<MediaInfo>,
    onDismissRequest: () -> Unit
) {
    StandardDialog(
        backgroundColor = dialogBackgroundColor(),
        onDismissRequest = onDismissRequest
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onDismissRequest
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(media) { mediaInfo ->
                MediaPropertiesItem(
                    mediaInfo = mediaInfo,
                    showExpansionButton = media.size > 1
                )
            }
        }

    }
}

@Composable
internal fun MediaPropertiesItem(
    modifier: Modifier = Modifier,
    mediaInfo: MediaInfo,
    showExpansionButton: Boolean = false
) {

    var expand by rememberSaveable(mediaInfo) {
        mutableStateOf(true)
    }

    val derivedFile by remember(mediaInfo) {
        derivedStateOf {
            mediaInfo.privateContentUri?.toFile()
        }
    }

    val rotationAnimation = animateFloatAsState(
        targetValue = if (expand) 180f else 0f,
        label = "RotationAnimationLabel"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaInfoComposable(
                modifier = Modifier
                    .width(0.dp)
                    .weight(1f),
                mediaInfo = mediaInfo,
                iconSizeDp = 24.dp,
                layoutMode = LayoutMode.COMPACT,
                onClick = {},
            )

            if (showExpansionButton) {
                IconButton(
                    onClick = { expand = !expand }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotationAnimation.value)
                    )
                }
            }
        }

        AnimatedVisibility(expand) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.width(90.dp),
                        text = "Path: ",
                        textAlign = TextAlign.End,
                        fontSize = 14.sp,
                        maxLines = 1,
                    )

                    CopiableText(
                        text = mediaInfo.privateContentUri?.path ?: "No path",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Blue
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.width(90.dp),
                        text = "Content URI: ",
                        textAlign = TextAlign.End,
                        fontSize = 14.sp,
                    )

                    CopiableText(
                        text = mediaInfo.contentUri?.toString() ?: "No content URI",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Blue,
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.width(90.dp),
                        text = "Size: ",
                        textAlign = TextAlign.End,
                        fontSize = 14.sp
                    )

                    Column {
                        Text(
                            text = derivedFile?.toReadableSize() ?: "0 B",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val items = remember(derivedFile) { derivedFile?.listFiles()?.size ?: 0 }
                        Text(
                            text = if (items > 0) "($items) files" else if (derivedFile?.isDirectory == true) "One folder" else "One file",
                            fontSize = 14.sp,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.width(90.dp),
                        text = "Modified: ",
                        textAlign = TextAlign.End,
                        fontSize = 14.sp
                    )

                    Text(
                        text = derivedFile?.lastModified()?.toLocalDateTime()?.toFileFormat() ?: "No date",
                        fontSize = 14.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.width(90.dp),
                        text = "Type: ",
                        textAlign = TextAlign.End,
                        fontSize = 14.sp
                    )

                    Text(
                        text = if (derivedFile?.isDirectory == true) "Folder" else "File",
                        fontSize = 14.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun CopiableText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {

    val localClipboardManager = LocalClipboard.current
    val localSnackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()

    TextClickable(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style,
        onClick = {
            coroutineScope.launch {
                localClipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText(null, AnnotatedString(text))))
                localSnackbarHostState.showSnackbar("Copied to clipboard")
            }
        }
    )
}

@Composable
internal fun TextClickable(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    onClick: ((Int) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick) {
        detectTapGestures { pos ->
            layoutResult.value?.let { layoutResult ->
                onClick?.invoke(layoutResult.getOffsetForPosition(pos))
            }
        }
    }
    Text(
        text = text,
        modifier = modifier.then(pressIndicator),
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout?.invoke(it)
        },
        style = style
    )
}


@Composable
internal fun TextClickable(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    onClick: ((Int) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick) {
        detectTapGestures { pos ->
            layoutResult.value?.let { layoutResult ->
                onClick?.invoke(layoutResult.getOffsetForPosition(pos))
            }
        }
    }
    Text(
        text = text,
        modifier = modifier.then(pressIndicator),
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout?.invoke(it)
        },
        style = style
    )
}