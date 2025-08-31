package dev.vaibhavp.visident.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vaibhavp.visident.ui.theme.VisidentTheme

@ExperimentalMaterial3Api
@Composable
fun StartSessionScreen(
    modifier: Modifier = Modifier,
    onStartNewSessionClick: () -> Unit,
    onSearchSessionClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(text = "Visident") })
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ElevatedButton(onClick = onStartNewSessionClick) {
                Text(text = "Start Session")
            }
            Spacer(modifier = Modifier.height(8.dp))
            ElevatedButton(onClick = onSearchSessionClick) {
                Text(text = "Search Session")
            }
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun StartSessionScreenPreview() {
    VisidentTheme {
        StartSessionScreen(
            onStartNewSessionClick = {},
            onSearchSessionClick = {}
        )
    }
}
