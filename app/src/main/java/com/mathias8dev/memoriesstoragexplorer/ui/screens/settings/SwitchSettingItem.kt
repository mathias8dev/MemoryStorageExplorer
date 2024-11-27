package com.mathias8dev.memoriesstoragexplorer.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SwitchSettingItem(
    actionTitle: String,
    actionContent: String,
    value: Boolean,
    onValueChanged: (updatedValue: Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .width(0.dp)
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = actionTitle,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 14.sp
            )
            Text(
                text = actionContent,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp
            )
        }

        Switch(
            modifier = Modifier.scale(0.8f),
            checked = value,
            onCheckedChange = onValueChanged
        )
    }
}

@Composable
fun SwitchSettingItem(
    @StringRes actionTitleRes: Int,
    @StringRes actionContentRes: Int,
    value: Boolean,
    onValueChanged: (updatedValue: Boolean) -> Unit
) {
    SwitchSettingItem(
        actionTitle = stringResource(id = actionTitleRes),
        actionContent = stringResource(id = actionContentRes),
        value = value,
        onValueChanged = onValueChanged
    )
}