$ErrorActionPreference = "Stop"

$buildDir = "SmartCity-build";
$currDir = (Split-Path -Path (Get-Location) -Leaf)

if  (!($currDir -eq $buildDir)) {
    if (!(Test-Path -Path $buildDir)){
        Set-Location scripts
        .\build.ps1;
        Set-Location ..
    }
    Set-Location $buildDir
}

$serve = "serve.cmd"
$arguments = "-s frontend";

Start-Process -NoNewWindow $serve $arguments
start http://localhost:5000

& "$Env:JAVA_HOME\bin\java.exe" -jar ".\backend\smartCity-2.0.jar"
