# Entrada local de demostracion. Compatible con Windows PowerShell 5.1.
# Abre Chrome con perfil separado; no modifica preferencias del perfil habitual.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$server = $null
$browser = $null
$demoProfile = $null
$exitCode = 0
try {
    $java = Get-Command java -ErrorAction Stop
    if (-not (Test-Path -LiteralPath (Join-Path $root 'dist\SIGSF.jar'))) {
        throw 'Falta dist\SIGSF.jar. Descomprima todo el ZIP en una carpeta nueva.'
    }
    $candidates = @(
        "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
        "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe",
        "$env:LOCALAPPDATA\Google\Chrome\Application\chrome.exe"
    )
    $chrome = $candidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
    if (-not $chrome) {
        $command = Get-Command chrome.exe -ErrorAction SilentlyContinue
        if ($command) { $chrome = $command.Source }
    }
    if (-not $chrome) { throw 'Se requiere Google Chrome para este iniciador automatico. Instale Chrome y vuelva a abrir INICIAR_DEMO.bat.' }
    # Detectar otra instancia antes de arrancar; nunca detener procesos ajenos.
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any, 8091)
    try { $listener.Start() }
    catch { throw 'El puerto 8091 esta ocupado. Cierre la consola de la demostracion anterior y vuelva a intentar.' }
    finally { $listener.Stop() }
    $env:SIGSF_PERSISTENCIA = 'memoria'
    $env:SIGSF_SERVER_PORT = '8091'
    $env:SIGSF_BACKUP_AUTOMATICO_HORAS = '0'
    Write-Host 'SIGSF 5.11.0 - ENTRADA AUTOMATICA CON SONIDO'
    Write-Host 'Iniciando servidor de demostracion...'
    $server = Start-Process -FilePath $java.Source -ArgumentList @('-jar', 'dist\SIGSF.jar') -WorkingDirectory $root -NoNewWindow -PassThru
    $ready = $false
    for ($attempt = 0; $attempt -lt 100; $attempt++) {
        $server.Refresh()
        if ($server.HasExited) { throw 'Java se detuvo antes de iniciar. Revise el mensaje anterior (requiere Java 17 o superior).' }
        try {
            $response = Invoke-WebRequest -Uri 'http://localhost:8091/api/health' -UseBasicParsing -TimeoutSec 1
            if ($response.StatusCode -eq 200) { $ready = $true; break }
        } catch { }
        Start-Sleep -Milliseconds 300
    }
    if (-not $ready) { throw 'El servidor no respondio a tiempo. Revise la salida de Java.' }
    $server.Refresh()
    if ($server.HasExited) { throw 'El servidor de esta demostracion se detuvo.' }
    # Perfil nuevo por sesion: evita que Chrome reutilice otro proceso sin los argumentos.
    $demoProfile = Join-Path ([System.IO.Path]::GetTempPath()) ('SIGSF-Demo-' + [guid]::NewGuid().ToString('N'))
    $arguments = @(
        ('--user-data-dir="' + $demoProfile + '"'),
        '--no-first-run',
        '--no-default-browser-check',
        '--autoplay-policy=no-user-gesture-required',
        '--start-fullscreen',
        '--new-window',
        'http://localhost:8091/'
    )
    $browser = Start-Process -FilePath $chrome -ArgumentList $arguments -PassThru
    Write-Host 'Chrome se abrira a pantalla completa. El video inicia con su sonido.'
    Write-Host 'F11: salir de pantalla completa. Alt+F4: cerrar la ventana de demostracion.'
    Write-Host 'Al terminar, cierre esa ventana y pulse Enter aqui para detener el servidor.'
    [void](Read-Host)
} catch {
    Write-Host ('No se pudo iniciar: ' + $_.Exception.Message) -ForegroundColor Red
    $exitCode = 1
} finally {
    if ($server) {
        $server.Refresh()
        if (-not $server.HasExited) { Stop-Process -InputObject $server -ErrorAction SilentlyContinue }
    }
    # Solo limpiar el perfil propio cuando Chrome ya se haya cerrado.
    if ($browser -and $demoProfile) {
        $browser.Refresh()
        if ($browser.HasExited -and (Test-Path -LiteralPath $demoProfile)) {
            Remove-Item -LiteralPath $demoProfile -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}
exit $exitCode
