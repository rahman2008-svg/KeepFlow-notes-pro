package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class NoteColor(val hex: Long, val name: String)

val NOTE_COLORS = listOf(
    NoteColor(0xFFFFFFFF, "White"),
    NoteColor(0xFFF28B82, "Red"),
    NoteColor(0xFFFBBC04, "Orange"),
    NoteColor(0xFFFFF475, "Yellow"),
    NoteColor(0xFFCCFF90, "Green"),
    NoteColor(0xFFA7FFEB, "Teal"),
    NoteColor(0xFFCBF0F8, "Blue"),
    NoteColor(0xFFD7AEFB, "Purple"),
    NoteColor(0xFFFDCFE8, "Pink"),
    NoteColor(0xFFE8EAED, "Grey")
)

@Composable
fun ColorPicker(
    selectedColorHex: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        items(NOTE_COLORS) { noteColor ->
            // In dark mode, convert light pastel colors to subtle dark shades
            val resolvedColor = getThemeAdjustedColor(noteColor.hex, isDark)
            val isSelected = selectedColorHex == noteColor.hex

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(resolvedColor)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(noteColor.hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(20.dp),
                        tint = if (resolvedColor == Color.White && !isDark) Color.Black else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

fun getThemeAdjustedColor(hex: Long, isDark: Boolean): Color {
    if (!isDark) return Color(hex)
    // Map light pastels into rich, premium dark colors
    return when (hex) {
        0xFFFFFFFF -> Color(0xFF1E1E1E) // Slate dark
        0xFFF28B82 -> Color(0xFF5C2B29) // Red
        0xFFFBBC04 -> Color(0xFF614A19) // Orange
        0xFFFFF475 -> Color(0xFF5E541A) // Yellow
        0xFFCCFF90 -> Color(0xFF344F34) // Green
        0xFFA7FFEB -> Color(0xFF2D5E57) // Teal
        0xFFCBF0F8 -> Color(0xFF1E355B) // Blue
        0xFFD7AEFB -> Color(0xFF42275E) // Purple
        0xFFFDCFE8 -> Color(0xFF5C2538) // Pink
        0xFFE8EAED -> Color(0xFF3C4043) // Grey Dark
        else -> Color(hex)
    }
}
