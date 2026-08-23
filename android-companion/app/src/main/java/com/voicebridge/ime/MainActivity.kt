package com.voicebridge.ime

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }
        root.addView(TextView(this).apply {
            text = "VoiceBridge\nLive, privacy-first voice typing"
            textSize = 22f
        })
        root.addView(TextView(this).apply {
            text = "Enable microphone access, then enable VoiceBridge in the system keyboard settings."
            textSize = 16f
            setPadding(0, 24, 0, 24)
        })
        root.addView(Button(this).apply {
            text = "Allow microphone"
            setOnClickListener { requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100) }
        })
        root.addView(Button(this).apply {
            text = "Enable VoiceBridge keyboard"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "Open keyboard picker"
            setOnClickListener { (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).showInputMethodPicker() }
        })
        setContentView(root)
    }
}
