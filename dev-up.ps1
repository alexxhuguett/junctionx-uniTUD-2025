<#
Simple dev orchestrator for the JunctionX project (PowerShell).
Runs: Python ML server, Java Spring Boot backend, and Frontend dev server.

Usage (from repo root: junctionx-uniTUD-2025):
  pwsh -File dev-up.ps1
  powershell -ExecutionPolicy Bypass -File dev-up.ps1

Environment overrides (optional):
  $env:PY_HOST = '0.0.0.0'
  $env:PY_PORT = '8000'
  $env:JAVA_HOST = '127.0.0.1'
  $env:JAVA_PORT = '8080'
  $env:PYTHONPATH_ENV = '.'
  $env:PY_EXCEL = 'ml/data/uber_hackathon_v2_mock_data.xlsx'
  $env:PY_MODEL = 'ml/artifacts/model.pkl'
  $env:VENV_DIR = '.venv'
  $env:REQUIREMENTS_TXT = 'ml/requirements.txt'
  $env:FRONTEND_DIR = 'frontend'  # or auto-detected if not set
  $env:FRONTEND_CMD = 'npm run dev'
  $env:FRONTEND_INSTALL = 'npm install'
#>

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-EnvDefault {
  param(
    [Parameter(Mandatory)] [string] $Name,
    [Parameter(Mandatory)] [string] $Default
  )
  $v = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($v)) { return $Default } else { return $v }
}

$RepoDir = (Get-Location).Path
$LogDir = Join-Path $RepoDir 'logs'
$PidsDir = Join-Path $RepoDir '.pids'
New-Item -ItemType Directory -Path $LogDir -Force | Out-Null
New-Item -ItemType Directory -Path $PidsDir -Force | Out-Null

# Config
$PyHost = Get-EnvDefault 'PY_HOST' '0.0.0.0'
$PyPort = [int](Get-EnvDefault 'PY_PORT' '8000')
$JavaHost = Get-EnvDefault 'JAVA_HOST' '127.0.0.1'
$JavaPort = [int](Get-EnvDefault 'JAVA_PORT' '8080')
$PythonPathEnv = Get-EnvDefault 'PYTHONPATH_ENV' '.'
$PyExcel = Get-EnvDefault 'PY_EXCEL' 'ml/data/uber_hackathon_v2_mock_data.xlsx'
$PyModel = Get-EnvDefault 'PY_MODEL' 'ml/artifacts/model.pkl'
$VenvDir = Get-EnvDefault 'VENV_DIR' '.venv'
$Requirements = Get-EnvDefault 'REQUIREMENTS_TXT' 'ml/requirements.txt'

# Frontend detection
function Detect-FrontendDir {
  $candidates = @('frontend','web','ui','app','client')
  foreach ($d in $candidates) {
    $pkg = Join-Path (Join-Path $RepoDir $d) 'package.json'
    if (Test-Path $pkg) { return $d }
  }
  return $null
}

$FrontendDir = [Environment]::GetEnvironmentVariable('FRONTEND_DIR')
if ([string]::IsNullOrWhiteSpace($FrontendDir)) {
  $det = Detect-FrontendDir
  if ($null -ne $det) {
    $FrontendDir = $det
  } else {
    Write-Warning 'Could not auto-detect frontend folder. Set $env:FRONTEND_DIR if needed.'
    $FrontendDir = ''
  }
}

$FrontendCmd = Get-EnvDefault 'FRONTEND_CMD' 'npm run dev'
$FrontendInstall = Get-EnvDefault 'FRONTEND_INSTALL' 'npm install'

# --------------------------- helpers ---------------------------
function Test-PortFree {
  param([string] $Host, [int] $Port)
  try {
    $client = [System.Net.Sockets.TcpClient]::new()
    $ar = $client.BeginConnect($Host, $Port, $null, $null)
    if ($ar.AsyncWaitHandle.WaitOne(300)) {
      # Connected => in use
      try { $client.EndConnect($ar) } catch {}
      $client.Close(); return $false
    } else {
      $client.Close(); return $true
    }
  } catch {
    return $true
  }
}

function Find-FreePort {
  param([string] $Host, [int] $Start)
  $p = [int]$Start
  $max = $p + 50
  while ($p -le $max) {
    if (Test-PortFree -Host $Host -Port $p) { return $p }
    $p++
  }
  return [int]$Start
}

function Wait-PortOpen {
  param([string] $Host, [int] $Port, [int] $Timeout = 30, [string] $Label = 'port')
  Write-Host "[dev-up] Waiting for $Label on $Host:$Port (timeout ${Timeout}s)..."
  $t = 0
  while ($t -lt $Timeout) {
    if (-not (Test-PortFree -Host $Host -Port $Port)) {
      Write-Host "[dev-up] $Label is up on $Host:$Port"
      return $true
    }
    Start-Sleep -Seconds 1
    $t++
  }
  Write-Warning "Timed out waiting for $Label on $Host:$Port"
  return $false
}

function Resolve-Python {
  $py = (Get-Command python -ErrorAction SilentlyContinue)?.Source
  if (-not $py) { $py = (Get-Command python3 -ErrorAction SilentlyContinue)?.Source }
  if (-not $py) { throw 'Python executable not found in PATH (need python or python3).'
  }
  return $py
}

function Ensure-PythonVenv {
  $venvPath = Join-Path $RepoDir $VenvDir
  if (-not (Test-Path $venvPath)) {
    Write-Host "[ml] Creating Python virtualenv at $VenvDir ..."
    $py = Resolve-Python
    & $py -m venv $venvPath
  }
  $global:VenvPy = if (Test-Path (Join-Path $venvPath 'Scripts/python.exe')) {
    Join-Path $venvPath 'Scripts/python.exe'
  } elseif (Test-Path (Join-Path $venvPath 'bin/python')) {
    Join-Path $venvPath 'bin/python'
  } else {
    throw "Python not found inside venv at $venvPath"
  }
  # Upgrade pip and install requirements if present
  try { & $global:VenvPy -m pip install --upgrade pip | Out-Null } catch {}
  $reqPath = Join-Path $RepoDir $Requirements
  if (Test-Path $reqPath) {
    Write-Host "[ml] Installing Python dependencies from $Requirements ..."
    & $global:VenvPy -m pip install -r $reqPath *>> (Join-Path $LogDir 'python.log')
  } else {
    Write-Host "[ml] No requirements file at $Requirements; skipping dependency install."
  }
}

function Start-BackgroundProcess {
  param(
    [Parameter(Mandatory)] [string] $FilePath,
    [Parameter(Mandatory)] [string[]] $Arguments,
    [Parameter(Mandatory)] [string] $LogPath,
    [string] $WorkingDirectory
  )
  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = $FilePath
  $psi.WorkingDirectory = ($WorkingDirectory ? $WorkingDirectory : $RepoDir)
  foreach ($a in $Arguments) { [void]$psi.ArgumentList.Add($a) }
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError = $true
  $psi.UseShellExecute = $false
  $proc = New-Object System.Diagnostics.Process
  $proc.StartInfo = $psi
  # Open log file streams (overwrite on each run)
  $stdout = [System.IO.StreamWriter]::new($LogPath, $false)
  $stderr = $stdout  # share same stream for interleaved output
  $proc.add_OutputDataReceived({ param($s,$e) if ($e.Data) { $stdout.WriteLine($e.Data) ; $stdout.Flush() } })
  $proc.add_ErrorDataReceived(  { param($s,$e) if ($e.Data) { $stderr.WriteLine($e.Data) ; $stderr.Flush() } })
  [void]$proc.Start()
  $proc.BeginOutputReadLine()
  $proc.BeginErrorReadLine()
  return $proc
}

function Start-PythonServer {
  $script:PyPort = Find-FreePort -Host $PyHost -Start $PyPort
  Write-Host "[ml] Starting Python ML server on $PyHost:$script:PyPort ..."
  $env:PYTHONPATH = $PythonPathEnv
  $log = Join-Path $LogDir 'python.log'
  $args = @('-m','ml.server','--excel', $PyExcel,'--model',$PyModel,'--host',$PyHost,'--port', "$script:PyPort")
  $proc = Start-BackgroundProcess -FilePath $global:VenvPy -Arguments $args -LogPath $log -WorkingDirectory $RepoDir
  Set-Content -Path (Join-Path $PidsDir 'python.pid') -Value $proc.Id
  Wait-PortOpen -Host $PyHost -Port $script:PyPort -Timeout 30 -Label 'ML' | Out-Null
  return $proc
}

function Start-JavaApi {
  $script:JavaPort = Find-FreePort -Host $JavaHost -Start $JavaPort
  Write-Host "[api] Starting Java Spring Boot backend on $JavaHost:$script:JavaPort ..."
  $log = Join-Path $LogDir 'java.log'
  $backendDir = Join-Path $RepoDir 'backend'
  $mvnwCmd = Join-Path $backendDir 'mvnw.cmd'
  $mvnwSh  = Join-Path $backendDir 'mvnw'
  if (Test-Path $mvnwCmd) {
    $file = $mvnwCmd
  } elseif (Test-Path $mvnwSh) {
    $file = $mvnwSh
  } else {
    $file = (Get-Command mvn -ErrorAction SilentlyContinue)?.Source
    if (-not $file) { throw 'Cannot find mvnw/mvnw.cmd or mvn in PATH.' }
  }
  $args = @('spring-boot:run', "-Dspring-boot.run.arguments=--server.port=$script:JavaPort")
  # If invoking mvnw shell script on Windows, use bash if available
  if ($file -like '*\mvnw' -and -not $file.ToLower().EndsWith('.cmd')) {
    $bash = (Get-Command bash -ErrorAction SilentlyContinue)?.Source
    if ($bash) {
      $args = @($file) + $args
      $file = $bash
    }
  }
  $proc = Start-BackgroundProcess -FilePath $file -Arguments $args -LogPath $log -WorkingDirectory $backendDir
  Set-Content -Path (Join-Path $PidsDir 'java.pid') -Value $proc.Id
  Wait-PortOpen -Host $JavaHost -Port $script:JavaPort -Timeout 60 -Label 'Java' | Out-Null
  return $proc
}

function Start-Frontend {
  if ([string]::IsNullOrWhiteSpace($FrontendDir)) {
    Write-Warning '[fe] Skipping frontend (FRONTEND_DIR not set and not detected).'
    return $null
  }
  Write-Host "[fe] Starting Frontend in $FrontendDir ..."
  $feDir = Join-Path $RepoDir $FrontendDir
  $log = Join-Path $LogDir 'frontend.log'
  Push-Location $feDir
  try {
    if (-not (Test-Path (Join-Path $feDir 'node_modules'))) {
      Write-Host '[fe] Installing dependencies ...'
      # Run install and wait
      $installFile = (Get-Command npm -ErrorAction SilentlyContinue)?.Source
      if (-not $installFile) { $installFile = (Get-Command npm.cmd -ErrorAction SilentlyContinue)?.Source }
      if ($installFile) {
        $args = @('run','--version') # dummy to ensure npm exists
        # Actually invoke install as a one-off process and wait
        $pi = Start-BackgroundProcess -FilePath $installFile -Arguments @('install') -LogPath $log -WorkingDirectory $feDir
        $pi.WaitForExit()
      } else {
        # Fallback to cmd.exe /c
        Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', $FrontendInstall -WorkingDirectory $feDir -RedirectStandardOutput $log -RedirectStandardError $log -Wait | Out-Null
      }
    }
    # Start dev server (do not wait)
    Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', $FrontendCmd -WorkingDirectory $feDir -RedirectStandardOutput $log -RedirectStandardError $log -PassThru |
      ForEach-Object { $_ }
  } finally {
    Pop-Location
  }
}

# --------------------------- run orchestrator ---------------------------
$procs = @()
try {
  Ensure-PythonVenv
  $p1 = Start-PythonServer; if ($p1) { $procs += $p1 }
  $p2 = Start-JavaApi;     if ($p2) { $procs += $p2 }
  $p3 = Start-Frontend;    if ($p3) { $procs += $p3 }

  Write-Host "[dev-up] All services launched. Logs: $LogDir"
  Write-Host "[dev-up] Python ML:   http://$PyHost:$script:PyPort/simulation"
  Write-Host "[dev-up] Java API:    check $LogDir\java.log for port info (Spring Boot)"
  Write-Host "[dev-up] Frontend:    check $LogDir\frontend.log for dev server URL"

  # Tail logs (Ctrl+C to stop all)
  Get-Content (Join-Path $LogDir '*.log') -Tail 0 -Wait
}
finally {
  Write-Host "`n[dev-up] Stopping processes ..."
  foreach ($p in $procs) {
    if ($p -and -not $p.HasExited) {
      try { Stop-Process -Id $p.Id -ErrorAction SilentlyContinue } catch {}
    }
  }
  Get-ChildItem $PidsDir -Filter '*.pid' -ErrorAction SilentlyContinue | ForEach-Object {
    try {
      $pid = Get-Content $_.FullName -ErrorAction SilentlyContinue
      if ($pid) { Stop-Process -Id $pid -ErrorAction SilentlyContinue }
    } catch {}
    try { Remove-Item $_.FullName -ErrorAction SilentlyContinue } catch {}
  }
}

