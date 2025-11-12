package com.mathias8dev.memoriesstoragexplorer.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.domain.enums.ThemeMode
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppearanceBottomSheet(
    sheetState: SheetState,
    currentSettings: AppSettings,
    onDismiss: () -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onSeedColorChanged: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // Theme Mode Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                ThemeMode.entries.forEach { mode ->
                    ThemeModeOption(
                        mode = mode,
                        isSelected = currentSettings.themeMode == mode,
                        onClick = { onThemeModeChanged(mode) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Seed Color Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Theme Color",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Text(
                    text = "Choose a color to personalize your app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 6
                ) {
                    predefinedColors.forEach { colorData ->
                        ColorOption(
                            color = colorData.color,
                            name = colorData.name,
                            isSelected = currentSettings.themeSeedColor == colorData.colorValue,
                            onClick = { onSeedColorChanged(colorData.colorValue) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = mode.toDisplayString(),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = when (mode) {
                    ThemeMode.LIGHT -> "Always use light theme"
                    ThemeMode.DARK -> "Always use dark theme"
                    ThemeMode.SYSTEM -> "Follow system settings"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ColorOption(
    color: Color,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// Helper function to calculate color luminance
private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

// Predefined color options
private data class ColorData(
    val name: String,
    val color: Color,
    val colorValue: Long
)

private val predefinedColors = listOf(
    ColorData("Purple", Color(0xFF6750A4), 0xFF6750A4),
    ColorData("Blue", Color(0xFF0061A4), 0xFF0061A4),
    ColorData("Teal", Color(0xFF006A6A), 0xFF006A6A),
    ColorData("Green", Color(0xFF386A20), 0xFF386A20),
    ColorData("Yellow", Color(0xFF685F12), 0xFF685F12),
    ColorData("Orange", Color(0xFF8B4513), 0xFF8B4513),
    ColorData("Red", Color(0xFFBA1A1A), 0xFFBA1A1A),
    ColorData("Pink", Color(0xFFC00F5C), 0xFFC00F5C),
    ColorData("Indigo", Color(0xFF4A5A92), 0xFF4A5A92),
    ColorData("Cyan", Color(0xFF006874), 0xFF006874),
    ColorData("Lime", Color(0xFF5D6F00), 0xFF5D6F00),
    ColorData("Brown", Color(0xFF775650), 0xFF775650)
)
