package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SmsGatewayViewModel

@Composable
fun CampaignsScreen(viewModel: SmsGatewayViewModel) {
    val campaigns by viewModel.campaigns.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "SMS Campaigns",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Retrieved campaign jobs from remote portal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (campaigns.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No active campaigns found. Click 'Fetch Jobs' on Dashboard.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(campaigns) { campaign ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = campaign.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Badge { Text(campaign.status) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Campaign ID: ${campaign.campaignId}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Total Messages: ${campaign.totalMessages}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
