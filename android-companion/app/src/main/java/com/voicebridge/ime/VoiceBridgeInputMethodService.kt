package com.voicebridge.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Minimal VoiceBridge IME surface.
 *
 * The service intentionally keeps the recognition boundary separate from the
 * keyboard UI. A local Whisper/Parakeet runtime can replace the placeholder
 * recognizer without changing how text reaches the focused application.
 */
class VoiceBridgeInputMethodService : InputMethodService() {
    private lateinit var interimText: TextView
    private var rawTranscript = ""

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
        }
        interimText = TextView(this).apply {
            text = "Hold the microphone button and speak"
            textSize = 16f
        }
        root.addView(interimText)
        return root
    }

    /** Update the visible words while audio is still being recognized. */
    fun showInterimText(committed: String, tentative: String) {
        rawTranscript = listOf(committed, tentative)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        interimText.text = rawTranscript.ifBlank { "Listening…" }
    }

    /** Commit only the finalized result; never silently rewrite it. */
    fun commitFinalText(text: String = rawTranscript) {
        val connection: InputConnection = currentInputConnection ?: return
        if (text.isNotBlank()) connection.commitText(text, 1)
        rawTranscript = ""
        interimText.text = "Hold the microphone button and speak"
    }
}
