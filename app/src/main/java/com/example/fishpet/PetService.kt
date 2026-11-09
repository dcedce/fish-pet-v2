package com.example.fishpet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat

/**
 * 前台服务：负责把大肥鱼画在屏幕上，并处理拖拽 / 点击。
 * 用前台服务是为了防止 App 退到后台后悬浮窗被系统回收。
 */
class PetService : Service() {

    private lateinit var windowManager: WindowManager
    private var petView: ImageView? = null
    private lateinit var params: WindowManager.LayoutParams

    private var downX = 0f
    private var downY = 0f
    private var startX = 0
    private var startY = 0
    private var moved = false

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showPet()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (petView == null) showPet()
        return START_STICKY
    }

    private fun showPet() {
        val size = (SIZE_DP * resources.displayMetrics.density).toInt()

        params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 300
        }

        val view = ImageView(this).apply {
            setImageResource(R.drawable.ic_pet)
            layoutParams = params
            setOnTouchListener(dragListener)
        }

        petView = view
        windowManager.addView(view, params)
    }

    private val dragListener = View.OnTouchListener { _, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                startX = params.x
                startY = params.y
                moved = false
                true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - downX).toInt()
                val dy = (event.rawY - downY).toInt()
                if (Math.abs(dx) > TOUCH_SLOP || Math.abs(dy) > TOUCH_SLOP) moved = true
                params.x = startX + dx
                params.y = startY + dy
                petView?.let { windowManager.updateViewLayout(it, params) }
                true
            }

            MotionEvent.ACTION_UP -> {
                if (!moved) openChat()
                true
            }

            else -> false
        }
    }

    private fun openChat() {
        val intent = Intent(this, ChatActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun buildNotification(): Notification {
        val channelId = "fishpet_overlay"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId, "大肥鱼运行中", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "保持大肥鱼悬浮窗存活" }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }

        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopPi = PendingIntent.getService(
            this, 1,
            Intent(this, PetService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_pet)
            .setContentTitle("大肥鱼在陪你")
            .setContentText("点一下鱼就能聊天，长按可拖动")
            .setContentIntent(pi)
            .addAction(0, "关闭", stopPi)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        petView?.let { v ->
            runCatching { windowManager.removeView(v) }
        }
        petView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.example.fishpet.STOP"
        private const val NOTIFICATION_ID = 1001
        private const val SIZE_DP = 96
        private const val TOUCH_SLOP = 12
    }
}
