# Браузер Maximeze

Современный минималистичный кроссплатформенный браузер для **Windows** и **Android** — создан с акцентом на производительность, приватность и единый дизайн.

[![Сборка Windows](https://github.com/ignatkinm00-afk/Maximeze-/actions/workflows/build-windows.yml/badge.svg)](https://github.com/ignatkinm00-afk/Maximeze-/actions/workflows/build-windows.yml)
[![Сборка Android](https://github.com/ignatkinm00-afk/Maximeze-/actions/workflows/build-android.yml/badge.svg)](https://github.com/ignatkinm00-afk/Maximeze-/actions/workflows/build-android.yml)
[![Лицензия: MIT](https://img.shields.io/badge/Лицензия-MIT-blue.svg)](LICENSE)
[![Релиз](https://img.shields.io/github/v/release/ignatkinm00-afk/Maximeze-)](https://github.com/ignatkinm00-afk/Maximeze-/releases)

---

## Возможности

- **Вкладки** — несколько вкладок с превью, ленивой загрузкой и приостановкой неактивных вкладок
- **Омнибокс** — единая адресная строка с автодополнением, поисковыми подсказками и быстрыми командами
- **Поисковые системы** — Google, Bing, DuckDuckGo (выбирается пользователем)
- **Закладки и история** — локальное хранение, импорт/экспорт в форматах Chrome/Firefox (HTML/JSON)
- **Приватный режим** — инкогнито, встроенная блокировка рекламы и трекеров (EasyList + EasyPrivacy)
- **Менеджер загрузок** — прогресс, пауза/возобновление
- **Темы** — светлая, тёмная, системная
- **Синхронизация** *(запланировано)* — закладки, история, открытые вкладки между Windows и Android
- **Жесты на Android** — свайп между вкладками, свайп для закрытия, нижняя панель навигации
- **Интеграция с Windows** — списки переходов, перетаскивание вкладок между окнами

---

## Технологический стек

| Платформа | Технологии |
|-----------|-----------|
| Windows   | [Tauri](https://tauri.app/) (Rust + WebView2) + React/TypeScript |
| Android   | Kotlin + Jetpack Compose + Android WebView (Chromium) |
| Общая логика | `packages/core-engine` (TypeScript) |
| Дизайн-система | `packages/design-system` (токены, иконки, шрифты) |

---

## Структура репозитория

```
maximeze/
├── apps/
│   ├── windows/          # Tauri-приложение (Windows)
│   │   ├── src/          # React/TypeScript фронтенд
│   │   └── src-tauri/    # Rust бэкенд
│   └── android/          # Kotlin/Compose Android-приложение
│       └── app/src/main/
├── packages/
│   ├── design-system/    # Общие токены, иконки, шрифты
│   └── core-engine/      # Общая логика браузера (закладки, история, настройки)
├── docs/                 # Документация
├── .github/workflows/    # CI/CD
├── README.md
├── CONTRIBUTING.md
└── LICENSE
```

---

## Быстрый старт

### Необходимые инструменты

| Инструмент | Версия | Назначение |
|-----------|--------|-----------|
| [Node.js](https://nodejs.org/) | 20+ | Сборка фронтенда |
| [pnpm](https://pnpm.io/) | 9+ | Менеджер пакетов |
| [Rust](https://rustup.rs/) | stable | Бэкенд Tauri (Windows) |
| [WebView2 Runtime](https://developer.microsoft.com/ru-ru/microsoft-edge/webview2/) | последняя | Движок рендеринга (Windows) |
| [Android Studio](https://developer.android.com/studio) | последняя | Разработка Android |
| [JDK](https://adoptium.net/) | 17+ | Сборка Android |

### Сборка для Windows

```bash
# 1. Клонировать репозиторий
git clone https://github.com/ignatkinm00-afk/Maximeze-.git
cd Maximeze-

# 2. Установить зависимости Node.js
pnpm install

# 3. Запустить в режиме разработки
cd apps/windows
pnpm tauri dev

# 4. Собрать релизный бинарник
pnpm tauri build
# Результат: apps/windows/src-tauri/target/release/bundle/
```

### Сборка для Android

```bash
# 1. Открыть Android-проект в Android Studio
#    File → Open → apps/android/

# 2. Или собрать через командную строку
cd apps/android
./gradlew assembleDebug
# Результат: apps/android/app/build/outputs/apk/debug/app-debug.apk

# Релизная сборка (требует конфигурацию подписи)
./gradlew assembleRelease
```

### Запуск тестов

```bash
# Тесты ядра браузера (TypeScript)
cd packages/core-engine
pnpm test

# Тесты фронтенда Windows
cd apps/windows
pnpm test

# JVM-тесты Android
cd apps/android
./gradlew test

# Инструментальные тесты Android (требуется устройство/эмулятор)
./gradlew connectedAndroidTest
```

---

## Скачать

Готовые сборки доступны на странице [Releases](https://github.com/ignatkinm00-afk/Maximeze-/releases):

- **Windows**: `Maximeze_x.x.x_x64-setup.exe` (установщик NSIS) или `.msi`
- **Android**: `app-release.apk` (APK) или `app-release.aab` (AAB для Google Play)

---

## Вклад в проект

Смотрите [CONTRIBUTING.md](CONTRIBUTING.md) — руководство по разработке, стиль кода и процесс Pull Request.

---

## Дорожная карта

- [x] Этап 0 — инициализация монорепо, CI/CD
- [ ] Этап 1 — MVP Windows (вкладки, адресная строка, навигация)
- [ ] Этап 2 — MVP Android (то же самое, Compose UI)
- [ ] Этап 3 — полировка дизайн-системы на обеих платформах
- [ ] Этап 4 — закладки, история, загрузки, настройки
- [ ] Этап 5 — приватность (блокировщик рекламы, инкогнито)
- [ ] Этап 6 — синхронизация между устройствами
- [ ] Этап 7 — релизные сборки (подписанные .msix, .aab)

---

## Лицензия

[MIT](LICENSE) © 2024 Maximeze Browser Contributors
