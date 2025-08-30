package dev.vaibhavp.visident.ui.session

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
    viewModel: SessionViewModel = viewModel()
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (cameraPermissionState.status.isGranted) {
                CameraPreviewContent(
                    viewModel = viewModel,
                    lifecycleOwner = lifecycleOwner,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Box() {}
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
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner
) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val capturedImageUri by viewModel.capturedImageUri.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(lifecycleOwner, viewModel) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(1f)) {
            surfaceRequest?.let { request ->
                CameraXViewfinder(
                    surfaceRequest = request,
                    modifier = modifier.fillMaxSize()
                )
            } ?: run {
//                Text("Upcoming camera")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(onClick = { viewModel.takePicture(context) }) {
            Icon(Icons.Filled.Add, contentDescription = "Capture Photo")
        }

        capturedImageUri?.let { uri ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("Captured: $uri")
            Button(onClick = { viewModel.clearCapturedImageUri() }) {
                Text("Clear Image")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
@Composable
fun CameraPermsDialog(requestCameraPerms: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
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

@Preview
@Composable
@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
fun CameraCaptureScreenPreview() {
    VisidentTheme {
        CameraCaptureScreen()
    }
}
