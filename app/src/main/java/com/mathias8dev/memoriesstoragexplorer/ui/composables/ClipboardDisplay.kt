package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.LocalClipboardHandler
import com.mathias8dev.memoriesstoragexplorer.domain.utils.otherwise
import com.mathias8dev.memoriesstoragexplorer.domain.utils.sumOf
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntry
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntryPayload
import kotlin.random.Random


@Composable
fun ClipboardDisplay(
    modifier: Modifier = Modifier,
    clipboard: List<ClipboardEntry>, // Assuming ClipboardEntry is your data model
    onClearEntry: (ClipboardEntry) -> Unit,
    onClearAll: () -> Unit,
    onEntryClick: (ClipboardEntry) -> Unit,
    onEntryPayloadClick: (ClipboardEntry, ClipboardEntryPayload) -> Unit,
) {
    Column(
        modifier = modifier
            .wrapContentSize()
            .padding(top = 16.dp, bottom = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        clipboard.forEachIndexed { index, entry ->

            ClipboardEntryDisplay(
                index = index,
                entry = entry,
                onClearEntry = onClearEntry,
                onEntryClick = onEntryClick,
                onEntryPayloadClick = onEntryPayloadClick
            )
        }

        // "Clear All" Button
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { onClearAll() },
            text = "CLEAR",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClipboardEntryDisplay(
    modifier: Modifier = Modifier,
    index: Int = Random.nextInt(),
    entry: ClipboardEntry,
    onEntryClick: (ClipboardEntry) -> Unit,
    onClearEntry: (ClipboardEntry) -> Unit,
    onEntryPayloadClick: (ClipboardEntry, ClipboardEntryPayload) -> Unit,
) {

    val localClipboardHandler = LocalClipboardHandler.current


    var showDetails by rememberSaveable {
        mutableStateOf(false)
    }

    val started by remember(entry) {
        derivedStateOf {
            entry.status == ClipboardEntry.Status.STARTED || entry.payloads.any { it.status == ClipboardEntryPayload.Status.STARTED }
        }
    }

    val statusString by remember(started) {
        derivedStateOf {
            when {
                started -> "RUNNING"
                else -> "NOT STARTED"
            }

        }
    }

    val text = remember {
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                val intent = if (entry.intent == ClipboardEntry.Intent.CUT) "CUT${if (started) "ING" else ""}" else "COPY${if (started) "ING" else ""}"
                append(intent)
            }

            withStyle(SpanStyle(fontSize = 14.sp)) {
                if (entry.payloads.size == 1) {
                    append(" 1 file")
                } else {
                    append(" ${entry.payloads.size} files")
                }

            }
        }
    }

    val progressString by remember(started, entry) {
        derivedStateOf {

            val progress = localClipboardHandler.fileOperationsProgress?.get(entry.uid)?.progress.otherwise {
                entry.payloads.sumOf { (localClipboardHandler.fileOperationsProgress?.get(it.uid)?.progress ?: 0F) }
            } * 100

            if (started) " ($progress%)" else ""
        }
    }

    Row(
        modifier = modifier
            .combinedClickable(
                onClick = { onEntryClick(entry) },
                onLongClick = {
                    showDetails = !showDetails
                }
            )
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .width(0.dp)
                .weight(1F)
                .wrapContentHeight()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                lineHeight = 16.sp
            )
            Text(
                text = "#${index + 1} $statusString$progressString",
                fontSize = 14.sp,
                lineHeight = 14.sp
            )
            Text(
                text = "Long press to expand/collapse",
                fontSize = 12.sp,
                lineHeight = 12.sp
            )

            AnimatedVisibility(showDetails) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entry.payloads.forEachIndexed { index, payload ->
                        ClipboardMediaInfoDisplay(
                            index = index,
                            started = started,
                            entryStarted = entry.status == ClipboardEntry.Status.STARTED,
                            payload = payload,
                            onClick = {
                                onEntryPayloadClick(entry, payload)
                            }
                        )
                    }
                }
            }
        }

        IconButton(
            modifier = Modifier.align(Alignment.Top),
            onClick = { onClearEntry(entry) }
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClipboardMediaInfoDisplay(
    modifier: Modifier = Modifier,
    index: Int,
    started: Boolean = false,
    entryStarted: Boolean = false,
    payload: ClipboardEntryPayload,
    innerPaddingValues: PaddingValues = PaddingValues(0.dp),
    onClick: (ClipboardEntryPayload) -> Unit,
) {


    val file by remember(payload) {
        derivedStateOf {
            payload.mediaInfo.privateContentUri!!.toFile()
        }
    }

    val statusString by remember(payload) {
        derivedStateOf {
            if (payload.status == ClipboardEntryPayload.Status.STARTED || entryStarted) "RUNNING"
            else "NOT STARTED"
        }
    }

    Column {
        if (started) {
            Text(
                text = "#${index + 1} $statusString",
                fontSize = 14.sp,
                lineHeight = 14.sp
            )

            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = modifier
                .combinedClickable(
                    onClick = {
                        onClick(payload)
                    },
                )
                .fillMaxWidth()
                .background(
                    color = Color.Transparent
                )
                .padding(innerPaddingValues)
        ) {
            Card(modifier = Modifier.size(32.dp)) {
                FileImage(
                    file = file
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .padding(horizontal = 8.dp)
                    .width(0.dp)
                    .weight(1F),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    lineHeight = 14.sp
                )

                Text(
                    text = file.absolutePath,
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }

}

