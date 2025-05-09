package com.mathias8dev.memoriesstoragexplorer.ui.composables.clipboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.ui.composables.StandardDialog
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntry


@Composable
fun ClipboardPasteInfoDialog(
    clipboardEntry: ClipboardEntry,
    onBackgroundClick: () -> Unit,
    onDismissRequest: () -> Unit
) {

    StandardDialog(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest
    ) {
        Text(
            "File Operation",
            fontWeight = FontWeight.Bold,
        )

        val action = remember {
            if (clipboardEntry.intent == ClipboardEntry.Intent.CUT) "Cutting" else "Copying"
        }

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "$action ${clipboardEntry.payloads.size} files",
            fontWeight = FontWeight.Bold,
        )

        LazyColumn(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            items(clipboardEntry.payloads) { (_, _, mediaInfo) ->
                val fileName = remember {
                    mediaInfo.name ?: mediaInfo.privateContentUri?.toFile()?.name
                }

                Text(fileName ?: "Unknown")
                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .wrapContentSize(),
                shape = RectangleShape,
                contentPadding = PaddingValues(8.dp),
                onClick = onDismissRequest
            ) {
                Text("ABORT", fontSize = 16.sp, lineHeight = 16.sp)
            }

            Button(
                modifier = Modifier.wrapContentSize(),
                shape = RectangleShape,
                contentPadding = PaddingValues(8.dp),
                onClick = onBackgroundClick
            ) {
                Text("BACKGROUND", fontSize = 16.sp, lineHeight = 16.sp)
            }
        }

    }
}