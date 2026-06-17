package dev.vaibhavp.visident.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import dev.vaibhavp.visident.ui.search.SearchSessionScreen
import dev.vaibhavp.visident.ui.session.CameraCaptureScreen
import dev.vaibhavp.visident.ui.session.EndSessionScreen
import dev.vaibhavp.visident.ui.session.SessionDetailScreen
import dev.vaibhavp.visident.ui.session.StartSessionScreen
import dev.vaibhavp.visident.viewmodel.CaptureViewModel

@Composable
fun VisidentNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = StartSessionRoute) {
        composable<StartSessionRoute> {
            StartSessionScreen(
                onStartNewSessionClick = { navController.navigate(CaptureGraph) { launchSingleTop = true } },
                onSearchSessionClick = { navController.navigate(SearchSessionsRoute) { launchSingleTop = true } },
            )
        }

        navigation<CaptureGraph>(startDestination = CameraCaptureRoute) {
            composable<CameraCaptureRoute> { entry ->
                CameraCaptureScreen(
                    viewModel = entry.captureGraphViewModel(navController),
                    onBack = { navController.popBackStack() },
                    onEndSessionClick = { navController.navigate(EndSessionRoute) { launchSingleTop = true } },
                )
            }
            composable<EndSessionRoute> { entry ->
                EndSessionScreen(
                    viewModel = entry.captureGraphViewModel(navController),
                    onBack = { navController.popBackStack() },
                    onNavigateToStart = {
                        navController.navigate(StartSessionRoute) {
                            // Clear the capture flow off the back stack after saving.
                            popUpTo(StartSessionRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        composable<SearchSessionsRoute> {
            SearchSessionScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onNavigateToSessionDetails = { id -> navController.navigate(SessionDetailsRoute(id)) { launchSingleTop = true } },
            )
        }

        composable<SessionDetailsRoute> { entry ->
            SessionDetailScreen(
                sessionID = entry.toRoute<SessionDetailsRoute>().sessionID,
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}

/** One CaptureViewModel scoped to the whole capture sub-graph, shared by its screens. */
@Composable
private fun NavBackStackEntry.captureGraphViewModel(
    navController: NavHostController,
): CaptureViewModel {
    val parentEntry = remember(this) { navController.getBackStackEntry(CaptureGraph) }
    return hiltViewModel(parentEntry)
}
