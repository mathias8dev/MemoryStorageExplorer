package com.mathias8dev.memoriesstoragexplorer.ui.composables.clipboard

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
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntry
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntryPayload


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
