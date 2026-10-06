param([switch]$Rebuild, [int]$Port = 8080)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$taskNode = (Get-Command node -ErrorAction SilentlyContinue).Source
$taskBundledRoot = Join-Path $env:USERPROFILE '.cache\codex-runtimes\codex-primary-runtime\dependencies'
if (!$taskNode) { $taskNode = Join-Path $taskBundledRoot 'node\bin\node.exe' }
$taskPnpm = (Get-Command pnpm.cmd -ErrorAction SilentlyContinue).Source
if (!$taskPnpm) { $taskPnpm = Join-Path $taskBundledRoot 'bin\fallback\pnpm.cmd' }
$taskNpm = (Get-Command npm.cmd -ErrorAction SilentlyContinue).Source
$taskMaven = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
$taskJar = Join-Path $PSScriptRoot 'backend\target\jobify-2.0.0.jar'
if ($Rebuild -or !(Test-Path -LiteralPath $taskJar)) {
    if (!(Test-Path -LiteralPath $taskNode)) { throw 'Install Node.js 22 or later to build the frontend.' }
    if (!$taskMaven) { throw 'Install Maven 3.6.3 or later and add it to PATH.' }
    Push-Location -LiteralPath (Join-Path $PSScriptRoot 'frontend')
    try {
        if (Test-Path -LiteralPath $taskPnpm) {
            & $taskPnpm install --frozen-lockfile
        } elseif ($taskNpm) { & $taskNpm install } else { throw 'Install pnpm or npm to install frontend dependencies.' }
        if ($LASTEXITCODE -ne 0) { throw 'Frontend dependency installation failed.' }
        & $taskNode 'node_modules/vite/bin/vite.js' build
        if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed.' }
    } finally { Pop-Location }
    & $taskMaven '-f' 'backend/pom.xml' 'package'
    if ($LASTEXITCODE -ne 0) { throw 'Backend build or tests failed.' }
}
$taskJava = (Get-Command java -ErrorAction SilentlyContinue).Source
if (!$taskJava) { throw 'Install Java 21 or later and add it to PATH.' }
Write-Host "Jobify+ is starting at http://localhost:$Port. Press Ctrl+C to stop." -ForegroundColor Cyan
& $taskJava '-jar' $taskJar "--server.port=$Port"
