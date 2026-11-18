package com.anamul.voice.net

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
    fun verify(key:String):Boolean { val j=JSONObject(post("/api/app/verify",JSONObject().put("key",key))); return j.optBoolean("valid") }
    fun voices():List<Voice> { val req=Request.Builder().url(BASE_URL+"/api/voices").get().build(); http.newCall(req).execute().use { r -> if(!r.isSuccessful) error("HTTP ${r.code}"); val a=org.json.JSONArray(r.body?.string().orEmpty()); return (0 until a.length()).map { val v=a.getJSONObject(it); Voice(v.optString("id"),v.optString("name"),v.optString("language","bn"),v.optString("provider")) } } }
    fun generate(text:String,voiceId:String):String { val j=JSONObject(post("/api/app/tts",JSONObject().put("text",text).put("voiceId",voiceId))); return j.optString("audioUrl") }
}
