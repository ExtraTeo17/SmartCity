#!/bin/bash
set -e

mainDir="..";
buildDirName="SmartCity-build"
buildDir="../$buildDirName";

rm -rf $buildDir
mkdir $buildDir

# backend
mvn package --errors -Dmaven.test.skip=true --file $mainDir/backend/pom.xml 

# frontend
cd $mainDir/frontend
npm run build
cd ..

mv ./backend/package/ ./$buildDirName/backend
mv ./frontend/build/ ./$buildDirName/frontend
cp ./run.sh $buildDirName/
cp ./run.ps1 $buildDirName/

cd scripts
