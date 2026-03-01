package com.assistant.voip.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.assistant.voip.presentation.Screen
import com.assistant.voip.presentation.MainViewModel

@Composable
fun BottomNavigationBar(
    viewModel: MainViewModel = MainViewModel()
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.nav_home)
                )
            },
            label = { Text(stringResource(R.string.nav_home)) },
            selected = viewModel.currentScreen.value == Screen.Main,
            onClick = {
                viewModel.handleNavigation(Screen.Main)
            },
            enabled = viewModel.canNavigateTo(Screen.Main)
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = stringResource(R.string.nav_call)
                )
            },
            label = { Text(stringResource(R.string.nav_call)) },
            selected = viewModel.currentScreen.value == Screen.Call,
            onClick = {
                viewModel.handleNavigation(Screen.Call)
            },
            enabled = viewModel.canNavigateTo(Screen.Call)
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.nav_settings)
                )
            },
            label = { Text(stringResource(R.string.nav_settings)) },
            selected = viewModel.currentScreen.value == Screen.Settings,
            onClick = {
                viewModel.handleNavigation(Screen.Settings)
            },
            enabled = viewModel.canNavigateTo(Screen.Settings)
        )
    }
}
