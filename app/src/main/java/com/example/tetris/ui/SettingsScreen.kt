package com.example.tetris.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tetris.logic.TetrisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: TetrisViewModel, onBack: () -> Unit) {
    var musicVolume by remember { mutableStateOf(viewModel.musicVolume) }
    var sfxVolume by remember { mutableStateOf(viewModel.sfxVolume) }
    var isMusicOn by remember { mutableStateOf(viewModel.isMusicOn) }
    var isSfxOn by remember { mutableStateOf(viewModel.isSfxOn) }
    var isVibrationOn by remember { mutableStateOf(viewModel.isVibrationOn) }
    var isGhostOn by remember { mutableStateOf(viewModel.isGhostOn) }
    var speedLevel by remember { mutableStateOf(viewModel.speedLevel) }
    var themeMode by remember { mutableStateOf(viewModel.themeMode) }

    val speedOptions = listOf("Chậm", "Trung bình", "Nhanh")
    val themeOptions = listOf("☀️ Sáng", "🌙 Tối")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Âm thanh", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Nhạc nền")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isMusicOn, onCheckedChange = {
                        isMusicOn = it
                        viewModel.isMusicOn = it
                        viewModel.setSoundManagerBGM(it)
                        viewModel.saveSettings()
                    })
                }
                if (isMusicOn) {
                    Slider(
                        value = musicVolume,
                        onValueChange = {
                            musicVolume = it
                            viewModel.musicVolume = it
                            viewModel.saveSettings()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hiệu ứng âm thanh")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isSfxOn, onCheckedChange = {
                        isSfxOn = it
                        viewModel.isSfxOn = it
                        viewModel.saveSettings()
                    })
                }
                if (isSfxOn) {
                    Slider(
                        value = sfxVolume,
                        onValueChange = {
                            sfxVolume = it
                            viewModel.sfxVolume = it
                            viewModel.saveSettings()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Text("Điều khiển", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rung khi Hard Drop")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isVibrationOn, onCheckedChange = {
                        isVibrationOn = it
                        viewModel.isVibrationOn = it
                        viewModel.saveSettings()
                    })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ghost Piece (khối mờ)")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isGhostOn, onCheckedChange = {
                        isGhostOn = it
                        viewModel.isGhostOn = it
                        viewModel.saveSettings()
                    })
                }
            }

            item {
                Text("Giao diện", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Chủ đề màu")
                    Spacer(modifier = Modifier.weight(1f))
                    SingleChoiceSegmentedButtonRow {
                        themeOptions.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = themeMode == index,
                                onClick = {
                                    themeMode = index
                                    viewModel.themeMode = index
                                    viewModel.saveSettings()
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            item {
                Text("Tốc độ rơi", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    speedOptions.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = speedLevel == index,
                            onClick = {
                                speedLevel = index
                                viewModel.changeSpeed(index)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = speedOptions.size),
                            label = { Text(label) }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.resetToDefaultSettings()
                        musicVolume = viewModel.musicVolume
                        sfxVolume = viewModel.sfxVolume
                        isMusicOn = viewModel.isMusicOn
                        isSfxOn = viewModel.isSfxOn
                        isVibrationOn = viewModel.isVibrationOn
                        isGhostOn = viewModel.isGhostOn
                        themeMode = viewModel.themeMode
                        speedLevel = viewModel.speedLevel
                        viewModel.saveSettings()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mặc định")
                }
            }
        }
    }
}