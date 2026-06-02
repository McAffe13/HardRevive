# Contributing to HardRevive

Thank you for considering a contribution! The following guidelines help keep the project consistent and the review process smooth.

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How to Contribute](#how-to-contribute)
3. [Development Setup](#development-setup)
4. [Code Style](#code-style)
5. [Commit Messages](#commit-messages)
6. [Pull Requests](#pull-requests)
7. [Reporting Bugs](#reporting-bugs)
8. [Suggesting Features](#suggesting-features)

---

## Code of Conduct

Be respectful and constructive. Harassment of any kind is not tolerated.

---

## How to Contribute

| Type | Where |
|---|---|
| Bug report | [GitHub Issues](https://github.com/hardrevive/HardRevive/issues) |
| Feature request | [GitHub Issues](https://github.com/hardrevive/HardRevive/issues) — label `enhancement` |
| Code contribution | Fork → branch → PR |
| Translation | Add `messages_xx.yml` and open a PR |

---

## Development Setup

**Prerequisites:** JDK 21, Maven 3.9+

```bash
git clone https://github.com/hardrevive/HardRevive.git
cd HardRevive
mvn verify
```

The shaded JAR lands in `target/HardRevive-*.jar`.

For a live test environment, copy the JAR to a local Paper 1.21+ server.

---

## Code Style

- **Java 21** — use records, sealed classes, pattern matching, and text blocks where they improve clarity.
- **Nullable safety** — annotate every method return and parameter with `@NotNull` or `@Nullable` (JetBrains Annotations, provided transitively by Paper).
- **No magic strings** — all user-facing text goes through `LanguageManager`; all config keys are read via `ConfigManager`.
- **No static state** — plugin instance is passed by constructor injection, not stored in static fields.
- **Listeners** — always unregister one-shot listeners (`HandlerList.unregisterAll(this)`).
- **Formatters** — use MiniMessage for all Adventure text; never use legacy `§` codes.

---

## Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
feat: add multi-world support
fix: prevent double-ban on simultaneous death events
docs: update permission table in README
refactor: extract ReviveService from ReviveCommand
```

---

## Pull Requests

1. Fork the repository and create a branch from `main`:
   ```bash
   git checkout -b feat/my-feature
   ```
2. Make your changes, following the code style above.
3. Run `mvn verify` and ensure it compiles cleanly.
4. Open a PR against `main` with a clear title and description.
5. Reference any related issues (e.g. `Closes #42`).

PRs that introduce new user-facing features should include:
- Updated/new entries in `messages_en.yml` (and `messages_de.yml` if you can)
- Updated `CHANGELOG.md` under an `[Unreleased]` section

---

## Reporting Bugs

Please include:

- Server software and version (e.g. Paper 1.21.4 build 123)
- Java version
- HardRevive version
- Steps to reproduce
- Full stack trace from `latest.log` (use a paste service)

---

## Suggesting Features

Open an issue with the label `enhancement` and describe:

- The problem you are trying to solve
- Your proposed solution
- Any alternatives you considered

---

## License

By contributing, you agree that your contributions are licensed under the **Mozilla Public License 2.0**.
