package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: PlannerViewModel,
    onNavigateToAddEditTask: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedDateTasks by viewModel.selectedDateTasks.collectAsState()

    var calendarViewMode by remember { mutableStateOf("month") } // "month", "week", "day"
    val currentCalendar = remember { val c = Calendar.getInstance(); c.timeInMillis = selectedDate; mutableStateOf(c) }
    var yearMonthLabel by remember { mutableStateOf("") }

    // Helper to format Month/Year header
    LaunchedEffect(selectedDate, calendarViewMode) {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        yearMonthLabel = format.format(Date(currentCalendar.value.timeInMillis))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendar Planner", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp) },
                actions = {
                    IconButton(onClick = {
                        val today = Calendar.getInstance().timeInMillis
                        viewModel.selectDate(today)
                        currentCalendar.value = Calendar.getInstance()
                    }) {
                        Icon(imageVector = Icons.Default.Today, contentDescription = "Jump to Today")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddEditTask(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("calendar_add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // View Mode Toggles
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                SegmentedButton(
                    selected = calendarViewMode == "month",
                    onClick = { calendarViewMode = "month" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("Month")
                }
                SegmentedButton(
                    selected = calendarViewMode == "week",
                    onClick = { calendarViewMode = "week" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("Week")
                }
                SegmentedButton(
                    selected = calendarViewMode == "day",
                    onClick = { calendarViewMode = "day" },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("Day")
                }
            }

            // Calendar Navigation Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cal = currentCalendar.value
                    when (calendarViewMode) {
                        "month" -> cal.add(Calendar.MONTH, -1)
                        "week" -> cal.add(Calendar.WEEK_OF_YEAR, -1)
                        "day" -> cal.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    currentCalendar.value = cal
                    viewModel.selectDate(cal.timeInMillis)
                }) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous")
                }

                Text(
                    text = yearMonthLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = {
                    val cal = currentCalendar.value
                    when (calendarViewMode) {
                        "month" -> cal.add(Calendar.MONTH, 1)
                        "week" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                        "day" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    currentCalendar.value = cal
                    viewModel.selectDate(cal.timeInMillis)
                }) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Render calendar grid / row according to mode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                when (calendarViewMode) {
                    "month" -> MonthCalendarGrid(
                        currentCalendar = currentCalendar.value,
                        selectedDate = selectedDate,
                        allTasks = allTasks,
                        onDateSelected = { timestamp ->
                            viewModel.selectDate(timestamp)
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = timestamp
                            currentCalendar.value = cal
                        }
                    )
                    "week" -> WeekCalendarRow(
                        currentCalendar = currentCalendar.value,
                        selectedDate = selectedDate,
                        allTasks = allTasks,
                        onDateSelected = { timestamp ->
                            viewModel.selectDate(timestamp)
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = timestamp
                            currentCalendar.value = cal
                        }
                    )
                    "day" -> DayTimelineView(selectedDate = selectedDate)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header for Day Tasks
            val dayLabel = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date(selectedDate))
            Text(
                text = "Tasks for $dayLabel",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Selected Day Tasks list
            if (selectedDateTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tasks scheduled for this day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(selectedDateTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                            onEditClick = { onNavigateToAddEditTask(task.id) },
                            onDeleteClick = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCalendarGrid(
    currentCalendar: Calendar,
    selectedDate: Long,
    allTasks: List<Task>,
    onDateSelected: (Long) -> Unit
) {
    val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Setup Calendar for calculations
    val cal = remember(currentCalendar) {
        val c = Calendar.getInstance()
        c.timeInMillis = currentCalendar.timeInMillis
        c.set(Calendar.DAY_OF_MONTH, 1)
        c
    }
    
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 to 6
    val totalCells = 42 // 6 weeks * 7 days
    
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Week Header Row
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        // Month Grid Layout (Horizontal Rows of Weeks)
        var cellIndex = 0
        val monthStartCal = Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)
        }

        for (week in 0 until 6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (day in 0 until 7) {
                    val cellTime = monthStartCal.timeInMillis
                    val isCurrentMonth = monthStartCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                    val dateValue = monthStartCal.get(Calendar.DAY_OF_MONTH)
                    val isSelected = isSameDay(cellTime, selectedDate)
                    val isToday = isSameDay(cellTime, System.currentTimeMillis())
                    
                    // Task check for indicator markers
                    val hasTasks = allTasks.any { isSameDay(it.dueDate, cellTime) }
                    val activeTasks = allTasks.filter { isSameDay(it.dueDate, cellTime) && !it.isCompleted }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { onDateSelected(cellTime) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dateValue.toString(),
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.primary
                                    !isCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontSize = 14.sp
                            )
                            
                            // Indicator markers
                            if (hasTasks) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.padding(top = 1.dp)
                                ) {
                                    if (activeTasks.isNotEmpty()) {
                                        // Red dot for pending tasks
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else Color(0xFFE53935))
                                        )
                                    } else {
                                        // Green dot if all tasks on this date are completed
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else Color(0xFF43A047))
                                        )
                                    }
                                }
                            }
                        }
                    }
                    monthStartCal.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
        }
    }
}

@Composable
fun WeekCalendarRow(
    currentCalendar: Calendar,
    selectedDate: Long,
    allTasks: List<Task>,
    onDateSelected: (Long) -> Unit
) {
    val weekCal = remember(currentCalendar) {
        val c = Calendar.getInstance()
        c.timeInMillis = currentCalendar.timeInMillis
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        c
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val weekFormat = SimpleDateFormat("E", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())

        for (i in 0 until 7) {
            val cellTime = weekCal.timeInMillis
            val dayName = weekFormat.format(Date(cellTime))
            val dayNumber = dayFormat.format(Date(cellTime))
            val isSelected = isSameDay(cellTime, selectedDate)
            val isToday = isSameDay(cellTime, System.currentTimeMillis())

            val hasTasks = allTasks.any { isSameDay(it.dueDate, cellTime) }
            val activeTasks = allTasks.filter { isSameDay(it.dueDate, cellTime) && !it.isCompleted }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .height(84.dp)
                    .clickable { onDateSelected(cellTime) }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayNumber,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    
                    if (hasTasks) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White 
                                    else if (activeTasks.isNotEmpty()) Color(0xFFE53935) 
                                    else Color(0xFF43A047)
                                )
                        )
                    }
                }
            }
            weekCal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

@Composable
fun DayTimelineView(selectedDate: Long) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = "Event icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                val fullFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDate))
                Text(
                    text = "Focused Daily View",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = fullFormat,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
