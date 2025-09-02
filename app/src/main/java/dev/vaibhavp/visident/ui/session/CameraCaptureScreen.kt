package dev.vaibhavp.visident.ui.session

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.vaibhavp.visident.ui.theme.VisidentTheme
import dev.vaibhavp.visident.viewmodel.SessionViewModel

@ExperimentalMaterial3Api
@ExperimentalPermissionsApi
@Composable
fun CameraCaptureScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel<SessionViewModel>(),
    onEndSessionClick: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current

    var showCameraPermsDialogTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(cameraPermissionState.status, lifecycleOwner, viewModel) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (cameraPermissionState.status.isGranted) {
                val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
                val pictureCount by viewModel.pictureCount.collectAsStateWithLifecycle()
                CameraPreviewContent(
                    modifier = Modifier.weight(1f),
                    surfaceRequest = surfaceRequest,
                    pictureCount = pictureCount,
                    onTakePicture = { viewModel.takePicture(context) },
                    onEndSessionClicked = onEndSessionClick
                )
            } else {
                Column(
                    modifier = Modifier.padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Can't move forward w/out perms",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp) // Add some padding
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showCameraPermsDialogTrigger = true }) {
                        Text("Okay")
                    }
                }
            }
        }
    }

    if (showCameraPermsDialogTrigger && !cameraPermissionState.status.isGranted) {
        CameraPermsDialog(
            requestCameraPerms = { cameraPermissionState.launchPermissionRequest() },
            { showCameraPermsDialogTrigger = false })
    }
}

@ExperimentalMaterial3Api
@Composable
fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    surfaceRequest: SurfaceRequest?,
    pictureCount: Int,
    onTakePicture: () -> Unit,
    onEndSessionClicked: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize()
            )
        }

        Surface(
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "$pictureCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        ElevatedButton(
            onClick = onTakePicture,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Capture Photo")
        }

        ElevatedButton(
            onClick = onEndSessionClicked,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 32.dp)
        ) {
            Text("End Session")
        }
    }
}


@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
@Composable
fun CameraPermsDialog(requestCameraPerms: () -> Unit, onDismissDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissDialog,
        icon = { Icon(Icons.Rounded.Warning, contentDescription = "Camera permission needed") },
        title = { Text(text = "Camera Permission") },
        text = { Text(text = "Visident needs camera permission to capture images for analysis.") },
        confirmButton = {
            Button(onClick = requestCameraPerms) {
                Text("Grant Permission")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
@ExperimentalMaterial3Api
fun CameraPreviewContentPreview() {
    VisidentTheme {
        CameraPreviewContent(
            surfaceRequest = null,
            pictureCount = 5,
            onTakePicture = {},
            onEndSessionClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
fun CameraCaptureScreenPreview() {
    VisidentTheme {
        CameraCaptureScreen(onEndSessionClick = {})
    }
}
