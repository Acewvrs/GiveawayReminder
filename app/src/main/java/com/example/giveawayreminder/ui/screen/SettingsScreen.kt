package com.example.giveawayreminder.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.giveawayreminder.model.GameViewModel
import com.example.giveawayreminder.ui.theme.GiveawayReminderTheme


@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        val intervalOptions = listOf("Every Day", "2 Days", "7 Days")
        val timesOptions = listOf("12 am", "1 am", "2 am", "3 am", "4 am", "5 am", "6 am", "7 am",
            "8 am", "9 am", "10 am", "11 am", "12 pm", "1 pm", "2 pm", "3 pm", "4 pm", "5 pm",
            "6 pm", "7 pm", "8 pm", "9 pm", "10 pm", "11 pm")

        val viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory)

        settingsTitle(titleText = "Notification")

        Column(modifier = Modifier
            .padding(start = 16.dp)
            .padding(vertical = 16.dp)) {

            notificationSettings(viewModel, intervalOptions, timesOptions)
        }
        
        settingsTitle(titleText = "Giveaway List")

        dataSettings(viewModel)
    }
}

@Composable
fun settingsTitle(titleText: String) {
    Text(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        text = titleText,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun notificationSettings(viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory), intervalOptions: List<String>, timesOptions: List<String>) {
    val intervalPreference = viewModel.notificationIntervalState.collectAsState().value
    val hourPreference = viewModel.notificationHourState.collectAsState().value

    var dayOptionsExpanded by remember { mutableStateOf(false) }
    var timeOptionsExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val permission = Manifest.permission.POST_NOTIFICATIONS

    var hasNotificationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    SideEffect {
        // This will be triggered on every recomposition to ensure the state is up to date
        hasNotificationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    Text(
        modifier = Modifier.padding(top = 16.dp),
        text = "Permission"
    )

    Button(onClick = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }) {
        Text("Open Settings for Permission")
    }

    Text(
        text = "Show notifications for"
    )
    ExposedDropdownMenuBox(
        modifier = Modifier.padding(top = 16.dp),
        expanded = dayOptionsExpanded,
        onExpandedChange = {
            dayOptionsExpanded = !dayOptionsExpanded
        }
    ) {
        TextField(
            value = intervalPreference,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayOptionsExpanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = dayOptionsExpanded,
            onDismissRequest = { dayOptionsExpanded = false }
        ) {
            intervalOptions.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        dayOptionsExpanded = false
                        viewModel.saveAndSetNotificationInterval(item)
                    }
                )
            }
        }
    }

    Text(
        modifier = Modifier.padding(top = 16.dp),
        text = "Get notified at"
    )
    ExposedDropdownMenuBox(
        modifier = Modifier.padding(top = 16.dp),
        expanded = timeOptionsExpanded,
        onExpandedChange = {
            timeOptionsExpanded = !timeOptionsExpanded
        }
    ) {
        TextField(
            value = hourPreference,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeOptionsExpanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = timeOptionsExpanded,
            onDismissRequest = { timeOptionsExpanded = false }
        ) {
            timesOptions.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        timeOptionsExpanded = false
                        viewModel.saveAndSetNotificationHour(item)
                    }
                )
            }
        }
    }
}

@Composable
fun dataSettings(viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory)) {
    val dataRetrieveOptions = listOf("6 hours", "12 hours", "24 hours")

    val updateIntervalPreference = viewModel.updateIntervalState.collectAsState().value

    Text(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        text = "Update Cycle"
    )

    Row(modifier = Modifier.padding(start = 16.dp)) {
        dataRetrieveOptions.forEach { item ->
            Row(
                modifier = Modifier.selectable(
                    selected = updateIntervalPreference == item,
                    onClick = {}
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = updateIntervalPreference == item,
                    onClick = {
                        viewModel.saveAndSetListUpdateInterval(item)
                    }
                )
                Text(item)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestPreview() {
    GiveawayReminderTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            SettingsScreen()
        }
    }
}