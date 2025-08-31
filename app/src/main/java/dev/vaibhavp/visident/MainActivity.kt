package dev.vaibhavp.visident

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.room.Room
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.vaibhavp.visident.data.db.AppDB

@ExperimentalMaterial3Api
@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {

    companion object {
        lateinit var database: AppDB
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Room.databaseBuilder(
            applicationContext,
            AppDB::class.java,
            "visident_db"
        ).build()
        enableEdgeToEdge()
        setContent {
            VisidentApp()
        }
    }
}
