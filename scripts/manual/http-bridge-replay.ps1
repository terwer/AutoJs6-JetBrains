<#
Manual HTTP bridge replay for runIde.
Prerequisites:
  1. Start the plugin with .\gradlew.bat runIde.
  2. Open or create an AutoJs6 project containing project.json.
  3. Tools -> AutoJs6 -> Start HTTP Bridge (safe loopback).
  4. Connect at least one AutoJs6 device if you want device-side effects.
#>

param(
  [Parameter(Mandatory=$true)][string]$ScriptPath,
  [Parameter(Mandatory=$true)][string]$ProjectRoot,
  [int]$Port = 10347
)

$ErrorActionPreference = 'Stop'
$encodedScript = [uri]::EscapeDataString((Resolve-Path -LiteralPath $ScriptPath).Path)
$encodedProject = [uri]::EscapeDataString((Resolve-Path -LiteralPath $ProjectRoot).Path)

$requests = @(
  "http://127.0.0.1:$Port/exec?cmd=run&path=$encodedScript",
  "http://127.0.0.1:$Port/exec?cmd=rerunProject&path=$encodedProject"
)

foreach ($url in $requests) {
  Write-Host "GET $url"
  $res = Invoke-WebRequest -UseBasicParsing -Uri $url
  Write-Host "HTTP $($res.StatusCode): $($res.Content)"
}

Write-Host "Expected: both requests return HTTP 200 and the IDE reports command dispatch; unknown commands must return HTTP 400."
