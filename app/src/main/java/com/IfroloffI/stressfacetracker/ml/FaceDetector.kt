package com.IfroloffI.stressfacetracker.ml

import android.graphics.Rect

/*
* import androidx.camera.core.ImageProxy
*
* interface FaceDetector {
*    suspend fun detectFaces(imageProxy: ImageProxy): FaceDetectionResult
}
*/

data class FaceDetectionResult(
    val faceCount: Int,
    val hasSingleFace: Boolean,
    val firstFaceBoundingBox: Rect? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val smilingProbability: Float? = null,
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null,
    val headEulerAngleX: Float? = null,
    val headEulerAngleY: Float? = null
)