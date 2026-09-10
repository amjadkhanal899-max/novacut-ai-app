package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ColorFilterType
import com.example.model.KenBurnsMotion
import com.example.model.MediaClip
import com.example.model.VideoProject
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HotPink
import com.example.ui.theme.MintGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrackBgmColor
import com.example.ui.theme.TrackCaptionColor
import com.example.ui.theme.TrackVideoColor
import com.example.ui.theme.TrackVoiceoverColor
import java.util.Locale

@Composable
fun TimelineView(
    project: VideoProject,
    currentTimeSec: Float,
    selectedClipIndex: Int,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSelectClip: (Int) -> Unit,
    onUpdateMotion: (String, KenBurnsMotion) -> Unit,
    onUpdateSpeed: (String, Float) -> Unit,
    onUpdateFilter: (String, ColorFilterType) -> Unit,
    onUpdateDuration: (String, Float) -> Unit,
    onAddSampleClip: (Int, String) -> Unit,
    onRemoveClip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val selectedClip = project.clips.getOrNull(selectedClipIndex)
    var activeSubTool by remember { mutableStateOf("MOTION") } // MOTION, SPEED, FILTER, DURATION

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .testTag("timeline_view_container")
    ) {
        // Timeline Header: Playback Controls & Time Ruler
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playback buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onSeek((currentTimeSec - 2f).coerceAtLeast(0f)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                        .clickable { onTogglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF00382E),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { onSeek((currentTimeSec + 2f).coerceAtMost(project.totalDurationSec)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick Add Clip & Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            val samples = listOf(
                                Pair(R.drawable.sample_urban_neon, "Cyber Neon"),
                                Pair(R.drawable.sample_tech_cyber, "AI Core"),
                                Pair(R.drawable.sample_nature_mountain, "Peak Sunset")
                            )
                            val pick = samples[(project.clips.size) % samples.size]
                            onAddSampleClip(pick.first, pick.second)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Clip", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Clip", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onRemoveClip,
                    enabled = project.clips.size > 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Clip",
                        tint = if (project.clips.size > 1) HotPink else DarkBorder,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Multi-Track Visual Timeline Scroll Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground)
                .padding(vertical = 6.dp)
        ) {
            // Track 1: Video & Photo Clips with Ken Burns Motion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track Label
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TrackVideoColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "VID", color = TrackVideoColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                project.clips.forEachIndexed { index, clip ->
                    val isSelected = index == selectedClipIndex
                    val widthDp = (clip.durationSec * 28).dp.coerceIn(80.dp, 200.dp)

                    Box(
                        modifier = Modifier
                            .width(widthDp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Brush.linearGradient(listOf(TrackVideoColor, ElectricViolet))
                                else Brush.linearGradient(listOf(TrackVideoColor.copy(alpha = 0.6f), Color(0xFF1E2640)))
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NeonCyan else DarkBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectClip(index) }
                            .padding(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = clip.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", clip.durationSec)}s",
                                    color = TextGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${clip.motion.iconDescription} ${clip.motion.title}",
                                        color = NeonCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (clip.speed != 1.0f) {
                                    Text(
                                        text = "${clip.speed}x",
                                        color = GoldAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Track 2: Voiceover Waveform Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TrackVoiceoverColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "VOX", color = ElectricViolet, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TrackVoiceoverColor.copy(alpha = 0.35f))
                        .border(0.5.dp, TrackVoiceoverColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Voiceover",
                                tint = ElectricViolet,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ElevenLabs: ${project.voiceover.voice.name} (${(project.voiceover.volume * 100).toInt()}%)",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "|||| |||||| ||||| |||||||",
                            color = NeonCyan.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Track 3: BGM Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TrackBgmColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "BGM", color = MintGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TrackBgmColor.copy(alpha = 0.25f))
                        .border(0.5.dp, MintGreen.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "BGM",
                                tint = MintGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${project.bgmTrack.title} • ${project.bgmTrack.genre} (${(project.bgmTrack.volume * 100).toInt()}%)",
                                color = TextPrimary,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = "🎵 Loop",
                            color = MintGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Sub-Tool Switcher for Selected Clip
        if (selectedClip != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("MOTION" to "Ken Burns Motion", "SPEED" to "Clip Speed", "FILTER" to "Color Shaders", "DURATION" to "Trim / Time").forEach { (key, label) ->
                        val isSelected = activeSubTool == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceElevated)
                                .border(1.dp, if (isSelected) NeonCyan else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { activeSubTool = key }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sub-Tool Controls
                when (activeSubTool) {
                    "MOTION" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KenBurnsMotion.entries.forEach { motion ->
                                val isChosen = selectedClip.motion == motion
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isChosen) androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(NeonCyan.copy(alpha = 0.3f), ElectricViolet.copy(alpha = 0.3f))
                                            ) else androidx.compose.ui.graphics.SolidColor(DarkSurfaceElevated)
                                        )
                                        .border(1.dp, if (isChosen) NeonCyan else DarkBorder, RoundedCornerShape(10.dp))
                                        .clickable { onUpdateMotion(selectedClip.id, motion) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = motion.iconDescription, fontSize = 16.sp)
                                        Text(
                                            text = motion.title,
                                            color = if (isChosen) NeonCyan else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "SPEED" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Playback Rate: ${selectedClip.speed}x", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(0.5f, 1.0f, 1.5f, 2.0f, 3.0f).forEach { spd ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selectedClip.speed == spd) GoldAccent else DarkSurfaceVariant)
                                                .clickable { onUpdateSpeed(selectedClip.id, spd) }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${spd}x",
                                                color = if (selectedClip.speed == spd) Color.Black else TextPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Slider(
                                value = selectedClip.speed,
                                onValueChange = { onUpdateSpeed(selectedClip.id, it) },
                                valueRange = 0.5f..3.0f,
                                steps = 4,
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldAccent,
                                    activeTrackColor = GoldAccent,
                                    inactiveTrackColor = DarkSurfaceVariant
                                )
                            )
                        }
                    }
                    "FILTER" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ColorFilterType.entries.forEach { filter ->
                                val isFilterChosen = selectedClip.filterType == filter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isFilterChosen) filter.glowColor.copy(alpha = 0.25f) else DarkSurfaceElevated)
                                        .border(1.dp, if (isFilterChosen) filter.glowColor else DarkBorder, RoundedCornerShape(10.dp))
                                        .clickable { onUpdateFilter(selectedClip.id, filter) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = filter.title,
                                        color = if (isFilterChosen) filter.glowColor else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    "DURATION" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Duration: ${String.format(Locale.US, "%.1f", selectedClip.durationSec)} seconds",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable { onUpdateDuration(selectedClip.id, (selectedClip.durationSec - 0.5f).coerceAtLeast(1.0f)) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "- 0.5s", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonCyan)
                                        .clickable { onUpdateDuration(selectedClip.id, (selectedClip.durationSec + 0.5f).coerceAtMost(15.0f)) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "+ 0.5s", color = Color(0xFF00382E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
