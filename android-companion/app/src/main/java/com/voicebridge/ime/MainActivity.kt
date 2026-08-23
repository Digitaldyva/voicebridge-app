package com.voicebridge.ime

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 48, 32, 32) }
        root.addView(TextView(this).apply { text = "VoiceBridge\nFloating live dictation"; textSize = 22f })
        root.addView(TextView(this).apply {
            text = "Grant microphone and overlay access, then enable VoiceBridge text insertion in Accessibility settings. A small microphone icon will remain on screen."
            textSize = 16f; setPadding(0, 24, 0, 24)
        })
        root.addView(Button(this).apply {
            text = "Allow microphone"
            setOnClickListener { requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100) }
        })
        root.addView(Button(this).apply {
            text = "Allow floating icon"
            setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }
        })
        root.addView(Button(this).apply {
            text = "Enable text insertion"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "Start VoiceBridge"
            setOnClickListener { startOverlay() }
        })
        setContentView(root)
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) return
        val intent = Intent(this, VoiceBridgeOverlayService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
        moveTaskToBack(true)
    }
}
