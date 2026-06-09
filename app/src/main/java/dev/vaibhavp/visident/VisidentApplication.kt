package dev.vaibhavp.visident

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point. Annotated for Hilt so it can generate the DI component. */
@HiltAndroidApp
class VisidentApplication : Application()
