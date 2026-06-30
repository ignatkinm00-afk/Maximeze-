# Contributing to Maximeze

Thank you for your interest in contributing! This document covers the development workflow, code style, and pull request process.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Repository Layout](#repository-layout)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

---

## Code of Conduct

Be respectful. Constructive criticism is welcome; personal attacks are not. Issues and PRs that violate this policy will be closed.

---

## Getting Started

1. **Fork** the repository and clone your fork.
2. Install all prerequisites listed in [README.md](README.md).
3. Install dependencies: `pnpm install` from the repo root.
4. Create a feature branch: `git checkout -b feat/your-feature-name`.
5. Make your changes, write tests, run the test suite.
6. Open a pull request against `main`.

---

## Repository Layout

```
maximeze/
├── apps/windows/          # Tauri (Rust + React/TS) — Windows browser
├── apps/android/          # Kotlin + Jetpack Compose — Android browser
├── packages/core-engine/  # Shared logic: history, bookmarks, settings, sync
├── packages/design-system/# Design tokens, icons, fonts
└── docs/                  # Architecture docs, ADRs, API specs
```

When changing shared logic in `packages/`, run tests for **both** apps that consume it.

---

## Development Workflow

### Windows (Tauri)

```bash
cd apps/windows
pnpm tauri dev          # hot-reload dev server
pnpm test               # run Vitest unit tests
pnpm lint               # ESLint + Prettier check
pnpm typecheck          # tsc --noEmit
cargo test              # Rust unit tests (from src-tauri/)
```

### Android

```bash
cd apps/android
./gradlew assembleDebug             # build debug APK
./gradlew test                      # JVM unit tests
./gradlew connectedAndroidTest      # instrumented tests (needs device/emulator)
./gradlew lint                      # Android Lint
```

### Core Engine

```bash
cd packages/core-engine
pnpm build              # compile TypeScript
pnpm test               # Vitest
pnpm lint
```

---

## Coding Standards

### TypeScript / React (Windows frontend)

- ESLint + Prettier enforced via pre-commit hooks.
- Functional components only; no class components.
- State management: React built-ins + Zustand for cross-component state.
- No `any` types — use `unknown` and narrow explicitly.
- File naming: `PascalCase` for components, `camelCase` for utilities.

### Rust (Tauri backend)

- Follow `rustfmt` defaults — run `cargo fmt` before committing.
- Run `cargo clippy` and fix all warnings.
- Document public APIs with `///` doc comments.
- Prefer `thiserror`-based error types over `anyhow` in library code.

### Kotlin / Compose (Android)

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Jetpack Compose: stateless composables where possible, hoist state up.
- Use `ktlint` formatting (enforced in CI).
- ViewModels in `ui/` package, repository logic in `data/` package.

### Design System

- All colors, spacing, and typography must use design tokens — no hardcoded values.
- SVG icons: 24×24 viewBox, `currentColor` fill/stroke.
- Naming: `--mxz-color-*`, `--mxz-spacing-*`, `--mxz-radius-*` (CSS variables).

---

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>

[optional body]

[optional footer]
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`, `ci`

**Scopes:** `windows`, `android`, `core-engine`, `design-system`, `ci`, `docs`

**Examples:**
```
feat(windows): add tab drag-and-drop between windows
fix(android): correct swipe gesture threshold on low-DPI screens
docs: update Android build prerequisites
test(core-engine): add bookmark import/export edge cases
```

---

## Pull Request Process

1. **One feature per PR** — keep diffs focused and reviewable.
2. **Link the issue** in the PR description (`Closes #123`).
3. **Pass CI** — all checks must be green before review.
4. **Write tests** — new features need unit tests; bug fixes need a regression test.
5. **Update docs** — if you change public APIs or UX behavior, update relevant docs.
6. **Screenshots / videos** for UI changes (both platforms if applicable).
7. **At least one approval** required before merge.
8. PRs are merged via **squash merge** — keep your commit history clean but it doesn't have to be perfect.

---

## Issue Reporting

Use the GitHub issue templates:

- **Bug report** — include platform (Windows/Android), version, steps to reproduce, expected vs actual behavior, logs.
- **Feature request** — describe the use case and proposed solution.
- **Performance issue** — include profiling data or reproduction steps.

Before opening an issue, search existing issues to avoid duplicates.

---

## Architecture Decision Records

Significant design decisions are documented as ADRs in `docs/adr/`. If your PR changes fundamental architecture (e.g., switching rendering engines, changing state management), add a new ADR.

---

Thank you for contributing to Maximeze!
