package dev.vaibhavp.visident

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.vaibhavp.visident.ui.session.CameraCaptureScreen
import dev.vaibhavp.visident.ui.session.StartSessionScreen
import dev.vaibhavp.visident.ui.theme.VisidentTheme

@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
@Composable
fun VisidentApp() {
    VisidentTheme {
        CameraCaptureScreen()
    }
}
