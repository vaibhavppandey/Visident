package dev.vaibhavp.visident.ui.session

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vaibhavp.visident.viewmodel.SessionViewModel

@Composable
fun SessionDetailScreen(
    sessionID: String,
    viewModel: SessionViewModel = hiltViewModel<SessionViewModel>()
) {

}