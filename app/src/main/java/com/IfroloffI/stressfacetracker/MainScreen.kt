package com.IfroloffI.stressfacetracker

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.IfroloffI.stressfacetracker.components.CameraPreviewComposable
import com.IfroloffI.stressfacetracker.ml.FaceDetectionResult

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission.value = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission.value) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            if (hasCameraPermission.value) {
                // Превью камеры + оверлей рамки
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreviewComposable(
                        modifier = Modifier.fillMaxSize(),
                        onFaceResult = { result ->
                            if (state.isAnalyzing && result != null) {
                                viewModel.onFaceDetectionResult(
                                    hasFace = result.faceCount > 0,
                                    box = result.firstFaceBoundingBox,
                                    imageWidth = result.imageWidth,
                                    imageHeight = result.imageHeight,
                                    smilingProbability = result.smilingProbability,
                                    leftEyeOpenProbability = result.leftEyeOpenProbability,
                                    rightEyeOpenProbability = result.rightEyeOpenProbability,
                                    headEulerAngleX = result.headEulerAngleX,
                                    headEulerAngleY = result.headEulerAngleY
                                )
                            }
                        }
                    )

                    FaceOverlay(
                        boundingBox = state.faceBoundingBox,
                        imageWidth = state.imageWidth,
                        imageHeight = state.imageHeight,
                        isFrontCamera = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Заглушка, если нет разрешения
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF020617),
                                    Color(0xFF0B1120),
                                    Color(0xFF1F2937)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Нет доступа к камере",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Разрешить")
                        }
                    }
                }
            }
        }

        StatusOverlay(
            state = state,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            viewModel.toggleAnalyzing()
        }
    }
}

@Composable
private fun StatusOverlay(
    state: MainUiState,
    modifier: Modifier = Modifier,
    onToggleClick: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF020617).copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val emotionEmoji = when (state.currentEmotion) {
                "HAPPY" -> "😄"
                "STRESSED" -> "😵‍💫"
                "TIRED" -> "🥱"
                "NEUTRAL" -> "😐"
                "ANGRY" -> "😡"
                else -> "🔍"
            }

            Text(
                text = if (state.isFaceDetected) "Лицо: обнаружено" else "Лицо: нет в кадре",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f)
            )

            Text(
                text = "$emotionEmoji Анализ изображения",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = if (state.isFaceDetected) "Лицо: обнаружено" else "Лицо: нет в кадре",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f)
            )

            Text(
                text = "Эмоция: ${state.currentEmotion}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f)
            )

            Text(
                text = "Стресс: ${state.currentStressLevel}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f)
            )

            if (state.smilingProbability != null) {
                Text(
                    text = "Улыбка: ${(state.smilingProbability * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }

            if (state.leftEyeOpenProbability != null && state.rightEyeOpenProbability != null) {
                val eyesOpen =
                    ((state.leftEyeOpenProbability + state.rightEyeOpenProbability) / 2f * 100).toInt()
                Text(
                    text = "Глаза: $eyesOpen% открыты",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }

            Text(
                text = if (state.isAnalyzing) "Анализ..." else "Готов к запуску",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            val buttonColors = if (state.isAnalyzing) {
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFB7185),
                    contentColor = Color.White
                )
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E),
                    contentColor = Color.White
                )
            }

            Button(
                onClick = onToggleClick,
                colors = buttonColors,
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 10.dp),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = if (state.isAnalyzing) "Остановить" else "Запустить",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FaceOverlay(
    boundingBox: Rect?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier,
    isFrontCamera: Boolean = true
) {
    if (boundingBox == null || imageWidth == 0 || imageHeight == 0) return

    var previewSize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = modifier.onGloballyPositioned { layoutCoordinates ->
            previewSize = layoutCoordinates.size
        }
    ) {
        if (previewSize == IntSize.Zero) return@Canvas

        val viewWidth = size.width
        val viewHeight = size.height

        // Соотношения сторон
        val imageRatio = imageWidth.toFloat() / imageHeight.toFloat()
        val viewRatio = viewWidth / viewHeight

        val scale: Float
        val offsetX: Float
        val offsetY: Float

        if (imageRatio > viewRatio) {
            scale = viewHeight / imageHeight
            val scaledWidth = imageWidth * scale
            offsetX = (scaledWidth - viewWidth) / 2f
            offsetY = 0f
        } else {
            scale = viewWidth / imageWidth
            val scaledHeight = imageHeight * scale
            offsetX = 0f
            offsetY = (scaledHeight - viewHeight) / 2f
        }

        // Преобразуем boundingBox в координаты вью
        var left = boundingBox.left * scale
        var top = boundingBox.top * scale
        var right = boundingBox.right * scale
        var bottom = boundingBox.bottom * scale

        // Смещения из-за кропа
        left -= offsetX
        right -= offsetX
        top -= offsetY
        bottom -= offsetY

        // Зеркалирование по X для фронталки
        if (isFrontCamera) {
            val tmpLeft = left
            left = viewWidth - right
            right = viewWidth - tmpLeft
        }

        val finalLeft = left.coerceIn(0f, viewWidth)
        val finalTop = top.coerceIn(0f, viewHeight)
        val finalRight = right.coerceIn(0f, viewWidth)
        val finalBottom = bottom.coerceIn(0f, viewHeight)

        if (finalRight > finalLeft && finalBottom > finalTop) {
            drawRect(
                color = Color(0xFF22C55E),
                topLeft = androidx.compose.ui.geometry.Offset(finalLeft, finalTop),
                size = androidx.compose.ui.geometry.Size(
                    width = finalRight - finalLeft,
                    height = finalBottom - finalTop
                ),
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}