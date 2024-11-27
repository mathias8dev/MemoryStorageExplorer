package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import timber.log.Timber


@Composable
fun HighlightText(
    modifier: Modifier = Modifier,
    text: String,
    highlightWith: String? = null,
    highlightStyle: SpanStyle = SpanStyle(
        color = Color(0xFFE47800),
        fontWeight = FontWeight.Normal
    ),
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
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {


    val annotatedQuery by remember(highlightWith) {
        derivedStateOf {
            val builder = AnnotatedString.Builder(text)
            highlightWith?.let {
                val startIndex = text.indexOf(it, 0, true)
                Timber.d("The startIndex is $startIndex")
                if (startIndex >= 0) {
                    val endIndex = startIndex + it.length
                    builder.addStyle(highlightStyle, startIndex, endIndex)
                }
            }

            builder.toAnnotatedString()
        }
    }

    Text(
        modifier = modifier,
        text = annotatedQuery,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight
    )

}

