package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VoiceoverSettings
import com.example.model.VoiceoverVoice
import com.example.service.VoiceoverEngineService
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MintGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun VoiceoverScreen(
    voiceoverSettings: VoiceoverSettings,
    voiceoverEngineService: VoiceoverEngineService,
    isFeatureUnlocked: (String) -> Unit,
    onSelectVoice: (VoiceoverVoice) -> Unit,
    onUpdateScript: (String) -> Unit,
    onUpdateVolume: (Float) -> Unit,
    onUpdateSpeed: (Float) -> Unit,
    onUpdateStability: (Float) -> Unit,
    onUpdateClarity: (Float) -> Unit,
    onUnlockWithAd: (String) -> Unit,
    isVoiceUnlocked: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val isPlaying by voiceoverEngineService.isPlayingPreview.collectAsState()
    val previewProgress by voiceoverEngineService.previewProgress.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("voiceover_screen")
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ElectricViolet, GoldAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "ElevenLabs Neural TTS Studio", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Studio-grade AI voiceovers with emotional cadence", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Voice Selector Cards
        Text(text = "Select AI Voice Actor", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        voiceoverEngineService.availableVoices.forEach { voice ->
            val isSelected = voice.id == voiceoverSettings.voice.id
            val unlocked = !voice.isPremium || isVoiceUnlocked(voice.name)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (unlocked) {
                            onSelectVoice(voice)
                        } else {
                            onUnlockWithAd("Voice: ${voice.name}")
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) DarkSurfaceElevated else DarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) NeonCyan else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play preview button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonCyan else DarkSurfaceVariant)
                                .clickable {
                                    if (isPlaying) {
                                        voiceoverEngineService.stopAudioPreview()
                                    } else {
                                        coroutineScope.launch {
                                            voiceoverEngineService.playAudioPreview()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying && isSelected) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play Voice Sample",
                                tint = if (isSelected) Color(0xFF00382E) else TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = voice.name,
                                    color = if (isSelected) NeonCyan else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(text = voice.accent, color = TextSecondary, fontSize = 9.sp)
                                }
                            }
                            Text(
                                text = voice.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    if (voice.isPremium && !unlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldAccent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "WATCH AD", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (voice.isPremium) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MintGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "UNLOCKED", color = MintGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Script Text to Narrate
        Text(text = "Voiceover Script Text", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = voiceoverSettings.scriptText,
            onValueChange = { onUpdateScript(it) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = NeonCyan
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Audio Sliders (Volume, Stability, Speed)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Voice Tuning & Mixing", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                // Volume Boost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Voiceover Volume", color = TextSecondary, fontSize = 11.sp)
                    Text(text = "${(voiceoverSettings.volume * 100).toInt()}%", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = voiceoverSettings.volume,
                    onValueChange = onUpdateVolume,
                    valueRange = 0f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent, inactiveTrackColor = DarkSurfaceVariant)
                )

                // Stability
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Neural Stability", color = TextSecondary, fontSize = 11.sp)
                    Text(text = "${(voiceoverSettings.stability * 100).toInt()}%", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = voiceoverSettings.stability,
                    onValueChange = onUpdateStability,
                    valueRange = 0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan, inactiveTrackColor = DarkSurfaceVariant)
                )

                // Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Speech Rate", color = TextSecondary, fontSize = 11.sp)
                    Text(text = "${voiceoverSettings.speed}x", color = ElectricViolet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = voiceoverSettings.speed,
                    onValueChange = onUpdateSpeed,
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet, inactiveTrackColor = DarkSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Synthesize Button
        Button(
            onClick = {
                coroutineScope.launch {
                    voiceoverEngineService.playAudioPreview()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "SYNTHESIZE & TEST VOICEOVER", fontWeight = FontWeight.Bold)
        }

        if (isPlaying) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { previewProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = NeonCyan,
                trackColor = DarkSurfaceVariant
            )
        }
    }
}
