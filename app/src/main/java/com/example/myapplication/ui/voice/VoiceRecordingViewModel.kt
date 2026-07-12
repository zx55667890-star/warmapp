package com.example.myapplication.ui.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class VoiceRecordingViewModel : ViewModel() {
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var recordFilePath: String? = null

    fun startRecording(context: Context) {
        if (_isRecording.value) return
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        val mime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder.OutputFormat.MPEG_4
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder.OutputFormat.MPEG_4
        }
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(mime)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = r
        recordFilePath = file.absolutePath
        _isRecording.value = true
        _elapsedSeconds.value = 0
        viewModelScope.launch {
            while (isActive && _isRecording.value) {
                delay(1000)
                _elapsedSeconds.value++
            }
        }
    }

    fun stopRecording(): String? {
        recorder?.apply {
            try { stop() } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;}
            release()
        }
        recorder = null
        _isRecording.value = false
        val path = recordFilePath
        recordFilePath = null
        return path
    }

    fun release() {
        recorder?.apply {
            try { stop() } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;}
            release()
        }
        recorder = null
    }

    override fun onCleared() {
        super.onCleared()
        release()
    }
}

