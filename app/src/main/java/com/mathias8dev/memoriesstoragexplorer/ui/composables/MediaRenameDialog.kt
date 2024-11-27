package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo


@Composable
fun MediaRenameDialog(
    mediaInfo: MediaInfo,
    onDismissRequest: () -> Unit,
    onRenameClick: (String) -> Unit,
) {
    var updatedMediaName by rememberSaveable(mediaInfo) {
        mutableStateOf(mediaInfo.name ?: "")
    }
    StandardDialog(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        makeContentScrollable = true,
        onDismissRequest = onDismissRequest
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Rename",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )

            IconButton(
                onClick = onDismissRequest
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }

        OutlinedTextField(
            modifier = Modifier.padding(top = 16.dp),
            value = updatedMediaName,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            onValueChange = { updatedMediaName = it },
            label = {
                Text("Enter the name")
            }
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Rename ${mediaInfo.name ?: "Unknown"}",
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            Button(
                modifier = Modifier
                    .wrapContentSize(),
                shape = RectangleShape,
                contentPadding = PaddingValues(8.dp),
                onClick = onDismissRequest
            ) {
                Text("CANCEL", fontSize = 16.sp, lineHeight = 16.sp)
            }
            Button(
                modifier = Modifier
                    .wrapContentSize(),
                shape = RectangleShape,
                contentPadding = PaddingValues(8.dp),
                onClick = { onRenameClick(updatedMediaName) }
            ) {
                Text(
                    text = "OK",
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}