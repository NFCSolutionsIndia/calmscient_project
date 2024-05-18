package com.calmscient.utils.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LineChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val  CIRCLE_RADIUS = 14f
    private val  DOT_RADIUS = 6f

    private val backgroundColor = Color.parseColor("#E8E7F4")

    // Create the Paint object with the desired color
    private val whitePaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
    }

    private val paint = Paint().apply {
        color = Color.parseColor("#6E6BB3")
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val axisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
    }

    private var points: List<Pair<Float, Float>> = listOf()
    private var dates :List<String> = listOf()

    fun setPoints(newPoints: List<Pair<Float, Float>>, dates: List<String>) {
        points = newPoints
        this.dates = dates
        invalidate() // Request a redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (points.isEmpty()) return

        val maxX = 6f // Fixed 9 weeks
        val maxY = 28f // Maximum value for Y-axis

        val scaleX = width / maxX
        val scaleY = height / maxY

        // Draw grid lines
        drawGridLines(canvas, maxX, maxY, scaleX, scaleY)

//        // Draw X and Y axes
//        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), axisPaint)
//        canvas.drawLine(0f, 0f, 0f, height.toFloat(), axisPaint)

        // Draw data points and lines
        for (i in 0 until points.size - 1) {
            val startX = points[i].first * scaleX
            val startY = height - (points[i].second * scaleY)
            val stopX = points[i + 1].first * scaleX
            val stopY = height - (points[i + 1].second * scaleY)
            canvas.drawLine(startX, startY, stopX, stopY, paint)

            // Draw filled circle with white color
            canvas.drawCircle(startX, startY, CIRCLE_RADIUS, whitePaint)
            // Draw smaller filled circle with the desired color
            canvas.drawCircle(startX, startY, DOT_RADIUS, paint)
        }

        // Draw filled circle with white color at the last data point
        if (points.isNotEmpty()) {
            val lastX = points.last().first * scaleX
            val lastY = height - (points.last().second * scaleY)
            canvas.drawCircle(lastX, lastY, CIRCLE_RADIUS, whitePaint)
            // Draw smaller filled circle with the desired color
            canvas.drawCircle(lastX, lastY, DOT_RADIUS, paint)
        }

        // Draw axis labels
        drawAxisLabels(canvas, maxX, maxY, scaleX, scaleY)
    }



    private fun drawGridLines(canvas: Canvas, maxX: Float, maxY: Float, scaleX: Float, scaleY: Float) {
        val numHorizontalLines = 14

        // Set the paint to draw dotted lines with visible dots
        gridPaint.pathEffect = DashPathEffect(floatArrayOf(1f, 10f), 0f)

        // Draw horizontal grid lines
        for (i in 0 until numHorizontalLines) {
            val y = i * (height / numHorizontalLines.toFloat())
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }

        // Reset the path effect to draw solid lines for other drawing operations
        gridPaint.pathEffect = null
    }



    private fun drawAxisLabels(canvas: Canvas, maxX: Float, maxY: Float, scaleX: Float, scaleY: Float) {
        val numYLabels = 14
        val yLabelInterval = maxY / numYLabels

        // Y-axis labels
        for (i in 0..numYLabels) {
            val y = height - (i * yLabelInterval * scaleY)
            val label = (i * yLabelInterval).toInt().toString()
            canvas.drawText(label, 10f, y, textPaint)
        }

        // X-axis labels
        if (dates.isNotEmpty()) {
            val numXLabels = dates.size
            val xLabelInterval = width.toFloat() / numXLabels

            for (i in dates.indices) {
                val x = xLabelInterval * (i + 0.5f)
                val label = dates[i]
                canvas.drawText(label, x, height.toFloat() - 10f, textPaint)
            }
        }
    }


}
