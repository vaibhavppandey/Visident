package dev.vaibhavp.visident

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import dev.vaibhavp.visident.ui.theme.VisidentTheme
import dev.vaibhavp.visident.ui.navigation.VisidentNavHost


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
                VisidentNavHost(navController = navController)
            }
        }
    }
}

