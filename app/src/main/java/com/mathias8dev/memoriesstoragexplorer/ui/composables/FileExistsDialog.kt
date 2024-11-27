package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.ui.utils.countDown
import java.io.File


@Composable
fun FileExistsDialog(
    file: File,
    onSkip: (remember: Boolean) -> Unit,
    onReplace: (remember: Boolean) -> Unit,
    onRename: (remember: Boolean) -> Unit,
    onAbort: (remember: Boolean) -> Unit = onSkip,
) {
    var remembered by rememberSaveable {
        mutableStateOf(false)
    }

    val remainingTime by countDown(
        initialValue = 25,
        onFinished = {
            onAbort(remembered)
        }
    )


    StandardDialog(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {

        Text(
            text = buildAnnotatedString {
                append("File Exists")
                withStyle(SpanStyle(color = Color.Gray)) {
                    append(" (${remainingTime}s)")
                }
            },
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium
        )

        FileDisplay(
            modifier = Modifier.padding(top = 16.dp),
            file = file,
            size = DpSize(72.dp, 72.dp),
        )


        Spacer(modifier = Modifier.height(16.dp))

        ActionItem(
            title = "Skip",
            description = "Skip the current file",
            onClick = {
                onSkip(remembered)
            }
        )

        ActionItem(
            title = "Replace",
            description = "The new file will overwrite the existing one",
            onClick = {
                onReplace(remembered)
            }
        )

        ActionItem(
            title = "Keep both",
            description = "The new file will be renamed",
            onClick = {
                onRename(remembered)
            }
        )


        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = remembered,
                onCheckedChange = { remembered = it }
            )
            Text(
                text = "Remember",
                fontSize = 16.sp,
                lineHeight = 16.sp
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                shape = RectangleShape,
                onClick = {
                    onAbort(remembered)
                }
            ) {
                Text("ABORT")
            }
        }
    }
}

@Composable
private fun ActionItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable {
                onClick()
            }
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 5.dp, horizontal = 5.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            fontSize = 14.sp,
            lineHeight = 14.sp
        )
    }

}