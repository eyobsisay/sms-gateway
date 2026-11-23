package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SmsGatewayViewModel

@Composable
fun QueueScreen(viewModel: SmsGatewayViewModel) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val queuedList = messages.filter { it.status == "QUEUED" || it.status == "PROCESSING" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "SMS Queue",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pending and actively processing SMS messages",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (queuedList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("SMS queue is currently empty.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(queuedList) { msg ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = msg.recipientName.ifBlank { msg.phoneNumber }, fontWeight = FontWeight.Bold)
                            Badge { Text(msg.status) }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Phone: ${msg.phoneNumber}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = msg.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
