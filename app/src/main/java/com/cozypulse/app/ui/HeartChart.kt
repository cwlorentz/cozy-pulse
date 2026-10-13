package com.cozypulse.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cozypulse.app.HrSample

/**
 * A soft line chart of heart rate over the session. Tingle moments show as
 * lavender dots along the bottom.
 */
@Composable
fun HeartChart(
    samples: List<HrSample>,
    startMs: Long,
    endMs: Long,
    tingles: List<Long>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    showLabels: Boolean = true,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = SoftCocoa, fontSize = 11.sp)

    Canvas(modifier.fillMaxWidth().height(height)) {
        val left = if (showLabels) 34.dp.toPx() else 0f
        val bottom = size.height - (if (showLabels) 22.dp.toPx() else 6.dp.toPx())
        val top = 8.dp.toPx()
        val right = size.width - 4.dp.toPx()
        val span = (endMs - startMs).coerceAtLeast(1L).toFloat()

        if (samples.isEmpty()) return@Canvas

        val lo = (samples.minOf { it.bpm } - 4).coerceAtLeast(30)
        val hi = samples.maxOf { it.bpm } + 4
        val range = (hi - lo).coerceAtLeast(1).toFloat()

        fun x(t: Long) = left + (right - left) * ((t - startMs) / span).coerceIn(0f, 1f)
        fun y(bpm: Int) = bottom - (bottom - top) * ((bpm - lo) / range)

        if (showLabels) {
            listOf(hi - 4, lo + 4).distinct().forEach { v ->
                val yy = y(v)
                drawLine(
                    color = Peach.copy(alpha = 0.6f),
                    start = Offset(left, yy),
                    end = Offset(right, yy),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                )
                drawText(measurer, "$v", Offset(0f, yy - 8.sp.toPx()), labelStyle)
            }
            drawText(measurer, formatClock(startMs), Offset(left, bottom + 5.dp.toPx()), labelStyle)
            val endLabel = measurer.measure(formatClock(endMs), labelStyle)
            drawText(endLabel, topLeft = Offset(right - endLabel.size.width, bottom + 5.dp.toPx()))
        }

        // Smooth the line with gentle cubic curves between points.
        val points = samples.map { Offset(x(it.time), y(it.bpm)) }
        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                val midX = (p0.x + p1.x) / 2
                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(points.last().x, bottom)
            lineTo(points.first().x, bottom)
            close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(Rose.copy(alpha = 0.28f), Peach.copy(alpha = 0.04f)), top, bottom))
        drawPath(
            line,
            color = Rose,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        if (points.size == 1) drawCircle(Rose, 4.dp.toPx(), points.first())

        tingles.forEach { t ->
            drawCircle(Lavender, 4.dp.toPx(), Offset(x(t), bottom - 4.dp.toPx()))
        }
    }
}
