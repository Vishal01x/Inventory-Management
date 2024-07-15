package com.example.inventorymanagement.HelperClass
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class CustomBarChartRenderer(
    chart: BarDataProvider,
    animator: com.github.mikephil.charting.animation.ChartAnimator,
    viewPortHandler: ViewPortHandler,
    private val labels: List<String>
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER // Center-align the text
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun drawValues(c: Canvas) {
        val data = mChart.barData

        val dataSets = data.dataSets

        val valueOffsetPlus = 4.5f
        var posOffset = 0f
        var negOffset = 0f
        val drawValueAboveBar = mChart.isDrawValueAboveBarEnabled

        for (i in dataSets.indices) {
            val dataSet = dataSets[i]

            if (!shouldDrawValues(dataSet))
                continue

            applyValueTextStyle(dataSet)

            val isInverted = mChart.isInverted(dataSet.axisDependency)

            // calculate the correct offset depending on the draw position of the value
            val valueTextHeight = mValuePaint.textSize
            posOffset = if (drawValueAboveBar) -valueOffsetPlus else valueTextHeight + valueOffsetPlus
            negOffset = if (drawValueAboveBar) valueTextHeight + valueOffsetPlus else -valueOffsetPlus

            if (isInverted) {
                posOffset = -posOffset - valueTextHeight
                negOffset = -negOffset - valueTextHeight
            }

            // get the buffer
            val buffer = mBarBuffers[i] ?: break
            Log.d("buffer", buffer.toString())

            // Skip if buffer is null
            val phaseY = mAnimator.phaseY

            for (j in buffer.buffer.indices step 4) {
                val entry = dataSet.getEntryForIndex(j / 4) as BarEntry

                val valOffset = if (entry.y >= 0) posOffset else negOffset

                if (!mViewPortHandler.isInBoundsTop(buffer.buffer[j + 1]))
                    continue

                if (!mViewPortHandler.isInBoundsX(buffer.buffer[j]))
                    break

                if (!mViewPortHandler.isInBoundsBottom(buffer.buffer[j + 1]))
                    continue
                // Draw custom text inside the bar
                if (j / 4 < labels.size) {
                    val label = labels[j / 4]
                    val textX = buffer.buffer[j + 2] - (mValuePaint.measureText(label) / 2)
                    val textY = buffer.buffer[j + 1] - 10 // Adjust as needed
                    c.drawText(label, textX, textY, textPaint)
                }
            }
        }
    }
}
