package dev.vaibhavp.visident.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 shape scale. Components read these via MaterialTheme.shapes; call sites should
// reference the scale (e.g. MaterialTheme.shapes.large) rather than hard-coding shapes — this
// is what replaces the previous CircleShape-on-text-field usage.
val VisidentShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
