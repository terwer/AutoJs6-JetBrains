<#
Manual runIde smoke checklist for AutoJs6 parity.
Run from repository root after building the plugin.
#>

param(
  [string]$Gradle = ".\gradlew.bat"
)

Write-Host "== AutoJs6 runIde smoke =="
Write-Host "1. Starting runIde. In the IDE window verify:"
Write-Host "   - Tools -> AutoJs6 exposes all parity actions."
Write-Host "   - Action Search finds AutoJs6 Run/Save/Run Project/Save Project/Commands Hierarchy."
Write-Host "   - Editor popup on .js shows AutoJs6 script actions."
Write-Host "   - Project View popup on folders shows Run Project / Save Project / New Project."
Write-Host "   - AutoJs6 Tool Window appears on the right."
Write-Host "2. Use scripts/manual/http-bridge-replay.ps1 after starting HTTP Bridge from Tools -> AutoJs6."
Write-Host "3. Use scripts/manual/adb-project-replay.py for raw protocol ADB project bytes_command replay."
& $Gradle runIde
