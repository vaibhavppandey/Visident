package dev.vaibhavp.visident.ui.session

import android.util.Log
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
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
    viewModel: SessionViewModel = hiltViewModel<SessionViewModel>(), // Use hiltViewModel()
    onEndSessionClick: () -> Unit = {
        Log.d(
            "CameraCaptureScreen",
            "Navigate to next screen triggered"
        )
    }
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
                    modifier = Modifier.weight(1f),
                    onEndSessionClicked = onEndSessionClick
                )
            } else {
                Box {}
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
    lifecycleOwner: LifecycleOwner,
    onEndSessionClicked: () -> Unit
) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val pictureCount by viewModel.pictureCount.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(lifecycleOwner, viewModel) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    Box(modifier = modifier.fillMaxSize()) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize()
            )
        }

        ElevatedButton(
            onClick = onEndSessionClicked,
            modifier = modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("End Session")
        }

        Surface(
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
                Text(
                    text = "$pictureCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        ElevatedButton(
            onClick = { viewModel.takePicture(context) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Capture Photo")
        }
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
         CameraCaptureScreen(onEndSessionClick = {})
    }
}
