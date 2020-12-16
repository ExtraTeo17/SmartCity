$ErrorActionPreference = "Stop"

$buildDir = "SmartCity-build";

Remove-Item -Recurse -Force $buildDir -ErrorAction Ignore
New-Item -ItemType Directory -Name $buildDir

# backend
mvn package --errors '-Dmaven.test.skip=true' --file backend/pom.xml 

# frontend
Set-Location .\frontend
npm run build
Set-Location ..

Move-Item -Path .\backend\package -Destination .\$buildDir\backend
Move-Item -Path .\frontend\build -Destination .\$buildDir\frontend

Copy-Item -Path '.\run.sh' -Destination .\$buildDir\
