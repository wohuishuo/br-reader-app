$ErrorActionPreference = 'Stop'
& "$PSScriptRoot\..\gradlew.bat" lintDebug testDebugUnitTest assembleDebug
