package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ResolutionPreset
import com.example.ui.components.InterstitialAdDialog
import com.example.ui.components.RewardedAdDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NovaCutAITheme
import com.example.viewmodel.EditorTab
import com.example.viewmodel.EditorViewModel

enum class AppScreen {
    DASHBOARD,
    EDITOR,
    EXPORT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovaCutAITheme {
                NovaCutApp()
            }
        }
    }
}

@Composable
fun NovaCutApp(
    viewModel: EditorViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val project by viewModel.currentProject.collectAsState()
    val isInterstitialShowing by viewModel.admobService.isInterstitialShowing.collectAsState()
    val isRewardedAdShowing by viewModel.admobService.isRewardedAdShowing.collectAsState()
    val pendingRewardTitle by viewModel.admobService.unlockedRewardItem.collectAsState()
    val selectedExportPreset by viewModel.selectedExportPreset.collectAsState()
    val isProUnlocked = viewModel.isFeatureUnlocked("NovaCut Pro Bundle")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == AppScreen.EXPORT || (targetState == AppScreen.EDITOR && initialState == AppScreen.DASHBOARD)) {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        currentProject = project,
                        isProUnlocked = isProUnlocked,
                        onOpenEditor = { currentScreen = AppScreen.EDITOR },
                        onOpenAiScript = {
                            viewModel.selectTab(EditorTab.AI_SCRIPT)
                            currentScreen = AppScreen.EDITOR
                        },
                        onOpenVoiceover = {
                            viewModel.selectTab(EditorTab.VOICEOVER)
                            currentScreen = AppScreen.EDITOR
                        },
                        onOpenSettings = { showSettingsDialog = true },
                        onUnlockPro = { viewModel.unlockWithRewardedAd("NovaCut Pro Bundle") }
                    )
                }

                AppScreen.EDITOR -> {
                    EditorScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = AppScreen.DASHBOARD },
                        onOpenExport = { currentScreen = AppScreen.EXPORT }
                    )
                }

                AppScreen.EXPORT -> {
                    ExportScreen(
                        project = project,
                        videoEngineService = viewModel.videoEngineService,
                        selectedPreset = selectedExportPreset,
                        onSelectPreset = { viewModel.setExportPreset(it) },
                        onTriggerExport = {
                            viewModel.triggerExportWithAd { _ ->
                                // Completed export callback
                            }
                        },
                        onUnlockWithRewardedAd = { feature ->
                            viewModel.unlockWithRewardedAd(feature)
                        },
                        isPresetUnlocked = { preset ->
                            viewModel.isFeatureUnlocked(preset)
                        },
                        onBackToEditor = { currentScreen = AppScreen.EDITOR }
                    )
                }
            }
        }

        // Global AdMob Interstitial Overlay
        InterstitialAdDialog(
            isVisible = isInterstitialShowing,
            onDismiss = {
                viewModel.admobService.dismissInterstitial()
            }
        )

        // Global AdMob Rewarded Video Overlay
        RewardedAdDialog(
            isVisible = isRewardedAdShowing,
            rewardName = pendingRewardTitle ?: "NovaCut AI Pro Feature",
            onCompleteReward = {
                viewModel.admobService.completeRewardedAd()
            },
            onDismiss = {
                viewModel.admobService.dismissRewardedAd()
            }
        )

        // Settings Dialog (API Keys)
        SettingsDialog(
            isVisible = showSettingsDialog,
            openAiKey = viewModel.aiScriptService.openAiApiKey,
            elevenLabsKey = viewModel.voiceoverService.elevenLabsApiKey,
            onSaveKeys = { openAi, elevenLabs ->
                viewModel.aiScriptService.openAiApiKey = openAi
                viewModel.voiceoverService.elevenLabsApiKey = elevenLabs
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
