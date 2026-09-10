package com.example.model

import com.example.R

enum class AspectRatio(val displayName: String, val ratio: Float, val iconName: String) {
    RATIO_9_16("9:16 Shorts", 9f / 16f, "vertical"),
    RATIO_16_9("16:9 Landscape", 16f / 9f, "horizontal"),
    RATIO_1_1("1:1 Square", 1f, "square")
}

enum class ResolutionPreset(
    val label: String,
    val resolutionText: String,
    val width: Int,
    val height: Int,
    val isPremium: Boolean,
    val badge: String
) {
    HD_720P("720p HD", "720 × 1280", 720, 1280, false, "FAST"),
    FHD_1080P("1080p FHD", "1080 × 1920", 1080, 1920, false, "POPULAR"),
    ULTRA_4K("4K Ultra HD", "2160 × 3840", 2160, 3840, true, "PRO / AD"),
    CINEMA_8K("8K Cinema", "4320 × 7680", 4320, 7680, true, "PRO / AD")
}

enum class KenBurnsMotion(val title: String, val iconDescription: String, val detail: String) {
    ZOOM_IN("Zoom In", "🔍+", "Smooth forward push into focal subject"),
    ZOOM_OUT("Zoom Out", "🔍-", "Cinematic reveal pulling backward"),
    PAN_LEFT("Pan Left", "◀️", "Sweeping horizontal pan left-to-right"),
    PAN_RIGHT("Pan Right", "▶️", "Dramatic horizontal drift right"),
    ORBIT_3D("3D Orbit", "🔄", "Dynamic diagonal rotation & tilt scale"),
    STATIC("Static", "⏹️", "Locked frame without camera motion")
}

enum class ColorFilterType(val title: String, val glowColorHex: Long) {
    NONE("Original", 0xFF888888L),
    CYBERPUNK("Neon Cyber", 0xFF00F5D4L),
    WARM_GOLD("Golden Hour", 0xFFFFD166L),
    CINEMATIC("Teal Orange", 0xFF06D6A0L),
    VINTAGE("Retro VHS", 0xFFFF477EL),
    NOIR("B&W Noir", 0xFFDDDDDDL);

    val glowColor: androidx.compose.ui.graphics.Color
        get() = androidx.compose.ui.graphics.Color(glowColorHex.toInt())
}

data class MediaClip(
    val id: String,
    val name: String,
    val drawableResId: Int = R.drawable.sample_urban_neon,
    val customUriString: String? = null,
    val durationSec: Float = 3.5f,
    val motion: KenBurnsMotion = KenBurnsMotion.ZOOM_IN,
    val speed: Float = 1.0f,
    val filterType: ColorFilterType = ColorFilterType.NONE,
    val isSelected: Boolean = false
)

data class VoiceoverVoice(
    val id: String,
    val name: String,
    val gender: String,
    val accent: String,
    val description: String,
    val isPremium: Boolean = false,
    val sampleText: String = "Welcome to NovaCut AI. Your video editing is now effortless."
)

data class VoiceoverSettings(
    val voice: VoiceoverVoice,
    val scriptText: String = "Did you know that 90% of viral shorts use automated kinetic motion and AI voiceovers? Here is how to create yours in seconds.",
    val volume: Float = 1.2f, // 0.0 to 2.0
    val speed: Float = 1.0f,  // 0.5 to 2.0
    val stability: Float = 0.75f,
    val clarity: Float = 0.85f,
    val isSynthesized: Boolean = true
)

data class BgmTrack(
    val id: String,
    val title: String,
    val artist: String,
    val genre: String,
    val durationSec: Float = 30f,
    val volume: Float = 0.35f, // 0.0 to 1.0
    val waveformPattern: List<Float> = listOf(0.3f, 0.6f, 0.9f, 0.4f, 0.8f, 0.5f, 0.7f, 0.9f, 0.4f, 0.6f, 0.3f)
)

data class CaptionItem(
    val text: String,
    val startSec: Float,
    val endSec: Float,
    val styleKey: String = "NEON_BOX"
)

data class ScriptScene(
    val sceneNumber: Int,
    val timeRange: String,
    val visualPrompt: String,
    val narration: String,
    val suggestedMotion: KenBurnsMotion
)

data class AiScriptResult(
    val title: String,
    val topic: String,
    val tone: String,
    val targetDurationSec: Int,
    val hook: String,
    val scenes: List<ScriptScene>,
    val callToAction: String,
    val hashtags: List<String>,
    val suggestedBgm: String
)

data class RenderProgressState(
    val isRendering: Boolean = false,
    val progress: Float = 0f,
    val currentPassText: String = "Idle",
    val currentFrame: Int = 0,
    val totalFrames: Int = 300,
    val fps: Int = 60,
    val elapsedTimeSec: Float = 0f,
    val etaSec: Float = 0f,
    val isCompleted: Boolean = false,
    val exportConfig: ResolutionPreset = ResolutionPreset.FHD_1080P,
    val outputFileSizeMb: Float = 14.8f
)

data class VideoProject(
    val id: String,
    val title: String,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val resolution: ResolutionPreset = ResolutionPreset.FHD_1080P,
    val clips: List<MediaClip> = emptyList(),
    val voiceover: VoiceoverSettings,
    val bgmTrack: BgmTrack,
    val captions: List<CaptionItem> = emptyList(),
    val totalDurationSec: Float = 10.5f,
    val lastModified: String = "Just now"
)
