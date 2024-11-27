package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.LocalClipboardHandler
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationProgress


@Composable
fun ProgressJobsDisplay(
    modifier: Modifier = Modifier,
    progressMap: Map<String, FileOperationProgress>,
    onItemClick: (String) -> Unit,
    onAbortAll: () -> Unit
) {

    val localClipboardHandler = LocalClipboardHandler.current

    Column(
        modifier = modifier
            .wrapContentSize()
            .padding(top = 16.dp, bottom = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        progressMap.forEach { uid, info ->


        }

        // "Clear All" Button
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { onAbortAll() },
            text = "CLEAR",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}