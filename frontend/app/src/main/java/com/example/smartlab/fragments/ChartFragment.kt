package com.example.smartlab.fragments
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartlab.R
import com.example.smartlab.model.Measurement
import kotlin.math.max
import kotlin.math.min

class ChartFragment : Fragment() {

    private val _measurements = mutableStateOf<List<Measurement>>(emptyList())
    private val measurements: State<List<Measurement>> = _measurements

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val composeView = view.findViewById<ComposeView>(R.id.composeChartView)
        composeView.setContent {
            ChartScreen(measurements.value)
        }
    }

    fun updateData(newData: List<Measurement>) {
        _measurements.value = newData
    }
}

@Composable
fun ChartScreen(data: List<Measurement>) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        MeasurementLineChart(data = data)
    }
}

@Composable
fun MeasurementLineChart(data: List<Measurement>) {
    if (data.isEmpty()) return

    var offsetX by remember { mutableFloatStateOf(0f) }
    var scaleX by remember { mutableFloatStateOf(1f) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val maxValue = data.maxOf { it.value.toFloat() }
    val minValue = data.minOf { it.value.toFloat() }
    val dataRange = (maxValue - minValue).coerceAtLeast(1f)

    val padding = 60.dp
    val paddingPx = with(LocalDensity.current) { padding.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scaleX * zoom).coerceIn(1f, 10f)
                    val maxOffset = size.width * (newScale - 1f)

                    scaleX = newScale
                    offsetX = (offsetX + pan.x).coerceIn(-maxOffset, 0f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (tapOffset.x < paddingPx || tapOffset.x > size.width - paddingPx / 2) return@detectTapGestures

                    val chartWidth = size.width - paddingPx - paddingPx / 2
                    val totalWidth = chartWidth * scaleX
                    val stepX = totalWidth / (data.size - 1).coerceAtLeast(1)
                    val relativeX = tapOffset.x - paddingPx - offsetX
                    val index = (relativeX / stepX).toInt().coerceIn(0, data.lastIndex)

                    selectedIndex = if (selectedIndex == index) null else index
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val chartWidth = width - paddingPx - paddingPx / 2
            val chartHeight = height - paddingPx * 1.5f
            val totalWidth = chartWidth * scaleX
            val stepX = totalWidth / (data.size - 1).coerceAtLeast(1)

            // Обчислення видимого діапазону
            val visibleStartIndex = ((-offsetX) / stepX).toInt().coerceAtLeast(0)
            val visibleEndIndex = (((-offsetX) + chartWidth) / stepX).toInt()
                .coerceAtMost(data.lastIndex)

            // === СІТКА ПО Y (температура) ===
            val gridLinesY = 5
            for (i in 0..gridLinesY) {
                val y = paddingPx / 2 + (chartHeight * (1 - i / gridLinesY.toFloat()))

                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(paddingPx, y),
                    end = Offset(width - paddingPx / 2, y),
                    strokeWidth = 1.dp.toPx()
                )

                val tempValue = minValue + i * (dataRange / gridLinesY)
                drawContext.canvas.nativeCanvas.drawText(
                    "%.1f°".format(tempValue),
                    15f,
                    y + 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#757575")
                        textSize = 32f
                        isAntiAlias = true
                    }
                )
            }

            // === СІТКА ПО X (дати) - тільки видимі точки ===
            val dateStep = max(1, ((visibleEndIndex - visibleStartIndex) / 5))
            for (i in visibleStartIndex..visibleEndIndex step dateStep) {
                val x = paddingPx + i * stepX + offsetX
                if (x < paddingPx || x > width - paddingPx / 2) continue

                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(x, paddingPx / 2),
                    end = Offset(x, height - paddingPx),
                    strokeWidth = 1.dp.toPx()
                )

                drawContext.canvas.nativeCanvas.drawText(
                    data[i].dateTime.take(10),
                    x - 40f,
                    height - 20f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#757575")
                        textSize = 28f
                        isAntiAlias = true
                    }
                )
            }

            // === ОБМЕЖЕННЯ ОБЛАСТІ МАЛЮВАННЯ ===
            clipRect(
                left = paddingPx,
                top = paddingPx / 2,
                right = width - paddingPx / 2,
                bottom = height - paddingPx
            ) {
                // === ЛІНІЯ ГРАФІКА ===
                val path = androidx.compose.ui.graphics.Path()
                var isFirst = true

                for (i in visibleStartIndex..visibleEndIndex) {
                    val measurement = data[i]
                    val x = paddingPx + i * stepX + offsetX
                    val normalizedValue = (measurement.value.toFloat() - minValue) / dataRange
                    val y = paddingPx / 2 + chartHeight * (1 - normalizedValue)

                    if (isFirst) {
                        path.moveTo(x, y)
                        isFirst = false
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = Color(0xFF2196F3),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // === ТОЧКИ НА ГРАФІКУ (показуємо тільки якщо масштаб достатній) ===
                if (scaleX > 2f) {
                    for (i in visibleStartIndex..visibleEndIndex) {
                        val measurement = data[i]
                        val x = paddingPx + i * stepX + offsetX
                        val normalizedValue = (measurement.value.toFloat() - minValue) / dataRange
                        val y = paddingPx / 2 + chartHeight * (1 - normalizedValue)

                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color(0xFF2196F3),
                            radius = 2.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                // === ВИДІЛЕНА ТОЧКА ===
                selectedIndex?.let { i ->
                    val x = paddingPx + i * stepX + offsetX
                    val normalizedValue = (data[i].value.toFloat() - minValue) / dataRange
                    val y = paddingPx / 2 + chartHeight * (1 - normalizedValue)

                    // Перехресні лінії
                    drawLine(
                        Color(0xFFFF5722).copy(alpha = 0.3f),
                        Offset(x, paddingPx / 2),
                        Offset(x, height - paddingPx),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                    drawLine(
                        Color(0xFFFF5722).copy(alpha = 0.3f),
                        Offset(paddingPx, y),
                        Offset(width - paddingPx / 2, y),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Виділена точка
                    drawCircle(
                        Color.White,
                        radius = 10.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        Color(0xFFFF5722),
                        radius = 7.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // === TOOLTIP ===
        selectedIndex?.let { i ->
            val m = data[i]
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = m.dateTime.take(10),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "${m.value}°C",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


