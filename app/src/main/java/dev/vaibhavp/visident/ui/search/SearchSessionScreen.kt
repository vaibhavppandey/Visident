package dev.vaibhavp.visident.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.vaibhavp.visident.ui.components.SessionDetailsCard
import dev.vaibhavp.visident.viewmodel.SessionViewModel

@ExperimentalMaterial3Api
@Composable
fun SearchSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel<SessionViewModel>(),
    onNavigateToSessionDetails: (String) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredSessions by viewModel.filteredSessions.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getAllSessions()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Search Sessions") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                shape = CircleShape,
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search by Name or ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (filteredSessions.isEmpty()) {
                    Text(
                        text = if (searchQuery.isBlank())
                            "No sessions found. Start by creating a new session!"
                        else
                            "No sessions match your search.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSessions, key = { it.sessionId }) { session ->
                            SessionDetailsCard(
                                session = session,
                                onNavigateClick = onNavigateToSessionDetails
                            )
                        }
                    }
                }
            }
        }
    }
}
