package com.example.ar_measure

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * AR noktalarını 2D olarak ekranda gösteren özel view
 *
 * Bu view, AR sahnesindeki 3D noktaların ekran koordinatlarını hesaplayıp
 * üzerlerine kırmızı daireler çizer.
 */
class PointOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Çizim için kullanılan paint nesneleri
    private val pointPaint = Paint().apply {
        color = Color.RED  // Kırmızı renk
        style = Paint.Style.FILL  // Dolu daire
        isAntiAlias = true  // Pürüzsüz kenarlar
    }

    private val linePaint = Paint().apply {
        color = Color.GREEN  // Yeşil renk
        strokeWidth = 8f  // 8 piksel kalınlık
        style = Paint.Style.STROKE  // Sadece çizgi
        isAntiAlias = true
    }

    private val whitePaint = Paint().apply {
        color = Color.WHITE  // Beyaz renk
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Ekran koordinatlarındaki noktalar
    private val screenPoints = mutableListOf<Pair<Float, Float>>()

    /**
     * Noktaları günceller ve ekranı yeniden çizer
     */
    fun updatePoints(points: List<Pair<Float, Float>>) {
        screenPoints.clear()
        screenPoints.addAll(points)
        invalidate()  // View'ı yeniden çiz
    }

    /**
     * Noktaları temizler
     */
    fun clearPoints() {
        screenPoints.clear()
        invalidate()
    }

    /**
     * Canvas üzerine noktaları ve çizgileri çizer
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Noktalar arasına çizgi çiz
        if (screenPoints.size > 1) {
            for (i in 0 until screenPoints.size - 1) {
                val (x1, y1) = screenPoints[i]
                val (x2, y2) = screenPoints[i + 1]
                canvas.drawLine(x1, y1, x2, y2, linePaint)
            }
        }

        // Her noktaya daire çiz
        for ((x, y) in screenPoints) {
            // Dış beyaz halka (görünürlük için)
            canvas.drawCircle(x, y, 25f, whitePaint)
            // İç kırmızı daire
            canvas.drawCircle(x, y, 20f, pointPaint)
        }
    }
}

