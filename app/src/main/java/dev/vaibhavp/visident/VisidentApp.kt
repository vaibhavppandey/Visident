package dev.vaibhavp.visident

import androidx.compose.runtime.Composable
import dev.vaibhavp.visident.ui.session.StartSessionScreen
import dev.vaibhavp.visident.ui.theme.VisidentTheme

@Composable
fun VisidentApp() {
    VisidentTheme {
        StartSessionScreen()
    }
}
