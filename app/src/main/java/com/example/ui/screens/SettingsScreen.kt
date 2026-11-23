package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DeviceConfigEntity
import com.example.data.local.entity.RetryConfigEntity
import com.example.ui.SmsGatewayViewModel

@Composable
fun SettingsScreen(viewModel: SmsGatewayViewModel) {
    val retryConfig by viewModel.retryConfig.collectAsStateWithLifecycle()
    val deviceConfig by viewModel.deviceConfig.collectAsStateWithLifecycle()
    val statusMsg by viewModel.statusMessage.collectAsStateWithLifecycle()

    var autoRetry by remember { mutableStateOf(false) }
    var maxAttempts by remember { mutableStateOf("3") }
    var retryDelay by remember { mutableStateOf("5") }
    var sendingDelaySeconds by remember { mutableStateOf("2") }

    LaunchedEffect(retryConfig, deviceConfig) {
        retryConfig?.let {
            autoRetry = it.autoRetryEnabled
            maxAttempts = it.maxAttempts.toString()
            retryDelay = it.retryDelayMinutes.toString()
        }
        deviceConfig?.let {
            sendingDelaySeconds = (it.sendingDelayMs / 1000L).toString()
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
                text = "Application Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configure retry policies, attempt limits, and SMS sending intervals",
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
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = statusMsg!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Campaign Dispatch Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = sendingDelaySeconds,
                        onValueChange = { sendingDelaySeconds = it },
                        label = { Text("Sending Interval Between SMS (Seconds)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Automatic Retry Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Enable Automatic Retry", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = autoRetry,
                            onCheckedChange = { autoRetry = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = maxAttempts,
                        onValueChange = { maxAttempts = it },
                        label = { Text("Maximum Retry Attempts") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = retryDelay,
                        onValueChange = { retryDelay = it },
                        label = { Text("Retry Interval (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        viewModel.saveRetryConfig(
                            RetryConfigEntity(
                                autoRetryEnabled = autoRetry,
                                maxAttempts = maxAttempts.toIntOrNull() ?: 3,
                                retryDelayMinutes = retryDelay.toLongOrNull() ?: 5L
                            )
                        )
                        deviceConfig?.let {
                            val sec = sendingDelaySeconds.toLongOrNull() ?: 2L
                            viewModel.saveDeviceConfig(it.copy(sendingDelayMs = sec * 1000L))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save All Settings")
                }

                OutlinedButton(
                    onClick = { viewModel.testConnectionAndRegister() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Server Connection")
                }
            }
        }
    }
}
