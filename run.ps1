$ErrorActionPreference = "Stop"

function Check-Command($cmdname) {
    return [bool](Get-Command -Name $cmdname -ErrorAction SilentlyContinue)
}

$buildDir = "SmartCity-build";
$jarFile="backend/smartCity-2.0.jar"

if  (!(Test-Path -Path $jarFile) -Or !(Test-Path -Path "frontend/index.html")) {
    if (!(Test-Path -Path $buildDir)){
        if (!(Test-Path -Path "scripts")){
            Set-Location scripts
            .\build.ps1;
            Set-Location ..
        }
        else { 
            Write-Host "Missing files required to run app"
            exit 1;
        }
    }
    Set-Location $buildDir
}

$serve = "serve.cmd"
$arguments = "-n -s frontend";

Start-Process -NoNewWindow $serve $arguments
if (Check-Command -cmdname 'start') {
    start http://localhost:5000
}
else {
    Write-Host "You need to start localhost:5000 by yourself"
}

& "$Env:JAVA_HOME\bin\java.exe" -jar ".\backend\smartCity-2.0.jar"
