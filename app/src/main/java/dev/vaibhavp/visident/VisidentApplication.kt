package dev.vaibhavp.visident

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// empty for hilt ofc
@HiltAndroidApp
class VisidentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // hilt init lmao
    }
}