package com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaInfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ShimmerAnimation


@Composable
fun MediaInfoLoadingComposable(
    modifier: Modifier = Modifier,
    innerPaddingValues: PaddingValues = PaddingValues(0.dp),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(innerPaddingValues)
    ) {
        // Placeholder for the file icon or image
        Card(modifier = Modifier.size(48.dp)) {
            ShimmerAnimation(
                modifier = Modifier.size(48.dp)
            )
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(0.dp)
                    .weight(1F),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Placeholder for the file name
                ShimmerAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp) // Simulate text height
                )

                // Placeholder for the last modified date
                ShimmerAnimation(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(15.dp) // Simulate smaller text
                )
            }

            // Placeholder for the file size or item count
            ShimmerAnimation(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(bottom = 18.dp)
                    .size(30.dp, 15.dp) // Simulate smaller text
            )
        }
    }
}