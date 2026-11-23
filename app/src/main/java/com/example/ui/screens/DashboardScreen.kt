package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SmsGatewayViewModel

@Composable
fun DashboardScreen(
    viewModel: SmsGatewayViewModel,
    onNavigate: (String) -> Unit
) {
    val total by viewModel.totalCount.collectAsStateWithLifecycle()
    val queued by viewModel.queuedCount.collectAsStateWithLifecycle()
    val sent by viewModel.sentCount.collectAsStateWithLifecycle()
    val delivered by viewModel.deliveredCount.collectAsStateWithLifecycle()
    val failed by viewModel.failedCount.collectAsStateWithLifecycle()
    val statusMsg by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isSendingActive by viewModel.isSendingActive.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SMS Gateway Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pull campaigns from server and send SMS one-by-one with interval",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!statusMsg.isNullOrBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSendingActive) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = statusMsg!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        item {
            // Prominent Campaign Dispatch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Campaign Dispatch Control",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pull phone numbers and templates from server, then transmit SMS sequentially with interval delay.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.startPullAndSendCampaign() },
                        enabled = !isSendingActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_campaign_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSendingActive) "Dispatching Campaign..." else "Start Campaign (Pull & Send One by One)")
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(title = "Total", count = total.toString(), icon = Icons.Default.Message, modifier = Modifier.weight(1f))
                StatCard(title = "Queued", count = queued.toString(), icon = Icons.Default.HourglassEmpty, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(title = "Sent", count = sent.toString(), icon = Icons.Default.Send, modifier = Modifier.weight(1f))
                StatCard(title = "Delivered", count = delivered.toString(), icon = Icons.Default.DoneAll, modifier = Modifier.weight(1f))
            }
        }

        item {
            StatCard(title = "Failed", count = failed.toString(), icon = Icons.Default.Error, modifier = Modifier.fillMaxWidth())
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.fetchCampaignJobs() },
                    enabled = !isSendingActive,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("fetch_jobs_button")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetch Jobs")
                }
                Button(
                    onClick = { viewModel.synchronizeNow() },
                    enabled = !isSendingActive,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sync_button")
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Now")
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.retryAllFailed() },
                enabled = !isSendingActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("retry_failed_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry All Eligible Failed SMS")
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
