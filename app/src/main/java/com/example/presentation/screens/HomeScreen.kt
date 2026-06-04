package com.example.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.domain.model.ChecklistItem
import com.example.domain.model.Label
import com.example.domain.model.Note
import com.example.presentation.components.NoteCard
import com.example.presentation.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

enum class DrawerFilter {
    ALL_NOTES,
    REMINDERS,
    LABEL,
    ARCHIVE,
    TRASH
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NoteViewModel,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Data streams from ViewModel
    val notes by viewModel.filteredNotes.collectAsState()
    val labels by viewModel.labels.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isArchivedView by viewModel.isArchivedView.collectAsState()
    val selectedLabelFilterId by viewModel.selectedLabelId.collectAsState()

    // Active Drawer Navigation Target state
    var currentFilterType by remember { mutableStateOf(DrawerFilter.ALL_NOTES) }
    var activeLabelFilterName by remember { mutableStateOf("") }

    // Dialog state for label manager
    var showLabelManagerDialog by remember { mutableStateOf(false) }
    var trashedNoteOptionsTarget by remember { mutableStateOf<Note?>(null) }

    // Notification permission checks for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Modal Drawer wrapper
    ModalNavigationDrawer(
        drawerState = drawerState,
        modifier = Modifier.fillMaxSize(),
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.8.dp, vertical = 16.dp)
                ) {
                    // Drawer Header / App Brand
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "KeepFlow Notes",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // 1. Primary notes feed
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Lightbulb, contentDescription = null) },
                        label = { Text("Notes") },
                        selected = currentFilterType == DrawerFilter.ALL_NOTES,
                        onClick = {
                            currentFilterType = DrawerFilter.ALL_NOTES
                            viewModel.setArchivedView(false)
                            viewModel.setTrashedView(false)
                            viewModel.selectLabelFilter(null)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // 2. Scheduled reminders feed
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                        label = { Text("Reminders") },
                        selected = currentFilterType == DrawerFilter.REMINDERS,
                        onClick = {
                            currentFilterType = DrawerFilter.REMINDERS
                            viewModel.setArchivedView(false)
                            viewModel.setTrashedView(false)
                            viewModel.selectLabelFilter(null)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Dynamic Labels list
                    if (labels.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            text = "LABELS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(labels) { label ->
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Outlined.Label, contentDescription = null) },
                                    label = { Text(label.name) },
                                    selected = currentFilterType == DrawerFilter.LABEL && selectedLabelFilterId == label.id,
                                    onClick = {
                                        currentFilterType = DrawerFilter.LABEL
                                        activeLabelFilterName = label.name
                                        viewModel.setArchivedView(false)
                                        viewModel.setTrashedView(false)
                                        viewModel.selectLabelFilter(label.id)
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Manage Labels Shortcut
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        label = { Text("Create/Edit labels") },
                        selected = false,
                        onClick = {
                            showLabelManagerDialog = true
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // 3. Archives feed
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                        label = { Text("Archive") },
                        selected = currentFilterType == DrawerFilter.ARCHIVE,
                        onClick = {
                            currentFilterType = DrawerFilter.ARCHIVE
                            viewModel.setArchivedView(true)
                            viewModel.setTrashedView(false)
                            viewModel.selectLabelFilter(null)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // 4. Trash feed
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                        label = { Text("Trash") },
                        selected = currentFilterType == DrawerFilter.TRASH,
                        onClick = {
                            currentFilterType = DrawerFilter.TRASH
                            viewModel.setTrashedView(true)
                            viewModel.selectLabelFilter(null)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Keep-style Search Header box
                Card(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .height(52.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }

                        // Search core
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    text = when (currentFilterType) {
                                        DrawerFilter.ALL_NOTES -> "Search your notes"
                                        DrawerFilter.REMINDERS -> "Search reminders"
                                        DrawerFilter.LABEL -> "Search under '$activeLabelFilterName'"
                                        DrawerFilter.ARCHIVE -> "Search archives"
                                        DrawerFilter.TRASH -> "Search in Trash"
                                    },
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }

                        // Layout view selector
                        IconButton(onClick = { viewModel.toggleGridView() }) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = if (isGridView) "List View" else "Grid View"
                            )
                        }

                        // Indicator avatar or logo
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "KF",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    // Quick-launch checklists note
                    FloatingActionButton(
                        onClick = {
                            // Enqueue a note direct template check
                            val checklistNote = Note(
                                title = "",
                                isChecklist = true,
                                checklistItems = listOf(ChecklistItem(text = ""))
                            )
                            viewModel.saveNote(checklistNote) { savedId ->
                                onNavigateToEdit(savedId)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Quick checklist",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Primary Add Note FAB
                    LargeFloatingActionButton(
                        onClick = { onNavigateToEdit(-1L) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Note",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        ) { innerPadding ->
            // Filter list content
            val flowNotes = when (currentFilterType) {
                DrawerFilter.REMINDERS -> notes.filter { it.reminderTime != null }
                else -> notes
            }

            if (flowNotes.isEmpty()) {
                // Polished UX empty states
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when (currentFilterType) {
                            DrawerFilter.ALL_NOTES -> Icons.Outlined.Lightbulb
                            DrawerFilter.REMINDERS -> Icons.Outlined.Notifications
                            DrawerFilter.LABEL -> Icons.Outlined.Label
                            DrawerFilter.ARCHIVE -> Icons.Outlined.Archive
                            DrawerFilter.TRASH -> Icons.Outlined.Delete
                        },
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (currentFilterType) {
                            DrawerFilter.ALL_NOTES -> "Notes you add appear here"
                            DrawerFilter.REMINDERS -> "Notes with upcoming reminders appear here"
                            DrawerFilter.LABEL -> "No notes have been associated with '$activeLabelFilterName' yet."
                            DrawerFilter.ARCHIVE -> "Your archived notes appear here"
                            DrawerFilter.TRASH -> "Trash is empty"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                // Notes feed render list
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 8.dp)
                ) {
                    val isTrashFolder = currentFilterType == DrawerFilter.TRASH
                    val pinnedNotes = if (isTrashFolder) emptyList() else flowNotes.filter { it.isPinned }
                    val otherNotes = if (isTrashFolder) flowNotes else flowNotes.filter { !it.isPinned }

                    if (isGridView) {
                        // Masonry Dynamic Grid
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            if (isTrashFolder) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Notes in Trash are kept locally and are not synced.",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { viewModel.emptyTrash() }) {
                                                Text("Empty Trash", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }

                            if (pinnedNotes.isNotEmpty() && otherNotes.isNotEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Text(
                                        text = "PINNED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            letterSpacing = 1.2.sp
                                        ),
                                        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
                                    )
                                }
                                items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                                    NoteCard(
                                        note = note,
                                        labelsList = labels,
                                        onClick = { onNavigateToEdit(note.id) },
                                        onLongClick = { onNavigateToEdit(note.id) },
                                        onPinToggle = { viewModel.togglePinNote(note) },
                                        onArchiveToggle = { viewModel.toggleArchiveNote(note) }
                                    )
                                }
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Text(
                                        text = "OTHERS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            letterSpacing = 1.2.sp
                                        ),
                                        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
                                    )
                                }
                                items(otherNotes, key = { "other_${it.id}" }) { note ->
                                    NoteCard(
                                        note = note,
                                        labelsList = labels,
                                        onClick = { onNavigateToEdit(note.id) },
                                        onLongClick = { onNavigateToEdit(note.id) },
                                        onPinToggle = { viewModel.togglePinNote(note) },
                                        onArchiveToggle = { viewModel.toggleArchiveNote(note) }
                                    )
                                }
                            } else {
                                items(flowNotes, key = { it.id }) { note ->
                                    NoteCard(
                                        note = note,
                                        labelsList = labels,
                                        onClick = {
                                            if (isTrashFolder) {
                                                trashedNoteOptionsTarget = note
                                            } else {
                                                onNavigateToEdit(note.id)
                                            }
                                        },
                                        onLongClick = {
                                            if (isTrashFolder) {
                                                trashedNoteOptionsTarget = note
                                            } else {
                                                onNavigateToEdit(note.id)
                                            }
                                        },
                                        onPinToggle = { viewModel.togglePinNote(note) },
                                        onArchiveToggle = {
                                            if (isTrashFolder) {
                                                viewModel.deleteNote(note)
                                            } else {
                                                viewModel.toggleArchiveNote(note)
                                            }
                                        },
                                        onRestore = if (isTrashFolder) { { viewModel.restoreNote(note) } } else null
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard Vertical List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            if (isTrashFolder) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Notes in Trash are kept locally and are not synced.",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { viewModel.emptyTrash() }) {
                                                Text("Empty Trash", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }

                            if (pinnedNotes.isNotEmpty() && otherNotes.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "PINNED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            letterSpacing = 1.2.sp
                                        ),
                                        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
                                    )
                                }
                                items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                                    NoteCard(
                                        note = note,
                                        labelsList = labels,
                                        onClick = { onNavigateToEdit(note.id) },
                                        onLongClick = { onNavigateToEdit(note.id) },
                                        onPinToggle = { viewModel.togglePinNote(note) },
                                        onArchiveToggle = { viewModel.toggleArchiveNote(note) }
                                    )
                                }
                                item {
                                    Text(
                                        text = "OTHERS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            letterSpacing = 1.2.sp
                                        ),
                                        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
                                    )
                                }
                                items(otherNotes, key = { "other_${it.id}" }) { note ->
                                    NoteCard(
                                        note = note,
                                        labelsList = labels,
                                        onClick = { onNavigateToEdit(note.id) },
                                        onLongClick = { onNavigateToEdit(note.id) },
                                        onPinToggle = { viewModel.togglePinNote(note) },
                                        onArchiveToggle = { viewModel.toggleArchiveNote(note) }
                                    )
                                }
                            } else {
                                items(flowNotes, key = { it.id }) { note ->
                                    NoteCard(
                                        note = note,
                                        labelsList = labels,
                                        onClick = {
                                            if (isTrashFolder) {
                                                trashedNoteOptionsTarget = note
                                            } else {
                                                onNavigateToEdit(note.id)
                                            }
                                        },
                                        onLongClick = {
                                            if (isTrashFolder) {
                                                trashedNoteOptionsTarget = note
                                            } else {
                                                onNavigateToEdit(note.id)
                                            }
                                        },
                                        onPinToggle = { viewModel.togglePinNote(note) },
                                        onArchiveToggle = {
                                            if (isTrashFolder) {
                                                viewModel.deleteNote(note)
                                            } else {
                                                viewModel.toggleArchiveNote(note)
                                            }
                                        },
                                        onRestore = if (isTrashFolder) { { viewModel.restoreNote(note) } } else null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Label master manager dialog
    if (showLabelManagerDialog) {
        var newLabelName by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showLabelManagerDialog = false }) {
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
                        text = "Edit Labels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Add new label input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (newLabelName.isNotBlank()) {
                                viewModel.createLabel(newLabelName)
                                newLabelName = ""
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Label",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextField(
                            value = newLabelName,
                            onValueChange = { newLabelName = it },
                            placeholder = { Text("Create new label") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Existing labels list scrolling
                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(labels) { label ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Label,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = label.name,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    IconButton(onClick = { viewModel.deleteLabel(label) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showLabelManagerDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Finish")
                    }
                }
            }
        }
    }

    if (trashedNoteOptionsTarget != null) {
        AlertDialog(
            onDismissRequest = { trashedNoteOptionsTarget = null },
            title = { Text("Restore or Delete Note?") },
            text = { Text("You can restore this note to your active feed, or delete it permanently from your device storage.") },
            confirmButton = {
                Button(
                    onClick = {
                        trashedNoteOptionsTarget?.let { viewModel.restoreNote(it) }
                        trashedNoteOptionsTarget = null
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        trashedNoteOptionsTarget?.let { viewModel.deleteNote(it) }
                        trashedNoteOptionsTarget = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            }
        )
    }
}
