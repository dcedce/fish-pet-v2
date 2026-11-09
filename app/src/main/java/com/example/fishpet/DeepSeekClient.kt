package com.example.fishpet

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** 极简 DeepSeek Chat 客户端：只保留单轮对话需要的最小实现 */
object DeepSeekClient {

    private const val ENDPOINT = "https://api.deepseek.com/v1/chat/completions"
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 阻塞式调用，必须在子线程执行。
     * @return 模型回复文本；失败时抛出异常由调用方处理
     */
    fun chat(apiKey: String, prompt: String, systemPrompt: String? = null): String {
        val messages = JSONArray()
        if (!systemPrompt.isNullOrBlank()) {
            messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
        }
        messages.put(JSONObject().put("role", "user").put("content", prompt))

        val payload = JSONObject()
            .put("model", "deepseek-chat")
            .put("messages", messages)
            .put("stream", false)
            .put("temperature", 1.2)
            .toString()

        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IllegalStateException("HTTP ${resp.code}: ${body.take(300)}")
            }
            val content = JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .optString("content")
            return content.ifBlank { "（大肥鱼没说话…）" }
        }
    }
}
