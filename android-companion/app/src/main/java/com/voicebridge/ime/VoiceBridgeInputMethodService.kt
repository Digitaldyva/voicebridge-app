package com.voicebridge.ime

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** VoiceBridge keyboard with live interim speech-to-text updates. */
class VoiceBridgeInputMethodService : InputMethodService() {
    private lateinit var interimText: TextView
    private lateinit var micButton: Button
    private var recognizer: SpeechRecognizer? = null
    private var rawTranscript = ""
    private var listening = false

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
        }
        interimText = TextView(this).apply {
            text = "Tap and speak"
            textSize = 16f
            setPadding(0, 0, 0, 12)
        }
        micButton = Button(this).apply {
            text = "Start dictation"
            setOnClickListener { toggleListening() }
        }
        root.addView(interimText)
        root.addView(micButton)
        return root
    }

    private fun toggleListening() {
        if (listening) stopListening() else startListening()
    }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            interimText.text = "Enable microphone permission in Android Settings"
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            interimText.text = "No Android speech recognizer is available"
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).also { it.setRecognitionListener(listener) }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        rawTranscript = ""
        listening = true
        micButton.text = "Stop dictation"
        interimText.text = "Listening…"
        recognizer?.startListening(intent)
    }

    private fun stopListening() {
        recognizer?.stopListening()
        listening = false
        micButton.text = "Start dictation"
        commitFinalText()
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) { interimText.text = "Listening…" }
        override fun onBeginningOfSpeech() { interimText.text = rawTranscript.ifBlank { "Listening…" } }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { listening = false; micButton.text = "Start dictation" }
        override fun onError(error: Int) {
            listening = false
            micButton.text = "Start dictation"
            if (rawTranscript.isBlank()) interimText.text = "Speech recognition error ($error)"
        }
        override fun onResults(results: android.os.Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) rawTranscript = text
            commitFinalText()
        }
        override fun onPartialResults(partialResults: android.os.Bundle?) {
            val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) showInterimText("", text)
        }
        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    }

    /** Update visible words while recognition is still in progress. */
    fun showInterimText(committed: String, tentative: String) {
        rawTranscript = listOf(committed, tentative).filter { it.isNotBlank() }.joinToString(" ")
        interimText.text = rawTranscript.ifBlank { "Listening…" }
    }

    /** Insert finalized text into the focused Android field. */
    fun commitFinalText(text: String = rawTranscript) {
        val connection: InputConnection = currentInputConnection ?: return
        if (text.isNotBlank()) connection.commitText(text, 1)
        rawTranscript = ""
        interimText.text = "Tap and speak"
    }

    override fun onDestroy() {
        recognizer?.destroy()
        recognizer = null
        super.onDestroy()
    }
}
