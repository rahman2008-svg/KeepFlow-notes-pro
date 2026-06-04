package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.PlannerViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val productivityScore by viewModel.productivityScore.collectAsState()

    // Real-time calculations
    val totalTasksCount = allTasks.size
    val completedCount = completedTasks.size
    val pendingCount = totalTasksCount - completedCount

    // Categories distribution
    val categoryDistribution = remember(allTasks) {
        allTasks.groupBy { it.category }.mapValues { it.value.size }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Productivity Stats", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
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
            // Stats Grid Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricMiniCard(
                        title = "Completed",
                        value = completedCount.toString(),
                        icon = Icons.Default.AssignmentTurnedIn,
                        color = Color(0xFF43A047),
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Pending",
                        value = pendingCount.toString(),
                        icon = Icons.Default.HourglassEmpty,
                        color = Color(0xFFFFB300),
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Total Tasks",
                        value = totalTasksCount.toString(),
                        icon = Icons.Default.Analytics,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }

            // Overall Progress Ring Card
            item {
                val progressContainerColor = if (isSystemInDarkTheme()) Color(0xFF25232A) else Color.White
                val progressBorderColor = if (isSystemInDarkTheme()) Color(0xFF49454F) else Color(0xFFE7E0EC)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = progressContainerColor),
                    border = BorderStroke(1.dp, progressBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Core Completion Ratio",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(160.dp)
                        ) {
                            val trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            val progressColor = MaterialTheme.colorScheme.primary
                            
                            Canvas(modifier = Modifier.size(140.dp)) {
                                drawArc(
                                    color = trackColor,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = progressColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * (productivityScore / 100f),
                                    useCenter = false,
                                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$productivityScore%",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Done",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Category Distribution Chart Card (Canvas Arc Pie Diagram)
            item {
                val catContainerColor = if (isSystemInDarkTheme()) Color(0xFF25232A) else Color.White
                val catBorderColor = if (isSystemInDarkTheme()) Color(0xFF49454F) else Color(0xFFE7E0EC)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = catContainerColor),
                    border = BorderStroke(1.dp, catBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Categorical Allocations",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (totalTasksCount == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No tasks created yet to generate distribution.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Draw Category Arc Pie
                                val palette = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.tertiary,
                                    Color(0xFFFF7043),
                                    Color(0xFF42A5F5),
                                    Color(0xFF66BB6A)
                                )

                                Box(
                                    modifier = Modifier.size(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(120.dp)) {
                                        var currentAngle = 0f
                                        categoryDistribution.toList().forEachIndexed { index, pair ->
                                            val sweep = (pair.second.toFloat() / totalTasksCount) * 360f
                                            val color = palette[index % palette.size]
                                            drawArc(
                                                color = color,
                                                startAngle = currentAngle,
                                                sweepAngle = sweep,
                                                useCenter = false,
                                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                            currentAngle += sweep
                                        }
                                    }
                                }

                                // Legend Block
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    categoryDistribution.toList().forEachIndexed { index, pair ->
                                        val color = palette[index % palette.size]
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Text(
                                                text = "${pair.first}: ${pair.second} tasks",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Periodic Health Check Text/Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "NexVora Productivity Insights 💡",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Focus Index measures task actions relative to scheduled plan limits. Complete task cards early to sustain positive velocity scores. Schedulers operate in complete device isolating database modes, guaranteeing full data ownership and battery conservations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
