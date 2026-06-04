package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PlannerViewModel,
    onNavigateToAddEditTask: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayTasks by viewModel.todayTasks.collectAsState()
    val upcomingTasks by viewModel.upcomingTasks.collectAsState()
    val productivityScore by viewModel.productivityScore.collectAsState()
    val weeklySummary by viewModel.weeklySummary.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    val nextReminderText = remember(allTasks) {
        val futureReminders = allTasks.filter { !it.isCompleted && it.reminderTime != null && it.reminderTime!! > System.currentTimeMillis() }
        if (futureReminders.isEmpty()) {
            "No alerts scheduled"
        } else {
            val nextRem = futureReminders.minByOrNull { it.reminderTime!! }
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            nextRem?.reminderTime?.let { timeFormat.format(Date(it)) } ?: "No alerts scheduled"
        }
    }

    val currentFocusTask = remember(todayTasks, upcomingTasks) {
        todayTasks.firstOrNull { !it.isCompleted } ?: upcomingTasks.firstOrNull { !it.isCompleted }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Smart Day Planner X",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
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
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("add_task_fab")
                    .padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Task"
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Bento Header / Profile greeting
            item {
                BentoProfileHeader()
            }

            // Bento Grid holding Productivity, Tasks count, Alerts, Focus object, and Weekly Summary Chart
            item {
                BentoGrid(
                    productivityScore = productivityScore,
                    tasksTodayCount = todayTasks.size,
                    nextReminderText = nextReminderText,
                    focusTask = currentFocusTask,
                    onToggleFocusComplete = { task ->
                        viewModel.toggleTaskCompletion(task)
                    },
                    weeklySummary = weeklySummary
                )
            }

            // Section Headline: Today's Agenda
            item {
                Text(
                    text = "Today's Agenda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (todayTasks.isEmpty()) {
                item {
                    EmptyAgendaPlaceholder(onActionClick = { onNavigateToAddEditTask(null) })
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    TaskCardItem(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                        onEditClick = { onNavigateToAddEditTask(task.id) },
                        onDeleteClick = { viewModel.deleteTask(task) }
                    )
                }
            }

            // Section Headline: Upcoming Schedule
            item {
                Text(
                    text = "Upcoming Plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (upcomingTasks.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "All caught up! No upcoming tasks.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(upcomingTasks.take(5), key = { it.id }) { task ->
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

private fun getGreeting(): String {
    val cal = Calendar.getInstance()
    return when (cal.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "GOOD MORNING"
        in 12..16 -> "GOOD AFTERNOON"
        else -> "GOOD EVENING"
    }
}

@Composable
fun BentoProfileHeader() {
    val greeting = remember { getGreeting() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Abdur Rahman",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // Initial badge "AR" inside circles
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isSystemInDarkTheme()) Color(0xFF4F378B) else Color(0xFFEADDFF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AR",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isSystemInDarkTheme()) Color(0xFFEADDFF) else Color(0xFF21005D)
            )
        }
    }
}

@Composable
fun BentoGrid(
    productivityScore: Int,
    tasksTodayCount: Int,
    nextReminderText: String,
    focusTask: Task?,
    onToggleFocusComplete: (Task) -> Unit,
    weeklySummary: Map<Int, Int>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Left Card (Productivity), Right Column (Tasks Today AND Next Alert)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Productivity Score Card (Takes height of two cards)
            ProductivityBentoCard(
                score = productivityScore,
                modifier = Modifier.weight(1f).height(190.dp)
            )

            // Right Column holding 'Tasks Today' and 'Next Alert'
            Column(
                modifier = Modifier.weight(1f).height(190.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TasksTodayBentoCard(
                    count = tasksTodayCount,
                    modifier = Modifier.weight(1f)
                )
                NextReminderBentoCard(
                    timeText = nextReminderText,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Today's Focus Card (Wide card)
        TodayFocusBentoCard(
            task = focusTask,
            onToggleComplete = { focusTask?.let { onToggleFocusComplete(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        // Row 3: Weekly Progress Chart Card (Wide card)
        WeeklyChartBentoCard(
            weeklySummary = weeklySummary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ProductivityBentoCard(
    score: Int,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSystemInDarkTheme()) Color(0xFF381E72) else Color(0xFFD0BCFF)
    val contentColor = if (isSystemInDarkTheme()) Color(0xFFEADDFF) else Color(0xFF21005D)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Lightning Bolt icon container
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "$score%",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 42.sp,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Daily Productivity",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TasksTodayBentoCard(
    count: Int,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSystemInDarkTheme()) Color(0xFF4A4458) else Color(0xFFE8DEF8)
    val textColor = if (isSystemInDarkTheme()) Color(0xFFE8DEF8) else Color(0xFF21005D)

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tasks\nToday",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 15.sp,
                color = textColor
            )
            
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isSystemInDarkTheme()) Color(0xFFD0BCFF) else Color(0xFF6750A4)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$count",
                    color = if (isSystemInDarkTheme()) Color(0xFF21005D) else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun NextReminderBentoCard(
    timeText: String,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSystemInDarkTheme()) Color(0xFF25232A) else Color(0xFFF3EDF7)
    val borderColor = if (isSystemInDarkTheme()) Color(0xFF49454F) else Color(0xFFCAC4D0)
    val textColor = if (isSystemInDarkTheme()) Color(0xFFE6E1E5) else Color(0xFF21005D)

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "NEXT EVENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TodayFocusBentoCard(
    task: Task?,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSystemInDarkTheme()) Color(0xFF25232A) else Color.White
    val borderColor = if (isSystemInDarkTheme()) Color(0xFF49454F) else Color(0xFFE7E0EC)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT FOCUS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                    letterSpacing = 1.sp
                )
                if (task != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryBadge(category = task.category)
                        PriorityBadge(priority = task.priority)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            if (task != null) {
                val priorityColor = when (task.priority.lowercase()) {
                    "high" -> Color(0xFFB3261E)
                    "medium" -> Color(0xFFE2873F)
                    else -> Color(0xFF388E3C)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vertical Priority Indicator Bar
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(55.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Button(
                        onClick = onToggleComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSystemInDarkTheme()) Color(0xFF4F378B) else Color(0xFF6750A4),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Complete",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Nice job! All focus items completed.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyChartBentoCard(
    weeklySummary: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF21005D),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Weekly Progress",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Steady Growth",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+12% vs last week",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD0BCFF)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Draw raw-Canvas bar columns with pill-capsule backdrops
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val days = listOf(
                    Calendar.SUNDAY,
                    Calendar.MONDAY,
                    Calendar.TUESDAY,
                    Calendar.WEDNESDAY,
                    Calendar.THURSDAY,
                    Calendar.FRIDAY,
                    Calendar.SATURDAY
                )
                val labels = listOf("S", "M", "T", "W", "T", "F", "S")
                
                val maxVal = (weeklySummary.values.maxOrNull() ?: 1).coerceAtLeast(3)
                
                days.forEachIndexed { idx, day ->
                    val completedCount = weeklySummary[day] ?: 0
                    val ratio = (completedCount.toFloat() / maxVal).coerceIn(0f, 1f)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(14.dp)
                                .height(64.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            
                            // 1. Draw rounded backdrop capsule
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.1f),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w / 2, w / 2)
                            )
                            
                            // 2. Draw active progress bar
                            if (ratio > 0f) {
                                val activeHeight = h * ratio
                                val activeOffset = h - activeHeight
                                drawRoundRect(
                                    color = Color(0xFFD0BCFF),
                                    topLeft = Offset(0f, activeOffset),
                                    size = androidx.compose.ui.geometry.Size(w, activeHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w / 2, w / 2)
                                )
                            }
                        }
                        
                        Text(
                            text = labels[idx],
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCardItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val containerColor = if (task.isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    } else {
        if (isSystemInDarkTheme()) Color(0xFF25232A) else Color.White
    }
    
    val borderColor = if (task.isCompleted) {
        Color.Transparent
    } else {
        if (isSystemInDarkTheme()) Color(0xFF49454F) else Color(0xFFE7E0EC)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_card_${task.id}")
            .clickable { onEditClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.testTag("task_complete_check_${task.id}")
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Complete",
                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryBadge(category = task.category)
                        PriorityBadge(priority = task.priority)
                        if (task.reminderTime != null) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alarm Active",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.testTag("task_delete_button_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String) {
    val containerColor = when (category.lowercase()) {
        "work" -> MaterialTheme.colorScheme.tertiaryContainer
        "personal" -> MaterialTheme.colorScheme.primaryContainer
        "health" -> MaterialTheme.colorScheme.errorContainer
        "education" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (category.lowercase()) {
        "work" -> MaterialTheme.colorScheme.onTertiaryContainer
        "personal" -> MaterialTheme.colorScheme.onPrimaryContainer
        "health" -> MaterialTheme.colorScheme.onErrorContainer
        "education" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val color = when (priority.lowercase()) {
        "high" -> Color(0xFFE53935)
        "medium" -> Color(0xFFFFB300)
        else -> Color(0xFF43A047)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun EmptyAgendaPlaceholder(onActionClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Your slate is completely clean!",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "You don't have any tasks scheduled for today. Add a new task to organize your day.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Task")
            }
        }
    }
}
