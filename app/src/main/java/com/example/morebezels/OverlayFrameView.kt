import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Ekranın kenarlarına yapışan, kalınlığı/rengi/köşe yarıçapı
 * ayarlanabilen özel bir çerçeve View'ı.
 *
 * Bu View, WindowManager overlay'i içine tam ekran boyutunda
 * eklenir ve dokunmaları geçirir (touch-through) çünkü sadece
 * kenarları boyar, içini boş bırakır.
 */
class OverlayFrameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ---- Ayarlanabilir parametreler ----

    /** Çerçeve kalınlığı (px cinsinden) */
    var strokeWidthPx: Float = 12f
        set(value) {
            field = value
            framePaint.strokeWidth = value
            invalidate()
        }

    /** Çerçeve rengi */
    var frameColor: Int = Color.RED
        set(value) {
            field = value
            framePaint.color = value
            invalidate()
        }

    /** Köşe yarıçapı (px cinsinden) */
    var cornerRadiusPx: Float = 24f
        set(value) {
            field = value
            invalidate()
        }

    // ---- Paint ----

    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = frameColor
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
    }

    private val drawRect = RectF()

    // Opsiyonel: yanıp sönen alarm efekti için
    private var pulseAnimator: ValueAnimator? = null

    init {
        // View'ın kendisi tıklamaları/dokunmaları yakalamasın,
        // WindowManager flag'i ile birlikte bu tam touch-through sağlar.
        isClickable = false
        isFocusable = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val half = strokeWidthPx / 2f
        // Çizgi kalınlığı View sınırlarının dışına taşmasın diye
        // stroke'un yarısı kadar içeri alıyoruz.
        drawRect.set(half, half, w - half, h - half)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRoundRect(drawRect, cornerRadiusPx, cornerRadiusPx, framePaint)
    }

    /** Örnek: dikkat çekmek için rengi/alfayı nabız gibi attırma */
    fun startPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = ValueAnimator.ofInt(80, 255).apply {
            duration = 800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { anim ->
                framePaint.alpha = anim.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        framePaint.alpha = 255
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pulseAnimator?.cancel()
    }
}
