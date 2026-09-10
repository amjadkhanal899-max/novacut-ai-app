package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatio
import com.example.model.ColorFilterType
import com.example.model.KenBurnsMotion
import com.example.model.MediaClip
import com.example.model.VideoProject
import com.example.service.MotionTransform
import com.example.service.VideoEngineService
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
import java.util.Locale

@Composable
fun VideoPlayerCanvas(
    project: VideoProject,
    currentTimeSec: Float,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    videoEngineService: VideoEngineService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Find active clip and local progress
    var accumulatedTime = 0f
    var activeClip = project.clips.firstOrNull()
    var clipProgress = 0f

    for (clip in project.clips) {
        if (currentTimeSec >= accumulatedTime && currentTimeSec < accumulatedTime + clip.durationSec) {
            activeClip = clip
            clipProgress = (currentTimeSec - accumulatedTime) / clip.durationSec.coerceAtLeast(0.1f)
            break
        }
        accumulatedTime += clip.durationSec
    }
    if (activeClip == null && project.clips.isNotEmpty()) {
        activeClip = project.clips.last()
        clipProgress = 1.0f
    }

    // Calculate Ken Burns transform
    val transform: MotionTransform = remember(activeClip?.motion, clipProgress) {
        if (activeClip != null) {
            videoEngineService.calculateTransform(activeClip.motion, clipProgress)
        } else {
            MotionTransform(1f, 0f, 0f, 0f)
        }
    }

    // Active subtitle
    val activeCaption = project.captions.firstOrNull {
        currentTimeSec >= it.startSec && currentTimeSec <= it.endSec
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .testTag("video_player_container"),
        contentAlignment = Alignment.Center
    ) {
        // Aspect Ratio Box
        val targetAspectRatio = project.aspectRatio.ratio
        Box(
            modifier = Modifier
                .fillMaxWidth(if (project.aspectRatio == AspectRatio.RATIO_9_16) 0.58f else 0.94f)
                .aspectRatio(targetAspectRatio)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(1.5.dp, Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.6f), ElectricViolet.copy(alpha = 0.6f))), RoundedCornerShape(16.dp))
                .clickable { onTogglePlayPause() },
            contentAlignment = Alignment.Center
        ) {
            // Clip Canvas Layer with Ken Burns Transform
            if (activeClip != null) {
                val colorFilter = when (activeClip.filterType) {
                    ColorFilterType.NONE -> null
                    ColorFilterType.CYBERPUNK -> ColorFilter.lighting(Color(0xFF00E5FF), Color(0xFF1B0033))
                    ColorFilterType.WARM_GOLD -> ColorFilter.lighting(Color(0xFFFFD166), Color(0xFF3A2000))
                    ColorFilterType.CINEMATIC -> ColorFilter.lighting(Color(0xFF80FFDB), Color(0xFF002933))
                    ColorFilterType.VINTAGE -> ColorFilter.lighting(Color(0xFFFF85A1), Color(0xFF240046))
                    ColorFilterType.NOIR -> ColorFilter.colorMatrix(
                        androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) }
                    )
                }

                Image(
                    painter = painterResource(id = activeClip.drawableResId),
                    contentDescription = activeClip.name,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(transform.scale)
                        .rotate(transform.rotationDegrees)
                )
            } else {
                Text(
                    text = "No media loaded",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Top Status Bar (Aspect ratio, Motion type, Audio status)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Motion Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(0.5.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${activeClip?.motion?.iconDescription ?: "🎬"} ${activeClip?.motion?.title ?: "Ken Burns"}",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Audio indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(0.5.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Track",
                            tint = GoldAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${project.voiceover.voice.name} + BGM",
                            color = TextGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Subtitle / Caption Overlay
            if (activeCaption != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp, start = 12.dp, end = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(1.dp, GoldAccent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = activeCaption.text,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            // Play / Pause big indicator when paused
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.5.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Bottom Timecode HUD
        val currentMinutes = (currentTimeSec / 60).toInt()
        val currentSeconds = (currentTimeSec % 60).toInt()
        val currentMillis = ((currentTimeSec * 10) % 10).toInt()

        val totalSec = project.totalDurationSec
        val totalMinutes = (totalSec / 60).toInt()
        val totalSeconds = (totalSec % 60).toInt()

        val timeString = String.format(
            Locale.US,
            "%02d:%02d.%01d / %02d:%02d.0",
            currentMinutes, currentSeconds, currentMillis,
            totalMinutes, totalSeconds
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DarkSurfaceElevated.copy(alpha = 0.9f))
                .border(0.5.dp, DarkBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeString,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
