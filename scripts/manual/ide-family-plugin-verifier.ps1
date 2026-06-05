<#
Manual IDE-family compatibility verifier driver.
Fill $VerifierJar and $IdeDirs before a release candidate.
#>

param(
  [Parameter(Mandatory=$true)][string]$VerifierJar,
  [Parameter(Mandatory=$true)][string]$PluginZip,
  [Parameter(Mandatory=$true)][string[]]$IdeDirs,
  [string]$OutDir = "build/plugin-verifier-results"
)

$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force $OutDir | Out-Null
foreach ($ide in $IdeDirs) {
  $safe = ($ide -replace '[:\\/ ]','_')
  $out = Join-Path $OutDir "$safe.txt"
  Write-Host "Verifying $PluginZip against $ide"
  java -jar $VerifierJar check-plugin $PluginZip $ide | Tee-Object -FilePath $out
}
Write-Host "Record results in docs/release-compatibility-matrix.md before claiming IDE-family support."
