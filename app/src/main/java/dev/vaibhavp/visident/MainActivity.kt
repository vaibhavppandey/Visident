package dev.vaibhavp.visident

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import dev.vaibhavp.visident.ui.navigation.CameraCaptureRoute
import dev.vaibhavp.visident.ui.session.CameraCaptureScreen
import dev.vaibhavp.visident.ui.theme.VisidentTheme
import androidx.navigation.compose.composable
import dev.vaibhavp.visident.ui.navigation.EndSessionRoute
import dev.vaibhavp.visident.ui.navigation.SearchSessionsRoute
import dev.vaibhavp.visident.ui.navigation.StartSessionRoute
import dev.vaibhavp.visident.ui.session.EndSessionScreen
import dev.vaibhavp.visident.ui.session.StartSessionScreen


@ExperimentalMaterial3Api
@ExperimentalPermissionsApi
@AndroidEntryPoint // hilt dagger hehe
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisidentTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = StartSessionRoute) {
                    composable<StartSessionRoute> {
                        StartSessionScreen(
                            onStartNewSessionClick = { navController.navigate(CameraCaptureRoute) },
                            onSearchSessionClick = { navController.navigate(SearchSessionsRoute) }

                        )
                    }
                    composable<CameraCaptureRoute> {
                        CameraCaptureScreen(
                            onEndSessionClick = { navController.navigate(EndSessionRoute) }
                        )
                    }
                    composable<EndSessionRoute> {
                        EndSessionScreen()
                    }
                }
            }
        }
    }
}
