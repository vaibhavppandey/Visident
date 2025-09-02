package dev.vaibhavp.visident.ui.session

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vaibhavp.visident.ui.theme.VisidentTheme
import dev.vaibhavp.visident.viewmodel.SessionViewModel
import java.util.UUID

@ExperimentalMaterial3Api
@Composable
fun EndSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel(),
    onNavigateToStart: () -> Unit
) {
    var sessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var name by remember { mutableStateOf("") }
    var ageString by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val pictureCount by viewModel.pictureCount.collectAsState()
    val context = LocalContext.current

    val isFormValid by remember(name, ageString) {
        derivedStateOf {
            name.isNotBlank() && ageString.isNotBlank() && ageString.toIntOrNull() != null && ageString.toInt() > 0
        }
    }


     LaunchedEffect(Unit) {
         sessionId = UUID.randomUUID().toString()
         name = ""
         ageString = ""
     }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("End Session (Images: $pictureCount)") })
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                shape = CircleShape,
                value = sessionId,
                onValueChange = { sessionId = it },
                label = { Text("Session ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            )

            OutlinedTextField(
                shape = CircleShape,
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                isError = name.isBlank() && name.isNotEmpty()
            )

            OutlinedTextField(
                shape = CircleShape,
                value = ageString,
                onValueChange = { ageString = it.filter { char -> char.isDigit() } },
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = ageString.isNotEmpty() && (ageString.toIntOrNull() == null || ageString.toInt() <= 0)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        val ageInt = ageString.toIntOrNull()
                        if (ageInt != null && ageInt > 0) {
                            isLoading = true
                            viewModel.finalizeSession(
                                sessionId = sessionId,
                                name = name,
                                age = ageInt,
                                imageCount = pictureCount,
                                onComplete = {
                                    isLoading = false
                                    Toast.makeText(context, "Session saved successfully", Toast.LENGTH_SHORT).show()
                                    onNavigateToStart()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Please enter a valid name and age.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Save Session")
                }
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
@ExperimentalMaterial3Api
fun EndSessionScreenPreview() {
    VisidentTheme {
        EndSessionScreen(onNavigateToStart = {})
    }
}

