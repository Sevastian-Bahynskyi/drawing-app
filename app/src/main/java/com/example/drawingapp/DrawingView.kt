package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.Stack

class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs)
{
    private var path: CustomPath? = null
    private var canvasBitmap: Bitmap? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    public var brushSize: Float = 0f
    public var color = Color.BLACK
    private var canvas: Canvas? = null
    private val paths = ArrayList<CustomPath>()


    init {
        setUpDrawing()
    }

    public fun goBack() {
        if(paths.isNotEmpty()) {
            paths.removeLast()

            canvas?.drawColor(0, PorterDuff.Mode.CLEAR)

            for (path in paths) {
                drawPaint?.strokeWidth = path.brushSize
                drawPaint?.color = path.color
                canvas?.drawPath(path, drawPaint!!)
            }

            invalidate()
        }
    }

    private fun setUpDrawing()
    {
        drawPaint = Paint()
        path = CustomPath(color, brushSize)
        drawPaint!!.color = color
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
        brushSize = 10f
        canvas = Canvas()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        for (path in paths) {
            drawPaint?.strokeWidth = path.brushSize
            drawPaint?.color = path.color
            canvas.drawPath(path, drawPaint!!)
        }

        if(!path!!.isEmpty) {
            drawPaint!!.strokeWidth = path!!.brushSize
            drawPaint!!.color = path!!.color
            canvas.drawPath(path!!, drawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                path!!.color = color
                path!!.brushSize = brushSize
                path!!.reset()

                if (touchX != null && touchY != null) {
                    path?.moveTo(touchX, touchY)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null && touchY != null) {
                    path!!.lineTo(touchX, touchY)

                }
            }

            MotionEvent.ACTION_UP -> {
                paths.add(path!!)
                path = CustomPath(color, brushSize)
            }

            else -> return false
        }

        invalidate()
        return true
    }

    internal inner class CustomPath(var color: Int, var brushSize: Float): Path() {

    }
}