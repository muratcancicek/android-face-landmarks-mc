@file:Suppress("DEPRECATION")

package com.dev.anzalone.luca.facelandmarks.camera

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.dev.anzalone.luca.facelandmarks.utils.mapTo

/**
 * The CameraOverlay class is placed on-top of the CameraPreview,
 * in order to draw the face region and landmarks easily.
 * Created by Luca on 10/04/2018.
 */

class CameraOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val rect = RectF()
    private val r = Rect()
    private var cursor: Pair<Float, Float> = Pair(0f, 0f)
    var face: Rect? = null
    private var landmarks: LongArray? = null
    lateinit var preview: CameraPreview

    fun setFaceAndLandmarks(face: Rect?, landmarks: LongArray?) {
        this.face = face
        this.landmarks = when (face) {
            null -> null
            else -> landmarks ?: this.landmarks // to avoid flickering while drawing
        }
    }

    /** map the given point to preview coordinate space */
    private fun adjustPoint(x0: Long, y0: Long) : Pair<Float, Float> {
        r.set(face)
        r.mapTo(preview.previewWidth, preview.previewHeight, preview.displayRotation)

        val x1 = x0.toFloat() / r.left
        val y1 = y0.toFloat() / r.top

        val x = x1 * rect.left
        val y = y1 * rect.top

        return Pair(x, y)
    }

    /** All the drawings occur here. onDraw is triggered when invalidate() is called */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        face?.let {
            r.set(it)
            r.mapTo(width, height, preview.displayRotation)
            rect.set(r)
            canvas.drawRect(rect, rPaint)


            landmarks?.let {
                if (it.isNotEmpty()) {
                   // var (x, y) = adjustPoint(it[60], it[61])
                  //  cursor = Pair(x, y)

                    var x = it[60].toFloat(); var y = it[61].toFloat()
                    var minX = it[98].toFloat(); var maxX = it[106].toFloat()

                    var minY = (it[3] + it[31]).toFloat()/2
                    var maxY = (it[9] + it[25]).toFloat()/2
                    x -= minX; y -= minY
                    maxX -= minX; minX = 0f
                    maxY -= minY; minY = 0f

                    x /= maxX; y /= maxY
                    x *= width; y *= height; y += 900f
                    canvas.drawCircle(x, y, 60f, cPaint)
                    /*
                    canvas.drawText((minX).toString(), 620f, 880f, white)
                    canvas.drawText((x).toString(), 720f, 680f, white)
                    canvas.drawText((maxX).toString(), 920f, 880f, white)
                    */
                }
                var count = 0
                for (i in it.indices step 2) {
                    var radius = 8f
                    val (xi, yi) = adjustPoint(it[i], it[i + 1])

                    if (count == 50 || count == 30 || count == 52) {
                        radius = 2f
                        canvas.drawText(i.toString(), xi, yi, white)
                    }
                    canvas.drawCircle(xi, yi, radius, pPaint)

                 //   canvas.drawText(count.toString(), x, yi, white)
                    count++
                }
            }
        }

        face = null
    }

    companion object {
        private val rPaint = Paint()
        private val pPaint = Paint()
        private val cPaint = Paint()
        private val white = Paint()

        init {
            rPaint.color = Color.rgb(255, 160, 0)
            rPaint.style = Paint.Style.STROKE
            rPaint.strokeWidth = 5f

            pPaint.color = Color.YELLOW
            pPaint.style = Paint.Style.FILL

            cPaint.color = Color.RED
            cPaint.style = Paint.Style.STROKE
            cPaint.strokeWidth = 25f

            white.color = Color.WHITE
            white.style = Paint.Style.STROKE
            white.textSize = 70f
        }
    }
}