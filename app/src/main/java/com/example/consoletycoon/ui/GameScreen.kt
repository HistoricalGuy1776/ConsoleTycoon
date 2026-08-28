package com.example.consoletycoon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consoletycoon.GameViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.consoletycoon.GameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Console Tycoon") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatsSection(state)
            
            DesignConsoleSection(
                onLaunch = { name, cpu, ram, os ->
                    viewModel.launchConsole(name, cpu, ram, os)
                }
            )
            
            SubscriptionSection(
                state = state,
                onLaunchSub = { viewModel.launchSubscriptionService() },
                onPriceChange = { viewModel.updateSubPrice(it) },
                onCloudGamingChange = { viewModel.updateCloudGaming(it) },
                onDayOneChange = { viewModel.updateDayOneExclusives(it) }
            )
            
            LogsSection(state.logs)
        }
    }
}

@Composable
fun StatsSection(state: GameState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = state.date.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cash: $${state.money}")
                Text("Daily Income: $${state.revenuePerDay}/day")
            }
            Text("Active Console: ${state.activeConsoleName}")
            Text("Subs: ${state.subscribers} ($${state.subMonthlyRevenue}/mo)")
        }
    }
}

@Composable
fun DesignConsoleSection(onLaunch: (String, Int, Int, Int) -> Unit) {
    var name by remember { mutableStateOf("Xbox 720") }
    var cpuIndex by remember { mutableStateOf(0) }
    var ramIndex by remember { mutableStateOf(0) }
    var osIndex by remember { mutableStateOf(0) }

    val cpuOptions = listOf("Low-End 733MHz ($5,000)", "Mid-Tier 1.2 GHz ($12,000)", "High-End 3.2 GHz ($30,000)")
    val ramOptions = listOf("64 MB ($2,000)", "512 MB ($8,000)", "4 GB ($20,000)")
    val osOptions = listOf("Windows CE ($3,000)", "Linux Kernel ($500)", "XOS ($15,000)")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Design Next Gen Console", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Console Name") },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownSelector("Select CPU Size", cpuOptions, cpuIndex) { cpuIndex = it }
            DropdownSelector("Select RAM Capacity", ramOptions, ramIndex) { ramIndex = it }
            DropdownSelector("Select OS", osOptions, osIndex) { osIndex = it }
            
            Button(
                onClick = { onLaunch(name, cpuIndex, ramIndex, osIndex) },
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
            ) {
                Text("Launch Console to Market!")
            }
        }
    }
}

@Composable
fun DropdownSelector(label: String, options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(vertical = 4.dp)) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${options[selectedIndex]}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SubscriptionSection(
    state: GameState,
    onLaunchSub: () -> Unit,
    onPriceChange: (Int) -> Unit,
    onCloudGamingChange: (Boolean) -> Unit,
    onDayOneChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Manage Subscription Service", fontWeight = FontWeight.Bold)
            if (!state.isSubActive) {
                Button(onClick = onLaunchSub, modifier = Modifier.fillMaxWidth()) {
                    Text("Launch Service Pass ($10,000 Setup)")
                }
            } else {
                Text("Service Status: ONLINE", color = MaterialTheme.colorScheme.primary)
                Text("Monthly Price: $${state.subPrice}")
                Slider(
                    value = state.subPrice.toFloat(),
                    onValueChange = { onPriceChange(it.toInt()) },
                    valueRange = 5f..30f,
                    steps = 25
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.includeCloudGaming, onCheckedChange = onCloudGamingChange)
                    Text("Cloud Gaming (+5,000 R&D)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.includeDayOneExclusives, onCheckedChange = onDayOneChange)
                    Text("Day-One Blockbusters (+15,000 R&D)")
                }
            }
        }
    }
}

@Composable
fun LogsSection(logs: List<String>) {
    if (logs.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Event Log", fontWeight = FontWeight.Bold)
                logs.forEach { log ->
                    Text(log, fontSize = 12.sp)
                }
            }
        }
    }
}
