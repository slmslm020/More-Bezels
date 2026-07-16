package com.example.morebezels

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Çizim izni verildi!", Toast.LENGTH_SHORT).show()
            updatePermissionStatuses()
        } else {
            Toast.makeText(this, "İzin verilmedi, çerçeve gösterilemez.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnToggleOverlay = findViewById<MaterialButton>(R.id.btnToggleOverlay)
        val btnOverlayPermission = findViewById<MaterialButton>(R.id.btnOverlayPermission)

        btnOverlayPermission.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            } else {
                Toast.makeText(this, "İzin zaten verilmiş!", Toast.LENGTH_SHORT).show()
            }
        }

        btnToggleOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Lütfen önce izin verin!", Toast.LENGTH_SHORT).show()
            } else {
                // Servisi çağırıyoruz
                val intent = Intent(this, OverlayService::class.java)
                startService(intent)
                Toast.makeText(this, "Servis başlatıldı!", Toast.LENGTH_SHORT).show()
            }
        }

        updatePermissionStatuses()
    }

    private fun updatePermissionStatuses() {
        val ivOverlayStatus = findViewById<ImageView>(R.id.ivOverlayStatus)
        if (Settings.canDrawOverlays(this)) {
            ivOverlayStatus.setImageResource(android.R.drawable.presence_online)
        } else {
            ivOverlayStatus.setImageResource(android.R.drawable.presence_offline)
        }
    }
}
