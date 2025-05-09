package com.mathias8dev.memoriesstoragexplorer.ui.composables.clipboard

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.ui.composables.FileImage
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntryPayload


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
