$ErrorActionPreference = "Stop"

$mainDir = "..";
$buildDirName = "SmartCity-build"
$buildDir = "../SmartCity-build";

Remove-Item -Recurse -Force $buildDir -ErrorAction Ignore
New-Item -ItemType Directory -Name $buildDir

# backend
mvn package --errors '-Dmaven.test.skip=true' --file $mainDir\backend\pom.xml 

# frontend
Set-Location .\$mainDir\frontend
npm install
npm run build
Set-Location ..

Move-Item -Path .\backend\package -Destination .\$buildDirName\backend
Move-Item -Path .\frontend\build -Destination .\$buildDirName\frontend

Copy-Item -Path .\run.sh -Destination .\$buildDirName\
Copy-Item -Path .\run.ps1 -Destination .\$buildDirName\

Set-Location scripts
