package dev.vaibhavp.visident

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.vaibhavp.visident.data.db.AppDB

// empty for hilt ofc
@HiltAndroidApp
class VisidentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // hilt init lmao
    }
}