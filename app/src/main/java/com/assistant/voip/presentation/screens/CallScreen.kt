package com.assistant.voip.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assistant.voip.presentation.MainViewModel

@Composable
fun CallScreen(
    viewModel: MainViewModel = MainViewModel()
) {
    val uiState = CallScreenUiState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (uiState.callStatus) {
                CallStatus.Inactive -> CallInactiveState()
                CallStatus.Connecting -> CallConnectingState()
                CallStatus.Active -> CallActiveState()
                CallStatus.Disconnecting -> CallDisconnectingState()
            }

            // 通话控制栏
            CallControlBar(
                callStatus = uiState.callStatus,
                isMuted = uiState.isMuted,
                isSpeakerphone = uiState.isSpeakerphone,
                isHold = uiState.isHold,
                onMuteClick = { /* 静音逻辑 */ },
                onSpeakerClick = { /* 扬声器逻辑 */ },
                onHoldClick = { /* 保持逻辑 */ },
                onEndCallClick = { /* 结束通话逻辑 */ }
            )
        }
    }
}

@Composable
fun CallInactiveState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_avatar),
                contentDescription = "用户头像",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 用户名
        Text(
            text = "贾维斯助手",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 状态
        Text(
            text = "离线",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 拨打按钮
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_call),
                contentDescription = "拨打按钮",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun CallConnectingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_avatar),
                contentDescription = "用户头像",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 用户名
        Text(
            text = "贾维斯助手",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 状态
        Text(
            text = "正在连接...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 进度指示器
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 取消按钮
        TextButton(
            onClick = { /* 取消通话逻辑 */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "取消",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun CallActiveState() {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerphone by remember { mutableStateOf(false) }
    var isHold by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_avatar),
                contentDescription = "用户头像",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 用户名
        Text(
            text = "贾维斯助手",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 状态
        Text(
            text = if (isHold) "通话保持" else "通话中",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 通话计时器
        Text(
            text = formatCallDuration(0),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun CallDisconnectingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_avatar),
                contentDescription = "用户头像",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 用户名
        Text(
            text = "贾维斯助手",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 状态
        Text(
            text = "正在挂断...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 进度指示器
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(56.dp)
        )
    }
}

@Composable
fun CallControlBar(
    callStatus: CallStatus,
    isMuted: Boolean,
    isSpeakerphone: Boolean,
    isHold: Boolean,
    onMuteClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    onHoldClick: () -> Unit,
    onEndCallClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (callStatus) {
                    CallStatus.Inactive -> {
                        // 拨打按钮
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_call),
                                contentDescription = "拨打按钮",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    CallStatus.Connecting -> {
                        // 取消按钮
                        TextButton(onClick = onEndCallClick) {
                            Text(
                                text = "取消",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    CallStatus.Active -> {
                        // 静音按钮
                        ControlButton(
                            icon = if (isMuted) R.drawable.ic_mute_on else R.drawable.ic_mute_off,
                            color = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                            onClick = onMuteClick
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // 扬声器按钮
                        ControlButton(
                            icon = if (isSpeakerphone) R.drawable.ic_speaker_on else R.drawable.ic_speaker_off,
                            color = if (isSpeakerphone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            onClick = onSpeakerClick
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // 保持按钮
                        ControlButton(
                            icon = if (isHold) R.drawable.ic_hold_on else R.drawable.ic_hold_off,
                            color = if (isHold) MaterialTheme.colorScheme.warning else MaterialTheme.colorScheme.secondary,
                            onClick = onHoldClick
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // 挂断按钮
                        ControlButton(
                            icon = R.drawable.ic_end_call,
                            color = MaterialTheme.colorScheme.error,
                            onClick = onEndCallClick
                        )
                    }

                    CallStatus.Disconnecting -> {
                        // 挂断中
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.error,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ControlButton(
    icon: Int,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = "控制按钮",
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun formatCallDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

data class CallScreenUiState(
    val callStatus: CallStatus = CallStatus.Inactive,
    val isMuted: Boolean = false,
    val isSpeakerphone: Boolean = false,
    val isHold: Boolean = false,
    val duration: Int = 0
)

enum class CallStatus {
    Inactive,
    Connecting,
    Active,
    Disconnecting
}

@Preview(showBackground = true)
@Composable
fun CallScreenPreview() {
    CallScreen()
}
