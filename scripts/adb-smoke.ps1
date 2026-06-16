$ErrorActionPreference = "Stop"

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
  throw "adb not found: $adb"
}

function Require-Text($name, $text, $pattern) {
  if ($text -notmatch $pattern) {
    throw "Missing UI marker: $name ($pattern)"
  }
  Write-Host "  OK $name" -ForegroundColor Green
}

Write-Host "[adb-smoke] checking device"
$devices = & $adb devices
if (($devices | Select-String -Pattern "`tdevice").Count -lt 1) {
  throw "No adb device is connected"
}

Write-Host "[adb-smoke] reverse ports"
& $adb reverse tcp:8080 tcp:8080 | Out-Null
& $adb reverse tcp:8082 tcp:8082 | Out-Null
& $adb reverse tcp:8083 tcp:8083 | Out-Null
& $adb reverse tcp:8084 tcp:8084 | Out-Null

Write-Host "[adb-smoke] install debug apk"
$installOutput = & $adb install -r -g "app\build\outputs\apk\debug\app-debug.apk" 2>&1
$installOutput | Out-Host
if ($LASTEXITCODE -ne 0 -or (($installOutput -join "`n") -notmatch "Success")) {
  throw "APK install failed. Unlock the phone and approve the install prompt, then rerun scripts/adb-smoke.ps1."
}

Write-Host "[adb-smoke] launch app"
& $adb shell monkey -p com.bookrealm.reader 1 | Out-Null
Start-Sleep -Seconds 4

$focus = (& $adb shell dumpsys window | Select-String -Pattern "mCurrentFocus|mFocusedApp") -join "`n"
Require-Text "main activity" $focus "com.bookrealm.reader"

Write-Host "[adb-smoke] dump ui"
& $adb shell uiautomator dump /sdcard/br-reader-smoke.xml | Out-Null
$xml = (& $adb shell cat /sdcard/br-reader-smoke.xml) -join "`n"

Require-Text "shelf search tag" $xml 'resource-id="shelf_search"'
Require-Text "shelf book tag" $xml 'resource-id="shelf_book_'
Require-Text "bottom shelf tab" $xml 'resource-id="bottom_tab_shelf"'
Require-Text "bottom store tab" $xml 'resource-id="bottom_tab_store"'
Require-Text "bottom me tab" $xml 'resource-id="bottom_tab_me"'

$version = (& $adb shell dumpsys package com.bookrealm.reader | Select-String -Pattern "versionCode|versionName") -join "`n"
Require-Text "version name" $version "0.3.0-alpha.20260616"

Write-Host "[adb-smoke] passed"
