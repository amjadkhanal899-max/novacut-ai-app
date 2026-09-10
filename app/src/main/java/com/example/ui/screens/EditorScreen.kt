package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatio
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.TimelineView
import com.example.ui.components.VideoPlayerCanvas
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.EditorTab
import com.example.viewmodel.EditorViewModel

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    onOpenExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project by viewModel.currentProject.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val selectedClipIndex by viewModel.selectedClipIndex.collectAsState()
    val currentTimeSec by viewModel.currentTimeSec.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBannerVisible by viewModel.admobService.isBannerVisible.collectAsState()
    val aiScriptResult by viewModel.aiScriptResult.collectAsState()
    val isGeneratingScript by viewModel.isGeneratingScript.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("editor_screen"),
        containerColor = DarkBackground,
        bottomBar = {
            AdMobBannerView(
                isVisible = isBannerVisible,
                onClose = { viewModel.admobService.hideBanner() },
                onAdClicked = { viewModel.unlockWithRewardedAd("NovaCut Pro Bundle") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Studio Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(0.5.dp, DarkBorder)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Aspect Ratio Selector Pills
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AspectRatio.entries.forEach { ratio ->
                        val isSelected = project.aspectRatio == ratio
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeonCyan else Color.Transparent)
                                .clickable { viewModel.updateAspectRatio(ratio) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = ratio.displayName.split(" ")[0],
                                color = if (isSelected) Color(0xFF00382E) else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Export Button
                Button(
                    onClick = onOpenExport,
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "EXPORT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            // Top Video Player Canvas Preview
            VideoPlayerCanvas(
                project = project,
                currentTimeSec = currentTimeSec,
                isPlaying = isPlaying,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                videoEngineService = viewModel.videoEngineService,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            // Studio Tabs Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EditorTab.entries.forEach { tab ->
                    val isTabSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTabSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isTabSelected) NeonCyan else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectTab(tab) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = tab.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tab.title,
                                color = if (isTabSelected) NeonCyan else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Active Tab Content View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DarkBackground)
            ) {
                when (activeTab) {
                    EditorTab.TIMELINE -> {
                        TimelineView(
                            project = project,
                            currentTimeSec = currentTimeSec,
                            selectedClipIndex = selectedClipIndex,
                            isPlaying = isPlaying,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSeek = { viewModel.seekTo(it) },
                            onSelectClip = { viewModel.selectClip(it) },
                            onUpdateMotion = { id, motion -> viewModel.updateClipMotion(id, motion) },
                            onUpdateSpeed = { id, speed -> viewModel.updateClipSpeed(id, speed) },
                            onUpdateFilter = { id, filter -> viewModel.updateClipFilter(id, filter) },
                            onUpdateDuration = { id, dur -> viewModel.updateClipDuration(id, dur) },
                            onAddSampleClip = { res, name -> viewModel.addSampleClip(res, name) },
                            onRemoveClip = { viewModel.removeSelectedClip() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    EditorTab.AI_SCRIPT -> {
                        AiScriptScreen(
                            isGenerating = isGeneratingScript,
                            scriptResult = aiScriptResult,
                            onGenerateScript = { topic, tone, dur -> viewModel.generateAiScript(topic, tone, dur) },
                            onApplyScript = { script -> viewModel.applyScriptToProject(script) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    EditorTab.VOICEOVER -> {
                        VoiceoverScreen(
                            voiceoverSettings = project.voiceover,
                            voiceoverEngineService = viewModel.voiceoverService,
                            isFeatureUnlocked = { viewModel.unlockWithRewardedAd(it) },
                            onSelectVoice = { viewModel.updateVoiceoverVoice(it) },
                            onUpdateScript = { viewModel.updateVoiceoverScript(it) },
                            onUpdateVolume = { viewModel.updateVoiceoverVolume(it) },
                            onUpdateSpeed = { viewModel.updateVoiceoverSpeed(it) },
                            onUpdateStability = { viewModel.updateVoiceoverStability(it) },
                            onUpdateClarity = { viewModel.updateVoiceoverClarity(it) },
                            onUnlockWithAd = { viewModel.unlockWithRewardedAd(it) },
                            isVoiceUnlocked = { viewModel.isFeatureUnlocked(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    EditorTab.AUDIO_MIX -> {
                        AudioMixerScreen(
                            currentBgm = project.bgmTrack,
                            bgmLibrary = viewModel.bgmLibrary,
                            voiceoverVolume = project.voiceover.volume,
                            onSelectBgm = { viewModel.updateBgmTrack(it) },
                            onUpdateBgmVolume = { viewModel.updateBgmVolume(it) },
                            onUpdateVoiceoverVolume = { viewModel.updateVoiceoverVolume(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    EditorTab.CAPTIONS -> {
                        CaptionsScreen(
                            captions = project.captions,
                            onAutoGenerateCaptions = { },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
