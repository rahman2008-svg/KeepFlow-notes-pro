package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: PlannerViewModel,
    taskId: Int?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTasks by viewModel.allTasks.collectAsState()
    
    // Find editing task if taskId is provided
    val taskToEdit = remember(taskId, allTasks) {
        if (taskId != null) allTasks.find { it.id == taskId } else null
    }

    LaunchedEffect(taskToEdit) {
        viewModel.startEditing(taskToEdit)
    }

    // Input States
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var category by remember { mutableStateOf("Others") }
    var priority by remember { mutableStateOf("Medium") }
    
    var isReminderEnabled by remember { mutableStateOf(false) }
    var reminderTimeState by remember { mutableStateOf<Long?>(null) }

    // Initialize inputs when editing
    LaunchedEffect(taskToEdit) {
        if (taskToEdit != null) {
            title = taskToEdit.title
            description = taskToEdit.description
            dueDate = taskToEdit.dueDate
            category = taskToEdit.category
            priority = taskToEdit.priority
            isReminderEnabled = taskToEdit.reminderTime != null
            reminderTimeState = taskToEdit.reminderTime
        }
    }

    val categories = listOf("Work", "Personal", "Health", "Education", "Shopping", "Others")
    val priorities = listOf("High", "Medium", "Low")

    val dateFormat = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (taskId == null) "Create Task" else "Edit Task", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopEditing()
                        onNavigateBack()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.saveTask(
                                    title = title,
                                    description = description,
                                    dueDate = dueDate,
                                    category = category,
                                    priority = priority,
                                    reminderTime = if (isReminderEnabled) reminderTimeState else null
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save Task")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title *") },
                placeholder = { Text("E.g. Attend team sync") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input")
            )

            // Task Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Task Description") },
                placeholder = { Text("E.g. Address bugs and check commits limit") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_desc_input")
            )

            // Due Date Card Selection via Native Picker
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val calendar = Calendar.getInstance().apply { timeInMillis = dueDate }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }
                                dueDate = selectedCal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Due Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormat.format(Date(dueDate)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Categories Selector Row
            Column {
                Text(
                    text = "Category Selection",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("category_chip_$cat")
                        )
                    }
                }
            }

            // Priorities Segmented Choose Row
            Column {
                Text(
                    text = "Task Priority",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    priorities.forEachIndexed { index, prio ->
                        SegmentedButton(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = priorities.size),
                            modifier = Modifier.testTag("priority_segment_$prio")
                        ) {
                            Text(prio)
                        }
                    }
                }
            }

            // Smart Alarm Reminder Section
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Smart Reminder Alarm",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Schedule precise alerts and notifications",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { isReminderEnabled = it },
                            modifier = Modifier.testTag("reminder_switch")
                        )
                    }

                    if (isReminderEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val nowCal = Calendar.getInstance().apply {
                                        timeInMillis = reminderTimeState ?: System.currentTimeMillis()
                                    }
                                    
                                    // First pick date, then time
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val timeCal = Calendar.getInstance().apply {
                                                timeInMillis = reminderTimeState ?: System.currentTimeMillis()
                                            }
                                            TimePickerDialog(
                                                context,
                                                { _, h, min ->
                                                    val finalCal = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, y)
                                                        set(Calendar.MONTH, m)
                                                        set(Calendar.DAY_OF_MONTH, d)
                                                        set(Calendar.HOUR_OF_DAY, h)
                                                        set(Calendar.MINUTE, min)
                                                        set(Calendar.SECOND, 0)
                                                    }
                                                    reminderTimeState = finalCal.timeInMillis
                                                },
                                                timeCal.get(Calendar.HOUR_OF_DAY),
                                                timeCal.get(Calendar.MINUTE),
                                                false
                                            ).show()
                                        },
                                        nowCal.get(Calendar.YEAR),
                                        nowCal.get(Calendar.MONTH),
                                        nowCal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val activeReminderVal = reminderTimeState
                            val displayTime = if (activeReminderVal != null) {
                                dateFormat.format(Date(activeReminderVal)) + " at " + timeFormat.format(Date(activeReminderVal))
                            } else {
                                "Tap to schedule reminder alarm"
                            }

                            Text(
                                text = displayTime,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Timing",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
