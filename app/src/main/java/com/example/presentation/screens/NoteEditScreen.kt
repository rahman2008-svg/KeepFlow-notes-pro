package com.example.presentation.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.ChecklistItem
import com.example.domain.model.Label
import com.example.domain.model.Note
import com.example.presentation.components.ColorPicker
import com.example.presentation.components.formatReminderTime
import com.example.presentation.components.getThemeAdjustedColor
import com.example.presentation.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val labels by viewModel.labels.collectAsState()

    // Load initial note state or a blank draft
    var initialized by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isChecklist by remember { mutableStateOf(false) }
    var checklistItems by remember { mutableStateOf<List<ChecklistItem>>(emptyList()) }
    var isPinned by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }
    var colorHex by remember { mutableStateOf(0xFFFFFFFF) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    val selectedLabelIds = remember { mutableStateOf<Set<Long>>(emptySet()) }

    var originalNote: Note? by remember { mutableStateOf(null) }

    LaunchedEffect(noteId) {
        if (!initialized) {
            if (noteId != -1L) {
                // Fetch existing note from combined states or database
                val matchedNote = viewModel.filteredNotes.value.find { it.id == noteId }
                if (matchedNote != null) {
                    originalNote = matchedNote
                    title = matchedNote.title
                    content = matchedNote.content
                    isChecklist = matchedNote.isChecklist
                    checklistItems = matchedNote.checklistItems
                    isPinned = matchedNote.isPinned
                    isArchived = matchedNote.isArchived
                    colorHex = matchedNote.colorHex
                    reminderTime = matchedNote.reminderTime
                    selectedLabelIds.value = matchedNote.labelIds.toSet()
                }
            }
            initialized = true
        }
    }

    // Modal view states
    var showColorDialog by remember { mutableStateOf(false) }
    var showLabelsDialog by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val resolvedBackground = getThemeAdjustedColor(colorHex, isDark)

    // Save notes logic helper
    val saveNoteAction = {
        if (title.isNotEmpty() || content.isNotEmpty() || (isChecklist && checklistItems.isNotEmpty())) {
            val updatedNote = Note(
                id = if (noteId == -1L) 0 else noteId,
                title = title,
                content = content,
                isChecklist = isChecklist,
                checklistItems = checklistItems,
                isPinned = isPinned,
                isArchived = isArchived,
                colorHex = colorHex,
                reminderTime = reminderTime,
                hasReminderOccurred = originalNote?.hasReminderOccurred ?: false,
                labelIds = selectedLabelIds.value.toList(),
                createdAt = originalNote?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            viewModel.saveNote(updatedNote) { savedId ->
                // Ensure reminder is updated in WorkManager if reminderTime exists and modified
                if (reminderTime != null && reminderTime != originalNote?.reminderTime) {
                    viewModel.scheduleReminder(context, if (noteId == -1L) savedId else noteId, reminderTime!!)
                } else if (reminderTime == null && originalNote?.reminderTime != null) {
                    viewModel.cancelReminder(context, if (noteId == -1L) savedId else noteId)
                }
            }
        } else if (noteId != -1L) {
            // Delete note if it has been cleared completely
            viewModel.deleteNoteById(noteId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = {
                        saveNoteAction()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { isArchived = !isArchived }) {
                        Icon(
                            imageVector = if (isArchived) Icons.Default.Archive else Icons.Outlined.Archive,
                            contentDescription = "Archive Note",
                            tint = if (isArchived) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { showColorDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Change Background",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { showLabelsDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Label,
                            contentDescription = "Manage Tags",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = resolvedBackground)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = resolvedBackground,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Text vs Checklist Converter
                        IconButton(onClick = { isChecklist = !isChecklist }) {
                            Icon(
                                imageVector = if (isChecklist) Icons.Default.TextFields else Icons.Default.Checklist,
                                contentDescription = if (isChecklist) "Convert to Text note" else "Convert to Checklist note",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Reminder Alarm Builder
                        IconButton(onClick = {
                            showEditDateTimePicker(context, reminderTime) { chosenTime ->
                                reminderTime = chosenTime
                            }
                        }) {
                            Icon(
                                imageVector = if (reminderTime != null) Icons.Default.Alarm else Icons.Outlined.Alarm,
                                contentDescription = "Add Reminder",
                                tint = if (reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Quick Delete note button
                        if (noteId != -1L) {
                            IconButton(onClick = {
                                viewModel.deleteNoteById(noteId)
                                onNavigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Note",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Text(
                        text = "Edited at " + Calendar.getInstance().let {
                            android.text.format.DateFormat.format("h:mm a", it).toString()
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        containerColor = resolvedBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Displays Active Reminders Pill
            AnimatedVisibility(visible = reminderTime != null) {
                reminderTime?.let { time ->
                    Row(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                            .clickable {
                                showEditDateTimePicker(context, reminderTime) { chosenTime ->
                                    reminderTime = chosenTime
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Reminder Alert",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reminder: " + formatReminderTime(time),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { reminderTime = null },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Reminder",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Display active tags inside Note Editing Screen
            val noteLabels = labels.filter { selectedLabelIds.value.contains(it.id) }
            if (noteLabels.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    noteLabels.forEach { label ->
                        InputChip(
                            selected = true,
                            onClick = { selectedLabelIds.value = selectedLabelIds.value - label.id },
                            label = { Text(label.name, fontSize = 12.sp) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Label",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Note Title Composition field
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body content vs checklist toggle
            if (isChecklist) {
                // Checklist Builder List View
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(checklistItems) { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { isChecked ->
                                    val updated = checklistItems.toMutableList()
                                    updated[index] = item.copy(isChecked = isChecked)
                                    checklistItems = updated
                                }
                            )

                            TextField(
                                value = item.text,
                                onValueChange = { text ->
                                    val updated = checklistItems.toMutableList()
                                    updated[index] = item.copy(text = text)
                                    checklistItems = updated
                                },
                                placeholder = { Text("List item") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(onClick = {
                                val updated = checklistItems.toMutableList()
                                updated.removeAt(index)
                                checklistItems = updated
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Item",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    checklistItems = checklistItems + ChecklistItem()
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add List Item",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Add list item",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            } else {
                // Freeform text note canvas
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = {
                        Text(
                            text = "Note",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }

    // Background color modification sheet
    if (showColorDialog) {
        Dialog(onDismissRequest = { showColorDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Customize Colors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ColorPicker(
                        selectedColorHex = colorHex,
                        onColorSelected = { hex ->
                            colorHex = hex
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { showColorDialog = false }) {
                        Text("Apply")
                    }
                }
            }
        }
    }

    // Label association multi-selector dialog
    if (showLabelsDialog) {
        Dialog(onDismissRequest = { showLabelsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Apply Labels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (labels.isEmpty()) {
                        Text(
                            text = "No labels configured. Create some from the navigation drawer first!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            itemsIndexed(labels) { _, label ->
                                val labelChecked = selectedLabelIds.value.contains(label.id)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLabelIds.value = if (labelChecked) {
                                                selectedLabelIds.value - label.id
                                            } else {
                                                selectedLabelIds.value + label.id
                                            }
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = labelChecked,
                                        onCheckedChange = { checked ->
                                            selectedLabelIds.value = if (checked == true) {
                                                selectedLabelIds.value + label.id
                                            } else {
                                                selectedLabelIds.value - label.id
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = label.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showLabelsDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

// System Datetime pick trigger helper
fun showEditDateTimePicker(
    context: Context,
    initialTimeMs: Long? = null,
    onDateTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    if (initialTimeMs != null) {
        calendar.timeInMillis = initialTimeMs
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    onDateTimeSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
