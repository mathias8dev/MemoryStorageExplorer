package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.domain.models.InstalledApp


@Composable
fun InstalledAppComposable(
    modifier: Modifier = Modifier,
    app: InstalledApp,
    query: String = "",
    onClick: (InstalledApp) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable {
                onClick(app)
            }
            .fillMaxWidth()
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageLoaderComposable(
            model = app.appIcon,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp)
        )

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .width(0.dp)
                    .weight(1F),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                HighlightText(
                    text = app.name,
                    highlightWith = query,
                    maxLines = 1
                )

                Text(
                    text = (if (app.isSystemApp) "#System" else "#User") + " - ${app.packageName}",
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
            }


        }
    }
}