package dev.vaibhavp.visident.ui.session

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import dev.vaibhavp.visident.viewmodel.CaptureViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CameraCaptureScreen(
    viewModel: CaptureViewModel,
    onBack: () -> Unit,
    onEndSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasRequested by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA,
        onPermissionResult = { hasRequested = true },
    )
    val status = cameraPermissionState.status

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                status is PermissionStatus.Granted ->
                    CameraContent(
                        viewModel = viewModel,
                        onBack = onBack,
                        onEndSessionClick = onEndSessionClick,
                    )

                status is PermissionStatus.Denied && status.shouldShowRationale ->
                    PermissionRationale(
                        message = "Visident needs the camera to capture session photos.",
                        actionLabel = "Grant permission",
                        onAction = { cameraPermissionState.launchPermissionRequest() },
                    )

                !hasRequested ->
                    PermissionRationale(
                        message = "Camera access is required to start a session.",
                        actionLabel = "Request permission",
                        onAction = { cameraPermissionState.launchPermissionRequest() },
                    )

                else ->
                    // Permanently denied: only the system Settings page can re-grant it.
                    PermissionRationale(
                        message = "Camera permission is permanently denied. Enable it in Settings to continue.",
                        actionLabel = "Open Settings",
                        onAction = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null),
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        },
                    )
            }
        }
    }
}

@Composable
private fun CameraContent(
    viewModel: CaptureViewModel,
    onBack: () -> Unit,
    onEndSessionClick: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val pictureCount by viewModel.pictureCount.collectAsStateWithLifecycle()
    val flashEnabled by viewModel.flashEnabled.collectAsStateWithLifecycle()
    val hasFlashUnit by viewModel.hasFlashUnit.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()

    // Rebind whenever the lens changes; the binding suspends until this effect is cancelled.
    LaunchedEffect(lensFacing) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }
    // Surface one-off capture errors as a toast.
    LaunchedEffect(Unit) {
        viewModel.captureError.collectLatest { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val coordinateTransformer = remember { MutableCoordinateTransformer() }
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                coordinateTransformer = coordinateTransformer,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            with(coordinateTransformer) { viewModel.focusOnPoint(tap.transform()) }
                        }
                    },
            )
        }

        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                if (hasFlashUnit) {
                    IconButton(onClick = { viewModel.toggleFlash() }) {
                        Icon(
                            if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = if (flashEnabled) "Flash on" else "Flash off",
                        )
                    }
                }
                IconButton(onClick = { viewModel.toggleLens() }) {
                    Icon(Icons.Filled.Cameraswitch, "Switch camera")
                }
            },
        )

        // Bottom control row: photo count (left) · capture (center) · end session (right).
        // A single Box wraps the FAB's height so the side controls sit vertically centred
        // against the FAB instead of sinking to its baseline.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Running photo count. A translucent scrim pill keeps it legible over any frame and
            // balances the End session button on the opposite side.
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(text = "$pictureCount", style = MaterialTheme.typography.titleMedium)
                }
            }

            LargeFloatingActionButton(
                onClick = { viewModel.takePicture(context) },
            ) {
                Icon(Icons.Filled.PhotoCamera, "Capture photo", Modifier.size(36.dp))
            }
            Button(
                onClick = onEndSessionClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp),
            ) {
                Text("End session")
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Rounded.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAction) { Text(actionLabel) }
    }
}
