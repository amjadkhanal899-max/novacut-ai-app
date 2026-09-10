package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.model.AiScriptResult
import com.example.model.AspectRatio
import com.example.model.BgmTrack
import com.example.model.CaptionItem
import com.example.model.ColorFilterType
import com.example.model.KenBurnsMotion
import com.example.model.MediaClip
import com.example.model.ResolutionPreset
import com.example.model.VideoProject
import com.example.model.VoiceoverSettings
import com.example.model.VoiceoverVoice
import com.example.service.AdMobService
import com.example.service.AiScriptService
import com.example.service.VideoEngineService
import com.example.service.VoiceoverEngineService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class EditorTab(val title: String, val icon: String) {
    TIMELINE("Timeline & Motion", "🎬"),
    AI_SCRIPT("AI Script", "✍️"),
    VOICEOVER("AI Voiceover", "🎙️"),
    AUDIO_MIX("Audio Mixer", "🎵"),
    CAPTIONS("Captions", "💬")
}

class EditorViewModel : ViewModel() {

    val admobService = AdMobService()
    val aiScriptService = AiScriptService()
    val voiceoverService = VoiceoverEngineService()
    val videoEngineService = VideoEngineService()

    private val defaultVoice = voiceoverService.availableVoices.first()
    private val defaultBgm = BgmTrack(
        id = "bgm_1",
        title = "Cyber Synth Pulse",
        artist = "NovaBeat",
        genre = "Synthwave / Cyberpunk",
        durationSec = 30f,
        volume = 0.35f
    )

    val bgmLibrary = listOf(
        defaultBgm,
        BgmTrack("bgm_2", "Golden Sunset Chill", "AuraSound", "Lo-Fi Beats", 45f, 0.40f),
        BgmTrack("bgm_3", "Epic Cinematic Trailer", "AudioEpic", "Orchestral", 60f, 0.30f),
        BgmTrack("bgm_4", "Punchy Tech Drift", "FutureByte", "Electronic", 30f, 0.35f)
    )

    private val initialClips = listOf(
        MediaClip(
            id = "c1",
            name = "Cyberpunk City",
            drawableResId = R.drawable.sample_urban_neon,
            durationSec = 3.5f,
            motion = KenBurnsMotion.ZOOM_IN,
            speed = 1.0f,
            filterType = ColorFilterType.CYBERPUNK
        ),
        MediaClip(
            id = "c2",
            name = "AI Brain Hologram",
            drawableResId = R.drawable.sample_tech_cyber,
            durationSec = 4.0f,
            motion = KenBurnsMotion.PAN_RIGHT,
            speed = 1.0f,
            filterType = ColorFilterType.NONE
        ),
        MediaClip(
            id = "c3",
            name = "Golden Mountain Peak",
            drawableResId = R.drawable.sample_nature_mountain,
            durationSec = 3.5f,
            motion = KenBurnsMotion.ORBIT_3D,
            speed = 1.0f,
            filterType = ColorFilterType.WARM_GOLD
        )
    )

    private val initialCaptions = listOf(
        CaptionItem("🚀 90% of viral videos use this motion!", 0f, 3.5f),
        CaptionItem("🧠 Automated AI motion in seconds", 3.5f, 7.5f),
        CaptionItem("✨ Export in studio-grade 4K 60FPS", 7.5f, 11.0f)
    )

    private val _currentProject = MutableStateFlow(
        VideoProject(
            id = "proj_101",
            title = "NovaCut AI Viral Hook",
            aspectRatio = AspectRatio.RATIO_9_16,
            resolution = ResolutionPreset.FHD_1080P,
            clips = initialClips,
            voiceover = VoiceoverSettings(
                voice = defaultVoice,
                scriptText = "Did you know that ninety percent of viral videos use automated kinetic motion and AI voiceovers? Here is how to create yours in seconds.",
                volume = 1.25f,
                speed = 1.0f
            ),
            bgmTrack = defaultBgm,
            captions = initialCaptions,
            totalDurationSec = 11.0f
        )
    )
    val currentProject: StateFlow<VideoProject> = _currentProject.asStateFlow()

    private val _activeTab = MutableStateFlow(EditorTab.TIMELINE)
    val activeTab: StateFlow<EditorTab> = _activeTab.asStateFlow()

    private val _selectedClipIndex = MutableStateFlow(0)
    val selectedClipIndex: StateFlow<Int> = _selectedClipIndex.asStateFlow()

    private val _currentTimeSec = MutableStateFlow(0f)
    val currentTimeSec: StateFlow<Float> = _currentTimeSec.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isGeneratingScript = MutableStateFlow(false)
    val isGeneratingScript: StateFlow<Boolean> = _isGeneratingScript.asStateFlow()

    private val _aiScriptResult = MutableStateFlow<AiScriptResult?>(null)
    val aiScriptResult: StateFlow<AiScriptResult?> = _aiScriptResult.asStateFlow()

    private val _isExportModalShowing = MutableStateFlow(false)
    val isExportModalShowing: StateFlow<Boolean> = _isExportModalShowing.asStateFlow()

    private val _selectedExportPreset = MutableStateFlow(ResolutionPreset.FHD_1080P)
    val selectedExportPreset: StateFlow<ResolutionPreset> = _selectedExportPreset.asStateFlow()

    private val _unlockedFeatures = MutableStateFlow(setOf<String>())
    val unlockedFeatures: StateFlow<Set<String>> = _unlockedFeatures.asStateFlow()

    private var playbackJob: Job? = null

    init {
        recalculateTotalDuration()
    }

    fun selectTab(tab: EditorTab) {
        _activeTab.value = tab
    }

    fun selectClip(index: Int) {
        if (index in _currentProject.value.clips.indices) {
            _selectedClipIndex.value = index
            // Jump playhead to clip start
            var offset = 0f
            for (i in 0 until index) {
                offset += _currentProject.value.clips[i].durationSec
            }
            seekTo(offset)
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (_isPlaying.value) return
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val total = _currentProject.value.totalDurationSec
            if (_currentTimeSec.value >= total - 0.1f) {
                _currentTimeSec.value = 0f
            }
            val intervalMs = 33L // ~30 fps update
            while (isActive && _isPlaying.value) {
                delay(intervalMs)
                val newTime = _currentTimeSec.value + (intervalMs / 1000f)
                if (newTime >= total) {
                    _currentTimeSec.value = 0f
                    _isPlaying.value = false
                    break
                } else {
                    _currentTimeSec.value = newTime
                }
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun seekTo(timeSec: Float) {
        val total = _currentProject.value.totalDurationSec
        _currentTimeSec.value = timeSec.coerceIn(0f, total)
    }

    fun updateAspectRatio(ratio: AspectRatio) {
        _currentProject.value = _currentProject.value.copy(aspectRatio = ratio)
    }

    fun updateClipMotion(clipId: String, motion: KenBurnsMotion) {
        val updated = _currentProject.value.clips.map {
            if (it.id == clipId) it.copy(motion = motion) else it
        }
        _currentProject.value = _currentProject.value.copy(clips = updated)
    }

    fun updateClipSpeed(clipId: String, speed: Float) {
        val updated = _currentProject.value.clips.map {
            if (it.id == clipId) it.copy(speed = speed) else it
        }
        _currentProject.value = _currentProject.value.copy(clips = updated)
    }

    fun updateClipFilter(clipId: String, filter: ColorFilterType) {
        val updated = _currentProject.value.clips.map {
            if (it.id == clipId) it.copy(filterType = filter) else it
        }
        _currentProject.value = _currentProject.value.copy(clips = updated)
    }

    fun updateClipDuration(clipId: String, durationSec: Float) {
        val updated = _currentProject.value.clips.map {
            if (it.id == clipId) it.copy(durationSec = durationSec.coerceIn(1.0f, 15.0f)) else it
        }
        _currentProject.value = _currentProject.value.copy(clips = updated)
        recalculateTotalDuration()
    }

    fun addSampleClip(drawableResId: Int, name: String) {
        val newClip = MediaClip(
            id = "c_${System.currentTimeMillis()}",
            name = name,
            drawableResId = drawableResId,
            durationSec = 3.5f,
            motion = KenBurnsMotion.ZOOM_IN,
            speed = 1.0f,
            filterType = ColorFilterType.NONE
        )
        val updated = _currentProject.value.clips + newClip
        _currentProject.value = _currentProject.value.copy(clips = updated)
        recalculateTotalDuration()
        _selectedClipIndex.value = updated.lastIndex
    }

    fun removeSelectedClip() {
        val clips = _currentProject.value.clips
        if (clips.size <= 1) return // Keep at least one clip
        val idx = _selectedClipIndex.value
        val updated = clips.toMutableList().also { it.removeAt(idx) }
        _currentProject.value = _currentProject.value.copy(clips = updated)
        _selectedClipIndex.value = (idx - 1).coerceAtLeast(0)
        recalculateTotalDuration()
    }

    fun updateVoiceoverVoice(voice: VoiceoverVoice) {
        val currentVo = _currentProject.value.voiceover
        _currentProject.value = _currentProject.value.copy(
            voiceover = currentVo.copy(voice = voice)
        )
    }

    fun updateVoiceoverScript(text: String) {
        val currentVo = _currentProject.value.voiceover
        _currentProject.value = _currentProject.value.copy(
            voiceover = currentVo.copy(scriptText = text)
        )
    }

    fun updateVoiceoverVolume(volume: Float) {
        val currentVo = _currentProject.value.voiceover
        _currentProject.value = _currentProject.value.copy(
            voiceover = currentVo.copy(volume = volume)
        )
    }

    fun updateVoiceoverSpeed(speed: Float) {
        val currentVo = _currentProject.value.voiceover
        _currentProject.value = _currentProject.value.copy(
            voiceover = currentVo.copy(speed = speed)
        )
    }

    fun updateVoiceoverStability(stability: Float) {
        val currentVo = _currentProject.value.voiceover
        _currentProject.value = _currentProject.value.copy(
            voiceover = currentVo.copy(stability = stability)
        )
    }

    fun updateVoiceoverClarity(clarity: Float) {
        val currentVo = _currentProject.value.voiceover
        _currentProject.value = _currentProject.value.copy(
            voiceover = currentVo.copy(clarity = clarity)
        )
    }

    fun updateBgmTrack(bgm: BgmTrack) {
        _currentProject.value = _currentProject.value.copy(bgmTrack = bgm)
    }

    fun updateBgmVolume(volume: Float) {
        val current = _currentProject.value.bgmTrack
        _currentProject.value = _currentProject.value.copy(bgmTrack = current.copy(volume = volume))
    }

    fun generateAiScript(topic: String, tone: String, durationSec: Int) {
        viewModelScope.launch {
            _isGeneratingScript.value = true
            val res = aiScriptService.generateShortScript(topic, tone, durationSec)
            _aiScriptResult.value = res
            _isGeneratingScript.value = false
        }
    }

    fun applyScriptToProject(script: AiScriptResult) {
        val fullNarration = script.scenes.joinToString(" ") { it.narration }
        updateVoiceoverScript(fullNarration)

        // Generate dynamic captions
        val captions = mutableListOf<CaptionItem>()
        var start = 0f
        val clipDuration = _currentProject.value.totalDurationSec / script.scenes.size.coerceAtLeast(1)
        script.scenes.forEach { scene ->
            captions.add(CaptionItem(scene.narration, start, start + clipDuration))
            start += clipDuration
        }
        _currentProject.value = _currentProject.value.copy(
            title = script.title,
            captions = captions
        )

        // Apply suggested motions to clips
        val updatedClips = _currentProject.value.clips.mapIndexed { index, clip ->
            val sceneMotion = script.scenes.getOrNull(index)?.suggestedMotion ?: clip.motion
            clip.copy(motion = sceneMotion)
        }
        _currentProject.value = _currentProject.value.copy(clips = updatedClips)
        _activeTab.value = EditorTab.TIMELINE
    }

    fun openExportModal(preset: ResolutionPreset = ResolutionPreset.FHD_1080P) {
        _selectedExportPreset.value = preset
        _isExportModalShowing.value = true
    }

    fun closeExportModal() {
        _isExportModalShowing.value = false
    }

    fun setExportPreset(preset: ResolutionPreset) {
        _selectedExportPreset.value = preset
    }

    fun triggerExportWithAd(onComplete: (String) -> Unit) {
        admobService.showInterstitialAd {
            viewModelScope.launch {
                videoEngineService.startRenderExport(
                    _currentProject.value,
                    _selectedExportPreset.value,
                    onComplete
                )
            }
        }
    }

    fun unlockWithRewardedAd(featureName: String) {
        admobService.showRewardedAd(featureName) { unlocked ->
            _unlockedFeatures.value = _unlockedFeatures.value + unlocked
        }
    }

    fun isFeatureUnlocked(featureName: String): Boolean {
        return admobService.isProUnlocked.value || _unlockedFeatures.value.contains(featureName)
    }

    private fun recalculateTotalDuration() {
        val sum = _currentProject.value.clips.sumOf { it.durationSec.toDouble() }.toFloat()
        _currentProject.value = _currentProject.value.copy(totalDurationSec = sum.coerceAtLeast(1f))
    }
}
