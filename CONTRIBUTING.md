# Contributing to LOR TO-DO

Thank you for your interest in contributing to **LOR TO-DO**! We welcome bug reports, feature requests, localizations, and pull requests.

## Core Guiding Principles

1. **Strictly Offline**: Never introduce dependencies or code that request or require the `android.permission.INTERNET` permission.
2. **Privacy First**: No telemetry, analytics, or third-party tracking SDKs are allowed.
3. **Data Ownership**: Any new data entities must be exportable and importable via open formats.
4. **Performance**: Keep cold starts under 1 second and lists responsive even with 10,000+ items.

## Development Workflow

1. Fork the repository and create your branch from `main`:
   ```bash
   git checkout -b feature/my-new-feature
   ```
2. Build and verify locally:
   ```bash
   ./gradlew testDebugUnitTest
   ./gradlew assembleDebug
   ```
3. Commit with Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`).
4. Submit a Pull Request.

## Translations & Localization

All user-visible strings are externalized in `app/src/main/res/values/strings.xml`. Community translations for additional languages can be submitted via PR or Weblate.
