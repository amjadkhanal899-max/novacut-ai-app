package com.example.ui.screens

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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BgmTrack
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

@Composable
fun AudioMixerScreen(
    currentBgm: BgmTrack,
    bgmLibrary: List<BgmTrack>,
    voiceoverVolume: Float,
    onSelectBgm: (BgmTrack) -> Unit,
    onUpdateBgmVolume: (Float) -> Unit,
    onUpdateVoiceoverVolume: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("audio_mixer_screen")
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MintGreen.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(MintGreen, NeonCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Multi-Track Audio Mixing Engine", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Background music auto-ducking & frequency separation", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sliders Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Master Volume Controls", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Voiceover volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Voiceover Track Level", color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(text = "${(voiceoverVolume * 100).toInt()}%", color = ElectricViolet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = voiceoverVolume,
                    onValueChange = onUpdateVoiceoverVolume,
                    valueRange = 0f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet, inactiveTrackColor = DarkSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // BGM Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = MintGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Background Music Level", color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(text = "${(currentBgm.volume * 100).toInt()}%", color = MintGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = currentBgm.volume,
                    onValueChange = onUpdateBgmVolume,
                    valueRange = 0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = MintGreen, activeTrackColor = MintGreen, inactiveTrackColor = DarkSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // BGM Sound Library
        Text(text = "Royalty-Free BGM Library", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        bgmLibrary.forEach { track ->
            val isSelected = track.id == currentBgm.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectBgm(track) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) DarkSurfaceElevated else DarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) MintGreen else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MintGreen else DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = track.title, color = if (isSelected) MintGreen else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${track.genre} • ${track.artist}", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MintGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "ACTIVE", color = MintGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
