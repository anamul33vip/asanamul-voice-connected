package com.anamul.voice.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class Voice(val id:String,val name:String,val language:String,val provider:String)
object ApiClient {
    // Replace with your deployed HTTPS backend URL. Never put provider secret keys in this app.
    private const val BASE_URL = "https://asanamul-voice-muse.onrender.com"
    private val http = OkHttpClient.Builder().callTimeout(60,TimeUnit.SECONDS).build()
    private fun post(path:String,json:JSONObject):String {
        val req=Request.Builder().url(BASE_URL+path).post(json.toString().toRequestBody("application/json".toMediaType())).build()
        http.newCall(req).execute().use { r -> if(!r.isSuccessful) error("HTTP ${r.code}"); return r.body?.string().orEmpty() }
    }
    // FIX (v2.7.1): the app called the network on the main thread, which
    // Android forbids — every call failed instantly, so the key could never
    // verify. Calls now run on Dispatchers.IO. The free backend also sleeps
    // when idle, so the key check retries twice while it wakes up.
    suspend fun verify(key:String):Boolean = withContext(Dispatchers.IO) {
        var last:Exception? = null
        repeat(3) { attempt ->
            try {
                val j=JSONObject(post("/api/app/verify",JSONObject().put("key",key)))
                return@withContext j.optBoolean("valid")
            } catch (e:Exception) {
                last = e
                if (attempt < 2) delay(5000)
            }
        }
        throw last ?: RuntimeException("verify failed")
    }
    suspend fun voices():List<Voice> = withContext(Dispatchers.IO) { val req=Request.Builder().url(BASE_URL+"/api/voices").get().build(); http.newCall(req).execute().use { r -> if(!r.isSuccessful) error("HTTP ${r.code}"); val a=org.json.JSONArray(r.body?.string().orEmpty()); return@withContext (0 until a.length()).map { val v=a.getJSONObject(it); Voice(v.optString("id"),v.optString("name"),v.optString("language","bn"),v.optString("provider")) } } }
    suspend fun generate(text:String,voiceId:String):String = withContext(Dispatchers.IO) { val j=JSONObject(post("/api/app/tts",JSONObject().put("text",text).put("voiceId",voiceId))); return@withContext j.optString("audioUrl") }
}
