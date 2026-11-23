package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.RetryConfigEntity
import com.example.ui.SmsGatewayViewModel

@Composable
fun RetryScreen(viewModel: SmsGatewayViewModel) {
    val retryConfig by viewModel.retryConfig.collectAsStateWithLifecycle()
    val failedList by viewModel.failedMessages.collectAsStateWithLifecycle()

    var autoRetry by remember { mutableStateOf(false) }
    var maxAttempts by remember { mutableStateOf("3") }
    var retryDelay by remember { mutableStateOf("5") }

    LaunchedEffect(retryConfig) {
        retryConfig?.let {
            autoRetry = it.autoRetryEnabled
            maxAttempts = it.maxAttempts.toString()
            retryDelay = it.retryDelayMinutes.toString()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Retry Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configure automated retry policies, attempt limits, and intervals",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Automatic Retry Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Enable Automatic Retry")
                        Switch(
                            checked = autoRetry,
                            onCheckedChange = {
                                autoRetry = it
                                viewModel.saveRetryConfig(
                                    RetryConfigEntity(
                                        autoRetryEnabled = it,
                                        maxAttempts = maxAttempts.toIntOrNull() ?: 3,
                                        retryDelayMinutes = retryDelay.toLongOrNull() ?: 5L
                                    )
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = maxAttempts,
                        onValueChange = {
                            maxAttempts = it
                            viewModel.saveRetryConfig(
                                RetryConfigEntity(
                                    autoRetryEnabled = autoRetry,
                                    maxAttempts = it.toIntOrNull() ?: 3,
                                    retryDelayMinutes = retryDelay.toLongOrNull() ?: 5L
                                )
                            )
                        },
                        label = { Text("Maximum Retry Attempts") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = retryDelay,
                        onValueChange = {
                            retryDelay = it
                            viewModel.saveRetryConfig(
                                RetryConfigEntity(
                                    autoRetryEnabled = autoRetry,
                                    maxAttempts = maxAttempts.toIntOrNull() ?: 3,
                                    retryDelayMinutes = it.toLongOrNull() ?: 5L
                                )
                            )
                        },
                        label = { Text("Retry Interval (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Retry Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Eligible Failed Messages: ${failedList.size}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.retryAllFailed() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Trigger Batch Retry Now")
                    }
                }
            }
        }
    }
}
