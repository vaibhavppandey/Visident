package dev.vaibhavp.visident.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object StartSessionRoute

/** Parent graph for the capture flow so Camera + EndSession share one CaptureViewModel. */
@Serializable
object CaptureGraph

@Serializable
object CameraCaptureRoute

@Serializable
object EndSessionRoute

@Serializable
object SearchSessionsRoute

@Serializable
data class SessionDetailsRoute(val sessionID: String)
