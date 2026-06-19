package dev.vaibhavp.visident.ui.session

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    // A session id is generated once and shown frozen; the user fills in name + age.
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
        bottomBar = {
            // Pinned above the IME so the primary action stays reachable while typing.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (isLoading) {
                    LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    val saveHeight = ButtonDefaults.LargeContainerHeight
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
                                onError = { message ->
                                    isLoading = false
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                },
                            )
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(saveHeight),
                        shapes = ButtonDefaults.shapesFor(saveHeight),
                        contentPadding = ButtonDefaults.contentPaddingFor(saveHeight),
                    ) {
                        Text("Save session", style = ButtonDefaults.textStyleFor(saveHeight))
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Frozen: the generated id is shown disabled so the user can't edit it.
            OutlinedTextField(
                value = sessionId,
                onValueChange = {},
                enabled = false,
                readOnly = true,
                label = { Text("Session ID") },
                supportingText = { Text("Generated automatically") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                isError = name.isBlank() && ageString.isNotEmpty(),
                supportingText = if (name.isBlank() && ageString.isNotEmpty()) {
                    { Text("Name is required") }
                } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = ageString,
                onValueChange = { input -> ageString = input.filter { it.isDigit() } },
                label = { Text("Age") },
                singleLine = true,
                isError = ageString.isNotEmpty() && !ageValid,
                supportingText = if (ageString.isNotEmpty() && !ageValid) {
                    { Text("Enter a valid age") }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
