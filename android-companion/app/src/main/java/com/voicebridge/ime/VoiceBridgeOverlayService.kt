package com.voicebridge.ime

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.speech.*
import android.view.*
import android.widget.*

class VoiceBridgeOverlayService : Service() {
    private lateinit var wm: WindowManager
    private var icon: TextView? = null
    private var panel: LinearLayout? = null
    private var transcript: TextView? = null
    private var recognizer: SpeechRecognizer? = null
    private var text = ""
    private var listening = false
    private val channel = "voicebridge"

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(7, Notification.Builder(this, channel)
            .setContentTitle("VoiceBridge is ready")
            .setContentText("Tap the floating microphone to dictate")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        showIcon()
    }

    private fun params(width: Int, height: Int): WindowManager.LayoutParams = WindowManager.LayoutParams(
        width, height,
        if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.END; x = 24; y = 180 }

    private fun showIcon() {
        icon = TextView(this).apply {
            text = "●"
            textSize = 30f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(42, 96, 180))
            gravity = Gravity.CENTER
            setPadding(14, 6, 14, 6)
            setOnClickListener { showPanel(); startListening() }
        }
        wm.addView(icon, params(76, 76))
    }

    private fun showPanel() {
        if (panel != null) return
        panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 16, 22, 16)
            setBackgroundColor(Color.WHITE)
        }
        transcript = TextView(this).apply { text = "Listening…"; textSize = 18f }
        val controls = LinearLayout(this).apply { gravity = Gravity.END }
        val cancel = Button(this).apply { text = "✕"; setOnClickListener { discard() } }
        val submit = Button(this).apply { text = "✓"; setOnClickListener { submit() } }
        controls.addView(cancel); controls.addView(submit)
        panel!!.addView(transcript); panel!!.addView(controls)
        wm.addView(panel, params(420, 180).apply { y = 140 })
        icon?.visibility = View.GONE
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { transcript?.text = "Speech recognition unavailable"; return }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).also { it.setRecognitionListener(listener) }
        text = ""
        listening = true
        recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        })
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(p: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(v: Float) {}
        override fun onBufferReceived(b: ByteArray?) {}
        override fun onEndOfSpeech() { listening = false }
        override fun onError(e: Int) { if (text.isBlank()) transcript?.text = "Recognition error ($e)" }
        override fun onResults(b: Bundle?) { text = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty(); transcript?.text = text }
        override fun onPartialResults(b: Bundle?) { val partial = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty(); if (partial.isNotBlank()) { text = partial; transcript?.text = partial } }
        override fun onEvent(t: Int, p: Bundle?) {}
    }

    private fun discard() { recognizer?.cancel(); text = ""; closePanel() }
    private fun submit() {
        recognizer?.stopListening()
        VoiceBridgeAccessibilityService.instance?.insertText(text)
        closePanel()
    }
    private fun closePanel() { panel?.let { wm.removeView(it) }; panel = null; icon?.visibility = View.VISIBLE }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(channel, "VoiceBridge", NotificationManager.IMPORTANCE_LOW))
    }
    override fun onDestroy() { icon?.let { wm.removeView(it) }; panel?.let { wm.removeView(it) }; recognizer?.destroy(); super.onDestroy() }
    override fun onBind(intent: Intent?) = null
}
