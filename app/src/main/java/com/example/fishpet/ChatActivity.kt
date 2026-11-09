package com.example.fishpet

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class ChatActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView
    private lateinit var svLog: ScrollView
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button

    private val io = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        tvLog = findViewById(R.id.tvLog)
        svLog = findViewById(R.id.svLog)
        etInput = findViewById(R.id.etInput)
        btnSend = findViewById(R.id.btnSend)

        tvLog.text = "大肥鱼：咕噜咕噜…（点一下鱼就来找你）\n\n"

        btnSend.setOnClickListener { send() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { tvLog.text = "" }
    }

    private fun send() {
        val text = etInput.text.toString().trim()
        if (text.isEmpty()) return

        val key = Prefs.getApiKey(this)
        if (key.isEmpty()) {
            Toast.makeText(this, "请先在主页填写 DeepSeek API Key", Toast.LENGTH_LONG).show()
            return
        }

        etInput.setText("")
        append("你：" + text + "\n")
        btnSend.isEnabled = false
        append("大肥鱼：思考中…\n")

        io.execute {
            val result = runCatching {
                DeepSeekClient.chat(
                    apiKey = key,
                    prompt = text,
                    systemPrompt = SYSTEM_PROMPT
                )
            }
            runOnUiThread {
                // 去掉“思考中…”占位，换成真实回复
                tvLog.text = tvLog.text.toString().removeSuffix("大肥鱼：思考中…\n")
                result.onSuccess { append("大肥鱼：" + it.trim() + "\n\n") }
                    .onFailure { append("大肥鱼：（出错了）" + (it.message ?: "未知错误") + "\n\n") }
                btnSend.isEnabled = true
                scrollBottom()
            }
        }
    }

    private fun append(text: String) {
        tvLog.append(text)
        scrollBottom()
    }

    private fun scrollBottom() {
        svLog.post { svLog.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        io.shutdownNow()
        super.onDestroy()
    }

    companion object {
        private const val SYSTEM_PROMPT =
            "你是「大肥鱼」，一只住在手机屏幕上的胖鱼桌宠。" +
                "说话短、萌、带点气泡音，可以用「喵？」「咕噜」之类的口头禅，" +
                "回答要简洁，避免长篇大论。"
    }
}
