# Maximeze — Обзор архитектуры

## Структура репозитория

```
maximeze/
├── apps/
│   ├── windows/          Tauri 2.x (Rust) + React 18 + TypeScript
│   └── android/          Kotlin 2.0 + Jetpack Compose + Room + WebView
├── packages/
│   ├── core-engine/      Чистый TypeScript — примитивы браузера
│   └── design-system/    Дизайн-токены, CSS-переменные, SVG-иконки
└── docs/
    ├── architecture.md   (этот файл)
    └── adr/              Записи архитектурных решений
```

---

## Выбор движка рендеринга

| Платформа | Движок | Обоснование |
|-----------|--------|-------------|
| Windows   | WebView2 (Chromium, через Tauri) | Поставляется с Windows 10/11, автоматически обновляется через Windows Update |
| Android   | System WebView (Chromium, через Android WebView) | Обновляется автоматически через Google Play, гарантирован уровень API 30+ |

Обе платформы используют движок Chromium, что обеспечивает единообразную совместимость с сайтами и функции конфиденциальности.

---

## Приложение Windows (Tauri)

```
apps/windows/
├── src/                    React + TypeScript UI
│   ├── components/         TabBar, Toolbar, Omnibox, WebContent, Sidebar, NewTabPage
│   ├── store/              Глобальное состояние Zustand (browserStore.ts)
│   └── styles/             Глобальные CSS-стили
├── src-tauri/              Rust-бэкенд
│   └── src/
│       ├── main.rs         Точка входа
│       ├── lib.rs          Tauri-строитель, регистрация плагинов
│       ├── commands.rs     Команды Tauri IPC (open_url, block_request, …)
│       └── error.rs        Типы ошибок
└── tauri.conf.json         Конфигурация окна, цели сборки
```

**Управление состоянием**: хранилище Zustand (`browserStore.ts`) хранит всё состояние браузера — вкладки, закладки, историю, загрузки, настройки. Функции core-engine — чистые и без состояния; Zustand оборачивает их реактивностью.

**IPC**: Фронтенд вызывает команды Tauri через `@tauri-apps/api/core`. Команды валидируют входные данные, применяют бизнес-логику (например, проверку схемы URL) и возвращают результаты.

**Персистентность**: `tauri-plugin-store` сериализует состояние на диск. При запуске приложение читает хранилище и гидратирует Zustand.

---

## Приложение Android (Kotlin + Compose)

```
apps/android/app/src/main/
├── java/com/maximeze/browser/
│   ├── MainActivity.kt             Корневой Compose
│   ├── data/
│   │   ├── db/                     База данных Room, DAO
│   │   └── model/                  Классы данных (Tab, Bookmark, HistoryEntry, …)
│   └── ui/
│       ├── MaximizeApp.kt          Корневой composable, переключение маршрутов
│       ├── BrowserViewModel.kt     AndroidViewModel, StateFlow
│       ├── browser/BrowserScreen.kt WebView + нижняя навигационная панель
│       ├── tabs/TabGridScreen.kt   Сетка вкладок
│       └── theme/                  Тема Compose Material 3
```

**Управление состоянием**: `BrowserViewModel` (AndroidViewModel) предоставляет `StateFlow<BrowserUiState>`. DAO Room предоставляют `Flow<List<…>>` для закладок/истории, собранные как `StateFlow` через `stateIn`.

**Персистентность**: База данных Room для закладок и истории. Настройки хранятся в DataStore Preferences.

**WebView**: Каждая вкладка рендерится в `AndroidView`-обёртке `WebView`. ViewModel обновляет состояние вкладки (`url`, `title`, `isLoading`) из коллбэков `WebViewClient`.

---

## Общие пакеты

### `@maximeze/core-engine`

Чистая TypeScript-библиотека — без DOM, без Node, без Android API. Функции чистые/иммутабельные:

- `createTab`, `activateTab`, `closeTab`, `reorderTabs` — управление вкладками
- `createBookmark`, `exportToHTML`, `importFromHTML`, `exportToJSON`, `importFromJSON` — закладки
- `recordVisit`, `searchHistory` — история
- `getSuggestions`, `isUrl`, `normalizeUrl` — омнибокс
- `createDownload`, `updateDownloadProgress`, `formatFileSize` — загрузки
- `defaultSettings`, `mergeSettings`, `validateSettings` — настройки

Android не использует этот пакет напрямую (экосистема Kotlin); логика реализована нативно для типобезопасности.

### `@maximeze/design-system`

- `src/tokens.ts` — TypeScript-константы для цветов, отступов, радиусов, типографики, анимации
- `src/icons.ts` — SVG-строки, тип-объединение `IconName`
- `src/styles.css` — CSS-переменные, включая светлую/тёмную тему

Android использует отдельную тему Material 3, согласованную с той же цветовой палитрой через `Theme.kt`.

---

## Архитектура конфиденциальности

### Блокировка рекламы и трекеров

**Windows**: Rust-функция `commands::block_request` применяет сопоставление URL-паттернов с встроенным списком блокировки. В будущих версиях EasyList/EasyPrivacy загружаются при старте и компилируются в фильтр (например, крейт Rust `adblock`).

**Android**: Планируется — `WebViewClient.shouldInterceptRequest` перехватывает сетевые запросы и сопоставляет их со списком блокировки из ресурсов.

### Режим инкогнито

**Windows**: Вкладки инкогнито хранятся только в памяти (Zustand) и никогда не сохраняются на диск через `tauri-plugin-store`. Rust-бэкенд пропускает запись истории, если вкладка помечена как инкогнито.

**Android**: Вкладки инкогнито используют отдельную конфигурацию `WebView` только в памяти, с очисткой кук и кэша при закрытии.

---

## Архитектура синхронизации (Планируется, Этап 6)

```
┌──────────────┐     HTTPS/JSON     ┌──────────────────┐
│  Windows     │ ◄─────────────────►│   Sync API       │
└──────────────┘                    │   (self-hosted    │
                                    │    или Firebase)  │
┌──────────────┐                    │                   │
│  Android     │ ◄─────────────────►│                   │
└──────────────┘                    └──────────────────┘
```

Схема полезной нагрузки синхронизации (JSON):
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

Разрешение конфликтов: последняя запись побеждает для каждого поля с метками времени `updatedAt`.

---

## CI/CD

| Воркфлоу | Триггер | Результат |
|----------|---------|-----------|
| `ci.yml` | Каждый push/PR | Lint, проверка типов, модульные тесты (все пакеты) |
| `build-windows.yml` | Push в main, теги | `.exe` (установщик NSIS), `.msi` |
| `build-android.yml` | Push в main, теги | `.apk` (debug/release), `.aab` (release) |

Артефакты релиза прикрепляются к GitHub Releases при тегах версий (`v*`).
