# AutoJs6 JetBrains Release Guide

This guide is for user-managed releases. It intentionally separates what is already verified from what must be verified before claiming a full parity release.

## 1. Local build and ZIP distribution

```powershell
.\gradlew.bat clean test buildPlugin --no-daemon
```

Expected ZIP:

```text
build/distributions/AutoJs6-JetBrains-<version>.zip
```

Descriptor compatibility gate:

- `buildPlugin` depends on `verifyPatchedPluginXmlCompatibility`.
- The ZIP's inner plugin JAR must contain `<idea-version since-build="242" />`.
- It must not contain `until-build`; otherwise 2025/2026 IDEs can reject the plugin as `242.* or older`.

Install locally:

```text
Settings / Preferences → Plugins → ⚙ → Install Plugin from Disk...
→ select build/distributions/AutoJs6-JetBrains-<version>.zip
```

Rollback:

1. Disable or uninstall the plugin from IDE Plugin settings.
2. Restart IDE.
3. Install the previous ZIP from the private release archive.
4. Re-run the smoke checklist below.

## 2. Required pre-release checks

```powershell
.\gradlew.bat clean test --no-daemon
.\gradlew.bat buildPlugin --no-daemon
```

Manual smoke checklist:

- Open the plugin in `runIde`.
- Verify **Tools → AutoJs6** action group and Action Search expose all 18 VSCode parity actions.
- Verify Editor popup and Project View popup show script/project actions.
- Connect using at least one real AutoJs6 device path: LAN or ADB.
- Run / Save / Stop / Stop All a `.js` file.
- Run Project / Save Project on a folder containing `project.json`.
- Confirm Tool Window shows device metadata and logs.
- Confirm Debug Help explains breakpoint/attach boundary.
- Confirm HTTP bridge remains disabled or loopback-safe by default.

## 3. Plugin Verifier

Use the IntelliJ Platform Gradle plugin verifier task or the JetBrains Plugin Verifier CLI against the target matrix from `docs/release-compatibility-matrix.md`.
Do not treat a default `verifyPlugin` pass as proof of all future IDE imports unless the target IDE builds are included in the verifier matrix and the descriptor gate above has passed.

Configured Gradle entry point for the baseline IC 2024.2 verifier target:

```powershell
.\gradlew.bat verifyPlugin --no-daemon
```

If using the standalone verifier, download it from JetBrains and run it against the built ZIP and each declared IDE build. Record every result in `docs/release-compatibility-matrix.md` before release.
For the IDEA 2026 row, either add that IDE to `intellijPlatform.pluginVerification.ides` for the release candidate or run `scripts/manual/ide-family-plugin-verifier.ps1` against the installed IDEA 2026 directory.

## 4. Signing

If publishing to Marketplace, configure signing credentials outside the repository, e.g. environment variables or Gradle properties ignored by VCS:

```powershell
$env:PRIVATE_KEY=<path-or-value>
$env:PRIVATE_KEY_PASSWORD=<password>
$env:CERTIFICATE_CHAIN=<path-or-value>
.\gradlew.bat signPlugin --no-daemon
```

Do not commit private keys, certificates with private material, tokens, or Marketplace credentials.

## 5. Marketplace upload by the user

1. Prepare version and changelog.
2. Build and verify the ZIP.
3. Sign the plugin if required.
4. Log in to JetBrains Marketplace using the user's account.
5. Upload the ZIP.
6. Fill release notes and compatibility range.
7. Submit for approval.
8. Monitor Marketplace validation and user feedback.

## 6. Private ZIP sharing

- Share only the built ZIP from `build/distributions/`.
- Include version, commit hash, Gradle/JDK version, target IDE build range, and known exceptions.
- Keep the previous ZIP available for rollback.
- Ask recipients to install through **Install Plugin from Disk**.

## 7. Version / changelog preparation

- Update `version` in `build.gradle.kts`.
- Update README and changelog/release notes.
- Summarize parity evidence:
  - command/action rows
  - protocol tests
  - project sync tests
  - real device replay
  - HTTP bridge safe default
  - JetBrains IDE matrix status

## 8. Approval checklist

Do not publish a full parity claim unless all are true:

- No runtime/protocol breakage from the baseline command payloads.
- New Project still scaffolds from bundled template only.
- All 18 VSCode command rows have JetBrains actions and UI placement evidence.
- Project sync uses real zip/md5/bytes ordering and reports failures.
- HTTP bridge is disabled or loopback-safe by default; wide binding requires explicit compatibility mode.
- No fake connected devices, fake success, static project bytes, guessed protocol fields, or unverified IDE-family claims.
- Plugin Verifier/manual matrix results are recorded.

## 9. Troubleshooting

- **ADB unavailable**: configure ADB path or ensure `adb` is on PATH; Windows fallback uses bundled `tools/adb.exe` when available.
- **ADB offline**: run `adb kill-server`, `adb start-server`, reconnect USB/debug authorization.
- **Handshake timeout**: ensure AutoJs6 Server mode or Client mode matches selected connection path and version is at least `6.7.0 / 3591`.
- **Project command rejected**: verify selected folder or current file belongs to a directory containing `project.json`.
- **No HTTP response**: verify bridge is enabled, bound to expected host/port, and command is whitelisted.
