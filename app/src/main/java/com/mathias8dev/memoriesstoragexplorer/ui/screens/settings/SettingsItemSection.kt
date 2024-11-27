package com.mathias8dev.memoriesstoragexplorer.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingsItemSection(
    modifier: Modifier = Modifier,
    sectionTitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 16.sp
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItemSection(
    modifier: Modifier = Modifier,
    sectionTitle: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sectionTitle()
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItemSection(
    modifier: Modifier = Modifier,
    @StringRes sectionTitleRes: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    SettingsItemSection(
        sectionTitle = stringResource(id = sectionTitleRes),
        modifier = modifier,
        content = content
    )
}