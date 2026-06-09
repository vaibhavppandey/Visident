package dev.vaibhavp.visident.ui.session

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.vaibhavp.visident.viewmodel.CaptureViewModel
import java.util.UUID

@Composable
fun EndSessionScreen(
    viewModel: CaptureViewModel,
    onBack: () -> Unit,
    onNavigateToStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A session id is generated once and shown read-only; the user fills in name + age.
    val sessionId = remember { UUID.randomUUID().toString() }
    var name by remember { mutableStateOf("") }
    var ageString by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val pictureCount by viewModel.pictureCount.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val ageValid = ageString.toIntOrNull()?.let { it > 0 } == true
    val isFormValid = name.isNotBlank() && ageValid

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("End session · $pictureCount photos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = sessionId,
                onValueChange = {},
                readOnly = true,
                label = { Text("Session ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                isError = name.isBlank() && ageString.isNotEmpty(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = ageString,
                onValueChange = { input -> ageString = input.filter { it.isDigit() } },
                label = { Text("Age") },
                singleLine = true,
                isError = ageString.isNotEmpty() && !ageValid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))

            if (isLoading) {
                LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        viewModel.finalizeSession(
                            sessionId = sessionId,
                            name = name.trim(),
                            age = ageString.toInt(),
                            imageCount = pictureCount,
                            onComplete = {
                                isLoading = false
                                Toast.makeText(context, "Session saved", Toast.LENGTH_SHORT).show()
                                onNavigateToStart()
                            },
                        )
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save session")
                }
            }
        }
    }
}
