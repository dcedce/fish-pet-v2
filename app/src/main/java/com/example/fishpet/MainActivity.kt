package com.example.fishpet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var etApiKey: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        etApiKey = findViewById(R.id.etApiKey)
        etApiKey.setText(Prefs.getApiKey(this))

        findViewById<Button>(R.id.btnSaveKey).setOnClickListener {
            Prefs.setApiKey(this, etApiKey.text.toString())
            toast(if (Prefs.hasApiKey(this)) "Key 已保存" else "Key 已清空")
        }

        findViewById<Button>(R.id.btnPermission).setOnClickListener {
            requestOverlayPermission()
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (ensureOverlayPermission()) {
                requestNotificationPermissionIfNeeded()
                startServiceCompat()
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopService(Intent(this, PetService::class.java))
            toast("大肥鱼已关闭")
        }

        findViewById<Button>(R.id.btnChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val overlay = canDrawOverlays()
        val key = if (Prefs.hasApiKey(this)) "已设置" else "未设置"
        tvStatus.text = "悬浮窗权限：" + (if (overlay) "已授权" else "未授权") + "　API Key：" + key
    }

    private fun canDrawOverlays(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            toast("请在系统设置里允许“显示在其他应用上层”")
        } else {
            toast("悬浮窗权限已授权")
        }
    }

    private fun ensureOverlayPermission(): Boolean {
        if (!canDrawOverlays()) {
            requestOverlayPermission()
            return false
        }
        return true
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val ok = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!ok) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun startServiceCompat() {
        // minSdk 26，直接 startForegroundService
        startForegroundService(Intent(this, PetService::class.java))
        toast("大肥鱼已启动")
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
