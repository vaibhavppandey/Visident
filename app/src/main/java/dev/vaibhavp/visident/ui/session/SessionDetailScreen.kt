package dev.vaibhavp.visident.ui.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.ui.components.ZoomableImage
import dev.vaibhavp.visident.viewmodel.LibraryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(
    sessionID: String,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val session by viewModel.selectedSession.collectAsStateWithLifecycle()
    val images by viewModel.selectedSessionImages.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    // The full-screen viewer is driven by a single nullable index, cleared only on dismiss.
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(sessionID) { viewModel.loadSession(sessionID) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Session details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEdit = true }, enabled = session != null) {
                        Icon(Icons.Filled.Edit, "Edit session")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, enabled = session != null) {
                        Icon(Icons.Filled.Delete, "Delete session")
                    }
                },
            )
        },
    ) { paddingValues ->
        val currentSession = session
        if (currentSession == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            ) {
                SessionInfo(currentSession, dateFormat)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text(
                    "Images",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))

                if (images.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No images for this session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(images) { imageFile ->
                            val index = images.indexOf(imageFile)
                            AsyncImage(
                                model = imageFile,
                                contentDescription = "Session image ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewerIndex = index },
                            )
                        }
                    }
                }
            }
        }
    }

    // Full-screen pinch-zoom viewer. onDismissRequest clears the index, so system back, scrim
    // tap, and the close button all dismiss it (fixes the previously stuck dialog).
    viewerIndex?.let { startIndex ->
        FullScreenImageViewer(
            images = images,
            startIndex = startIndex,
            onDismiss = { viewerIndex = null },
        )
    }

    if (showEdit && session != null) {
        EditSessionDialog(
            session = session!!,
            onDismiss = { showEdit = false },
            onSave = { updated ->
                viewModel.updateSession(updated)
                showEdit = false
            },
        )
    }

    if (showDeleteConfirm && session != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Filled.Delete, null) },
            title = { Text("Delete session?") },
            text = { Text("This permanently removes the session and its images.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSession(session!!, onDeleted = onBack)
                    showDeleteConfirm = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SessionInfo(session: SessionEntity, dateFormat: SimpleDateFormat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InfoRow("Session ID", session.sessionId)
        InfoRow("Name", session.name)
        InfoRow("Age", session.age.toString())
        InfoRow("Created", dateFormat.format(Date(session.createdAt)))
        InfoRow("Total images", session.imageCount.toString())
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FullScreenImageViewer(
    images: List<File>,
    startIndex: Int,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
        ) {
            val pagerState = rememberPagerState(
                initialPage = startIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
                pageCount = { images.size },
            )
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                ZoomableImage(
                    painter = rememberAsyncImagePainter(model = images[page]),
                    contentDescription = "Image ${page + 1}",
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(Icons.Filled.Close, "Close", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun EditSessionDialog(
    session: SessionEntity,
    onDismiss: () -> Unit,
    onSave: (SessionEntity) -> Unit,
) {
    var name by remember { mutableStateOf(session.name) }
    var ageString by remember { mutableStateOf(session.age.toString()) }
    val ageValid = ageString.toIntOrNull()?.let { it > 0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = ageString,
                    onValueChange = { input -> ageString = input.filter { it.isDigit() } },
                    label = { Text("Age") },
                    singleLine = true,
                    isError = ageString.isNotEmpty() && !ageValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(session.copy(name = name.trim(), age = ageString.toInt())) },
                enabled = name.isNotBlank() && ageValid,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
