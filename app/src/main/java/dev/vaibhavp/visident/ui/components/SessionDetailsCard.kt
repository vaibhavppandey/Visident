package dev.vaibhavp.visident.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.ui.theme.VisidentTheme
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionDetailsCard(
    session: SessionEntity,
    modifier: Modifier = Modifier,
    onNavigateClick: (String) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Session ID", style = MaterialTheme.typography.bodyMedium)
                    Text("Name", style = MaterialTheme.typography.bodyMedium)
                    Text("Age", style = MaterialTheme.typography.bodyMedium)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(session.sessionId, style = MaterialTheme.typography.bodyMedium)
                    Text(session.name, style = MaterialTheme.typography.bodyMedium)
                    Text(session.age.toString(), style = MaterialTheme.typography.bodyMedium)
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Images: ${session.imageCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Created: ${session.createdAt.toDateString()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

//                 Spacer(modifier = Modifier.weight(1f)) // button = far end

                Button(
                    onClick = { onNavigateClick(session.sessionId) }
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "View Details")
                }
            }
        }
    }
}

private fun Long.toDateString(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(date)
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SessionDetailsCardPreview() {
    val session = SessionEntity(
        sessionId = "12345",
        name = "John Doe",
        age = 30,
        createdAt = System.currentTimeMillis(),
        imageCount = 5
    )
    VisidentTheme {
        SessionDetailsCard(
            session = session,
            onNavigateClick = { sessionId ->
                Timber.wtf("Navigate to details of session $sessionId")
            }
        )
    }
}
