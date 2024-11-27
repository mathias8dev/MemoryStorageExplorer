package com.mathias8dev.memoriesstoragexplorer.ui.composables


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mathias8dev.memoriesstoragexplorer.ui.utils.on


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    cornerRadius: Dp = 8.dp,
    makeContentScrollable: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(cornerRadius),
                )
                .padding(contentPadding)
                .on(
                    condition = makeContentScrollable,
                    use = { it.verticalScroll(scrollState) }
                )
        ) {
            content()
        }
    }
}

