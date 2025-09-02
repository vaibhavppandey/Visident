package dev.vaibhavp.visident.ui.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.vaibhavp.visident.viewmodel.SessionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@ExperimentalMaterial3Api
@Composable
fun SessionDetailScreen(
    sessionID: String,
    viewModel: SessionViewModel = hiltViewModel<SessionViewModel>()
) {
    val session by viewModel.selectedSession.collectAsStateWithLifecycle()
    val images by viewModel.selectedSessionImages.collectAsStateWithLifecycle()
    var zoomedImageFile by remember { mutableStateOf<File?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    LaunchedEffect(sessionID) {
        viewModel.getSessionById(sessionID)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Session Details") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (session == null) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            } else {
                session?.let { currentSession ->
                    // Details Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Session ID", style = MaterialTheme.typography.titleSmall)
                                Text("Name", style = MaterialTheme.typography.titleSmall)
                                Text("Age", style = MaterialTheme.typography.titleSmall)
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(
                                    8.dp,
                                    alignment = Alignment.CenterVertically
                                ),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    currentSession.sessionId,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    currentSession.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    currentSession.age.toString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Created @", style = MaterialTheme.typography.titleSmall)
                                Text("Total images", style = MaterialTheme.typography.titleSmall)
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    dateFormat.format(Date(currentSession.createdAt)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    currentSession.imageCount.toString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Images Section Title
                    Text(
                        "Images:",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Start
                    )

                    if (images.isEmpty()) {
                        Text(
                            "No images for this session.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 120.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(images) { imageFile ->
                                AsyncImage(
                                    model = imageFile,
                                    contentDescription = "session img",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clickable { zoomedImageFile = imageFile },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (zoomedImageFile != null) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.60f)
                    .clickable { zoomedImageFile = null },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {

                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = zoomedImageFile,
                        contentDescription = "zoomed in imaze yeah",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { zoomedImageFile = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Zoomed Image",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
