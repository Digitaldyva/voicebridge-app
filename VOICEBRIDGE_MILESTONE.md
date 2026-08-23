# VoiceBridge Milestone Report

**Author:** Manus AI  
**Repository:** `/home/ubuntu/voicebridge/Handy`  
**Branch:** `voicebridge-mvp`  
**Commit:** `8dcc750`

## What was implemented

VoiceBridge is now established as a Windows-first, Android-companion speech-input project based on a maintained local dictation foundation. The desktop application is rebranded from Handy to VoiceBridge, the original publisher-specific Windows signing command and update endpoint were removed, and the project now includes a product specification covering privacy, activation, live text, direct insertion, custom vocabulary, and optional rewriting.

The most important requested behavior—**text appearing while the user is speaking**—is supported by the existing streaming pipeline. The recording overlay subscribes to `streamTextEvent` and renders separate `committed` and `tentative` text. Committed words remain stable while the newest tentative words update during capture. This is preferable to waiting for the entire utterance and avoids repainting the whole sentence on every partial result.

The default Windows activation remains a two-key push-to-talk shortcut, `Ctrl+Space`. The existing Tauri shortcut implementation supports configurable shortcuts and push-to-talk behavior. The desktop overlay also provides a live waveform, elapsed capture time, cancellation, and a distinct processing state.

## Android companion

A minimal Android Input Method Editor scaffold was added under `android-companion/`. It contains a Kotlin `InputMethodService`, Android manifest metadata, Gradle configuration, and an interim-text surface. The service exposes two explicit boundaries:

| Method | Purpose |
|---|---|
| `showInterimText(committed, tentative)` | Update the visible transcript while the user is speaking |
| `commitFinalText(text)` | Insert finalized text into the focused Android field using `InputConnection.commitText` |

The Android recognizer is intentionally left behind a clean boundary so a local Whisper or Parakeet runtime can be integrated without changing how text is delivered to other applications.

## Privacy and annoyance-reduction defaults

| Area | Current design |
|---|---|
| Audio processing | Local-first desktop transcription inherited from the Handy pipeline |
| Cloud processing | Optional future provider/BYOK boundary; not required by the desktop baseline |
| AI polishing | Existing post-processing setting is disabled by default |
| Live feedback | Interim transcript and waveform are shown during recording |
| Activation | Hold `Ctrl+Space` by default, with configurable shortcuts and toggle mode available |
| Text insertion | Existing Windows insertion path with clipboard fallback support |
| User control | Cancel action, configurable settings, custom words, and model selection |
| Subscription pressure | No VoiceBridge subscription prompts were added |

## Verification performed

The repository JSON configuration passed parsing, `git diff --check` reported no whitespace errors, the live event contract was verified in both the overlay and generated bindings, and the Android source layout was inspected. The milestone was committed successfully.

A full native build was not run in this sandbox because the Rust compiler/Cargo and Android SDK are not installed. The source is therefore a **development scaffold and integration milestone**, not a signed Windows installer or installable Android APK yet. Build on a Windows machine or CI runner with Tauri prerequisites, and on an Android build environment with the Android SDK and Gradle tooling.

## Next engineering steps

The next implementation pass should add a user-visible raw-versus-polished toggle, a fast revert action that restores the raw transcript, app presets for email/chat/IDE contexts, a shared settings schema between desktop and Android, and a real embedded Android recognizer. Cloud/BYOK adapters should remain opt-in and should expose clear privacy status before any audio leaves the device.

## References

[1]: https://github.com/cjpais/Handy "cjpais/Handy GitHub repository"
[2]: https://handy.computer/ "Handy project site"
[3]: https://github.com/primaprashant/awesome-voice-typing "Awesome Voice Typing directory"

The implementation choice is grounded in the maintained Handy project, which documents local transcription, configurable shortcut activation, and streaming-capable models [1] [2]. The cross-platform and Android design space was cross-checked against the curated voice-typing directory [3].
