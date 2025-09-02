package dev.vaibhavp.visident.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.vaibhavp.visident.ui.search.SearchSessionScreen
import dev.vaibhavp.visident.ui.session.CameraCaptureScreen
import dev.vaibhavp.visident.ui.session.EndSessionScreen
import dev.vaibhavp.visident.ui.session.StartSessionScreen
import dev.vaibhavp.visident.viewmodel.SessionViewModel

@ExperimentalMaterial3Api
@ExperimentalPermissionsApi
@Composable
fun VisidentNavHost(navController: NavHostController) {
    // TODO: migrate to hilt nav later on
    val viewModel = hiltViewModel<SessionViewModel>()
    NavHost(navController = navController, startDestination = StartSessionRoute) {
        composable<StartSessionRoute> {
            StartSessionScreen(
                onStartNewSessionClick = { navController.navigate(CameraCaptureRoute) },
                onSearchSessionClick = { navController.navigate(SearchSessionsRoute) }

            )
        }

        composable<SearchSessionsRoute> {
            SearchSessionScreen(
                viewModel = viewModel
            )
        }

        composable<CameraCaptureRoute> {
            CameraCaptureScreen(
                viewModel = viewModel,
                onEndSessionClick = { navController.navigate(EndSessionRoute) }
            )
        }

        composable<EndSessionRoute> {
            EndSessionScreen(
                viewModel = viewModel,
                onNavigateToStart = {
                    navController.navigate(StartSessionRoute) {
                        popUpTo(StartSessionRoute) {
                            inclusive = true
                        }
                        launchSingleTop = true // @ top
                    }
                }
            )
        }
    }
}

