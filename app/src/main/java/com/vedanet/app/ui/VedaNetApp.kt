package com.vedanet.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vedanet.app.data.Profile
import com.vedanet.app.data.ProfileStore
import com.vedanet.app.data.Protocol
import com.vedanet.app.ui.theme.VedaNetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VedaNetApp() {
    val context = LocalContext.current
    val store = remember { ProfileStore(context) }
    val profiles = remember { mutableStateListOf<Profile>().apply { addAll(store.load()) } }

    var showAddDialog by remember { mutableStateOf(false) }

    VedaNetTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("VedaNet") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Not connected",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Select a profile to connect",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = { showAddDialog = true }) {
                            Text("Add profile")
                        }
                    }
                }

                Text(
                    text = "Profiles",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(profiles) { profile ->
                        ProfileCard(
                            profile = profile,
                            onDelete = {
                                store.delete(profile.id)
                                profiles.removeAll { it.id == profile.id }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddProfileDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, protocol, address, port, secret, sni ->
                val profile = store.add(name, protocol, address, port, secret, sni)
                profiles.add(profile)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: Profile,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${profile.protocol.name} • ${profile.address}:${profile.port}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onSave: (String, Protocol, String, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("443") }
    var secret by remember { mutableStateOf("") }
    var sni by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf(Protocol.VLESS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Server address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text("UUID / password") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sni,
                    onValueChange = { sni = it },
                    label = { Text("SNI (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Protocol: ${protocol.name}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Protocol.entries.forEach { selected ->
                        TextButton(onClick = { protocol = selected }) {
                            Text(selected.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        protocol,
                        address,
                        port.toIntOrNull() ?: 443,
                        secret,
                        sni
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}