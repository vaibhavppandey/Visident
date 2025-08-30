package dev.vaibhavp.visident.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vaibhavp.visident.ui.theme.VisidentTheme

@Composable
fun StartSessionScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxSize().padding(padding)
        ) {
            ElevatedButton(onClick = {}) {
                Text(text = "Start Session")
            }
            Spacer(modifier = modifier.height(8.dp))
            TextButton (onClick = {}) {
                Text(text = "Search Session")
            }
        }
    }
}

@Preview
@Composable
fun StartSessionScreenPreview() {
    VisidentTheme {
        StartSessionScreen()
    }
}