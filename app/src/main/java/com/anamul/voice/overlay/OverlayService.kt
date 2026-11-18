package com.anamul.voice.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

// Floating status/control surface for this app. It does not inject audio into another app's microphone.
class OverlayService : Service() {
    private var window: WindowManager? = null
    private var view: TextView? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
        if (view == null) {
            window = getSystemService(WINDOW_SERVICE) as WindowManager
            view = TextView(this).apply { text="  AS VOICE • PLAYBACK  "; textSize=13f; setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(0xDD17172A.toInt()); setPadding(24,18,24,18) }
            val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT).apply { gravity=Gravity.TOP or Gravity.END; x=12; y=180 }
            window?.addView(view,params)
        }
        return START_STICKY
    }
    override fun onDestroy() { view?.let { window?.removeView(it) }; view=null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
