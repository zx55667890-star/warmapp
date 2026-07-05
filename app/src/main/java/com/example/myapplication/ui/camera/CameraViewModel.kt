package com.example.myapplication.ui.camera

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class CameraCaptureState { CAPTURING, PREVIEW }

data class CameraUiState(
    val cameraState: CameraCaptureState = CameraCaptureState.CAPTURING,
    val capturedFileUri: Uri? = null,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    val isRecording: Boolean = false,
    val recordingElapsed: Long = 0L,
    val isVideoFile: Boolean = false
)

class CameraViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    val imageCapture = ImageCapture.Builder().build()
    val recorder = Recorder.Builder().build()
    val videoCapture = VideoCapture.withOutput(recorder)
    var activeRecording: Recording? = null
    var recVideoFile: File? = null

    fun toggleFlashMode() {
        _uiState.update {
            it.copy(flashMode = when (it.flashMode) {
                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            })
        }
    }

    fun toggleLensFacing() {
        _uiState.update {
            it.copy(lensFacing = if (it.lensFacing == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
        }
    }

    fun onPhotoCaptured(uri: Uri) {
        _uiState.update { it.copy(capturedFileUri = uri, isVideoFile = false, cameraState = CameraCaptureState.PREVIEW) }
    }

    fun onVideoFinalized(uri: Uri) {
        _uiState.update { it.copy(capturedFileUri = uri, isVideoFile = true, cameraState = CameraCaptureState.PREVIEW) }
    }

    fun onDiscard() {
        _uiState.update { it.copy(capturedFileUri = null, isVideoFile = false, cameraState = CameraCaptureState.CAPTURING) }
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
        _uiState.update { it.copy(isRecording = false) }
    }

    fun startRecording() {
        _uiState.update { it.copy(isRecording = true, recordingElapsed = 0L) }
    }

    fun resetState() {
        activeRecording?.stop()
        activeRecording = null
        recVideoFile = null
        _uiState.update { CameraUiState() }
    }

    fun startTimer() {
        viewModelScope.launch {
            while (isActive && _uiState.value.isRecording) {
                delay(1000)
                _uiState.update { it.copy(recordingElapsed = it.recordingElapsed + 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeRecording?.stop()
    }
}
