import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Ekranın tüm sınırlarını kaplayan, dokunmaları geçiren bir
 * overlay çerçeve gösteren Foreground Service.
 *
 * Kullanım:
 *   val intent = Intent(context, FrameOverlayService::class.java).apply {
 *       putExtra(EXTRA_COLOR, Color.RED)
 *       putExtra(EXTRA_STROKE_DP, 6f)
 *       putExtra(EXTRA_RADIUS_DP, 16f)
 *   }
 *   ContextCompat.startForegroundService(context, intent)
 *
 * Not: Servisi başlatmadan önce Settings.canDrawOverlays(context)
 * kontrolünü ve gerekiyorsa izin isteğini Activity tarafında yapmalısın.
 */
class FrameOverlayService : Service() {

    companion object {
        const val EXTRA_COLOR = "extra_color"
        const val EXTRA_STROKE_DP = "extra_stroke_dp"
        const val EXTRA_RADIUS_DP = "extra_radius_dp"

        private const val CHANNEL_ID = "overlay_frame_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private var windowManager: WindowManager? = null
    private var overlayView: OverlayFrameView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        val color = intent?.getIntExtra(EXTRA_COLOR, Color.RED) ?: Color.RED
        val strokeDp = intent?.getFloatExtra(EXTRA_STROKE_DP, 6f) ?: 6f
        val radiusDp = intent?.getFloatExtra(EXTRA_RADIUS_DP, 16f) ?: 16f

        if (overlayView == null) {
            addOverlay(color, dpToPx(strokeDp), dpToPx(radiusDp))
        } else {
            updateOverlay(color, dpToPx(strokeDp), dpToPx(radiusDp))
        }

        // START_STICKY: sistem servisi öldürürse tekrar başlatmayı dener
        return START_STICKY
    }

    private fun addOverlay(color: Int, strokePx: Float, radiusPx: Float) {
        val metrics = getScreenMetrics()

        val view = OverlayFrameView(this).apply {
            frameColor = color
            strokeWidthPx = strokePx
            cornerRadiusPx = radiusPx
        }

        val overlayType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            metrics.widthPixels,
            metrics.heightPixels,
            overlayType,
            // FLAG_NOT_FOCUSABLE + FLAG_NOT_TOUCHABLE = dokunmalar arkadaki
            // uygulamaya geçer, overlay hiçbir etkileşimi engellemez.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            x = 0
            y = 0
        }

        windowManager?.addView(view, params)
        overlayView = view
    }

    private fun updateOverlay(color: Int, strokePx: Float, radiusPx: Float) {
        overlayView?.apply {
            frameColor = color
            strokeWidthPx = strokePx
            cornerRadiusPx = radiusPx
        }
    }

    /** Ekran boyutlarını (çentik/kenar dahil) almak için kullanılıyor */
    private fun getScreenMetrics(): DisplayMetrics {
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager?.currentWindowMetrics?.bounds
            if (bounds != null) {
                metrics.widthPixels = bounds.width()
                metrics.heightPixels = bounds.height()
                return metrics
            }
        }
        @Suppress("DEPRECATION")
        windowManager?.defaultDisplay?.getRealMetrics(metrics)
        return metrics
    }

    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Çerçeve Servisi",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, FrameOverlayService::class.java)
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Overlay çerçeve aktif")
            .setContentText("Dokunarak kaldırmak için bildirime dokun")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(stopPendingIntent)
            .build()
    }

    override fun onDestroy() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        super.onDestroy()
    }
}
