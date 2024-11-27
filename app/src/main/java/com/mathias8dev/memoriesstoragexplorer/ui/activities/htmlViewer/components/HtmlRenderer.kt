package com.mathias8dev.memoriesstoragexplorer.ui.activities.htmlViewer.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.domain.utils.otherwise
import com.mathias8dev.memoriesstoragexplorer.ui.composables.TextClickable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import timber.log.Timber
import java.util.Locale

internal const val URL_TAG = "url_tag"

@Composable
fun HtmlRenderer(
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
    style: TextStyle = LocalTextStyle.current,
    linkClicked: ((String) -> Unit)? = null,
) {
    val annotatedTextPair = remember(text) {
        parseHtmlToAnnotatedString(text)
    }



    TextClickable(
        modifier = modifier.fillMaxWidth(),
        text = annotatedTextPair.second,
        style = style.copy(
            lineHeight = TextUnit.Unspecified
        ),
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = onTextLayout,
        onClick = {
            annotatedTextPair.second
                .getStringAnnotations(URL_TAG, it, it)
                .firstOrNull()
                ?.let { stringAnnotation -> linkClicked?.invoke(stringAnnotation.item) }
        }

    )
}

internal object DefaultFontSize {
    var value: Float = 16F
}

fun parseHtmlToAnnotatedString(html: String): Pair<Number, AnnotatedString> {
    Timber.d("The html is $html")
    val document = Jsoup.parse(html)
    if (document.body().children().isEmpty()) return DefaultFontSize.value to AnnotatedString(html)
    val builder = AnnotatedString.Builder()
    val maxFontSize = DefaultFontSize

    document.body().children().forEach { element ->
        processElement(element, builder)
    }

    return DefaultFontSize.value to builder.toAnnotatedString()
}


fun processElement(element: Element, builder: AnnotatedString.Builder) {
    val spanStyle = element.attr("style").takeIf { it.isNotBlank() }?.let {
        parseStyle(it)
    } ?: SpanStyle()

    val textAlign = element.attr("style").takeIf { it.isNotBlank() }?.let {
        val alignString = it.styleToStyleMap()["text-align"]
        when (alignString) {
            "center" -> TextAlign.Center
            "right" -> TextAlign.End
            "left" -> TextAlign.Start
            else -> TextAlign.Unspecified
        }
    } ?: TextAlign.Unspecified

    builder.withStyle(style = spanStyle) {
        if (element.isSpaceHandleBlockElement()) {
            builder.append("\n")
        }

        when (element.tagName()) {
            "div" -> {
                builder.withStyle(
                    ParagraphStyle(
                        textAlign = textAlign
                    )
                ) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }

            "p" -> {
                builder.withStyle(
                    ParagraphStyle(
                        textAlign = textAlign
                    )
                ) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }

            "li" -> {
                builder.withStyle(
                    ParagraphStyle(
                        textAlign = textAlign
                    )
                ) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }

            "ul" -> {
                builder.withStyle(
                    ParagraphStyle(
                        textIndent = TextIndent(20.sp, 20.sp),
                        textAlign = textAlign
                    )
                ) {

                    val children = element.children()
                    children.forEachIndexed { index, li ->
                        builder.append("•\t\t")
                        li.childNodes().forEach { child ->
                            processNode(child, builder)
                        }
                        if (index < children.size - 1) append("\n")
                    }
                }
            }

            "ol" -> {
                builder.withStyle(
                    ParagraphStyle(
                        textIndent = TextIndent(20.sp, 20.sp),
                        textAlign = textAlign
                    )
                ) {

                    val children = element.children()
                    children.forEachIndexed { index, li ->
                        append("${index + 1}.\t\t")
                        li.childNodes().forEach { child ->
                            processNode(child, builder)
                        }
                        if (index < children.size - 1) append("\n")
                    }
                }
            }

            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                val fontSize = when (element.tagName()) {
                    "h1" -> 24.sp
                    "h2" -> 22.sp
                    "h3" -> 20.sp
                    "h4" -> 18.sp
                    "h5" -> 16.sp
                    "h6" -> 14.sp
                    else -> 16.sp
                }
                builder.withStyle(
                    ParagraphStyle(
                        textAlign = textAlign
                    )
                ) {
                    withStyle(
                        SpanStyle(
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                        ).merge(spanStyle)
                    ) {
                        element.childNodes().forEach { child ->
                            processNode(child, builder)
                        }
                    }
                }
                DefaultFontSize.value = fontSize.value
            }

            "blockquote" -> builder.withStyle(
                style = ParagraphStyle(
                    textAlign = textAlign,
                )
            ) {
                builder.withStyle(style = spanStyle.copy(fontStyle = FontStyle.Italic)) {
                    append("“")
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                    append("”")
                }
            }

            "b", "strong" -> {
                builder.withStyle(style = spanStyle.copy(fontWeight = FontWeight.Bold)) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }

            "i", "em", "cite", "dfn" -> {
                builder.withStyle(style = spanStyle.copy(fontStyle = FontStyle.Italic)) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }

            "a" -> {
                val link = element.attr("href")
                builder.pushStringAnnotation(
                    tag = URL_TAG,
                    annotation = link,
                )
                builder.withStyle(
                    style = SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
                builder.pop()
            }

            "sup" -> {
                builder.withStyle(style = spanStyle.copy(baselineShift = BaselineShift.Superscript)) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }

            "sub" -> {
                builder.withStyle(style = spanStyle.copy(baselineShift = BaselineShift.Subscript)) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }


            "big" -> builder.withStyle(style = SpanStyle(fontSize = 20.sp).merge(spanStyle)) {
                element.childNodes().forEach { child ->
                    processNode(child, builder)
                }
            }

            "small" -> builder.withStyle(style = SpanStyle(fontSize = 12.sp).merge(spanStyle)) {
                element.childNodes().forEach { child ->
                    processNode(child, builder)
                }
            }

            "pre" -> {
                builder.withStyle(
                    style = ParagraphStyle(
                        textAlign = textAlign,
                    )
                ) {
                    builder.withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color.LightGray
                        ).merge(spanStyle)
                    ) {
                        element.childNodes().forEach { child ->
                            processNode(child, builder)
                        }
                    }
                }
            }

            "code", "tt" -> builder.withStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color.LightGray
                ).merge(spanStyle)
            ) {
                element.childNodes().forEach { child ->
                    processNode(child, builder)
                }
            }


            "br" -> builder.append("\n")

            "u" -> {
                builder.withStyle(
                    style = SpanStyle(textDecoration = TextDecoration.Underline).merge(spanStyle)
                ) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }

            "font" -> {
                val color = element.attr("color")
                val parsedColor = kotlin.runCatching { Color(android.graphics.Color.parseColor(color)) }.getOrNull()
                    ?: Color.Unspecified
                val size = element.attr("size").toFloatOrNull()?.sp ?: 16.sp
                if (size.value > DefaultFontSize.value) DefaultFontSize.value = size.value
                builder.withStyle(
                    style = SpanStyle(
                        color = parsedColor,
                        fontSize = size
                    ).merge(spanStyle)
                ) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }

            "span" -> {
                builder.withStyle(style = spanStyle) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }


            else -> {
                builder.withStyle(style = spanStyle) {
                    element.childNodes().forEach { child ->
                        processNode(child, builder)
                    }
                }
            }
        }

        if (element.isSpaceHandleBlockElement() && !element.isChildOfSpaceHandleBlockElement() && element.notSucceededBySpaceHandleBlockElement()) {
            builder.append("\n")
        }

    }

}


internal fun String.styleToStyleMap(): Map<String, String> {
    return this.split(";").mapNotNull { declaration ->
        val parts = declaration.split(":").map { it.trim() }
        if (parts.size == 2) parts[0] to parts[1] else null
    }.toMap()
}

fun parseStyle(style: String): SpanStyle {
    val styleMap = style.styleToStyleMap()

    var spanStyle = SpanStyle()

    styleMap.forEach { (property, value) ->
        spanStyle = when (property.lowercase(Locale.getDefault())) {
            "color" -> spanStyle.copy(color = Color(android.graphics.Color.parseColor(value)))
            "font-size" -> {
                val size = value.removeSuffix("px").toFloatOrNull()
                size?.let { if (it > DefaultFontSize.value) DefaultFontSize.value = it }
                if (size != null) spanStyle.copy(fontSize = size.sp) else spanStyle
            }

            "font-weight" -> spanStyle.copy(
                fontWeight = when (value) {
                    "bold" -> FontWeight.Bold
                    else -> FontWeight.Normal
                }
            )

            "font-style" -> spanStyle.copy(
                fontStyle = when (value) {
                    "italic" -> FontStyle.Italic
                    else -> FontStyle.Normal
                }
            )

            "text-decoration" -> spanStyle.copy(
                textDecoration = when (value) {
                    "underline" -> TextDecoration.Underline
                    "line-through" -> TextDecoration.LineThrough
                    else -> null
                }
            )

            "background-color" -> spanStyle.copy(
                background = Color(
                    android.graphics.Color.parseColor(
                        value
                    )
                )
            )

            else -> spanStyle
        }
    }

    return spanStyle
}

fun processNode(node: org.jsoup.nodes.Node, builder: AnnotatedString.Builder) {
    when (node) {
        is TextNode -> {
            builder.append(node.text())
            Timber.d("On append text ${node}")
        }

        is Element -> processElement(node, builder)
    }
}


fun Element.notPrecededBySpaceHandleBlockElement(): Boolean {
    return this.previousElementSibling()?.let { !it.isSpaceHandleBlockElement() }.otherwise(true)
}

fun Element.isBlockElement(): Boolean {
    return this.tagName() in arrayOf("ul", "ol", "li", "blockquote", "pre", "div", "p")
}

fun Element.notSucceededBySpaceHandleBlockElement(): Boolean {
    return this.nextElementSibling()?.let { !it.isSpaceHandleBlockElement() }.otherwise(true)
}

fun Element.isSpaceHandleBlockElement(): Boolean {
    return this.tagName() in arrayOf("ul", "ol", "blockquote", "pre", "p")
}

fun Element.isChildOfSpaceHandleBlockElement(): Boolean {
    return this.parent()?.isSpaceHandleBlockElement().otherwise(false)
}