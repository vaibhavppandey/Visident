package dev.vaibhavp.visident.ui.session

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vaibhavp.visident.ui.theme.VisidentTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndSessionScreen(
    modifier: Modifier = Modifier,
    onSaveSession: (sessionId: String, name: String, age: Int) -> Unit = { sessionId, name, age ->
        Log.d("EndSessionScreen", "Save Session: ID=$sessionId, Name=$name, Age=$age")
    }
) {
    var sessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var name by remember { mutableStateOf("") }
    var ageString by remember { mutableStateOf("") }

    val isFormValid by remember(sessionId, name, ageString) {
        derivedStateOf {
            sessionId.isNotBlank() && name.isNotBlank() && ageString.isNotBlank()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("End Session") })
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = sessionId,
                onValueChange = { sessionId = it },
                label = { Text("Session ID") },
                singleLine = true,
                modifier = modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            )

            OutlinedTextField(
                value = ageString,
                onValueChange = { ageString = it },
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    ageString.toIntOrNull()?.let { ageInt ->
                        if (ageInt > 0) {
                            onSaveSession(sessionId, name, ageInt)
                        } else {
                            Log.d("EndSessionScreen", "Age must be a positive number.")
                        }
                    } ?: run {
                        Log.d("EndSessionScreen", "Invalid age format.")
                    }
                },
                enabled = isFormValid,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Save Session")
            }
        }
    }
}


@Preview
@Composable
fun EndSessionScreenPreview() {
    VisidentTheme {
        EndSessionScreen()
    }
}