package com.IfroloffI.stressfacetracker.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.IfroloffI.stressfacetracker.ml.FaceDetectionResult
import com.IfroloffI.stressfacetracker.ml.MlKitFaceDetector
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun CameraPreviewComposable(
    modifier: Modifier = Modifier,
    onFaceResult: (FaceDetectionResult?) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val faceDetector = remember { MlKitFaceDetector() }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val cameraProvider = context.getCameraProvider()

            val previewUseCase = Preview.Builder().build().also { preview ->
                preview.surfaceProvider = previewView.surfaceProvider
            }

            val analysisUseCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysisUseCase.setAnalyzer(
                ContextCompat.getMainExecutor(context)
            ) { imageProxy ->
                kotlinx.coroutines.GlobalScope.launch(Dispatchers.Default) {
                    val result = faceDetector.detect(imageProxy)
                    withContext(Dispatchers.Main) {
                        onFaceResult(result)
                    }
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewUseCase,
                analysisUseCase
            )
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}

private suspend fun android.content.Context.getCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                val provider = cameraProviderFuture.get()
                if (continuation.isActive) {
                    continuation.resume(provider)
                }
            },
            ContextCompat.getMainExecutor(this)
        )
    }