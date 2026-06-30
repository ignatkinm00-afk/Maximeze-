# ADR 001: Tauri over Electron for Windows

**Date**: 2024  
**Status**: Accepted

## Context

The Windows browser app needs a shell framework that embeds a web rendering engine and exposes native OS APIs (file system, notifications, system tray, jump lists).

## Options considered

### Option A: Electron
- Bundles a full Chromium + Node.js runtime
- Mature ecosystem, widely deployed
- Binary size: ~100–150 MB for a minimal app
- Memory overhead: significant (Chromium + Node)
- Supports Chrome extensions out of the box

### Option B: Tauri
- Uses the OS-provided WebView2 (Chromium on Windows)
- Backend in Rust — small, fast, memory-safe
- Binary size: ~5–15 MB for a minimal app
- No bundled browser engine — depends on system WebView2 (always present on Windows 10/11)
- Extension support: not built-in (would require custom WebExtensions runtime)

## Decision

**Tauri** is chosen as the primary framework because:
1. **Size**: 10–15× smaller binaries improve download and startup time.
2. **Security**: Rust backend reduces attack surface; Tauri's permission model is more restrictive than Electron's by default.
3. **Performance**: No Node.js in the critical path; IPC is via channels, not HTTP.
4. **WebView2 availability**: Shipped with all Windows 10 (1803+) / Windows 11 installs and kept up-to-date automatically via Windows Update.

### Trade-offs accepted
- Chrome extension support is not available in MVP. This is acceptable for v0.1; a future ADR may explore a WebExtensions compatibility layer.
- Tauri's API surface is smaller than Electron's — some features need custom Rust commands.

## Consequences
- `apps/windows/src-tauri/` holds the Rust backend.
- Frontend is a standard Vite + React app, served locally during dev and bundled into the binary in production.
- Extension support deferred to a future milestone.
