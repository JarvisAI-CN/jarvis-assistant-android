package com.assistant.voip.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.assistant.voip.features.call.ui.CallScreen
import com.assistant.voip.features.file.ui.FileScreen
import com.assistant.voip.features.task.ui.TaskScreen
import com.assistant.voip.presentation.MainViewModel
import com.assistant.voip.presentation.components.BottomNavigationBar
import com.assistant.voip.presentation.components.MainContent
import com.assistant.voip.presentation.components.PermissionRequestDialog
import com.assistant.voip.presentation.components.TopAppBar
import com.assistant.voip.presentation.screens.CallScreen
import com.assistant.voip.presentation.screens.MainScreen
import com.assistant.voip.presentation.screens.TaskScreen
import com.assistant.voip.presentation.screens.FileScreen
import com.assistant.voip.ui.theme.VoipAssistantTheme

@Composable
fun JarvisAssistantApp() {
    VoipAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar()
            },
            bottomBar = {
                BottomNavigationBar()
            },
            content = { padding ->
                JarvisAssistantAppContent(padding)
            }
        )
    }
}

@Composable
fun JarvisAssistantAppContent(padding: androidx.compose.foundation.layout.PaddingValues) {
    val viewModel = MainViewModel()
    var isPermissionDialogVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        // 模拟应用初始化
        try {
            // 检查权限
            val hasPermissions = viewModel.checkPermissions()
            if (!hasPermissions) {
                isPermissionDialogVisible = true
            }

            // 初始化应用
            viewModel.initializeApp()

        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color.White)
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // 加载动画
            LoadingScreen()
        }

        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MainContent()
        }

        // 权限请求对话框
        PermissionRequestDialog(
            visible = isPermissionDialogVisible,
            onDismiss = { isPermissionDialogVisible = false },
            onRequest = {
                viewModel.requestPermissions()
                isPermissionDialogVisible = false
            }
        )
    }
}

@Composable
fun MainContent() {
    val viewModel = MainViewModel()
    val currentScreen = viewModel.currentScreen

    when (currentScreen) {
        Screen.Main -> MainScreen()
        Screen.Call -> CallScreen()
        Screen.Tasks -> TaskScreen()
        Screen.Files -> FileScreen()
        Screen.Settings -> SettingsScreen()
    }
}

@Composable
fun LoadingScreen() {
    // 简单的加载动画
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 加载动画实现
    }
}

@Composable
fun SettingsScreen() {
    // 设置界面
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 设置内容
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewJarvisAssistantApp() {
    JarvisAssistantApp()
}
