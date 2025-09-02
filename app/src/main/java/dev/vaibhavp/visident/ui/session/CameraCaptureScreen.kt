package dev.vaibhavp.visident.ui.session

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    LaunchedEffect(cameraPermissionState.status, lifecycleOwner, viewModel) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
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
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Camera permission not granted.", Modifier.align(Alignment.Center))
                }
            }
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        CameraPermsDialog(requestCameraPerms = { cameraPermissionState.launchPermissionRequest() })
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

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElevatedButton(
                onClick = onTakePicture,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Capture Photo")
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(
                onClick = onEndSessionClicked,
            ) {
                Text("End Session")
            }
        }
    }
}


@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
@Composable
fun CameraPermsDialog(requestCameraPerms: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* TODO: Decide if dismissible and what happens */ },
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
            surfaceRequest = null, // Or a mock SurfaceRequest if possible for preview
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
