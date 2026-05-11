package com.IfroloffI.stressfacetracker

import android.graphics.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun toggleAnalyzing() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = !_uiState.value.isAnalyzing
            )
        }
    }

    fun onFaceDetectionResult(
        hasFace: Boolean,
        box: Rect?,
        imageWidth: Int,
        imageHeight: Int,
        smilingProbability: Float?,
        leftEyeOpenProbability: Float?,
        rightEyeOpenProbability: Float?,
        headEulerAngleX: Float?,
        headEulerAngleY: Float?
    ) {
        viewModelScope.launch {
            val emotionStress = computeEmotionAndStress(
                smilingProbability,
                leftEyeOpenProbability,
                rightEyeOpenProbability,
                headEulerAngleX,
                headEulerAngleY
            )

            _uiState.value = _uiState.value.copy(
                isFaceDetected = hasFace,
                faceBoundingBox = box,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                smilingProbability = smilingProbability,
                leftEyeOpenProbability = leftEyeOpenProbability,
                rightEyeOpenProbability = rightEyeOpenProbability,
                headEulerAngleX = headEulerAngleX,
                headEulerAngleY = headEulerAngleY,
                currentEmotion = emotionStress.first,
                currentStressLevel = emotionStress.second
            )
        }
    }

    private fun computeEmotionAndStress(
        smile: Float?,
        leftEye: Float?,
        rightEye: Float?,
        eulerX: Float?,
        eulerY: Float?
    ): Pair<String, String> {
        if (smile == null && leftEye == null && rightEye == null) {
            return "N/A" to "N/A"
        }

        val smileVal = smile ?: 0f
        val leftEyeVal = leftEye ?: 0f
        val rightEyeVal = rightEye ?: 0f

        val eyesOpen = (leftEyeVal + rightEyeVal) / 2f

        // Простая эвристика:
        val emotion = when {
            smileVal > 0.7f && eyesOpen > 0.5f -> "HAPPY"
            eyesOpen < 0.2f && smileVal < 0.3f -> "TIRED"
            eyesOpen > 0.5f && smileVal < 0.2f -> "STRESSED"
            else -> "NEUTRAL"
        }

        val stress = when {
            emotion == "HAPPY" && smileVal > 0.8f -> "LOW"
            emotion == "NEUTRAL" -> "MEDIUM"
            emotion == "STRESSED" || emotion == "TIRED" -> "HIGH"
            else -> "MEDIUM"
        }

        return emotion to stress
    }
}