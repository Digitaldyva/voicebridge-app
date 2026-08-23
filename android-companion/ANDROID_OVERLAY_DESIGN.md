# Android floating dictation control

VoiceBridge Android now targets a persistent overlay flow rather than a keyboard-only flow.

| State | UI | Action |
|---|---|---|
| Idle | Small microphone icon | Tap to begin recording |
| Recording | Transcript panel, live interim words, X, check | X discards; check submits |
| Processing | Transcript remains visible | Final result is inserted into the focused field |
| Idle | Small microphone icon returns | Ready for the next utterance |

The overlay requires the user to grant `SYSTEM_ALERT_WINDOW` permission and start the foreground microphone service from the visible VoiceBridge app. Text submission uses an enabled Android accessibility service to set text in the currently focused editable node; the keyboard IME remains available as a fallback for apps that do not expose an editable accessibility node.
