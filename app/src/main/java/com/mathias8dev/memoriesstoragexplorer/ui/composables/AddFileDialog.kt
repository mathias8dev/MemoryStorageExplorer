package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.R


@Composable
fun AddFileDialog(
    onSave: (String, String, Int) -> Unit,
    onDismissRequest: () -> Unit
) {

    var fileName by rememberSaveable {
        mutableStateOf("")
    }

    var extension by rememberSaveable {
        mutableStateOf("")
    }

    var count by rememberSaveable {
        mutableIntStateOf(1)
    }

    StandardDialog(
        backgroundColor = dialogBackgroundColor(),
        onDismissRequest = onDismissRequest
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.add_file),
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

        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .width(0.dp)
                    .weight(1F),
                value = fileName,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                onValueChange = { fileName = it },
                label = {
                    Text(stringResource(R.string.file_name))
                }
            )


            OutlinedTextField(
                modifier = Modifier.width(100.dp),
                value = count.toString(),
                onValueChange = {
                    count = it.toIntOrNull() ?: 0
                },
                label = {
                    Text(stringResource(R.string.count))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        OutlinedTextField(
            modifier = Modifier
                .padding(top = 16.dp),
            value = extension,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            onValueChange = { extension = it },
            label = {
                Text(stringResource(R.string.extension))
            }
        )



        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            Button(
                modifier = Modifier
                    .wrapContentSize(),
                shape = RectangleShape,
                contentPadding = PaddingValues(8.dp),
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.cancel).uppercase(),
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                )
            }
            Button(
                modifier = Modifier
                    .wrapContentSize(),
                shape = RectangleShape,
                contentPadding = PaddingValues(8.dp),
                onClick = { onSave(fileName, extension, count) }
            ) {
                Text(
                    text = stringResource(R.string.save).uppercase(),
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

