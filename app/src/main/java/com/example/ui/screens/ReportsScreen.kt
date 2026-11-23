package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SmsGatewayViewModel

@Composable
fun ReportsScreen(viewModel: SmsGatewayViewModel) {
    val total by viewModel.totalCount.collectAsStateWithLifecycle()
    val sent by viewModel.sentCount.collectAsStateWithLifecycle()
    val delivered by viewModel.deliveredCount.collectAsStateWithLifecycle()
    val failed by viewModel.failedCount.collectAsStateWithLifecycle()

    val successRate = if (total > 0) ((delivered.toFloat() / total.toFloat()) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Retry Reports & Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Performance metrics and delivery success analytics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Delivery Success Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$successRate%", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { if (total > 0) delivered.toFloat() / total.toFloat() else 0f }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
