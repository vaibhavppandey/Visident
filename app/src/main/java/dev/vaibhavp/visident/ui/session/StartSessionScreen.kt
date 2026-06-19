package dev.vaibhavp.visident.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vaibhavp.visident.ui.theme.VisidentTheme

@Composable
fun StartSessionScreen(
    onStartNewSessionClick: () -> Unit,
    onSearchSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Visident") },
                subtitle = { Text("Session-based capture") },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))
            Text("Capture a session", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Start a new photo session or browse your saved ones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))

            val height = ButtonDefaults.LargeContainerHeight
            Button(
                onClick = onStartNewSessionClick,
                modifier = Modifier.fillMaxWidth().heightIn(height),
                shapes = ButtonDefaults.shapesFor(height),
                contentPadding = ButtonDefaults.contentPaddingFor(height),
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(ButtonDefaults.iconSizeFor(height)))
                Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(height)))
                Text("Start session", style = ButtonDefaults.textStyleFor(height))
            }
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(
                onClick = onSearchSessionClick,
                modifier = Modifier.fillMaxWidth().heightIn(height),
                shapes = ButtonDefaults.shapesFor(height),
                contentPadding = ButtonDefaults.contentPaddingFor(height),
            ) {
                Icon(Icons.Filled.Search, null, Modifier.size(ButtonDefaults.iconSizeFor(height)))
                Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(height)))
                Text("Search sessions", style = ButtonDefaults.textStyleFor(height))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StartSessionScreenPreview() {
    VisidentTheme {
        StartSessionScreen(onStartNewSessionClick = {}, onSearchSessionClick = {})
    }
}
