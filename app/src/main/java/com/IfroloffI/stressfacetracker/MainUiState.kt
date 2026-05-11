package com.IfroloffI.stressfacetracker

import android.graphics.Rect

data class MainUiState(
    val isAnalyzing: Boolean = false,
    val currentEmotion: String = "N/A",
    val currentStressLevel: String = "N/A",
    val isFaceDetected: Boolean = false,
    val faceBoundingBox: Rect? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val smilingProbability: Float? = null,
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null,
    val headEulerAngleX: Float? = null,
    val headEulerAngleY: Float? = null
)