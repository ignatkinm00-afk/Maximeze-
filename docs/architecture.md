# Maximeze — Architecture Overview

## Repository Structure

```
maximeze/
├── apps/
│   ├── windows/          Tauri 2.x (Rust) + React 18 + TypeScript
│   └── android/          Kotlin 2.0 + Jetpack Compose + Room + WebView
├── packages/
│   ├── core-engine/      Pure TypeScript — browser primitives
│   └── design-system/    Design tokens, CSS variables, SVG icons
└── docs/
    ├── architecture.md   (this file)
    └── adr/              Architecture Decision Records
```

---

## Rendering Engine Decision

| Platform | Engine | Rationale |
|----------|--------|-----------|
| Windows  | WebView2 (Chromium, via Tauri) | Ships with Windows 10/11, always up-to-date via Windows Update |
| Android  | System WebView (Chromium, via Android WebView) | Updated automatically via Google Play, 30+ API level guaranteed |

Both use the Chromium engine, ensuring consistent site compatibility and privacy features across platforms.

---

## Windows App (Tauri)

```
apps/windows/
├── src/                    React + TypeScript UI
│   ├── components/         TabBar, Toolbar, Omnibox, WebContent, Sidebar, NewTabPage
│   ├── store/              Zustand global state (browserStore.ts)
│   └── styles/             Global CSS
├── src-tauri/              Rust backend
│   └── src/
│       ├── main.rs         Entry point
│       ├── lib.rs          Tauri builder, plugin registration
│       ├── commands.rs     Tauri IPC commands (open_url, block_request, …)
│       └── error.rs        Error types
└── tauri.conf.json         Window config, bundle targets
```

**State management**: Zustand store (`browserStore.ts`) holds all browser state — tabs, bookmarks, history, downloads, settings. Core-engine functions are pure and stateless; Zustand wraps them with reactivity.

**IPC**: Frontend calls Tauri commands via `@tauri-apps/api/core`. Commands validate inputs, apply business logic (e.g., URL scheme checks), and return results.

**Persistence**: `tauri-plugin-store` serializes state to disk. On startup, the app reads the store and hydrates Zustand.

---

## Android App (Kotlin + Compose)

```
apps/android/app/src/main/
├── java/com/maximeze/browser/
│   ├── MainActivity.kt             Compose root
│   ├── data/
│   │   ├── db/                     Room database, DAOs
│   │   └── model/                  Data classes (Tab, Bookmark, HistoryEntry, …)
│   └── ui/
│       ├── MaximizeApp.kt          Root composable, route switching
│       ├── BrowserViewModel.kt     AndroidViewModel, StateFlow
│       ├── browser/BrowserScreen.kt WebView + bottom nav bar
│       ├── tabs/TabGridScreen.kt   Tab grid overview
│       └── theme/                  Compose Material 3 theme
```

**State management**: `BrowserViewModel` (AndroidViewModel) exposes `StateFlow<BrowserUiState>`. Room DAOs expose `Flow<List<…>>` for bookmarks/history, collected as `StateFlow` via `stateIn`.

**Persistence**: Room database for bookmarks and history. Settings stored in DataStore Preferences.

**WebView**: Each tab renders in an `AndroidView`-wrapped `WebView`. The ViewModel updates tab state (`url`, `title`, `isLoading`) from `WebViewClient` callbacks.

---

## Shared packages

### `@maximeze/core-engine`

Pure TypeScript library — no DOM, no Node, no Android APIs. Functions are pure/immutable:

- `createTab`, `activateTab`, `closeTab`, `reorderTabs` — tab management
- `createBookmark`, `exportToHTML`, `importFromHTML`, `exportToJSON`, `importFromJSON` — bookmarks
- `recordVisit`, `searchHistory` — history
- `getSuggestions`, `isUrl`, `normalizeUrl` — omnibox
- `createDownload`, `updateDownloadProgress`, `formatFileSize` — downloads
- `defaultSettings`, `mergeSettings`, `validateSettings` — settings

Android doesn't consume this package directly (Kotlin ecosystem); it replicates the same logic natively for type safety.

### `@maximeze/design-system`

- `src/tokens.ts` — TypeScript constants for colors, spacing, radius, typography, animation
- `src/icons.ts` — SVG strings, `IconName` union type
- `src/styles.css` — CSS custom properties (variables) including light/dark mode

Android uses a separate Material 3 theme aligned with the same color palette via `Theme.kt`.

---

## Privacy Architecture

### Ad/Tracker Blocking

**Windows**: Rust `commands::block_request` applies URL-pattern matching against a built-in blocklist. In future versions, EasyList/EasyPrivacy are loaded at startup and compiled to a filter engine (e.g., Rust's `adblock` crate).

**Android**: Planned — `WebViewClient.shouldInterceptRequest` intercepts network requests and matches against a blocklist loaded from assets.

### Incognito Mode

**Windows**: Incognito tabs are stored only in memory (Zustand), never persisted to disk via `tauri-plugin-store`. Rust backend skips history recording when tab is flagged incognito.

**Android**: Incognito tabs use a separate in-memory-only `WebView` configuration with cookies and cache cleared on close.

---

## Sync Architecture (Planned, Stage 6)

```
┌──────────────┐     HTTPS/JSON     ┌──────────────────┐
│  Windows app │ ◄─────────────────►│   Sync API       │
└──────────────┘                    │   (self-hosted    │
                                    │    or Firebase)   │
┌──────────────┐                    │                   │
│  Android app │ ◄─────────────────►│                   │
└──────────────┘                    └──────────────────┘
```

Sync payload schema (JSON):
```json
{
  "version": 1,
  "userId": "...",
  "lastSync": 1720000000000,
  "bookmarks": [...],
  "bookmarkFolders": [...],
  "history": [...],
  "settings": {...},
  "openTabs": [...]
}
```

Conflict resolution: last-write-wins per field, with `updatedAt` timestamps.

---

## CI/CD

| Workflow | Trigger | Output |
|----------|---------|--------|
| `ci.yml` | Every push/PR | Lint, typecheck, unit tests (all packages) |
| `build-windows.yml` | Push to main, tags | `.exe` (NSIS installer), `.msi` |
| `build-android.yml` | Push to main, tags | `.apk` (debug/release), `.aab` (release) |

Release artifacts are attached to GitHub Releases on version tags (`v*`).
