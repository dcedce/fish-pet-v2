package com.example.fishpet

import android.content.Context
import android.content.SharedPreferences

/** API Key 等本地配置的存取 */
object Prefs {
    private const val NAME = "fishpet_prefs"
    private const val KEY_API = "deepseek_api_key"

    private fun sp(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun getApiKey(ctx: Context): String =
        sp(ctx).getString(KEY_API, "")?.trim().orEmpty()

    fun setApiKey(ctx: Context, value: String) {
        sp(ctx).edit().putString(KEY_API, value.trim()).apply()
    }

    fun hasApiKey(ctx: Context): Boolean = getApiKey(ctx).isNotEmpty()
}
