# Maximeze Browser

A modern, minimalist cross-platform browser for **Windows** and **Android** — built with performance, privacy, and a unified design language in mind.

[![Build Windows](https://github.com/ignatkinm00-afk/Maximeze-/actions/workflows/build-windows.yml/badge.svg)](https://github.com/ignatkinm00-afk/Maximeze-/actions/workflows/build-windows.yml)
[![Build Android](https://github.com/ignatkinm00-afk/Maximeze-/actions/workflows/build-android.yml/badge.svg)](https://github.com/ignatkinm00-afk/Maximeze-/actions/workflows/build-android.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## Features

- **Multi-tab browsing** — with previews, lazy loading, and tab suspension for inactive tabs
- **Omnibox** — unified address bar with autocomplete, search shortcuts, and quick commands
- **Search engines** — Google, Bing, DuckDuckGo — user-selectable
- **Bookmarks & History** — local storage, import/export (Chrome/Firefox-compatible HTML/JSON)
- **Privacy mode** — incognito, built-in ad/tracker blocker (EasyList + EasyPrivacy)
- **Download manager** — progress, pause/resume
- **Themes** — light, dark, system-auto
- **Sync** *(planned)* — bookmarks, history, open tabs between Windows and Android
- **Android gestures** — swipe between tabs, swipe to close, bottom navigation bar
- **Windows integration** — jump lists, drag & drop tabs between windows

---

## Technology Stack

| Platform | Technology |
|----------|-----------|
| Windows  | [Tauri](https://tauri.app/) (Rust + WebView2) + React/TypeScript frontend |
| Android  | Kotlin + Jetpack Compose + Android WebView (Chromium) |
| Shared logic | `packages/core-engine` (TypeScript) |
| Design system | `packages/design-system` (tokens, icons, fonts) |

---

## Repository Structure

```
maximeze/
├── apps/
│   ├── windows/          # Tauri app (Windows)
│   │   ├── src/          # React/TypeScript frontend
│   │   └── src-tauri/    # Rust backend
│   └── android/          # Kotlin/Compose Android app
│       └── app/src/main/
├── packages/
│   ├── design-system/    # Shared tokens, icons, fonts
│   └── core-engine/      # Shared browser logic (bookmarks, history, settings)
├── docs/                 # Documentation
├── .github/workflows/    # CI/CD
├── README.md
├── CONTRIBUTING.md
└── LICENSE
```

---

## Getting Started

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| [Node.js](https://nodejs.org/) | 20+ | Frontend build |
| [pnpm](https://pnpm.io/) | 9+ | Package manager |
| [Rust](https://rustup.rs/) | stable | Tauri backend (Windows) |
| [WebView2 Runtime](https://developer.microsoft.com/en-us/microsoft-edge/webview2/) | latest | Rendering engine (Windows) |
| [Android Studio](https://developer.android.com/studio) | latest | Android development |
| [JDK](https://adoptium.net/) | 17+ | Android build |

### Windows Build

```bash
# 1. Clone the repository
git clone https://github.com/ignatkinm00-afk/Maximeze-.git
cd Maximeze-

# 2. Install Node.js dependencies
pnpm install

# 3. Run in development mode
cd apps/windows
pnpm tauri dev

# 4. Build a release binary
pnpm tauri build
# Output: apps/windows/src-tauri/target/release/bundle/
```

### Android Build

```bash
# 1. Open the Android project in Android Studio
#    File → Open → apps/android/

# 2. Or build from command line
cd apps/android
./gradlew assembleDebug
# Output: apps/android/app/build/outputs/apk/debug/app-debug.apk

# Release build (requires signing config)
./gradlew assembleRelease
```

### Running Tests

```bash
# Core engine unit tests
cd packages/core-engine
pnpm test

# Windows frontend tests
cd apps/windows
pnpm test

# Android unit tests
cd apps/android
./gradlew test

# Android instrumented tests
./gradlew connectedAndroidTest
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines, code style, and pull request process.

---

## Roadmap

- [x] Stage 0 — Monorepo setup, CI/CD
- [ ] Stage 1 — Windows MVP (tabs, address bar, navigation)
- [ ] Stage 2 — Android MVP (same features, Compose UI)
- [ ] Stage 3 — Design system polish on both platforms
- [ ] Stage 4 — Bookmarks, history, downloads, settings
- [ ] Stage 5 — Privacy features (ad blocker, incognito)
- [ ] Stage 6 — Cross-device sync
- [ ] Stage 7 — Release builds (signed .msix, .aab)

---

## License

[MIT](LICENSE) © 2024 Maximeze Browser Contributors
