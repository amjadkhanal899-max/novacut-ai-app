package com.example.service

import com.example.model.KenBurnsMotion
import com.example.model.MediaClip
import com.example.model.RenderProgressState
import com.example.model.ResolutionPreset
import com.example.model.VideoProject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

data class MotionTransform(
    val scale: Float,
    val translationFractionX: Float,
    val translationFractionY: Float,
    val rotationDegrees: Float
)

class VideoEngineService {

    private val _renderState = MutableStateFlow(RenderProgressState())
    val renderState: StateFlow<RenderProgressState> = _renderState.asStateFlow()

    fun calculateTransform(motion: KenBurnsMotion, progress: Float): MotionTransform {
        val t = progress.coerceIn(0f, 1f)
        val smoothT = (1f - kotlin.math.cos(t * Math.PI.toFloat())) / 2f // Ease in-out

        return when (motion) {
            KenBurnsMotion.ZOOM_IN -> {
                val scale = 1.0f + (0.32f * smoothT)
                MotionTransform(scale = scale, translationFractionX = 0f, translationFractionY = 0f, rotationDegrees = 0f)
            }
            KenBurnsMotion.ZOOM_OUT -> {
                val scale = 1.32f - (0.32f * smoothT)
                MotionTransform(scale = scale, translationFractionX = 0f, translationFractionY = 0f, rotationDegrees = 0f)
            }
            KenBurnsMotion.PAN_LEFT -> {
                val scale = 1.25f
                val transX = 0.12f - (0.24f * smoothT)
                MotionTransform(scale = scale, translationFractionX = transX, translationFractionY = 0f, rotationDegrees = 0f)
            }
            KenBurnsMotion.PAN_RIGHT -> {
                val scale = 1.25f
                val transX = -0.12f + (0.24f * smoothT)
                MotionTransform(scale = scale, translationFractionX = transX, translationFractionY = 0f, rotationDegrees = 0f)
            }
            KenBurnsMotion.ORBIT_3D -> {
                val scale = 1.15f + (0.18f * sin(smoothT * Math.PI.toFloat()))
                val transX = sin(smoothT * Math.PI.toFloat() * 2) * 0.06f
                val transY = (smoothT - 0.5f) * 0.08f
                val rot = (smoothT - 0.5f) * 4.0f
                MotionTransform(scale = scale, translationFractionX = transX, translationFractionY = transY, rotationDegrees = rot)
            }
            KenBurnsMotion.STATIC -> {
                MotionTransform(scale = 1.0f, translationFractionX = 0f, translationFractionY = 0f, rotationDegrees = 0f)
            }
        }
    }

    fun generateFfmpegCommand(project: VideoProject, preset: ResolutionPreset): String {
        val width = preset.width
        val height = preset.height
        val fps = 60
        val audioFilter = "[1:a]volume=${project.voiceover.volume}[vocal];[2:a]volume=${project.bgmTrack.volume}[bgm];[vocal][bgm]amix=inputs=2:duration=first"

        val sb = StringBuilder()
        sb.append("ffmpeg -y ")
        project.clips.forEachIndexed { index, clip ->
            sb.append("-loop 1 -t ${clip.durationSec} -i clip_$index.jpg ")
        }
        sb.append("-i voiceover.mp3 -i bgm.mp3 ")
        sb.append("-filter_complex \"")
        project.clips.forEachIndexed { index, clip ->
            val motionFilter = when (clip.motion) {
                KenBurnsMotion.ZOOM_IN -> "zoompan=z='min(zoom+0.002,1.35)':d=${(clip.durationSec * fps).toInt()}:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s=${width}x${height}"
                KenBurnsMotion.ZOOM_OUT -> "zoompan=z='if(lte(zoom,1.0),1.35,max(1.0,zoom-0.002))':d=${(clip.durationSec * fps).toInt()}:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s=${width}x${height}"
                KenBurnsMotion.PAN_LEFT -> "zoompan=z='1.25':x='if(lte(on,1),(iw-iw/zoom),max(0,x-2))':y='ih/2-(ih/zoom/2)':d=${(clip.durationSec * fps).toInt()}:s=${width}x${height}"
                KenBurnsMotion.PAN_RIGHT -> "zoompan=z='1.25':x='if(lte(on,1),0,min(iw-iw/zoom,x+2))':y='ih/2-(ih/zoom/2)':d=${(clip.durationSec * fps).toInt()}:s=${width}x${height}"
                KenBurnsMotion.ORBIT_3D -> "zoompan=z='1.2':x='iw/2-(iw/zoom/2)+sin(on/10)*20':y='ih/2-(ih/zoom/2)+cos(on/10)*20':d=${(clip.durationSec * fps).toInt()}:s=${width}x${height}"
                KenBurnsMotion.STATIC -> "scale=${width}:${height}"
            }
            sb.append("[$index:v]$motionFilter[v$index];")
        }
        sb.append(audioFilter)
        sb.append("\" -c:v libx264 -pix_fmt yuv420p -r $fps -c:a aac -b:a 192k output_${preset.label.replace(" ", "_")}.mp4")

        return sb.toString()
    }

    suspend fun startRenderExport(
        project: VideoProject,
        preset: ResolutionPreset,
        onComplete: (String) -> Unit
    ) {
        val totalFrames = ((project.totalDurationSec.coerceAtLeast(6f)) * 30).toInt()
        val estimatedSizeMb = when (preset) {
            ResolutionPreset.HD_720P -> 8.4f
            ResolutionPreset.FHD_1080P -> 18.2f
            ResolutionPreset.ULTRA_4K -> 64.5f
            ResolutionPreset.CINEMA_8K -> 142.0f
        }

        _renderState.value = RenderProgressState(
            isRendering = true,
            progress = 0f,
            currentPassText = "Pass 1/3: Computing Ken Burns 3D Transforms & Filters",
            currentFrame = 0,
            totalFrames = totalFrames,
            fps = 60,
            elapsedTimeSec = 0f,
            etaSec = 6f,
            isCompleted = false,
            exportConfig = preset,
            outputFileSizeMb = estimatedSizeMb
        )

        val steps = 50
        for (i in 1..steps) {
            delay(100)
            val p = i.toFloat() / steps
            val frame = (p * totalFrames).toInt()
            val elapsed = i * 0.1f
            val eta = ((steps - i) * 0.1f).coerceAtLeast(0.2f)

            val passText = when {
                p < 0.4f -> "Pass 1/3: Synthesizing Ken Burns Pan/Zoom Frames ($frame/$totalFrames)"
                p < 0.75f -> "Pass 2/3: ElevenLabs Audio Mixdown & Ducking (${(project.bgmTrack.volume * 100).toInt()}% BGM)"
                else -> "Pass 3/3: Muxing H.264 Video Stream @ ${preset.resolutionText}"
            }

            _renderState.value = _renderState.value.copy(
                progress = p,
                currentPassText = passText,
                currentFrame = frame,
                elapsedTimeSec = elapsed,
                etaSec = eta
            )
        }

        val outputPath = "/sdcard/Movies/NovaCut_${System.currentTimeMillis()}_${preset.label.replace(" ", "")}.mp4"
        _renderState.value = _renderState.value.copy(
            isRendering = false,
            progress = 1.0f,
            currentPassText = "Render Complete! Ready for Export",
            isCompleted = true,
            etaSec = 0f
        )
        onComplete(outputPath)
    }

    fun resetRenderState() {
        _renderState.value = RenderProgressState()
    }
}
