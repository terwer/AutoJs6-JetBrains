# AutoJs6 JetBrains MVP

Gradle-based IntelliJ Platform plugin implementing the AutoJs6 MVP from `openspec/changes/add-autojs6-jetbrains-mvp`.

## Compatibility guardrails

- Historical AutoJs6 project/runtime/protocol compatibility first.
- Preserve VSCode extension user workflow intent while using JetBrains-native Actions, Dialogs and Notifications.
- Implemented features must not be behavior-shrunk versus the observed VSCode flow.
- No mock/fake/speculative success: deferred work is explicit and unavailable.

## MVP decisions

- Target: JetBrains IDE family via IntelliJ Platform APIs, not IntelliJ IDEA-only.
- ADB: user-configured path, then PATH, then bundled Windows fallback from plugin resources.
- UI: MVP intentionally uses Actions, Dialogs and Notifications only; device Tool Window is deferred.
