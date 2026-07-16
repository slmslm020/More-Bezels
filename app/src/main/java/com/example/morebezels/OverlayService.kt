package com.example.morebezels

import android.app.Service
import android.content.Intent
import android.os.IBinder

class OverlayService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Çerçeveyi çizecek kodlar buraya gelecek, uygulama artık çökmeyecek!
        return START_STICKY
    }
}
