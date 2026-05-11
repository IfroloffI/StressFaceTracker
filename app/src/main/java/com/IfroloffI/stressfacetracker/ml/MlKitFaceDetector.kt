package com.IfroloffI.stressfacetracker.ml

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

class MlKitFaceDetector {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun detect(imageProxy: ImageProxy): FaceDetectionResult {
        val mediaImage = imageProxy.image ?: return FaceDetectionResult(0, false, null)

        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)

        val bufferWidth = mediaImage.width
        val bufferHeight = mediaImage.height

        return try {
            val faces = detector.process(image).await()
            val count = faces.size
            val first = faces.firstOrNull()

            val firstBox = first?.boundingBox
            val smile = first?.smilingProbability
            val leftEye = first?.leftEyeOpenProbability
            val rightEye = first?.rightEyeOpenProbability
            val eulerX = first?.headEulerAngleX
            val eulerY = first?.headEulerAngleY

            FaceDetectionResult(
                faceCount = count,
                hasSingleFace = count == 1,
                firstFaceBoundingBox = firstBox,
                imageWidth = bufferWidth,
                imageHeight = bufferHeight,
                smilingProbability = smile,
                leftEyeOpenProbability = leftEye,
                rightEyeOpenProbability = rightEye,
                headEulerAngleX = eulerX,
                headEulerAngleY = eulerY
            )
        } finally {
            imageProxy.close()
        }
    }
}