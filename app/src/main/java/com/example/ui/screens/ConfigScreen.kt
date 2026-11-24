package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DeviceConfigEntity
import com.example.ui.SmsGatewayViewModel

@Composable
fun ConfigScreen(viewModel: SmsGatewayViewModel) {
    val config by viewModel.deviceConfig.collectAsStateWithLifecycle()

    var baseUrl by remember { mutableStateOf("") }
    var apiToken by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var pollingInterval by remember { mutableStateOf("5") }
    var sendingDelaySeconds by remember { mutableStateOf("2") }
    var maxBatchSize by remember { mutableStateOf("10") }

    LaunchedEffect(config) {
        config?.let {
            baseUrl = it.serverBaseUrl
            apiToken = it.apiToken
            deviceName = it.deviceName
            pollingInterval = it.pollingIntervalMinutes.toString()
            sendingDelaySeconds = (it.sendingDelayMs / 1000L).toString()
            maxBatchSize = it.maxBatchSize.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Server Configuration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Connect this Android gateway to your remote campaign management portal",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Server Base URL") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = apiToken,
            onValueChange = { apiToken = it },
            label = { Text("API Authentication Token") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = deviceName,
            onValueChange = { deviceName = it },
            label = { Text("Device Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = maxBatchSize,
            onValueChange = { maxBatchSize = it },
            label = { Text("Jobs Batch Size (per pull)") },
            supportingText = { Text("How many jobs to request from the portal each pull (1–50)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pollingInterval,
            onValueChange = { pollingInterval = it },
            label = { Text("Polling Interval (Minutes)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = sendingDelaySeconds,
            onValueChange = { sendingDelaySeconds = it },
            label = { Text("Sending Interval Between SMS (Seconds)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val current = config ?: DeviceConfigEntity()
                    val sec = sendingDelaySeconds.toLongOrNull() ?: 2L
                    val batch = (maxBatchSize.toIntOrNull() ?: 10).coerceIn(1, 50)
                    viewModel.saveDeviceConfig(
                        current.copy(
                            serverBaseUrl = baseUrl,
                            apiToken = apiToken,
                            deviceName = deviceName,
                            pollingIntervalMinutes = pollingInterval.toIntOrNull() ?: 5,
                            sendingDelayMs = sec * 1000L,
                            maxBatchSize = batch
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Configuration")
            }

            OutlinedButton(
                onClick = { viewModel.testConnectionAndRegister() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Test & Register")
            }
        }
    }
}
