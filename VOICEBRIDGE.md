# VoiceBridge

VoiceBridge is a privacy-first, open-source voice-typing application for Windows and Android. It is based on the proven local transcription pipeline in Handy, with product changes aimed at reducing the most frustrating parts of cloud-first dictation tools.

## Core behavior

VoiceBridge uses **push-to-talk by default**. Hold `Ctrl+Space` on Windows, speak, and see interim words appear in the floating HUD while you are still speaking. Releasing the shortcut finalizes the transcript and inserts it into the active text field. A toggle mode remains available for hands-free dictation.

The live text is deliberately split into committed and tentative portions. Committed text remains stable while the newest words may still change as the recognizer receives more audio. This avoids the disorienting effect of repainting the whole sentence after every recognition update.

## Product decisions

| Concern | VoiceBridge default |
|---|---|
| Privacy | Local model first; cloud/BYOK providers are opt-in |
| Live feedback | Interim transcript plus waveform while speaking |
| AI rewriting | Off by default; raw transcript remains available |
| Revert | Preserve raw and polished forms so the user can undo polishing |
| Activation | Hold `Ctrl+Space`; configurable two-key shortcut |
| Annoying prompts | No recurring subscription prompts or forced rewriting |
| Input delivery | Direct keyboard input on Windows, with clipboard fallback only when required |
| Custom vocabulary | User-managed dictionary for names, brands, and technical terms |
| Android | VoiceBridge Input Method Editor (IME) for dictation in any text field |

## Existing live-streaming implementation

The desktop overlay already listens to `streamTextEvent`, which carries `committed` and `tentative` text, and updates the HUD as events arrive. Streaming-capable local models are listed in `src-tauri/src/catalog/catalog.json`. The recommended English starter models are the Parakeet streaming model for CPU-oriented machines or a Whisper-family streaming model when hardware acceleration is available.

The implementation should retain this event contract when adding future cloud providers:

```ts
interface StreamTextEvent {
  committed: string;
  tentative: string;
}
```

## Android plan

The Android companion is an IME rather than a normal text editor. This is the Android mechanism that lets a keyboard place text into the currently focused field in another application. The first Android milestone uses Android speech recognition where available and keeps a provider boundary ready for a future embedded Whisper/Parakeet runtime. The IME shows the interim transcript in its compact keyboard surface, then commits the final text with `InputConnection.commitText`.

## Build notes

The desktop source is in `src/` and `src-tauri/`. The project requires the Rust toolchain plus the JavaScript dependencies described in `package.json`. This sandbox does not currently have Rust or the Android SDK installed, so a Windows build should be performed on a Windows development machine or CI runner with Tauri prerequisites.

The original Handy source is MIT licensed. VoiceBridge changes in this repository are intended to remain MIT-compatible; review and retain upstream notices when distributing binaries.

## Roadmap

1. Keep the existing streaming HUD and local inference path as the Windows MVP.
2. Add the VoiceBridge settings copy, raw-versus-polished output state, and instant revert action.
3. Add the Android IME and shared settings schema.
4. Add optional cloud/BYOK adapters without making them required for local use.
5. Test live updates in browsers, office suites, terminals, remote desktops, and Android text fields.
