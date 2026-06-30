# ADR 002: Android System WebView over GeckoView

**Date**: 2024  
**Status**: Accepted

## Context

The Android browser app needs a rendering engine. Two viable options exist: the system Android WebView (Chromium-based) and Mozilla GeckoView.

## Options considered

### Option A: Android WebView (Chromium)
- Built into Android — no extra download
- Updated automatically via Google Play on Android 5+
- Well-documented, broad StackOverflow / docs coverage
- API: `android.webkit.WebView`
- Does not support WebExtensions

### Option B: GeckoView (Mozilla)
- Mozilla's Android-first Gecko embedding
- Full WebExtensions support
- Better privacy controls out of the box (Enhanced Tracking Protection)
- ~40 MB extra download per ABI
- Smaller community resources; API more complex

## Decision

**Android System WebView** is chosen for MVP because:
1. **Zero extra download size** — critical for adoption.
2. **Automatic updates** — engine security patches via Play Store.
3. **API simplicity** — faster MVP development.
4. **Sufficient features** for MVP scope (no extension support required in v0.1).

### Trade-offs accepted
- No WebExtensions in v0.1 Android build.
- Privacy features (ETP) must be implemented manually at the app layer rather than relying on built-in engine features.

## Consequences
- `BrowserScreen.kt` uses `AndroidView { WebView(...) }`.
- Ad/tracker blocking implemented in `WebViewClient.shouldInterceptRequest`.
- GeckoView may be revisited in Stage 5 (privacy) or Stage 6 (extensions).
